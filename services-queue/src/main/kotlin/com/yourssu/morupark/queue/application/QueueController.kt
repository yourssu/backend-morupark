package com.yourssu.morupark.queue.application

import com.yourssu.morupark.queue.business.QueueService
import org.apache.el.parser.Token
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping(path = ["/queues"])
@RestController
class QueueController(
    private val queueService : QueueService
) {

    @PostMapping
    fun post(@RequestHeader accessToken: String): ResponseEntity<Void> {
        queueService.enqueue(accessToken)
        return ResponseEntity.accepted().build()
    }

    @GetMapping
    fun getTicket(@RequestHeader accessToken: String): ResponseEntity<String> {
        val waitingToken = queueService.getWaitingToken(accessToken)
        return ResponseEntity.ok().body(waitingToken)
    }
}
