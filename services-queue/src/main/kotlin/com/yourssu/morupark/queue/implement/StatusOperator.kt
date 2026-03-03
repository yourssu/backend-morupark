package com.yourssu.morupark.queue.implement

import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class StatusOperator(
    private val queueAdapter: QueueAdapter,
    private val kafkaProducer: KafkaProducer,

    @Value("\${queue.max-size}")
    private val maxSize: Long,
) {

    @Scheduled(fixedDelayString = "\${queue.processing-interval}")
    fun processQueue() {
        val waitingTokens = queueAdapter.popFromWaitingQueue(maxSize)
        if (waitingTokens.isNullOrEmpty()) return

        for (waitingToken in waitingTokens) {
            val userInfo = queueAdapter.getUserInfo(waitingToken) ?: continue
            val (studentId, phoneNumber) = userInfo.split("|")
            kafkaProducer.send(studentId, phoneNumber)
        }
    }
}
