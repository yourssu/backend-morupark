package com.yourssu.morupark.auth.storage.persistence

import com.yourssu.morupark.auth.implement.domain.Platform
import jakarta.persistence.*

@Entity
@Table(name = "platform")
class PlatformEntity(

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "redirect_url", nullable = false)
    val redirectUrl: String,

    @Column(name = "secret_key", nullable = false)
    val secretKey: String,
) {
    companion object {
        fun from(platform: Platform): PlatformEntity {
            return PlatformEntity(
                id = platform.id,
                name = platform.name,
                redirectUrl = platform.redirectUrl,
                secretKey = platform.secretKey,
            )
        }
    }

    fun toDomain(): Platform {
        return Platform(
            id = id!!,
            name = name,
            redirectUrl = redirectUrl,
            secretKey = secretKey,
        )
    }
}
