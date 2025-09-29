package com.yourssu.morupark.auth.storage.persistence

import com.yourssu.morupark.auth.implement.domain.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "users")
class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "platform_id", nullable = false)
    val platformId: Long,
){
    companion object {
        fun from(user: User): UserEntity {
            return UserEntity(
                id = user.id,
                platformId = user.platformId,
            )
        }
    }

    fun toDomain(): User {
        return User(
            id = id,
            platformId = platformId,
        )
    }
}
