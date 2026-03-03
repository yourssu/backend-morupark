package com.yourssu.morupark.goods.business

import com.yourssu.morupark.goods.domain.Winner
import com.yourssu.morupark.goods.implement.TicketResultProducer
import com.yourssu.morupark.goods.implement.GoodsRepository
import com.yourssu.morupark.goods.implement.WinnerRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.random.Random

@Service
class GoodsService(
    private val winnerRepository: WinnerRepository,
    private val goodsRepository: GoodsRepository,
    private val ticketResultProducer: TicketResultProducer,
) {
    companion object {
        private const val GOODS_ID = 1L
    }

    @Transactional
    fun processTicket(waitingToken: String, studentId: String, phoneNumber: String) {
        if (!isWinner()) {
            ticketResultProducer.sendFailed(waitingToken, "당첨되지 않았습니다.")
            return
        }

        val updated = goodsRepository.decrementStock(GOODS_ID)

        if (updated > 0) {
            winnerRepository.save(Winner(studentId = studentId, phoneNumber = phoneNumber))
            ticketResultProducer.sendSuccess(waitingToken)
        } else {
            ticketResultProducer.sendFailed(waitingToken, "재고 없음")
            ticketResultProducer.sendSoldOut()
        }
    }

    private fun isWinner(): Boolean = Random.nextFloat() < 0.05f
}
