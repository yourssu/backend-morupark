package com.yourssu.morupark.queue.implement

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class WaitingTimeEstimator (
    @Value("\${scheduler.allow.count}")
    private val batchSize: Long,

    @Value("\${scheduler.allow.delay}")
    private val intervalInMillis: Long
){
    fun estimate(rank: Long): Long {
        if (batchSize <= 0 || intervalInMillis <= 0) {
            return -1L
        }
        val intervalInSeconds = intervalInMillis / 1000.0
        val groupsAhead = rank / batchSize
        return (groupsAhead * intervalInSeconds).toLong()
    }
}
