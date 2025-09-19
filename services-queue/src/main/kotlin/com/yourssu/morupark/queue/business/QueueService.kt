package com.yourssu.morupark.queue.business

import com.yourssu.morupark.queue.implement.AuthAdapter
import com.yourssu.morupark.queue.implement.KafkaProducer
import org.springframework.stereotype.Service

@Service
class QueueService(
    private val kafkaProducer: KafkaProducer,
    private val authAdapter: AuthAdapter
) {

    fun enqueue(accessToken: String) {
        if (!authAdapter.isTokenValid(accessToken)) {
            throw IllegalStateException("Access token is invalid")
        }
        kafkaProducer.send(accessToken)
    }
}
