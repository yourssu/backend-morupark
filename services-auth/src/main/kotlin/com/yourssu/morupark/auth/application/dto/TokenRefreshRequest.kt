package com.yourssu.morupark.auth.application.dto

import com.yourssu.morupark.auth.business.command.TokenRefreshCommand

class TokenRefreshRequest(
    val refreshToken: String,
) {
    fun toCommand(): TokenRefreshCommand {
        return TokenRefreshCommand(
            refreshToken = refreshToken,
        )
    }
}
