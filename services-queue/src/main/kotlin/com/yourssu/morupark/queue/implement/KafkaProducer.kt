package com.yourssu.morupark.queue.implement

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>
) {
    private val TOPIC: String = "WAITING"

    fun send(accessToken: String) {
        val timestamp = System.nanoTime()
        kafkaTemplate.send(TOPIC, (timestamp % 500).toInt(), timestamp, accessToken, accessToken)
    }
}
