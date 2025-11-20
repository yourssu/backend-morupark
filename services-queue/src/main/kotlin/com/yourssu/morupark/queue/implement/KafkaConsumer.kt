package com.yourssu.morupark.queue.implement

import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Component

@Component
class KafkaConsumer(
    private val queueAdapter: QueueAdapter,
    private val authAdapter: AuthAdapter,
) {

    companion object {
        private const val TAG = "WAITING"
        private const val GROUP_ID = "ticketing_group"
    }

    @KafkaListener(topics = [TAG], groupId = GROUP_ID)
    fun listen(
        accessToken: String,
        @Header(KafkaHeaders.TIMESTAMP) timestamp: Long,
    ) {
        val userInfo = authAdapter.getUserInfo(accessToken)
        val platformId = userInfo.platform.platformId
        val tps = userInfo.platform.tps
        ServerTPSMap.put(platformId, tps)
        queueAdapter.addToWaitingQueue(accessToken, timestamp, platformId)
    }

}
