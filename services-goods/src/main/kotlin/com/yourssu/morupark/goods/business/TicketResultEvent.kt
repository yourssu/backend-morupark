package com.yourssu.morupark.goods.business

sealed class TicketResultEvent

data class TicketSuccessEvent(val waitingToken: String) : TicketResultEvent()
data class TicketFailedEvent(val waitingToken: String, val reason: String) : TicketResultEvent()
class SoldOutEvent : TicketResultEvent()
