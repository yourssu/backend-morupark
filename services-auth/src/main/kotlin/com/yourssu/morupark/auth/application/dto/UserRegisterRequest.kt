package com.yourssu.morupark.auth.application.dto

import com.yourssu.morupark.auth.business.command.UserRegisterCommand

class UserRegisterRequest(
    val platformName : String
) {
    fun toCommand(): UserRegisterCommand {
        return UserRegisterCommand(
            platformName = platformName
        )
    }
}
