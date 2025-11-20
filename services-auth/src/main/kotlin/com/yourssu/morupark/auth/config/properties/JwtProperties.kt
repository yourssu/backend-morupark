package com.yourssu.morupark.auth.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "security.jwt")
data class JwtProperties(
    val secret: String,
    val accessTokenExpiration: Long = 86400000,
    val refreshTokenExpiration: Long = 604800000, // 7 days by default
)
