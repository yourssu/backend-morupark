package com.yourssu.morupark.queue.implement

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class StatusOperator(
    private val queueAdapter: QueueAdapter
) {
    private val countToAllow: Long = 1L
    companion object {
        private const val timeToAllow = 1000L
    }

    /**
     * 시간당 상태를 Waiting -> Allowed로 바꿔준다.
     */
    @Scheduled(fixedDelay = timeToAllow)
    fun changeStatus() {
        val accessTokens = queueAdapter.popFromWaitingQueue(countToAllow)
        if (!accessTokens.isNullOrEmpty())
            queueAdapter.addToAllowedQueue(accessTokens)
    }
}