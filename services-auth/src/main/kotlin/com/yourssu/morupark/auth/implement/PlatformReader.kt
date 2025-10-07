package com.yourssu.morupark.auth.implement

import com.yourssu.morupark.auth.implement.domain.Platform
import org.springframework.stereotype.Component

@Component
class PlatformReader(
    private val platformRepository: PlatformRepository,
) {
    fun getByName(name: String): Platform {
        return platformRepository.getByName(name)
    }

    fun getById(platformId: Long): Platform {
        return platformRepository.getById(platformId)
    }
}
