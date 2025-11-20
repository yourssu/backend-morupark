package com.yourssu.morupark.auth.application.dto

class UserRegisterResponse(
    val accessToken: String,
    val expiredIn: Long,
    val refreshToken: String,
    val refreshExpiredIn: Long,
) {
    companion object {
        fun of(
            accessToken: String,
            expiredIn: Long,
            refreshToken: String,
            refreshExpiredIn: Long,
        ): UserRegisterResponse {
            return UserRegisterResponse(
                accessToken = accessToken,
                expiredIn = expiredIn,
                refreshToken = refreshToken,
                refreshExpiredIn = refreshExpiredIn,
            )
        }
    }
}
