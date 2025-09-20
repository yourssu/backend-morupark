package com.yourssu.morupark.queue.implement

import com.yourssu.morupark.queue.business.TicketStatus
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class QueueAdapter(
    private val redisTemplate: RedisTemplate<String, String>
) {

    private val QUEUE_WAITING_KEY = "queue:waiting"
    private val QUEUE_ALLOWED_KEY = "queue:allowed"

    fun addToWaitingQueue(accessToken: String, timestamp: Long) {
        redisTemplate.opsForZSet().add(QUEUE_WAITING_KEY, accessToken, timestamp.toDouble())
    }

    fun addToAllowedQueue(accessToken: String) {
        redisTemplate.opsForSet().add(QUEUE_ALLOWED_KEY, accessToken)
    }

    fun deleteFromWaitingQueue(accessToken: String) {
        redisTemplate.opsForZSet().remove(QUEUE_WAITING_KEY, accessToken)
    }

    fun deleteFromAllowedQueue(accessToken: String) {
        redisTemplate.opsForSet().remove(QUEUE_ALLOWED_KEY, accessToken)
    }

    fun isInQueue(accessToken: String) : Boolean {
        val score = redisTemplate.opsForZSet().score(QUEUE_WAITING_KEY, accessToken)
            ?: redisTemplate.opsForZSet().score(QUEUE_ALLOWED_KEY, accessToken)
        return score != null
    }

    fun getTicketStatus(accessToken: String) : TicketStatus {
        val rank = redisTemplate.opsForZSet().rank(QUEUE_WAITING_KEY, accessToken)
        return if (rank != null) TicketStatus.WAITING else TicketStatus.ALLOWED
    }

    fun getRank(accessToken: String) : Long? {
        return redisTemplate.opsForZSet().rank(QUEUE_WAITING_KEY, accessToken)
    }
}
