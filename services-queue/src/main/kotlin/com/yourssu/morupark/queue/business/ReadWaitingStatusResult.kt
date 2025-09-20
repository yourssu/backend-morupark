package com.yourssu.morupark.queue.business

data class ReadWaitingStatusResult(
    val status: TicketStatus,
    val rank: Long,
    val estimatedWaitingTime: Long
)
