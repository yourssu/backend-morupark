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

    fun addToAllowedQueue(accessTokens: Set<String>) {
        redisTemplate.opsForSet().add(QUEUE_ALLOWED_KEY, *accessTokens.toTypedArray())
    }

    fun popFromWaitingQueue(count: Long): Set<String>? {
        val items = redisTemplate.opsForZSet().range(QUEUE_WAITING_KEY, 0, count - 1)
        redisTemplate.opsForZSet().remove(QUEUE_WAITING_KEY, items)
        return items
    }

    fun deleteFromAllowedQueue(accessToken: String) {
        redisTemplate.opsForSet().remove(QUEUE_ALLOWED_KEY, accessToken)
    }

    fun isInQueue(accessToken: String) : Boolean {
        val score = redisTemplate.opsForZSet().score(QUEUE_WAITING_KEY, accessToken)
        if(score != null) {
            return true
        }
        return redisTemplate.opsForSet().isMember(QUEUE_ALLOWED_KEY, accessToken)!!
    }

    fun getTicketStatus(accessToken: String) : TicketStatus {
        val rank = redisTemplate.opsForZSet().rank(QUEUE_WAITING_KEY, accessToken)
        if (rank != null) return TicketStatus.WAITING
        val allowed = redisTemplate.opsForSet().isMember(QUEUE_ALLOWED_KEY, accessToken)
        if (allowed != null && allowed) return TicketStatus.ALLOWED
        throw IllegalStateException("현재 대기열에 존재하지 않습니다.")
    }

    fun getRank(accessToken: String) : Long? {
        return redisTemplate.opsForZSet().rank(QUEUE_WAITING_KEY, accessToken)
    }
}
