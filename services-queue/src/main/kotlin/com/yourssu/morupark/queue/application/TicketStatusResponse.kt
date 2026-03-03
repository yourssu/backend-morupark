package com.yourssu.morupark.queue.application

import com.yourssu.morupark.queue.business.TicketStatus

data class TicketStatusResponse(
    val status: TicketStatus,
    val rank: Long? = null,
    val estimatedWaitSeconds: Long? = null,
    val message: String? = null
)
