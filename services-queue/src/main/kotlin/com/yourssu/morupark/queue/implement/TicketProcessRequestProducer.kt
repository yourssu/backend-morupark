package com.yourssu.morupark.queue.implement

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class TicketProcessRequestProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>
) {
    companion object {
        private const val TOPIC: String = "WAITING"
        private const val PARTITION_COUNT: Int = 500
    }

    fun send(waitingToken: String, studentId: String, phoneNumber: String) {
        val timestamp = System.currentTimeMillis()
        val message = "$waitingToken|$studentId|$phoneNumber"
        kafkaTemplate.send(TOPIC, (timestamp % PARTITION_COUNT).toInt(), timestamp, studentId, message)
    }
}
