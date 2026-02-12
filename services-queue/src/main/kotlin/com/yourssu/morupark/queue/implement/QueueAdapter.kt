package com.yourssu.morupark.queue.implement

import com.yourssu.morupark.queue.business.TicketStatus
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ScanOptions
import org.springframework.stereotype.Component

@Component
class QueueAdapter(
    private val redisTemplate: RedisTemplate<String, String>
) {

    private fun getWaitingKey(platformId: Long) = "queue:$platformId:waiting"
    private fun getAllowedKey(platformId: Long) = "queue:$platformId:allowed"

    fun addToWaitingQueue(accessToken: String, timestamp: Long, platformId: Long) {
        redisTemplate.opsForZSet().add(getWaitingKey(platformId), accessToken, timestamp.toDouble())
    }

    fun addToAllowedQueue(accessTokens: Set<String>, platformId: Long) {
        redisTemplate.opsForSet().add(getAllowedKey(platformId), *accessTokens.toTypedArray())
    }

    fun popFromWaitingQueue(count: Long, platformId: Long): Set<String>? {
        val waitingKey = getWaitingKey(platformId)
        val items = redisTemplate.opsForZSet().range(waitingKey, 0, count - 1)
        if (!items.isNullOrEmpty()) {
            redisTemplate.opsForZSet().remove(waitingKey, *items.toTypedArray())
        }
        return items
    }

    fun deleteFromAllowedQueue(accessToken: String, platformId: Long) {
        redisTemplate.opsForSet().remove(getAllowedKey(platformId), accessToken)
    }

    fun isInQueue(accessToken: String, platformId: Long): Boolean {
        val score = redisTemplate.opsForZSet().score(getWaitingKey(platformId), accessToken)
        if (score != null) {
            return true
        }
        return redisTemplate.opsForSet().isMember(getAllowedKey(platformId), accessToken)!!
    }

    fun getTicketStatus(accessToken: String, platformId: Long): TicketStatus {
        val rank = redisTemplate.opsForZSet().rank(getWaitingKey(platformId), accessToken)
        if (rank != null) return TicketStatus.WAITING
        val allowed = redisTemplate.opsForSet().isMember(getAllowedKey(platformId), accessToken)
        if (allowed != null && allowed) return TicketStatus.ALLOWED
        throw IllegalStateException("현재 대기열에 존재하지 않습니다.")
    }

    fun getRank(accessToken: String, platformId: Long): Long? {
        return redisTemplate.opsForZSet().rank(getWaitingKey(platformId), accessToken)
    }

    fun getAllPlatformWaitingKeys(): Set<String> {
        val keys = mutableSetOf<String>()
        val scanOptions = ScanOptions.scanOptions().match("queue:*:waiting").count(100).build()
        redisTemplate.scan(scanOptions).use { cursor ->
            cursor.forEach { key -> keys.add(key) }
        }
        return keys
    }

    fun extractPlatformIdFromKey(key: String): Long {
        val parts = key.split(":")
        return parts[1].toLong()
    }
}
