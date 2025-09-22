package com.yourssu.morupark.queue.business

import com.yourssu.morupark.queue.implement.AuthAdapter
import com.yourssu.morupark.queue.implement.KafkaProducer
import com.yourssu.morupark.queue.implement.QueueAdapter
import com.yourssu.morupark.queue.implement.WaitingTimeEstimator
import org.springframework.stereotype.Service

@Service
class QueueService(
    private val kafkaProducer: KafkaProducer,
    private val authAdapter: AuthAdapter,
    private val queueAdapter: QueueAdapter,
    private val waitingTimeEstimator: WaitingTimeEstimator
) {

    fun enqueue(accessToken: String) {
        if (!authAdapter.isTokenValid(accessToken)) {
            throw IllegalStateException("Access token is invalid")
        }
        kafkaProducer.send(accessToken)
    }

    fun getWaitingToken(accessToken: String): String {
        if (!authAdapter.isTokenValid(accessToken)) {
            throw IllegalStateException("Access token is invalid")
        }

        if (!queueAdapter.isInQueue(accessToken)) {
            throw IllegalStateException("Access token is not in queue")
        }

        return authAdapter.getWaitingToken(accessToken)
    }

    fun getTicketStatusResult (accessToken: String, waitingToken: String): Any {
        val ticketStatus = queueAdapter.getTicketStatus(accessToken)
        if(ticketStatus == TicketStatus.ALLOWED) {
            queueAdapter.deleteFromAllowedQueue(accessToken)
            return getAllowedStatusResult(waitingToken)
        }
        return getWaitingStatusResult(accessToken)
    }

    private fun getWaitingStatusResult(accessToken: String): ReadWaitingStatusResult {
        val rank = queueAdapter.getRank(accessToken)!!
        val estimatedWaitingTime = waitingTimeEstimator.estimate(rank)
        return ReadWaitingStatusResult(TicketStatus.WAITING, rank, estimatedWaitingTime)
    }

    private fun getAllowedStatusResult(waitingToken: String): ReadAllowedStatusResult {
        val externalServerToken = authAdapter.getExternalServerToken(waitingToken)
        return ReadAllowedStatusResult(TicketStatus.ALLOWED, externalServerToken)
    }
}
