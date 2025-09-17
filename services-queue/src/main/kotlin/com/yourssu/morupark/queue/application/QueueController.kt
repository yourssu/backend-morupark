package com.yourssu.morupark.queue.application

import com.yourssu.morupark.queue.business.QueueService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping(path = ["/queues"])
@RestController
class QueueController(
    private val queueService : QueueService
) {

    @PostMapping
    fun post(@RequestHeader userId: Long): ResponseEntity<Void> {
        queueService.addToQueue(userId)
        return ResponseEntity.accepted().build()
    }



}
