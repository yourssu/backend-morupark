package com.yourssu.morupark.queue.business

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class QueueService(
    private val kafkaTemplate: KafkaTemplate<String, String>
) {
    private val topic = "TOPIC" // const 필요

    fun addToQueue(userId: Long) {
        kafkaTemplate.send(topic, userId.toString())
    }



}
