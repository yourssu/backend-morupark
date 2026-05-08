package com.yourssu.morupark.queue.sub.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(InvalidWaitingTokenException::class)
    fun handleInvalidWaitingTokenException(e: InvalidWaitingTokenException): ResponseEntity<Map<String, String>> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            mapOf(
                "error" to "InvalidWaitingToken",
                "message" to (e.message ?: "유효하지 않은 번호표입니다. 다시 시도해주세요.")
            )
        )
    }

    @ExceptionHandler(SoldOutException::class)
    fun handleSoldOutException(e: SoldOutException): ResponseEntity<Map<String, String>> {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            mapOf(
                "error" to "SoldOut",
                "message" to "재고가 모두 소진되었습니다."
            )
        )
    }
}