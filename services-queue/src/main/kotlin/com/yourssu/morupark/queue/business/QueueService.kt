package com.yourssu.morupark.queue.business

import com.yourssu.morupark.queue.implement.KafkaProducer
import com.yourssu.morupark.queue.implement.QueueUser
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class QueueService(
    private val kafkaProducer: KafkaProducer
) {

    fun enqueue(accessToken: String) {
        kafkaProducer.send(QueueUser(accessToken))
    }
}
