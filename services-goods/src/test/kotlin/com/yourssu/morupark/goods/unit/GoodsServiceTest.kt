package com.yourssu.morupark.goods.unit

import com.yourssu.morupark.goods.business.*
import com.yourssu.morupark.goods.implement.TicketProcessor
import com.yourssu.morupark.goods.implement.TicketValidator
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.context.ApplicationEventPublisher
import kotlin.test.assertTrue

@ExtendWith(MockKExtension::class)
class GoodsServiceTest {

    @MockK
    private lateinit var ticketValidator: TicketValidator

    @MockK
    private lateinit var ticketProcessor: TicketProcessor

    @MockK
    private lateinit var eventPublisher: ApplicationEventPublisher

    @InjectMockKs
    private lateinit var goodsService: GoodsService

    private val waitingToken = "test-token"
    private val studentId = "20230001"
    private val phoneNumber = "010-1234-5678"

    @Test
    fun `이미 품절된 경우 당첨 판정 없이 SOLD_OUT 실패 이벤트를 발행한다`() {
        // given
        val events = mutableListOf<Any>()
        every { ticketValidator.isSoldOut(any()) } returns true
        every { eventPublisher.publishEvent(capture(events)) } just runs

        // when
        goodsService.processTicket(waitingToken, studentId, phoneNumber)

        // then
        assertTrue(events.any { it == TicketFailedEvent(waitingToken, FailureReason.SOLD_OUT) })
        verify(exactly = 0) { ticketValidator.isWinner() }
        verify(exactly = 0) { ticketProcessor.process(any(), any(), any(), any()) }
    }

    @Test
    fun `낙첨 시 LOST 실패 이벤트를 발행한다`() {
        // given
        val events = mutableListOf<Any>()
        every { ticketValidator.isSoldOut(any()) } returns false
        every { ticketValidator.isWinner() } returns false
        every { eventPublisher.publishEvent(capture(events)) } just runs

        // when
        goodsService.processTicket(waitingToken, studentId, phoneNumber)

        // then
        assertTrue(events.any { it == TicketFailedEvent(waitingToken, FailureReason.LOST) })
        verify(exactly = 0) { ticketProcessor.process(any(), any(), any(), any()) }
    }

    @Test
    fun `당첨 시 TicketProcessor에 처리를 위임한다`() {
        // given
        every { ticketValidator.isSoldOut(any()) } returns false
        every { ticketValidator.isWinner() } returns true
        every { ticketProcessor.process(any(), any(), any(), any()) } just runs

        // when
        goodsService.processTicket(waitingToken, studentId, phoneNumber)

        // then
        verify { ticketProcessor.process(waitingToken, studentId, phoneNumber, 1L) }
    }
}
