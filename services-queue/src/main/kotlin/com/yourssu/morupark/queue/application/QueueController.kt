package com.yourssu.morupark.queue.application

import com.yourssu.morupark.queue.business.QueueService
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/api/queues")
@RestController
class QueueController(
    private val queueService: QueueService
) {

    @PostMapping
    fun enqueue(
        @Parameter(hidden = true) @RequestHeader("X-User-Id") studentId: String,
        @Parameter(hidden = true) @RequestHeader("X-Phone-Number") phoneNumber: String,
    ): ResponseEntity<EnqueueResponse> {
        val enqueueResponse = queueService.enqueue(studentId, phoneNumber)
        return ResponseEntity.accepted().body(enqueueResponse)
    }

    @GetMapping("/status")
    fun getStatus(
        @RequestHeader("X-Waiting-Token") waitingToken: String,
    ): ResponseEntity<TicketStatusResponse> {
        val result = queueService.getStatus(waitingToken)
        return ResponseEntity.ok().body(result)
    }
}
