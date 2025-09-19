package com.yourssu.morupark.queue.implement

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneOffset

@Component
class KafkaProducer (
    private val kafkaTemplate: KafkaTemplate<String, QueueUser>
){
    private val WAITING_TOPIC = "waiting-topic"

    fun enter(accessToken: String) {
        // 현재 시간 계산
        val today = LocalDateTime.now()
        val timestamp = today.toInstant(ZoneOffset.UTC).toEpochMilli().toDouble()

        // userId와 현재 시간을 담은 이벤트를 생성
        val event = QueueUser(accessToken = accessToken, timestamp = timestamp)

        // waiting-topic으로 이벤트를 전송(Produce)합니다.
        // 키를 userId로 설정하면 동일한 유저의 이벤트는 같은 파티션으로 전달되어 순서 보장에 유리합니다.
        kafkaTemplate.send(WAITING_TOPIC, accessToken, event)
    }
}