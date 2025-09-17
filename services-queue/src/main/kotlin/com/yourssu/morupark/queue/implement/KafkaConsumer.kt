package com.yourssu.morupark.queue.implement

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class KafkaConsumer(
    private val redisTemplate: RedisTemplate<String, QueueUser>
) {

    companion object {
        private const val TAG = "TOPIC"
        private const val GROUP_ID = "ticketing_group"
    }

    @KafkaListener(topics = [TAG], groupId = GROUP_ID)
    fun listen(
        accessToken: String,
        timestamp: Double,
    ) {
        redisTemplate.opsForZSet().add("queue", QueueUser(accessToken), timestamp)
    }

}
