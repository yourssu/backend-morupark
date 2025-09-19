package com.yourssu.morupark.queue.implement

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class WaitingQueueAdapter(
    // String-Object 형태의 RedisTemplate 주입
    private val redisTemplate: RedisTemplate<String, String> // RedisTemplate<String, String>과 StringRedisTemplate의 차이는?
) {
    // Redis의 Sorted Set을 사용하기 위한 키
    private val WAITING_QUEUE_KEY = "waiting:queue" // 왜 aaa:bbb 형태?

    /**
     * 대기열에 유저를 추가합니다.
     * @param userId 유저 ID
     * @param score 순서를 정할 점수 (보통은 입장 시간 timestamp)
     */
    fun addUserToQueue(userId: String, score: Double) {
        redisTemplate.opsForZSet().add(WAITING_QUEUE_KEY, userId, score) // ZSet가 정확히 뭔지?
    }

    /**
     * 특정 유저의 대기 순번(rank)을 조회합니다. (0부터 시작)
     * @param userId 유저 ID
     * @return 대기 순번 (없으면 null)
     */
    fun getUserRank(userId: String): Long? {
        // ZRANK 명령어에 해당합니다.-> ZRANK가 뭔데?
        return redisTemplate.opsForZSet().rank(WAITING_QUEUE_KEY, userId)
    }

    /**
     * 현재 대기열의 총 인원을 조회합니다.
     * @return 총 대기 인원
     */
    fun getQueueSize(): Long {
        return redisTemplate.opsForZSet().size(WAITING_QUEUE_KEY) ?: 0L
    }

    /**
     * 대기열의 맨 앞에서부터 지정된 수만큼 유저를 추출(입장 처리)합니다.
     * @param count 입장시킬 유저 수
     * @return 입장한 유저 ID 목록
     */
    fun allowUsers(count: Int): Set<String>? {
        // ZPOPMIN 명령어와 유사한 기능. 0부터 count-1 까지의 멤버를 가져옵니다.
        val allowedUsers = redisTemplate.opsForZSet().range(WAITING_QUEUE_KEY, 0, count - 1L)
        // 실제 입장한 유저들은 대기열에서 제거합니다.
        allowedUsers?.let {
            redisTemplate.opsForZSet().remove(WAITING_QUEUE_KEY, *it.toTypedArray())
        }
        return allowedUsers
    }
}