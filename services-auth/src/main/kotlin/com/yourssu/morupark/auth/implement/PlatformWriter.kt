package com.yourssu.morupark.auth.implement

import com.yourssu.morupark.auth.implement.domain.Platform
import org.springframework.stereotype.Component

@Component
class PlatformWriter(
    private val platformRepository: PlatformRepository,
) {
    fun save(platform: Platform): Platform {
        return platformRepository.save(platform)
    }
}
