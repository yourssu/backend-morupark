package com.yourssu.morupark.queue.business

data class ReadStatusResult(
    val status: TicketStatus,
    val rank: Long,
    val totalAhead: Long,
    val estimatedWaitingTime: Long
)
