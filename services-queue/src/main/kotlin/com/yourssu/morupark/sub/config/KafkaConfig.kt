package com.yourssu.morupark.sub.config

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class KafkaConfig {

    @Bean
    fun waitingTopic(): NewTopic {
        return TopicBuilder.name("WAITING")
            .build()
    }
}
