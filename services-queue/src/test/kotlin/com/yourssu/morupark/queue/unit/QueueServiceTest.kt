package com.yourssu.morupark.queue.unit

import com.yourssu.morupark.queue.business.FailureReason
import com.yourssu.morupark.queue.business.QueueService
import com.yourssu.morupark.queue.business.TicketStatus
import com.yourssu.morupark.queue.implement.QueueAdapter
import com.yourssu.morupark.queue.implement.WaitingTimeEstimator
import com.yourssu.morupark.sub.exception.InvalidWaitingTokenException
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals
import kotlin.test.assertNull

@ExtendWith(MockKExtension::class)
class QueueServiceTest {

    @MockK
    private lateinit var queueAdapter: QueueAdapter

    @MockK
    private lateinit var waitingTimeEstimator: WaitingTimeEstimator

    @InjectMockKs
    private lateinit var queueService: QueueService

    private val waitingToken = "test-token"

    @Test
    fun `대기 중인 사용자는 WAITING 상태와 순위를 반환한다`() {
        // given
        val rank = 5L
        val estimatedWait = 30L
        every { queueAdapter.isInQueue(waitingToken) } returns true
        every { queueAdapter.getRank(waitingToken) } returns rank
        every { waitingTimeEstimator.estimateWaitingTime(rank) } returns estimatedWait

        // when
        val result = queueService.getStatus(waitingToken)

        // then
        assertEquals(TicketStatus.WAITING, result.status)
        assertEquals(rank, result.rank)
        assertEquals(estimatedWait, result.estimatedWaitSeconds)
        assertNull(result.reason)
    }

    @Test
    fun `PROCESSING 상태를 반환한다`() {
        // given
        every { queueAdapter.isInQueue(waitingToken) } returns false
        every { queueAdapter.getStatusFromResult(waitingToken) } returns TicketStatus.PROCESSING.name

        // when
        val result = queueService.getStatus(waitingToken)

        // then
        assertEquals(TicketStatus.PROCESSING, result.status)
        assertNull(result.reason)
    }

    @Test
    fun `SUCCESS 상태를 반환한다`() {
        // given
        every { queueAdapter.isInQueue(waitingToken) } returns false
        every { queueAdapter.getStatusFromResult(waitingToken) } returns TicketStatus.SUCCESS.name

        // when
        val result = queueService.getStatus(waitingToken)

        // then
        assertEquals(TicketStatus.SUCCESS, result.status)
        assertNull(result.reason)
    }

    @Test
    fun `낙첨 시 FAILED 상태와 LOST reason을 반환한다`() {
        // given
        every { queueAdapter.isInQueue(waitingToken) } returns false
        every { queueAdapter.getStatusFromResult(waitingToken) } returns "FAILED:LOST"

        // when
        val result = queueService.getStatus(waitingToken)

        // then
        assertEquals(TicketStatus.FAILED, result.status)
        assertEquals(FailureReason.LOST, result.reason)
    }

    @Test
    fun `재고 소진 시 FAILED 상태와 SOLD_OUT reason을 반환한다`() {
        // given
        every { queueAdapter.isInQueue(waitingToken) } returns false
        every { queueAdapter.getStatusFromResult(waitingToken) } returns "FAILED:SOLD_OUT"

        // when
        val result = queueService.getStatus(waitingToken)

        // then
        assertEquals(TicketStatus.FAILED, result.status)
        assertEquals(FailureReason.SOLD_OUT, result.reason)
    }

    @Test
    fun `유효하지 않은 토큰으로 조회 시 예외가 발생한다`() {
        // given
        every { queueAdapter.isInQueue(waitingToken) } returns false
        every { queueAdapter.getStatusFromResult(waitingToken) } returns null

        // when & then
        assertThrows<InvalidWaitingTokenException> {
            queueService.getStatus(waitingToken)
        }
    }
}
