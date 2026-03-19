package com.yourssu.morupark.goods.sub.config

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class KafkaConfig(
    @Value("\${kafka.topic.ticket-result.partitions}") private val ticketResultPartitions: Int,
) {

    @Bean
    fun ticketResultTopic(): NewTopic {
        return TopicBuilder.name("TICKET_RESULT")
            .partitions(ticketResultPartitions)
            .build()
    }
}
