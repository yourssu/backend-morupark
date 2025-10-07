package com.yourssu.morupark.auth.business.command

import com.yourssu.morupark.auth.implement.domain.Platform

class PlatformRegisterCommand(
    val name: String,
    val redirectUrl: String,
) {
    fun toDomain(): Platform {
        return Platform.create(
            name = name,
            redirectUrl = redirectUrl,
        )
    }
}
