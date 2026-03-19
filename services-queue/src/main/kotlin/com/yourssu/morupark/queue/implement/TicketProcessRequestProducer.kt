package com.yourssu.morupark.queue.implement

import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class TicketProcessRequestProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    @Value("\${kafka.topic.waiting.partitions}") private val partitionCount: Int,
) {
    companion object {
        private const val TOPIC: String = "WAITING"
    }

    fun send(waitingToken: String, studentId: String, phoneNumber: String) {
        val timestamp = System.currentTimeMillis()
        val message = "$waitingToken|$studentId|$phoneNumber"
        kafkaTemplate.send(TOPIC, (timestamp % partitionCount).toInt(), timestamp, studentId, message)
    }
}
