package com.yourssu.morupark.auth.business.dto

import com.yourssu.morupark.auth.implement.domain.Platform

data class UserInfoResponse(
    val userId : Long,
    val platform : PlatformInfoResponse
) {
    companion object {
        fun of(userId: Long, platform : Platform): UserInfoResponse {
            return UserInfoResponse(
                userId = userId,
                platform = PlatformInfoResponse.of(platform.id!!, platform.name
                )
            )
        }
    }
}
data class PlatformInfoResponse(
    val platformId : Long,
    val platformName : String
) {
    companion object{
        fun of(platformId: Long, platformName: String): PlatformInfoResponse {
            return PlatformInfoResponse(
                platformId = platformId,
                platformName = platformName
            )
        }
    }
}
