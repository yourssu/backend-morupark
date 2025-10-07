package com.yourssu.morupark.auth.business

import com.yourssu.morupark.auth.application.dto.UserRegisterResponse
import com.yourssu.morupark.auth.business.command.UserRegisterCommand
import com.yourssu.morupark.auth.config.properties.JwtProperties
import com.yourssu.morupark.auth.implement.PlatformReader
import com.yourssu.morupark.auth.implement.UserWriter
import com.yourssu.morupark.auth.implement.domain.User
import com.yourssu.morupark.auth.util.JwtUtil
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userWriter: UserWriter,
    private val platformReader: PlatformReader,
    private val jwtProperties: JwtProperties,
) {
    fun register(command: UserRegisterCommand): UserRegisterResponse {
        val platform = platformReader.getByName(command.platformName)
        val user = userWriter.save(User(platformId = platform.id!!))
        val jwtUtil = JwtUtil(platform.secretKey, jwtProperties.accessTokenExpiration)
        val token = jwtUtil.generateToken(user.id!!)
        return UserRegisterResponse.of(
            accessToken = token,
            expiredIn = jwtProperties.accessTokenExpiration
        )
    }
}
