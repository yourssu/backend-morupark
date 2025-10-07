package com.yourssu.morupark.auth.application.dto

class UserRegisterResponse(
    val accessToken : String,
    val expiredIn: Long,
) {
    companion object {
        fun of(accessToken: String, expiredIn: Long): UserRegisterResponse {
            return UserRegisterResponse(
                accessToken = accessToken,
                expiredIn = expiredIn
            )
        }
    }
}
