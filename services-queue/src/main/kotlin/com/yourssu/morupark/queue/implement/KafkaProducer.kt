package com.yourssu.morupark.queue.implement

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaProducer(
    private val kafkaTemplate: KafkaTemplate<String, QueueUser>
) {
    private val TOPIC: String = "WAITING"

    fun send(message: QueueUser) {
        kafkaTemplate.send(TOPIC, message)
    }
}