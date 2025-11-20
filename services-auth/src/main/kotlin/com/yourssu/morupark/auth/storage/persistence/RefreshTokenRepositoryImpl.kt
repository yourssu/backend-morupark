package com.yourssu.morupark.auth.storage.persistence

import com.yourssu.morupark.auth.implement.RefreshTokenRepository
import com.yourssu.morupark.auth.implement.domain.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
class RefreshTokenRepositoryImpl(
    private val refreshTokenJpaRepository: RefreshTokenJpaRepository,
) : RefreshTokenRepository {
    override fun save(refreshToken: RefreshToken): RefreshToken {
        val entity = RefreshTokenEntity.from(refreshToken)
        return refreshTokenJpaRepository.save(entity).toDomain()
    }

    override fun findByToken(token: String): RefreshToken? {
        return refreshTokenJpaRepository.findByToken(token)?.toDomain()
    }

    override fun deleteByToken(token: String) {
        refreshTokenJpaRepository.deleteByToken(token)
    }
}

interface RefreshTokenJpaRepository : JpaRepository<RefreshTokenEntity, Long> {
    fun findByToken(token: String): RefreshTokenEntity?
    fun deleteByToken(token: String)
}
