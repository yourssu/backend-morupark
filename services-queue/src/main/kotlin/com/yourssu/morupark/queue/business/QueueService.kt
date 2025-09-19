package com.yourssu.morupark.queue.business

import com.yourssu.morupark.queue.implement.AuthApiAdapter
import com.yourssu.morupark.queue.implement.KafkaProducer
import com.yourssu.morupark.queue.implement.QueueUser
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import sun.jvm.hotspot.debugger.win32.coff.DebugVC50X86RegisterEnums.TAG
import java.time.LocalDateTime
import java.time.ZoneOffset

@Service
class QueueService(
    private val authApiAdapter: AuthApiAdapter,
    private val kafkaProducer: KafkaProducer
) {
    fun addToQueue(accessToken: String) {
        // 1. 다른 모듈(service-auth)에 API를 호출하여 유저를 검증
        if (!authApiAdapter.isTokenValid(accessToken)) {
            throw IllegalArgumentException("Invalid user.")
        }

        // 2. 검증 통과 시 Kafka에 이벤트 발행
        kafkaProducer.enter(accessToken)
    }
}
