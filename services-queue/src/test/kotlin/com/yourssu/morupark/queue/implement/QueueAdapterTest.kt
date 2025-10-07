package com.yourssu.morupark.queue.implement

import com.yourssu.morupark.queue.business.TicketStatus
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
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

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

    @Test
    fun `허용된 사용자들을 허용 큐에 추가한다`() {
        // given
        every { redisTemplate.opsForSet() } returns setOps
        val accessTokens = setOf("token1", "token2", "token3")
        every { setOps.add(QUEUE_ALLOWED_KEY, *accessTokens.toTypedArray()) } returns 3L

        // when
        queueAdapter.addToAllowedQueue(accessTokens)

        // then
        verify { setOps.add(QUEUE_ALLOWED_KEY, *accessTokens.toTypedArray()) }
    }

    @Test
    fun `대기열에서 지정된 개수만큼 팝한다`() {
        // given
        every { redisTemplate.opsForZSet() } returns zSetOps
        val count = 5L
        val expectedItems = setOf("token1", "token2", "token3")
        every { zSetOps.range(QUEUE_WAITING_KEY, 0, count - 1) } returns expectedItems
        every { zSetOps.remove(QUEUE_WAITING_KEY, expectedItems) } returns 3L

        // when
        val result = queueAdapter.popFromWaitingQueue(count)

        // then
        assertEquals(expectedItems, result)
        verify { zSetOps.range(QUEUE_WAITING_KEY, 0, count - 1) }
        verify { zSetOps.remove(QUEUE_WAITING_KEY, expectedItems) }
    }

    @Test
    fun `허용 큐에서 특정 토큰을 삭제한다`() {
        // given
        every { redisTemplate.opsForSet() } returns setOps
        every { setOps.remove(QUEUE_ALLOWED_KEY, token) } returns 1L

        // when
        queueAdapter.deleteFromAllowedQueue(token)

        // then
        verify { setOps.remove(QUEUE_ALLOWED_KEY, token) }
    }

    @Test
    fun `사용자가 대기열에 있으면 true를 반환한다`() {
        // given
        every { redisTemplate.opsForZSet() } returns zSetOps
        every { redisTemplate.opsForSet() } returns setOps
        every { zSetOps.score(QUEUE_WAITING_KEY, token) } returns 1000.0

        // when
        val result = queueAdapter.isInQueue(token)

        // then
        assertTrue(result)
        verify { zSetOps.score(QUEUE_WAITING_KEY, token) }
    }

    @Test
    fun `사용자가 허용 큐에 있으면 true를 반환한다`() {
        // given
        every { redisTemplate.opsForZSet() } returns zSetOps
        every { redisTemplate.opsForSet() } returns setOps
        every { zSetOps.score(QUEUE_WAITING_KEY, token) } returns null
        every { setOps.isMember(QUEUE_ALLOWED_KEY, token) } returns true

        // when
        val result = queueAdapter.isInQueue(token)

        // then
        assertTrue(result)
        verify { zSetOps.score(QUEUE_WAITING_KEY, token) }
        verify { setOps.isMember(QUEUE_ALLOWED_KEY, token) }
    }

    @Test
    fun `사용자가 어느 큐에도 없으면 false를 반환한다`() {
        // given
        every { redisTemplate.opsForZSet() } returns zSetOps
        every { redisTemplate.opsForSet() } returns setOps
        every { zSetOps.score(QUEUE_WAITING_KEY, token) } returns null
        every { setOps.isMember(QUEUE_ALLOWED_KEY, token) } returns false

        // when
        val result = queueAdapter.isInQueue(token)

        // then
        assertFalse(result)
    }

    @Test
    fun `대기열에 있는 사용자의 상태는 WAITING이다`() {
        // given
        every { redisTemplate.opsForZSet() } returns zSetOps
        every { zSetOps.rank(QUEUE_WAITING_KEY, token) } returns 5L

        // when
        val result = queueAdapter.getTicketStatus(token)

        // then
        assertEquals(TicketStatus.WAITING, result)
    }

    @Test
    fun `allowed 집합에 사용자가 있으면 ALLOWED 상태이다`() {
        // given
        every { redisTemplate.opsForZSet() } returns zSetOps
        every { redisTemplate.opsForSet() } returns setOps
        every { zSetOps.rank(QUEUE_WAITING_KEY, token) } returns null
        every { setOps.isMember(QUEUE_ALLOWED_KEY, token) } returns true

        // when
        val result = queueAdapter.getTicketStatus(token)

        // then
        assertEquals(TicketStatus.ALLOWED, result)
    }

    @Test
    fun `사용자가 두 큐 모두 없으면 예외를 반환한다`() {
        // given
        every { redisTemplate.opsForZSet() } returns zSetOps
        every { redisTemplate.opsForSet() } returns setOps
        every { zSetOps.rank(QUEUE_WAITING_KEY, token) } returns null
        every { setOps.isMember(QUEUE_ALLOWED_KEY, token) } returns false

        // when
        val exception = assertFailsWith<IllegalStateException> { queueAdapter.getTicketStatus(token) }

        // then
        assertEquals("현재 대기열에 존재하지 않습니다.", exception.message)
    }

    @Test
    fun `대기열에 있는 사용자의 순위를 반환한다`() {
        // given
        every { redisTemplate.opsForZSet() } returns zSetOps
        val expectedRank = 10L
        every { zSetOps.rank(QUEUE_WAITING_KEY, token) } returns expectedRank

        // when
        val result = queueAdapter.getRank(token)

        // then
        assertEquals(expectedRank, result)
    }

    @Test
    fun `대기열에 없는 사용자의 순위는 null이다`() {
        // given
        every { redisTemplate.opsForZSet() } returns zSetOps
        every { zSetOps.rank(QUEUE_WAITING_KEY, token) } returns null

        // when
        val result = queueAdapter.getRank(token)

        // then
        assertNull(result)
    }
}
