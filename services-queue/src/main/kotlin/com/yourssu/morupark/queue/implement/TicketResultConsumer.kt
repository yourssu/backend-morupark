package com.yourssu.morupark.queue.implement

import org.springframework.kafka.annotation.KafkaListener
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
    fun consume(message: String) {
        if (message == "SOLD_OUT") {
            // TODO: 남은 대기자 전부 FAILED 처리 (다음 이슈)
            return
        }

        val parts = message.split("|", limit = 2)
        if (parts.size < 2) return

        val (waitingToken, status) = parts
        queueAdapter.saveStatus(waitingToken, status)
    }
}
