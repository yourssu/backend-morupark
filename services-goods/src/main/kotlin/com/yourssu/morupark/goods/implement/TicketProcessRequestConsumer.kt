package com.yourssu.morupark.goods.implement

import com.yourssu.morupark.goods.business.GoodsService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.RetryableTopic
import org.springframework.retry.annotation.Backoff
import org.springframework.stereotype.Component

@Component
class TicketProcessRequestConsumer(
    private val goodsService: GoodsService
) {
    private val log = LoggerFactory.getLogger(this::class.java)

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
        log.info("[CONSUMER] 메시지 수신: $message")
        val (waitingToken, studentId, phoneNumber) = message.split("|")
        goodsService.processTicket(waitingToken, studentId, phoneNumber)
    }
}
