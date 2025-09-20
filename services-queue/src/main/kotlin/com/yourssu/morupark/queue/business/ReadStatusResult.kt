package com.yourssu.morupark.queue.business

data class ReadStatusResult(
    val status: TicketStatus,
    val rank: Long,
    val estimatedWaitingTime: Long
)
