package com.yourssu.morupark.goods.implement

import com.yourssu.morupark.goods.business.GoodsService
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.RetryableTopic
import org.springframework.retry.annotation.Backoff
import org.springframework.stereotype.Component

@Component
class TicketProcessRequestConsumer(
    private val goodsService: GoodsService
) {
    companion object {
        private const val TOPIC = "WAITING"
        private const val GROUP_ID = "goods-group"
    }

    @KafkaListener(topics = [TOPIC], groupId = GROUP_ID)
    @RetryableTopic(
        attempts = "4",
        backoff = Backoff(delay = 1000, multiplier = 2.0),
        dltTopicSuffix = ".dlt"
    )
    fun consume(message: String) {
        val (waitingToken, studentId, phoneNumber) = message.split("|")
        goodsService.processTicket(waitingToken, studentId, phoneNumber)
    }
}
