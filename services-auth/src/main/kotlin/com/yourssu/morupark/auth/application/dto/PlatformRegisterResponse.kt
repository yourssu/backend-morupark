package com.yourssu.morupark.auth.application.dto

import com.yourssu.morupark.auth.implement.domain.Platform

class PlatformRegisterResponse(
    val secretKey: String
) {
    companion object {
        fun from(platform: Platform): PlatformRegisterResponse {
            return PlatformRegisterResponse(
                secretKey = platform.secretKey
            )
        }
    }
}
