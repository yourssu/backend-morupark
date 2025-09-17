package com.yourssu.morupark.queue.implement

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class KafkaConsumer(
    private val redisTemplate: RedisTemplate<String, >
) {

    companion object {
        private const val TAG = "TOPIC"
        private const val GROUP_ID = "ticketing_group"
    }

    @KafkaListener(topics = [TAG], groupId = GROUP_ID)
    fun listen(
        userId: Long
    ) {

    }

}
