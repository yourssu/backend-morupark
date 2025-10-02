package com.yourssu.morupark.auth.implement

import com.yourssu.morupark.auth.implement.domain.Platform

interface PlatformRepository {
    fun save(platform: Platform): Platform
}
