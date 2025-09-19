package com.yourssu.morupark.queue.implement

import org.apache.kafka.common.protocol.types.Field.Bool
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

    fun isInQueue(accessToken: String) : Boolean {
        val score = redisTemplate.opsForZSet().score(QUEUE_KEY, accessToken)
        return score != null
    }
}
