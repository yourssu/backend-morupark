package com.yourssu.morupark.goods.business

import com.yourssu.morupark.goods.implement.TicketResultProducer
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class KafkaEventPublisher(
    private val ticketResultProducer: TicketResultProducer
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: TicketResultEvent) {
        log.info("[PUBLISHER] 이벤트 발행: ${event::class.simpleName}")
        when (event) {
            is TicketSuccessEvent -> ticketResultProducer.sendSuccess(event.waitingToken)
            is TicketFailedEvent  -> ticketResultProducer.sendFailed(event.waitingToken, event.reason)
            is SoldOutEvent       -> ticketResultProducer.sendSoldOut()
        }
    }
}