package com.yourssu.morupark.queue.implement

import com.yourssu.morupark.queue.business.TicketStatus
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class QueueAdapter(
    private val redisTemplate: RedisTemplate<String, UserInfo>
) {

    private val QUEUE_KEY = "QUEUE"

    fun addToQueue(accessToken: String, timestamp: Long) {
        redisTemplate.opsForZSet().add(QUEUE_KEY, UserInfo(accessToken, TicketStatus.WAITING), timestamp.toDouble())
    }

    fun isInQueue(accessToken: String) : Boolean {
        val score = redisTemplate.opsForZSet().score(QUEUE_KEY, UserInfo(accessToken, TicketStatus.WAITING))
        return score != null
    }
}
