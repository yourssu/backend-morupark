package com.yourssu.morupark.auth.storage

import com.yourssu.morupark.auth.implement.PlatformRepository
import com.yourssu.morupark.auth.implement.domain.Platform
import com.yourssu.morupark.auth.storage.persistence.PlatformEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
class PlatformRepositoryImpl(
    private val platformJpaRepository: PlatformJpaRepository,
): PlatformRepository {
    override fun save(platform: Platform): Platform {
        return platformJpaRepository.save(PlatformEntity.from(platform))
            .toDomain()
    }
}

interface PlatformJpaRepository: JpaRepository<PlatformEntity, Long>
