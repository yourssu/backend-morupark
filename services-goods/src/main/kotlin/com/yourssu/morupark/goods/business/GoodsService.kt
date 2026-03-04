package com.yourssu.morupark.goods.business

import com.yourssu.morupark.goods.implement.GoodsRepository
import com.yourssu.morupark.goods.implement.Winner
import com.yourssu.morupark.goods.implement.WinnerRepository
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.random.Random

@Service
class GoodsService(
    private val winnerRepository: WinnerRepository,
    private val goodsRepository: GoodsRepository,
    private val eventPublisher: ApplicationEventPublisher,
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    companion object {
        private const val GOODS_ID = 1L
        private const val WIN_PROBABILITY = 0.90f
    }

    @Transactional
    fun processTicket(waitingToken: String, studentId: String, phoneNumber: String) {
        if (!isWinner()) {
            log.info("[SERVICE] 낙첨 - token: $waitingToken")
            eventPublisher.publishEvent(TicketFailedEvent(waitingToken, "당첨되지 않았습니다."))
            return
        }

        val updated = goodsRepository.decrementStock(GOODS_ID)

        if (updated > 0) {
            log.info("[SERVICE] 당첨 - token: $waitingToken, studentId: $studentId")
            winnerRepository.save(Winner(studentId = studentId, phoneNumber = phoneNumber))
            eventPublisher.publishEvent(TicketSuccessEvent(waitingToken))
        } else {
            log.warn("[SERVICE] 재고 없음 - token: $waitingToken")
            eventPublisher.publishEvent(TicketFailedEvent(waitingToken, "재고 없음"))
            val marked = goodsRepository.markSoldOut(GOODS_ID)
            if (marked > 0) {
                log.warn("[SERVICE] SOLD_OUT 처리")
                eventPublisher.publishEvent(SoldOutEvent())
            }
        }
    }

    private fun isWinner(): Boolean = Random.nextFloat() < WIN_PROBABILITY
}
