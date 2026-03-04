package com.yourssu.morupark.goods.storage

import com.yourssu.morupark.goods.implement.GoodsRepository
import org.springframework.stereotype.Repository

@Repository
class GoodsRepositoryImpl(
    private val jpaGoodsRepository: JpaGoodsRepository,
) : GoodsRepository {

    override fun decrementStock(id: Long): Int = jpaGoodsRepository.decrementStock(id)

    override fun markSoldOut(id: Long): Int = jpaGoodsRepository.markSoldOut(id)
}
