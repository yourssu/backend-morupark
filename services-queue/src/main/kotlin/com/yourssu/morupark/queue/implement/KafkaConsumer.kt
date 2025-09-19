package com.yourssu.morupark.queue.implement

import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class KafkaConsumer(
    private val waitingQueueAdapter: WaitingQueueAdapter
) {
    @KafkaListener(topics = ["\${queue.kafka.topic}"], groupId = "queue-group")
    fun listen(queueUser: QueueUser) {
        // 이벤트를 받아서 Redis에 실제 대기열 정보를 추가합니다.
        // 점수(score)로 진입 시간을 사용하면 먼저 들어온 사람이 앞으로 갑니다.
        waitingQueueAdapter.addUserToQueue(queueUser.accessToken, queueUser.timestamp)
    }

}
