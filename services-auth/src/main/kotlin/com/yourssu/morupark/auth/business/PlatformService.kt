package com.yourssu.morupark.auth.business

import com.yourssu.morupark.auth.application.dto.PlatformRegisterResponse
import com.yourssu.morupark.auth.business.command.PlatformRegisterCommand
import com.yourssu.morupark.auth.business.dto.PlatformUrlResponse
import com.yourssu.morupark.auth.implement.AdminValidator
import com.yourssu.morupark.auth.implement.PlatformReader
import com.yourssu.morupark.auth.implement.PlatformWriter
import com.yourssu.morupark.auth.implement.UserReader
import org.springframework.stereotype.Service

@Service
class PlatformService(
    private val platformWriter: PlatformWriter,
    private val userReader: UserReader,
    private val platformReader: PlatformReader,
    private val adminValidator: AdminValidator,
) {

    fun register(command : PlatformRegisterCommand) : PlatformRegisterResponse {
        val platform = platformWriter.save(command.toDomain())
        return PlatformRegisterResponse.from(platform)
    }

    fun getRedirectUrl(userId: Long, adminKey: String): PlatformUrlResponse {
        adminValidator.validate(adminKey)
        val user = userReader.getById(userId)
        val platform = platformReader.getById(user.platformId)
        return PlatformUrlResponse(url = platform.redirectUrl)
    }

}
