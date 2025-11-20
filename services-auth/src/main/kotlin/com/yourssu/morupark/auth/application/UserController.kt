package com.yourssu.morupark.auth.application

import com.yourssu.morupark.auth.application.dto.TokenRefreshRequest
import com.yourssu.morupark.auth.application.dto.UserRegisterRequest
import com.yourssu.morupark.auth.application.dto.UserRegisterResponse
import com.yourssu.morupark.auth.business.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth/token")
class UserController(
    private val userService: UserService,
) {

    @PostMapping
    fun register(@RequestBody request : UserRegisterRequest) : ResponseEntity<UserRegisterResponse> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(userService.register(request.toCommand()))
    }

    @PostMapping("/refresh")
    fun refresh(@RequestBody request: TokenRefreshRequest): ResponseEntity<UserRegisterResponse> {
        return ResponseEntity.ok(userService.refresh(request.toCommand()))
    }
}
