package com.yourssu.morupark.goods.unit

import com.yourssu.morupark.goods.business.*
import com.yourssu.morupark.goods.implement.GoodsRepository
import com.yourssu.morupark.goods.implement.Winner
import com.yourssu.morupark.goods.implement.WinnerRepository
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.context.ApplicationEventPublisher
import kotlin.random.Random
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExtendWith(MockKExtension::class)
class GoodsServiceTest {

    @MockK
    private lateinit var winnerRepository: WinnerRepository

    @MockK
    private lateinit var goodsRepository: GoodsRepository

    @MockK
    private lateinit var eventPublisher: ApplicationEventPublisher

    @InjectMockKs
    private lateinit var goodsService: GoodsService

    private val waitingToken = "test-token"
    private val studentId = "20230001"
    private val phoneNumber = "010-1234-5678"

    @BeforeEach
    fun setUp() {
        mockkObject(Random.Default)
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(Random.Default)
    }

    @Test
    fun `당첨되고 재고 차감 성공 시 당첨 이벤트를 발행한다`() {
        // given
        val events = mutableListOf<Any>()
        every { Random.Default.nextFloat() } returns 0.5f
        every { goodsRepository.decrementStock(any()) } returns 1
        every { winnerRepository.save(any()) } returns Winner(studentId = studentId, phoneNumber = phoneNumber)
        every { eventPublisher.publishEvent(capture(events)) } just runs

        // when
        goodsService.processTicket(waitingToken, studentId, phoneNumber)

        // then
        assertTrue(events.any { it == TicketSuccessEvent(waitingToken) })
        verify { winnerRepository.save(any()) }
    }

    @Test
    fun `당첨됐지만 재고 없음 시 SOLD_OUT 실패 이벤트와 SoldOutEvent를 발행한다`() {
        // given
        val events = mutableListOf<Any>()
        every { Random.Default.nextFloat() } returns 0.5f
        every { goodsRepository.decrementStock(any()) } returns 0
        every { goodsRepository.markSoldOut(any()) } returns 1
        every { eventPublisher.publishEvent(capture(events)) } just runs

        // when
        goodsService.processTicket(waitingToken, studentId, phoneNumber)

        // then
        assertTrue(events.any { it == TicketFailedEvent(waitingToken, FailureReason.SOLD_OUT) })
        assertTrue(events.any { it is SoldOutEvent })
    }

    @Test
    fun `당첨됐지만 이미 SOLD_OUT 처리된 경우 SoldOutEvent를 발행하지 않는다`() {
        // given
        val events = mutableListOf<Any>()
        every { Random.Default.nextFloat() } returns 0.5f
        every { goodsRepository.decrementStock(any()) } returns 0
        every { goodsRepository.markSoldOut(any()) } returns 0
        every { eventPublisher.publishEvent(capture(events)) } just runs

        // when
        goodsService.processTicket(waitingToken, studentId, phoneNumber)

        // then
        assertTrue(events.any { it == TicketFailedEvent(waitingToken, FailureReason.SOLD_OUT) })
        assertFalse(events.any { it is SoldOutEvent })
    }

    @Test
    fun `낙첨 시 LOST 실패 이벤트를 발행한다`() {
        // given
        val events = mutableListOf<Any>()
        every { Random.Default.nextFloat() } returns 0.99f
        every { eventPublisher.publishEvent(capture(events)) } just runs

        // when
        goodsService.processTicket(waitingToken, studentId, phoneNumber)

        // then
        assertTrue(events.any { it == TicketFailedEvent(waitingToken, FailureReason.LOST) })
        verify(exactly = 0) { goodsRepository.decrementStock(any()) }
    }
}
