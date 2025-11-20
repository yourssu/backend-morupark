package com.yourssu.morupark.auth.implement

import com.yourssu.morupark.auth.implement.domain.RefreshToken
import org.springframework.stereotype.Component

@Component
class RefreshTokenWriter(
    private val refreshTokenRepository: RefreshTokenRepository,
) {
    fun save(refreshToken: RefreshToken): RefreshToken {
        return refreshTokenRepository.save(refreshToken)
    }

    fun deleteByToken(token: String) {
        refreshTokenRepository.deleteByToken(token)
    }
}
