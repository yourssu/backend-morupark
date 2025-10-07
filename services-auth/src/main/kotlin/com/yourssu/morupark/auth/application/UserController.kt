package com.yourssu.morupark.auth.application

import com.yourssu.morupark.auth.annotation.UserId
import com.yourssu.morupark.auth.business.dto.UserInfoResponse
import com.yourssu.morupark.auth.application.dto.UserRegisterRequest
import com.yourssu.morupark.auth.application.dto.UserRegisterResponse
import com.yourssu.morupark.auth.business.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class UserController(
    private val userService: UserService,
) {

    @PostMapping("/token")
    fun register(@RequestBody request : UserRegisterRequest) : ResponseEntity<UserRegisterResponse> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(userService.register(request.toCommand()))
    }

    @GetMapping("/me")
    fun verify(@UserId userId: Long,) : ResponseEntity<UserInfoResponse> {
        return ResponseEntity.ok(userService.verify(userId))
    }
    
}
