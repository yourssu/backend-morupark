package com.yourssu.morupark.queue.implement

import org.springframework.data.redis.core.RedisTemplate

class QueueAdapter(
    private val redisTemplate: RedisTemplate<String, QueueUser>
) {

    private val QUEUE_KEY = ""

    fun addToQueue(accessToken: String) {
//        redisTemplate.opsForZSet().add()
    }
}