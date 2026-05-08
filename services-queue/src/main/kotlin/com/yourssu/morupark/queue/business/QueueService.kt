package com.yourssu.morupark.queue.business

import com.yourssu.morupark.queue.application.EnqueueResponse
import com.yourssu.morupark.queue.application.TicketStatusResponse
import com.yourssu.morupark.queue.implement.QueueAdapter
import com.yourssu.morupark.queue.implement.WaitingTimeEstimator
import com.yourssu.morupark.queue.sub.exception.InvalidWaitingTokenException
import com.yourssu.morupark.queue.sub.exception.SoldOutException
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class QueueService(
    private val queueAdapter: QueueAdapter,
    private val waitingTimeEstimator: WaitingTimeEstimator
) {
    companion object {
        // TODO: 현재 단일 상품만 지원하므로 goodsId를 1L로 고정함.
        //  다중 상품 지원 시 아래 수정 필요:
        //  - [services-queue] enqueue API 요청에서 goodsId를 파라미터로 받아 isSoldOut(goodsId) 체크
        //  - [services-queue] WAITING Kafka 메시지에 goodsId 포함 (waitingToken|studentId|phoneNumber|goodsId)
        //  - [services-goods] TicketProcessRequestConsumer에서 goodsId 파싱 후 processTicket에 전달
        //  - [services-goods] processTicket 파라미터에 goodsId 추가, 하드코딩 제거
        private const val GOODS_ID = 1L
    }

    fun enqueue(studentId: String, phoneNumber: String): EnqueueResponse {
        if (queueAdapter.isSoldOut(GOODS_ID)) throw SoldOutException()
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
