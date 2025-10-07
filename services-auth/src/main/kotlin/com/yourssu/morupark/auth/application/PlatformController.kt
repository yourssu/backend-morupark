package com.yourssu.morupark.auth.application

import com.yourssu.morupark.auth.annotation.UserId
import com.yourssu.morupark.auth.application.dto.PlatformRegisterRequest
import com.yourssu.morupark.auth.application.dto.PlatformRegisterResponse
import com.yourssu.morupark.auth.business.PlatformService
import com.yourssu.morupark.auth.business.dto.PlatformUrlResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/platforms")
class PlatformController(
    private val platformService: PlatformService,
) {
    @PostMapping
    fun registerPlatform(@RequestBody request: PlatformRegisterRequest): ResponseEntity<PlatformRegisterResponse> {
        val response = platformService.register(request.toCommand())
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(response)
    }

    @GetMapping("/url")
    fun getPlatformUrl(@UserId userId: Long, @RequestHeader("X-Auth") adminKey: String): ResponseEntity<PlatformUrlResponse> {
        return ResponseEntity.ok(platformService.getRedirectUrl(userId, adminKey))
    }

}
