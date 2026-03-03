package com.yourssu.morupark.goods.business

import com.yourssu.morupark.goods.implement.TicketResultProducer
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class KafkaEventPublisher(
    private val ticketResultProducer: TicketResultProducer
) {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: TicketResultEvent) {
        when (event) {
            is TicketSuccessEvent -> ticketResultProducer.sendSuccess(event.waitingToken)
            is TicketFailedEvent  -> ticketResultProducer.sendFailed(event.waitingToken, event.reason)
            is SoldOutEvent       -> ticketResultProducer.sendSoldOut()
        }
    }
}