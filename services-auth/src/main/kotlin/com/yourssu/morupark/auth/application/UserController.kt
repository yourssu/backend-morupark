package com.yourssu.morupark.auth.application

import com.yourssu.morupark.auth.business.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class UserController(
    private val userService: UserService,
) {
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<LoginResponse> {
        val loginResponse = userService.login(request.studentId, request.phoneNumber)
        return ResponseEntity.ok(loginResponse)
    }
}
