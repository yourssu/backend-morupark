package com.yourssu.morupark.goods.implement

import com.yourssu.morupark.goods.business.FailureReason
import com.yourssu.morupark.goods.business.SoldOutEvent
import com.yourssu.morupark.goods.business.TicketFailedEvent
import com.yourssu.morupark.goods.business.TicketSuccessEvent
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class TicketProcessor(
    private val goodsUpdater: GoodsUpdater,
    private val winnerRepository: WinnerRepository,
    private val eventPublisher: ApplicationEventPublisher,
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    fun process(waitingToken: String, studentId: String, phoneNumber: String, goodsId: Long) {
        val stockDecremented = goodsUpdater.decrementStock(goodsId)
        if (stockDecremented) {
            log.info("[PROCESSOR] 당첨 - token: $waitingToken, studentId: $studentId")
            winnerRepository.save(Winner(studentId = studentId, phoneNumber = phoneNumber))
            eventPublisher.publishEvent(TicketSuccessEvent(waitingToken))
        } else {
            log.warn("[PROCESSOR] 재고 없음 - token: $waitingToken")
            eventPublisher.publishEvent(TicketFailedEvent(waitingToken, FailureReason.SOLD_OUT))

            val soldOutMarked = goodsUpdater.markSoldOut(goodsId)
            if (soldOutMarked) {
                log.warn("[PROCESSOR] SOLD_OUT 처리")
                eventPublisher.publishEvent(SoldOutEvent(goodsId))
            }
        }
    }
}
