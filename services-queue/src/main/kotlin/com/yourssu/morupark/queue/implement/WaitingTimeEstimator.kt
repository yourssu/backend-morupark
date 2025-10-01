package com.yourssu.morupark.queue.implement

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class WaitingTimeEstimator(
    @Value("\${queue.max-size}")
    private val maxSize: Int,

    @Value("\${queue.processing-interval}")
    private val intervalInMillis: Long,
) {
    fun estimateWaitingTime(rank: Long): Long {
        val groupAhead = rank / maxSize
        val intervalInSeconds = intervalInMillis / 1000.0
        return (groupAhead * intervalInSeconds).toLong()
    }

}
