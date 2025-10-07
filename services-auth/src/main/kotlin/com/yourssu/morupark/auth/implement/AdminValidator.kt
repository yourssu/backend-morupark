package com.yourssu.morupark.auth.implement

import com.yourssu.morupark.auth.config.properties.AdminProperties
import org.springframework.stereotype.Component

@Component
class AdminValidator(
    private val adminProperties: AdminProperties,
) {
    fun validate(adminKey: String) {
        if (adminKey != adminProperties.adminKey) {
            throw IllegalArgumentException("Invalid admin key")
        }
    }
}
