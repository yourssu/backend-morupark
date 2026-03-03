package com.yourssu.morupark.queue.implement

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>
) {
    private val TOPIC: String = "WAITING"

    fun send(studentId: String, phoneNumber: String) {
        val timestamp = System.nanoTime()
        val message = "$studentId|$phoneNumber"
        kafkaTemplate.send(TOPIC, (timestamp % 500).toInt(), timestamp, studentId, message)
    }
}
