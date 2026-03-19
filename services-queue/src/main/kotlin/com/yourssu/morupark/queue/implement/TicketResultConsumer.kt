package com.yourssu.morupark.queue.implement

import com.yourssu.morupark.queue.business.FailureReason
import com.yourssu.morupark.queue.business.TicketStatus
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.RetryableTopic
import org.springframework.retry.annotation.Backoff
import org.springframework.stereotype.Component

@Component
class TicketResultConsumer(
    private val queueAdapter: QueueAdapter
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    companion object {
        private const val TOPIC = "TICKET_RESULT"
        private const val GROUP_ID = "queue-group"
    }

    @KafkaListener(topics = [TOPIC], groupId = GROUP_ID)
    @RetryableTopic(
        attempts = "4",
        backoff = Backoff(delay = 1000, multiplier = 2.0),
        dltTopicSuffix = ".dlt"
    )
    fun consume(message: String) {
        log.info("[CONSUMER] 결과 수신: $message")
        if (message == "SOLD_OUT") {
            val remaining = queueAdapter.popAllFromWaitingQueue()
            log.warn("[CONSUMER] SOLD_OUT - 잔여 대기자 FAILED 처리: ${remaining?.size ?: 0}명")
            remaining?.forEach { token ->
                queueAdapter.saveStatus(token, "${TicketStatus.FAILED.name}:${FailureReason.SOLD_OUT.name}")
            }
            return
        }

        val parts = message.split("|", limit = 2)
        if (parts.size < 2) return

        val (waitingToken, status) = parts
        log.info("[CONSUMER] 상태 저장 - token: $waitingToken, status: $status")
        queueAdapter.saveStatus(waitingToken, status)
    }
}
