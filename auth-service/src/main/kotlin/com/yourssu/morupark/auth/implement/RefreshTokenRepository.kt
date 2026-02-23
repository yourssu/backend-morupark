package com.yourssu.morupark.auth.implement

import com.yourssu.morupark.auth.implement.domain.RefreshToken

interface RefreshTokenRepository {
    fun save(refreshToken: RefreshToken): RefreshToken
    fun findByToken(token: String): RefreshToken?
    fun deleteByToken(token: String)
}
