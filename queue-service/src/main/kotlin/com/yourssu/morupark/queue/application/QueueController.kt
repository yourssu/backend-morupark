package com.yourssu.morupark.queue.application

import com.yourssu.morupark.queue.business.QueueService
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
    fun post(@RequestHeader("Authorization") accessToken: String): ResponseEntity<Void> {
        queueService.enqueue(accessToken)
        return ResponseEntity.accepted().build()
    }

    @GetMapping
    fun getTicket(@RequestHeader("Authorization") accessToken: String): ResponseEntity<String> {
        val waitingToken = queueService.getWaitingToken(accessToken)
        return ResponseEntity.ok().body(waitingToken)
    }

    @GetMapping("/status")
    fun getStatus(
        @RequestHeader("Authorization") accessToken: String,
        @RequestHeader("X-Waiting-Token") waitingToken: String,
    ): ResponseEntity<Any> {
        val result = queueService.getTicketStatusResult(accessToken, waitingToken)
        return ResponseEntity.ok().body(result)
    }

    @GetMapping("/test")
    fun test(@RequestHeader("X-User-Id") userId: String): ResponseEntity<Map<String, String?>> {
        return ResponseEntity.ok(mapOf("message" to "gateway 연결 성공", "userId" to userId))
    }
}
