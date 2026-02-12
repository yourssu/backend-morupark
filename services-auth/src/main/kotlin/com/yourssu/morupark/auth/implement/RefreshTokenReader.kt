package com.yourssu.morupark.auth.implement

import com.yourssu.morupark.auth.implement.domain.RefreshToken
import org.springframework.stereotype.Component

@Component
class RefreshTokenReader(
    private val refreshTokenRepository: RefreshTokenRepository,
) {
    fun getByToken(token: String): RefreshToken? {
        return refreshTokenRepository.findByToken(token)
    }
}
