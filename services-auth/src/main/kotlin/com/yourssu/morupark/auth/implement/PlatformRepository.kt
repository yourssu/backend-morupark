package com.yourssu.morupark.auth.implement

import com.yourssu.morupark.auth.implement.domain.Platform

interface PlatformRepository {
    fun save(platform: Platform): Platform
    fun getByName(name: String): Platform
    fun getById(id: Long): Platform
    fun getById(platformId: Long): Platform
}
