package com.yourssu.morupark.auth.storage.persistence

import com.yourssu.morupark.auth.implement.UserRepository
import com.yourssu.morupark.auth.implement.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
class UserRepositoryImpl(
    private val userJpaRepository: UserJpaRepository
): UserRepository {
    override fun save(user: User): User {
        val entity = UserEntity.from(user)
        return userJpaRepository.save(entity).toDomain()
    }
}

interface UserJpaRepository: JpaRepository<UserEntity, Long>
