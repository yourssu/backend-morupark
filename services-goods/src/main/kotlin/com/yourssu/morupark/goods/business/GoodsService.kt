package com.yourssu.morupark.goods.business

import com.yourssu.morupark.goods.implement.TicketDeduplicator
import com.yourssu.morupark.goods.implement.TicketProcessor
import com.yourssu.morupark.goods.implement.TicketValidator
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GoodsService(
    private val ticketValidator: TicketValidator,
    private val ticketProcessor: TicketProcessor,
    private val ticketDeduplicator: TicketDeduplicator,
    private val eventPublisher: ApplicationEventPublisher,
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    companion object {
        // TODO: 현재 단일 상품만 지원하므로 goodsId를 1L로 고정함.
        //  다중 상품 지원 시 아래 수정 필요:
        //  - [services-queue] enqueue API 요청에서 goodsId를 파라미터로 받아 isSoldOut(goodsId) 체크
        //  - [services-queue] WAITING Kafka 메시지에 goodsId 포함 (waitingToken|studentId|phoneNumber|goodsId)
        //  - [services-goods] TicketProcessRequestConsumer에서 goodsId 파싱 후 processTicket에 전달
        //  - [services-goods] processTicket 파라미터에 goodsId 추가, 하드코딩 제거
        private const val GOODS_ID = 1L
    }

    @Transactional
    fun processTicket(waitingToken: String, studentId: String, phoneNumber: String) {
        if (ticketDeduplicator.isDuplicate(waitingToken)) {
            log.warn("[SERVICE] 중복 메시지 감지 - token: $waitingToken")
            return
        }

        if (ticketValidator.isSoldOut(GOODS_ID)) {
            log.warn("[SERVICE] 재고 없음 - token: $waitingToken")
            eventPublisher.publishEvent(TicketFailedEvent(waitingToken, FailureReason.SOLD_OUT))
            return
        }

        if (!ticketValidator.isWinner()) {
            log.info("[SERVICE] 낙첨 - token: $waitingToken")
            eventPublisher.publishEvent(TicketFailedEvent(waitingToken, FailureReason.LOST))
            return
        }

        ticketProcessor.process(waitingToken, studentId, phoneNumber, GOODS_ID)
    }
}
