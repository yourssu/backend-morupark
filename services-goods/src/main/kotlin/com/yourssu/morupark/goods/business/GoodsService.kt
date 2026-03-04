package com.yourssu.morupark.goods.business

import com.yourssu.morupark.goods.implement.GoodsRepository
import com.yourssu.morupark.goods.implement.Winner
import com.yourssu.morupark.goods.implement.WinnerRepository
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
    companion object {
        private const val GOODS_ID = 1L
    }

    @Transactional
    fun processTicket(waitingToken: String, studentId: String, phoneNumber: String) {
        if (!isWinner()) {
            eventPublisher.publishEvent(TicketFailedEvent(waitingToken, "당첨되지 않았습니다."))
            return
        }

        val updated = goodsRepository.decrementStock(GOODS_ID)

        if (updated > 0) {
            winnerRepository.save(Winner(studentId = studentId, phoneNumber = phoneNumber))
            eventPublisher.publishEvent(TicketSuccessEvent(waitingToken))
        } else {
            eventPublisher.publishEvent(TicketFailedEvent(waitingToken, "재고 없음"))
            val marked = goodsRepository.markSoldOut(GOODS_ID)
            if (marked > 0) {
                eventPublisher.publishEvent(SoldOutEvent())
            }
        }
    }

    private fun isWinner(): Boolean = Random.nextFloat() < 0.05f
}
