package com.yourssu.morupark.queue.implement

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class QueueAdapter(
    private val redisTemplate: RedisTemplate<String, String>
) {

    companion object {
        private const val WAITING_KEY = "queue:waiting"
        private const val USER_HASH_KEY = "queue:user"
        private const val STATUS_HASH_KEY = "queue:status"
    }

    fun addToWaitingQueue(waitingToken: String, studentId: String, phoneNumber: String, timestamp: Long) {
        redisTemplate.opsForZSet().add(WAITING_KEY, waitingToken, timestamp.toDouble())
        redisTemplate.opsForHash<String, String>().put(USER_HASH_KEY, waitingToken, "$studentId|$phoneNumber")
    }

    fun popFromWaitingQueue(count: Long): Set<String>? {
        val items = redisTemplate.opsForZSet().range(WAITING_KEY, 0, count - 1)
        if (!items.isNullOrEmpty()) {
            redisTemplate.opsForZSet().remove(WAITING_KEY, *items.toTypedArray())
        }
        return items
    }

    fun getUserInfo(waitingToken: String): String? {
        return redisTemplate.opsForHash<String, String>().get(USER_HASH_KEY, waitingToken)
    }

    fun getRank(waitingToken: String): Long? {
        return redisTemplate.opsForZSet().rank(WAITING_KEY, waitingToken)
    }

    fun isInQueue(waitingToken: String): Boolean {
        return redisTemplate.opsForZSet().score(WAITING_KEY, waitingToken) != null
    }

    fun saveStatus(waitingToken: String, status: String) {
        redisTemplate.opsForHash<String, String>().put(STATUS_HASH_KEY, waitingToken, status)
    }

    fun getStatusFromResult(waitingToken: String): String? {
        return redisTemplate.opsForHash<String, String>().get(STATUS_HASH_KEY, waitingToken)
    }

    fun popAllFromWaitingQueue(): Set<String>? {
        val items = redisTemplate.opsForZSet().range(WAITING_KEY, 0, -1)
        if (!items.isNullOrEmpty()) {
            redisTemplate.opsForZSet().remove(WAITING_KEY, *items.toTypedArray())
        }
        return items
    }
}
