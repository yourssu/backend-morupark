package com.yourssu.morupark.queue.implement

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class QueueAdapter(
    private val redisTemplate: RedisTemplate<String, String>
) {

    private val QUEUE_KEY = "QUEUE"

    fun addToQueue(accessToken: String, timestamp: Long) {
        redisTemplate.opsForZSet().add(QUEUE_KEY, accessToken, timestamp.toDouble())
    }
}
