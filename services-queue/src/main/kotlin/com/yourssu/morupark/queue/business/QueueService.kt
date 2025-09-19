package com.yourssu.morupark.queue.business

import com.yourssu.morupark.queue.implement.AuthAdapter
import com.yourssu.morupark.queue.implement.KafkaProducer
import com.yourssu.morupark.queue.implement.QueueAdapter
import org.springframework.stereotype.Service

@Service
class QueueService(
    private val kafkaProducer: KafkaProducer,
    private val authAdapter: AuthAdapter,
    private val queueAdapter: QueueAdapter
) {

    fun enqueue(accessToken: String) {
        if (!authAdapter.isTokenValid(accessToken)) {
            throw IllegalStateException("Access token is invalid")
        }
        kafkaProducer.send(accessToken)
    }

    fun getWaitingToken(accessToken: String) : String {
        if (!authAdapter.isTokenValid(accessToken)) {
            throw IllegalStateException("Access token is invalid")
        }

        if(!queueAdapter.isInQueue(accessToken)) {
            throw IllegalStateException("Access token is not in queue")
        }

        return authAdapter.getWaitingToken(accessToken)
    }

}
