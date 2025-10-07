package com.yourssu.morupark.auth.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "security.admin")
data class AdminProperties(
    val adminKey: String,
)
