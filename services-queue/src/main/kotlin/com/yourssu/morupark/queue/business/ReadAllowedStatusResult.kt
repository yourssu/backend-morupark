package com.yourssu.morupark.queue.business

data class ReadAllowedStatusResult(
    val status: TicketStatus,
    val externalServerToken: String
)
