package com.yourssu.morupark.auth.application

import com.yourssu.morupark.auth.application.dto.PlatformRegisterRequest
import com.yourssu.morupark.auth.application.dto.PlatformRegisterResponse
import com.yourssu.morupark.auth.business.PlatformService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/external-servers")
class PlatformController(
    private val platformService: PlatformService,
) {
    @PostMapping
    fun registerPlatform(@RequestBody request: PlatformRegisterRequest): ResponseEntity<PlatformRegisterResponse> {
        val response = platformService.register(request.toCommand())
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(response)
    }
}
