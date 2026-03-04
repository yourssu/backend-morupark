package com.yourssu.morupark.goods.storage

import com.yourssu.morupark.goods.implement.Winner
import com.yourssu.morupark.goods.implement.WinnerRepository
import org.springframework.stereotype.Repository

@Repository
class WinnerRepositoryImpl(
    private val jpaWinnerRepository: JpaWinnerRepository,
) : WinnerRepository {

    override fun save(winner: Winner): Winner =
        jpaWinnerRepository.save(WinnerEntity.from(winner)).toDomain()
}
