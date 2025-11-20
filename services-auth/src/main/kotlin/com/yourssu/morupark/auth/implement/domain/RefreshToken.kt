package com.yourssu.morupark.auth.implement.domain

import java.time.Instant
import java.util.UUID

class RefreshToken(
    val id: Long? = null,
    val userId: Long,
    val token: String,
    val expiresAt: Instant,
    val platformId: Long,
) {
    companion object {
        fun create(userId: Long, platformId: Long, expiresAt: Instant): RefreshToken {
            return RefreshToken(
                userId = userId,
                token = UUID.randomUUID().toString(),
                expiresAt = expiresAt,
                platformId = platformId,
            )
        }
    }
}
