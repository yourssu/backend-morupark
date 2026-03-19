package com.yourssu.morupark.queue.application

import com.fasterxml.jackson.annotation.JsonInclude
import com.yourssu.morupark.queue.business.FailureReason
import com.yourssu.morupark.queue.business.TicketStatus

@JsonInclude(JsonInclude.Include.NON_NULL)
data class TicketStatusResponse(
    val status: TicketStatus,
    val rank: Long? = null,
    val estimatedWaitSeconds: Long? = null,
    val reason: FailureReason? = null
)
