package com.yourssu.morupark.goods.unit

import com.yourssu.morupark.goods.business.FailureReason
import com.yourssu.morupark.goods.business.SoldOutEvent
import com.yourssu.morupark.goods.business.TicketFailedEvent
import com.yourssu.morupark.goods.business.TicketSuccessEvent
import com.yourssu.morupark.goods.implement.GoodsUpdater
import com.yourssu.morupark.goods.implement.TicketProcessor
import com.yourssu.morupark.goods.implement.Winner
import com.yourssu.morupark.goods.implement.WinnerRepository
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.context.ApplicationEventPublisher
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExtendWith(MockKExtension::class)
class TicketProcessorTest {

    @MockK
    private lateinit var goodsUpdater: GoodsUpdater

    @MockK
    private lateinit var winnerRepository: WinnerRepository

    @MockK
    private lateinit var eventPublisher: ApplicationEventPublisher

    @InjectMockKs
    private lateinit var ticketProcessor: TicketProcessor

    private val waitingToken = "test-token"
    private val studentId = "20230001"
    private val phoneNumber = "010-1234-5678"
    private val goodsId = 1L

    @Test
    fun `재고 차감 성공 시 당첨 이벤트를 발행한다`() {
        // given
        val events = mutableListOf<Any>()
        every { goodsUpdater.decrementStock(any()) } returns true
        every { winnerRepository.save(any()) } returns Winner(studentId = studentId, phoneNumber = phoneNumber)
        every { eventPublisher.publishEvent(capture(events)) } just runs

        // when
        ticketProcessor.process(waitingToken, studentId, phoneNumber, goodsId)

        // then
        assertTrue(events.any { it == TicketSuccessEvent(waitingToken) })
        verify { winnerRepository.save(any()) }
    }

    @Test
    fun `재고 없음 시 SOLD_OUT 실패 이벤트와 SoldOutEvent를 발행한다`() {
        // given
        val events = mutableListOf<Any>()
        every { goodsUpdater.decrementStock(any()) } returns false
        every { goodsUpdater.markSoldOut(any()) } returns true
        every { eventPublisher.publishEvent(capture(events)) } just runs

        // when
        ticketProcessor.process(waitingToken, studentId, phoneNumber, goodsId)

        // then
        assertTrue(events.any { it == TicketFailedEvent(waitingToken, FailureReason.SOLD_OUT) })
        assertTrue(events.any { it is SoldOutEvent && (it as SoldOutEvent).goodsId == goodsId })
    }

    @Test
    fun `재고 없음이지만 이미 SOLD_OUT 처리된 경우 SoldOutEvent를 발행하지 않는다`() {
        // given
        val events = mutableListOf<Any>()
        every { goodsUpdater.decrementStock(any()) } returns false
        every { goodsUpdater.markSoldOut(any()) } returns false
        every { eventPublisher.publishEvent(capture(events)) } just runs

        // when
        ticketProcessor.process(waitingToken, studentId, phoneNumber, goodsId)

        // then
        assertTrue(events.any { it == TicketFailedEvent(waitingToken, FailureReason.SOLD_OUT) })
        assertFalse(events.any { it is SoldOutEvent })
    }
}
