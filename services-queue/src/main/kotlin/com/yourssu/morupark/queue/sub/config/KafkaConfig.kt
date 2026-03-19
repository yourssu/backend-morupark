package com.yourssu.morupark.queue.sub.config

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class KafkaConfig(
    @Value("\${kafka.topic.waiting.partitions}") private val waitingPartitions: Int,
) {

    @Bean
    fun waitingTopic(): NewTopic {
        return TopicBuilder.name("WAITING")
            .partitions(waitingPartitions)
            .build()
    }
}
