package com.yourssu.morupark.queue.business

import com.yourssu.morupark.queue.application.EnqueueResponse
import com.yourssu.morupark.queue.application.TicketStatusResponse
import com.yourssu.morupark.queue.implement.QueueAdapter
import com.yourssu.morupark.queue.implement.WaitingTimeEstimator
import com.yourssu.morupark.queue.sub.exception.InvalidWaitingTokenException
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class QueueService(
    private val queueAdapter: QueueAdapter,
    private val waitingTimeEstimator: WaitingTimeEstimator
) {

    fun enqueue(studentId: String, phoneNumber: String): EnqueueResponse {
        val waitingToken = UUID.randomUUID().toString()
        queueAdapter.addToWaitingQueue(waitingToken, studentId, phoneNumber, System.currentTimeMillis())
        return EnqueueResponse(waitingToken)
    }

    fun getStatus(waitingToken: String): TicketStatusResponse {
        if (queueAdapter.isInQueue(waitingToken)) {
            return getWaitingStatusResult(waitingToken)
        }
        return getStatusResult(waitingToken)
    }

    private fun getWaitingStatusResult(waitingToken: String): TicketStatusResponse {
        val rank = queueAdapter.getRank(waitingToken)!!
        val estimatedWaitSeconds = waitingTimeEstimator.estimateWaitingTime(rank)
        return TicketStatusResponse(
            status = TicketStatus.WAITING,
            rank = rank,
            estimatedWaitSeconds = estimatedWaitSeconds
        )
    }

    private fun getStatusResult(waitingToken: String): TicketStatusResponse {
        val statusStr = queueAdapter.getStatusFromResult(waitingToken)
            ?: throw InvalidWaitingTokenException()
        return when {
            statusStr == TicketStatus.PROCESSING.name -> TicketStatusResponse(status = TicketStatus.PROCESSING)
            statusStr == TicketStatus.SUCCESS.name -> TicketStatusResponse(status = TicketStatus.SUCCESS)
            statusStr.startsWith("FAILED:") -> TicketStatusResponse(
                status = TicketStatus.FAILED,
                reason = FailureReason.valueOf(statusStr.removePrefix("FAILED:"))
            )
            else -> throw InvalidWaitingTokenException()
        }
    }
}
