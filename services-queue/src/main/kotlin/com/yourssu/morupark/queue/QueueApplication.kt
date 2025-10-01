package com.yourssu.morupark.queue

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class QueueApplication

fun main(args: Array<String>) {
    runApplication<QueueApplication>(*args)
}
