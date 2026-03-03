package com.yourssu.morupark.queue.implement

import com.yourssu.morupark.queue.business.TicketStatus
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.RetryableTopic
import org.springframework.retry.annotation.Backoff
import org.springframework.stereotype.Component

@Component
class TicketResultConsumer(
    private val queueAdapter: QueueAdapter
) {
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
        if (message == "SOLD_OUT") {
            val remaining = queueAdapter.popAllFromWaitingQueue()
            remaining?.forEach { token ->
                queueAdapter.saveStatus(token, "${TicketStatus.FAILED.name}:재고 없음")
            }
            return
        }

        val parts = message.split("|", limit = 2)
        if (parts.size < 2) return

        val (waitingToken, status) = parts
        queueAdapter.saveStatus(waitingToken, status)
    }
}
