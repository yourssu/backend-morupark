package com.yourssu.morupark.auth.business

import com.yourssu.morupark.auth.application.dto.PlatformRegisterResponse
import com.yourssu.morupark.auth.business.command.PlatformRegisterCommand
import com.yourssu.morupark.auth.implement.PlatformWriter
import org.springframework.stereotype.Service

@Service
class PlatformService(
    private val platformWriter: PlatformWriter,
) {

    fun register(command : PlatformRegisterCommand) : PlatformRegisterResponse {
        val platform = platformWriter.save(command.toDomain())
        return PlatformRegisterResponse.from(platform)
    }

}
