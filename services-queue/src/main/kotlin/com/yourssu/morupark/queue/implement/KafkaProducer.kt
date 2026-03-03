package com.yourssu.morupark.queue.implement

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>
) {
    companion object {
        private const val TOPIC: String = "WAITING"
        private const val PARTITION_COUNT: Int = 500
    }

    fun send(studentId: String, phoneNumber: String) {
        val timestamp = System.nanoTime()
        val message = "$studentId|$phoneNumber"
        kafkaTemplate.send(TOPIC, (timestamp % PARTITION_COUNT).toInt(), timestamp, studentId, message)
    }
}
