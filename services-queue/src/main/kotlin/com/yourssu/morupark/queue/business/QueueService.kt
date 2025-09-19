package com.yourssu.morupark.queue.business

import com.yourssu.morupark.queue.implement.KafkaProducer
import org.springframework.stereotype.Service

@Service
class QueueService(
    private val kafkaProducer: KafkaProducer
) {

    fun enqueue(accessToken: String) {
        kafkaProducer.send(accessToken)
    }
}
