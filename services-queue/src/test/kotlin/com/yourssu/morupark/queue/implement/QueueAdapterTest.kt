package com.yourssu.morupark.queue.implement

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.SetOperations
import org.springframework.data.redis.core.ZSetOperations

@ExtendWith(MockKExtension::class)
class QueueAdapterTest {

    @MockK
    private lateinit var redisTemplate: RedisTemplate<String, String>

    @MockK
    private lateinit var zSetOps: ZSetOperations<String, String>

    @MockK
    private lateinit var setOps: SetOperations<String, String>

    @InjectMockKs
    private lateinit var queueAdapter: QueueAdapter

    private val token = "token123"
    private val QUEUE_WAITING_KEY = "queue:waiting"
    private val QUEUE_ALLOWED_KEY = "queue:allowed"

    @Test
    fun `사용자가 대기열에 추가되면, waiting queue에 저장된다`() {
        // given
        every { redisTemplate.opsForZSet() } returns zSetOps
        every { zSetOps.add(any(), any(), any()) } returns true
        val timestamp = System.nanoTime()

        // when
        queueAdapter.addToWaitingQueue(token, timestamp)

        // then
        verify {
            zSetOps.add(QUEUE_WAITING_KEY, token, timestamp.toDouble())
        }
    }
}
