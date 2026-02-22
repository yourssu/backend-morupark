package com.yourssu.morupark.auth.application.dto

import com.yourssu.morupark.auth.business.command.PlatformRegisterCommand

class PlatformRegisterRequest (
    val name: String,
    val redirectUrl: String,
) {
    fun toCommand(): PlatformRegisterCommand {
        return PlatformRegisterCommand(
            name = name,
            redirectUrl = redirectUrl,
        )
    }
}
