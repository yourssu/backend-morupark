package com.yourssu.morupark.queue.business

data class TicketStatus(
    val status: Status,
    val rank: Long,
    val totalAhead: Long,
    val estimatedWaitingTime: Long
)
