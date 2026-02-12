package com.yourssu.morupark.auth.business

import com.yourssu.morupark.auth.application.dto.UserRegisterResponse
import com.yourssu.morupark.auth.business.command.TokenRefreshCommand
import com.yourssu.morupark.auth.business.command.UserRegisterCommand
import com.yourssu.morupark.auth.business.dto.UserInfoResponse
import com.yourssu.morupark.auth.config.properties.JwtProperties
import com.yourssu.morupark.auth.implement.PlatformReader
import com.yourssu.morupark.auth.implement.RefreshTokenReader
import com.yourssu.morupark.auth.implement.RefreshTokenWriter
import com.yourssu.morupark.auth.implement.UserReader
import com.yourssu.morupark.auth.implement.UserWriter
import com.yourssu.morupark.auth.implement.domain.RefreshToken
import com.yourssu.morupark.auth.implement.domain.User
import com.yourssu.morupark.auth.util.JwtUtil
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class UserService(
    private val userWriter: UserWriter,
    private val platformReader: PlatformReader,
    private val refreshTokenWriter: RefreshTokenWriter,
    private val refreshTokenReader: RefreshTokenReader,
    private val jwtProperties: JwtProperties,
    private val userReader: UserReader,
) {
    fun register(command: UserRegisterCommand): UserRegisterResponse {
        val platform = platformReader.getByName(command.platformName)
        val user = userWriter.save(User(platformId = platform.id!!))
        val jwtUtil = JwtUtil(platform.secretKey, jwtProperties.accessTokenExpiration)
        val token = jwtUtil.generateToken(user.id!!)
        val refreshToken = createRefreshToken(user.id!!, platform.id!!)
        refreshTokenWriter.save(refreshToken)
        return UserRegisterResponse.of(
            accessToken = token,
            expiredIn = jwtProperties.accessTokenExpiration,
            refreshToken = refreshToken.token,
            refreshExpiredIn = jwtProperties.refreshTokenExpiration,
        )
    }

    fun refresh(command: TokenRefreshCommand): UserRegisterResponse {
        val existing = refreshTokenReader.getByToken(command.refreshToken)
            ?: throw IllegalArgumentException("Invalid refresh token")

        if (existing.expiresAt.isBefore(Instant.now())) {
            refreshTokenWriter.deleteByToken(existing.token)
            throw IllegalArgumentException("Expired refresh token")
        }

        val platform = platformReader.getById(existing.platformId)
        val jwtUtil = JwtUtil(platform.secretKey, jwtProperties.accessTokenExpiration)
        val newAccessToken = jwtUtil.generateToken(existing.userId)

        refreshTokenWriter.deleteByToken(existing.token)
        val rotatedRefreshToken = createRefreshToken(existing.userId, existing.platformId)
        refreshTokenWriter.save(rotatedRefreshToken)

        return UserRegisterResponse.of(
            accessToken = newAccessToken,
            expiredIn = jwtProperties.accessTokenExpiration,
            refreshToken = rotatedRefreshToken.token,
            refreshExpiredIn = jwtProperties.refreshTokenExpiration,
        )
    }

    private fun createRefreshToken(userId: Long, platformId: Long): RefreshToken {
        val expiresAt = Instant.now().plusMillis(jwtProperties.refreshTokenExpiration)
        return RefreshToken.create(
            userId = userId,
            platformId = platformId,
            expiresAt = expiresAt,
        )
    }

    fun verify(userId: Long) : UserInfoResponse {
        val user = userReader.getById(userId)
        val platform = platformReader.getById(user.platformId)
        return UserInfoResponse.of(userId = user.id!!, platform = platform)
    }
}
