package com.yourssu.morupark.queue.business

import com.yourssu.morupark.queue.implement.QueueUser
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneOffset

@Service
class QueueService(
    private val kafkaTemplate: KafkaTemplate<String, QueueUser>
) {
    companion object {
        private const val TAG = "TOPIC"
    }

    fun addToQueue(accessToken: String) {
        val today = LocalDateTime.now()
        val timestamp = today.toInstant(ZoneOffset.UTC).toEpochMilli().toDouble()
        kafkaTemplate.send(TAG, QueueUser(accessToken, timestamp))
    }
}
