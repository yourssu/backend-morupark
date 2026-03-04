package com.yourssu.morupark.queue.implement

import com.yourssu.morupark.queue.business.TicketStatus
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class StatusOperator(
    private val queueAdapter: QueueAdapter,
    private val ticketProcessRequestProducer: TicketProcessRequestProducer,

    @Value("\${queue.max-size}")
    private val maxSize: Long,
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @Scheduled(fixedDelayString = "\${queue.processing-interval}")
    fun processQueue() {
        val waitingTokens = queueAdapter.popFromWaitingQueue(maxSize)
        if (waitingTokens.isNullOrEmpty()) return

        log.info("[SCHEDULER] 처리 시작 - 대상 수: ${waitingTokens.size}")
        for (waitingToken in waitingTokens) {
            queueAdapter.saveStatus(waitingToken, TicketStatus.PROCESSING.name)
            val userInfo = queueAdapter.getUserInfo(waitingToken) ?: continue
            val (studentId, phoneNumber) = userInfo.split("|")
            log.info("[SCHEDULER] Kafka 발행 - token: $waitingToken, studentId: $studentId")
            ticketProcessRequestProducer.send(waitingToken, studentId, phoneNumber)
        }
    }
}
