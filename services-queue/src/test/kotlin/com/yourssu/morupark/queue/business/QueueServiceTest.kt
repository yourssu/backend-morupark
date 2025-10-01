package com.yourssu.morupark.queue.business

import com.yourssu.morupark.queue.implement.AuthAdapter
import com.yourssu.morupark.queue.implement.KafkaProducer
import com.yourssu.morupark.queue.implement.QueueAdapter
import com.yourssu.morupark.queue.implement.WaitingTimeEstimator
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@ExtendWith(MockKExtension::class)
class QueueServiceTest {

    @MockK
    private lateinit var kafkaProducer: KafkaProducer

    @MockK
    private lateinit var authAdapter: AuthAdapter

    @MockK
    private lateinit var queueAdapter: QueueAdapter

    @MockK
    private lateinit var waitingTimeEstimator: WaitingTimeEstimator

    @InjectMockKs
    private lateinit var queueService: QueueService

    private val accessToken = "valid-access-token"
    private val waitingToken = "waiting-token"
    private val externalServerToken = "external-server-token"

    @Test
    fun `유효한 토큰으로 큐에 등록한다`() {
        // given
        every { authAdapter.isTokenValid(accessToken) } returns true
        every { kafkaProducer.send(accessToken) } just runs

        // when
        queueService.enqueue(accessToken)

        // then
        verify { authAdapter.isTokenValid(accessToken) }
        verify { kafkaProducer.send(accessToken) }
    }

    @Test
    fun `유효하지 않은 토큰으로 큐 등록 시 예외가 발생한다`() {
        // given
        every { authAdapter.isTokenValid(accessToken) } returns false

        // when & then
        val exception = assertFailsWith<IllegalStateException> {
            queueService.enqueue(accessToken)
        }
        assertEquals("Access token is invalid", exception.message)
        verify { authAdapter.isTokenValid(accessToken) }
        verify(exactly = 0) { kafkaProducer.send(any()) }
    }

    @Test
    fun `유효한 토큰과 큐에 있는 사용자의 대기 토큰을 반환한다`() {
        // given
        every { authAdapter.isTokenValid(accessToken) } returns true
        every { queueAdapter.isInQueue(accessToken) } returns true
        every { authAdapter.getWaitingToken(accessToken) } returns waitingToken

        // when
        val result = queueService.getWaitingToken(accessToken)

        // then
        assertEquals(waitingToken, result)
        verify { authAdapter.isTokenValid(accessToken) }
        verify { queueAdapter.isInQueue(accessToken) }
        verify { authAdapter.getWaitingToken(accessToken) }
    }

    @Test
    fun `유효하지 않은 토큰으로 대기 토큰 요청 시 예외가 발생한다`() {
        // given
        every { authAdapter.isTokenValid(accessToken) } returns false

        // when & then
        val exception = assertFailsWith<IllegalStateException> {
            queueService.getWaitingToken(accessToken)
        }
        assertEquals("Access token is invalid", exception.message)
        verify { authAdapter.isTokenValid(accessToken) }
        verify(exactly = 0) { queueAdapter.isInQueue(any()) }
        verify(exactly = 0) { authAdapter.getWaitingToken(any()) }
    }

    @Test
    fun `큐에 없는 사용자의 대기 토큰 요청 시 예외가 발생한다`() {
        // given
        every { authAdapter.isTokenValid(accessToken) } returns true
        every { queueAdapter.isInQueue(accessToken) } returns false

        // when & then
        val exception = assertFailsWith<IllegalStateException> {
            queueService.getWaitingToken(accessToken)
        }
        assertEquals("Access token is not in queue", exception.message)
        verify { authAdapter.isTokenValid(accessToken) }
        verify { queueAdapter.isInQueue(accessToken) }
        verify(exactly = 0) { authAdapter.getWaitingToken(any()) }
    }

    @Test
    fun `ALLOWED 상태인 사용자는 허용된 상태 결과를 반환한다`() {
        // given
        every { queueAdapter.getTicketStatus(accessToken) } returns TicketStatus.ALLOWED
        every { queueAdapter.deleteFromAllowedQueue(accessToken) } just runs
        every { authAdapter.getExternalServerToken(waitingToken) } returns externalServerToken

        // when
        val result = queueService.getTicketStatusResult(accessToken, waitingToken)

        // then
        assertTrue(result is ReadAllowedStatusResult)
        assertEquals(TicketStatus.ALLOWED, result.status)
        assertEquals(externalServerToken, result.externalServerToken)

        verify { queueAdapter.getTicketStatus(accessToken) }
        verify { queueAdapter.deleteFromAllowedQueue(accessToken) }
        verify { authAdapter.getExternalServerToken(waitingToken) }
    }

    @Test
    fun `WAITING 상태인 사용자는 대기 상태 결과를 반환한다`() {
        // given
        val rank = 10L
        val estimatedWaitingTime = 300L
        every { queueAdapter.getTicketStatus(accessToken) } returns TicketStatus.WAITING
        every { queueAdapter.getRank(accessToken) } returns rank
        every { waitingTimeEstimator.estimateWaitingTime(rank) } returns estimatedWaitingTime

        // when
        val result = queueService.getTicketStatusResult(accessToken, waitingToken)

        // then
        assertTrue(result is ReadWaitingStatusResult)
        assertEquals(TicketStatus.WAITING, result.status)
        assertEquals(rank, result.rank)
        assertEquals(estimatedWaitingTime, result.estimatedWaitingTime)

        verify { queueAdapter.getTicketStatus(accessToken) }
        verify { queueAdapter.getRank(accessToken) }
        verify { waitingTimeEstimator.estimateWaitingTime(rank) }
        verify(exactly = 0) { queueAdapter.deleteFromAllowedQueue(any()) }
    }
}