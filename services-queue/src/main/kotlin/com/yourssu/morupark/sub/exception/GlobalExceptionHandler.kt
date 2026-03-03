package com.yourssu.morupark.sub.exception

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
}