package com.yourssu.morupark.queue.implement

import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class StatusOperator(
    private val queueAdapter: QueueAdapter,

    @Value("\${queue.max-size}")
    private val maxSize: Long,

    ) {

    /**
     * 시간당 상태를 Waiting -> Allowed로 바꿔준다.
     * 모든 플랫폼의 대기열을 Redis 키 패턴 스캔으로 찾아서 처리한다.
     * 각 플랫폼의 TPS 설정에 따라 처리량을 조절한다.
     */
    @Scheduled(fixedDelayString = "\${queue.processing-interval}")
    fun changeStatus() {
        val waitingKeys = queueAdapter.getAllPlatformWaitingKeys()

        for (key in waitingKeys) {
            val platformId = queueAdapter.extractPlatformIdFromKey(key)
            val tps = ServerTPSMap.get(platformId) ?: maxSize
            val accessTokens = queueAdapter.popFromWaitingQueue(tps, platformId)
            if (!accessTokens.isNullOrEmpty()) {
                queueAdapter.addToAllowedQueue(accessTokens, platformId)
            }
        }
    }
}
