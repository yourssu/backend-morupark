package com.yourssu.morupark.auth.storage.persistence

import com.yourssu.morupark.auth.implement.domain.RefreshToken
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

@Entity
@Table(name = "refresh_token")
class RefreshTokenEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "token", nullable = false, unique = true, length = 200)
    val token: String,

    @Column(name = "expires_at", nullable = false)
    val expiresAt: LocalDateTime,

    @Column(name = "platform_id", nullable = false)
    val platformId: Long,
) {
    companion object {
        fun from(refreshToken: RefreshToken): RefreshTokenEntity {
            return RefreshTokenEntity(
                id = refreshToken.id,
                userId = refreshToken.userId,
                token = refreshToken.token,
                expiresAt = LocalDateTime.ofInstant(refreshToken.expiresAt, ZoneOffset.UTC),
                platformId = refreshToken.platformId,
            )
        }
    }

    fun toDomain(): RefreshToken {
        return RefreshToken(
            id = id,
            userId = userId,
            token = token,
            expiresAt = expiresAt.toInstant(ZoneOffset.UTC),
            platformId = platformId,
        )
    }
}
