package com.yourssu.morupark.queue.implement

import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class StatusOperator(
    private val queueAdapter: QueueAdapter
) {
    @Value("\${scheduler.allow.count}")
    private val countToAllow: Long = 1L

    @Scheduled(fixedDelayString = "\${scheduler.allow.delay}")
    fun changeStatus() {
        val accessTokens = queueAdapter.popFromWaitingQueue(countToAllow)
        if (!accessTokens.isNullOrEmpty())
            queueAdapter.addToAllowedQueue(accessTokens)
    }
}