package com.yourssu.morupark.queue.implement

import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import kotlin.math.max

@Component
class StatusOperator(
    private val queueAdapter: QueueAdapter,

    @Value("\${queue.max-size}")
    private val maxSize: Long,
    
) {

    /**
     * 시간당 상태를 Waiting -> Allowed로 바꿔준다.
     */
    @Scheduled(fixedDelayString = "\${queue.processing-interval}")
    fun changeStatus() {
        val accessTokens = queueAdapter.popFromWaitingQueue(maxSize)
        if (!accessTokens.isNullOrEmpty())
            queueAdapter.addToAllowedQueue(accessTokens)
    }
}
