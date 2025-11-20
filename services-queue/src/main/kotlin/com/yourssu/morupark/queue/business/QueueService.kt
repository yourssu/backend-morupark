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
        // Validate token by getting user info
        authAdapter.getUserInfo(accessToken)
        kafkaProducer.send(accessToken)
    }

    fun getWaitingToken(accessToken: String): String {
        val userInfo = authAdapter.getUserInfo(accessToken)
        val platformId = userInfo.platform.platformId

        if (!queueAdapter.isInQueue(accessToken, platformId)) {
            throw IllegalStateException("Access token is not in queue")
        }

        return authAdapter.getWaitingToken(accessToken)
    }

    fun getTicketStatusResult(accessToken: String, waitingToken: String): Any {
        val userInfo = authAdapter.getUserInfo(accessToken)
        val platformId = userInfo.platform.platformId

        val ticketStatus = queueAdapter.getTicketStatus(accessToken, platformId)
        if (ticketStatus == TicketStatus.ALLOWED) {
            queueAdapter.deleteFromAllowedQueue(accessToken, platformId)
            return getAllowedStatusResult(waitingToken)
        }
        return getWaitingStatusResult(accessToken, platformId)
    }

    private fun getWaitingStatusResult(accessToken: String, platformId: Long): ReadWaitingStatusResult {
        val rank = queueAdapter.getRank(accessToken, platformId)!!
        val estimatedWaitingTime = waitingTimeEstimator.estimateWaitingTime(rank)
        return ReadWaitingStatusResult(TicketStatus.WAITING, rank, estimatedWaitingTime)
    }

    private fun getAllowedStatusResult(waitingToken: String): ReadAllowedStatusResult {
        val externalServerToken = authAdapter.getExternalServerToken(waitingToken)
        return ReadAllowedStatusResult(TicketStatus.ALLOWED, externalServerToken)
    }
}
