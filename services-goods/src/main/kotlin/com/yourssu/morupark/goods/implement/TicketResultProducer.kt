package com.yourssu.morupark.goods.implement

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class TicketResultProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>
) {
    companion object {
        private const val TOPIC = "TICKET_RESULT"
    }

    fun sendSuccess(waitingToken: String) {
        kafkaTemplate.send(TOPIC, waitingToken, "$waitingToken|SUCCESS")
    }

    fun sendFailed(waitingToken: String, message: String) {
        kafkaTemplate.send(TOPIC, waitingToken, "$waitingToken|FAILED:$message")
    }

    fun sendSoldOut() {
        kafkaTemplate.send(TOPIC, "SYSTEM", "SOLD_OUT")
    }
}
