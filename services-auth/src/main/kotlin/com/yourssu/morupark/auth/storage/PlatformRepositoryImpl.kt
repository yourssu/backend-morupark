package com.yourssu.morupark.auth.storage

import com.yourssu.morupark.auth.implement.PlatformRepository
import com.yourssu.morupark.auth.implement.domain.Platform
import com.yourssu.morupark.auth.storage.persistence.PlatformEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
class PlatformRepositoryImpl(
    private val platformJpaRepository: PlatformJpaRepository,
): PlatformRepository {
    override fun save(platform: Platform): Platform {
        return platformJpaRepository.save(PlatformEntity.from(platform))
            .toDomain()
    }

    override fun getById(id: Long): Platform {
        return platformJpaRepository.findById(id)
            .orElseThrow { NoSuchElementException("No platform with id: $id") }
            .toDomain()
    }

    override fun getByName(name: String): Platform {
        return platformJpaRepository.findByName(name)
            ?.toDomain()
            ?: throw NoSuchElementException("No platform with name: $name")
    }

    override fun getById(platformId: Long): Platform {
        return platformJpaRepository.findById(platformId)
            .orElseThrow { NoSuchElementException("No platform with id: $platformId") }
            .toDomain()
    }
}

interface PlatformJpaRepository: JpaRepository<PlatformEntity, Long> {
    @Query("select p from PlatformEntity p where p.name = :name")
    fun findByName(name: String): PlatformEntity?
}
