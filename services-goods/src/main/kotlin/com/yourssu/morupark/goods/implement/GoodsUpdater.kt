package com.yourssu.morupark.goods.implement

import org.springframework.stereotype.Component

@Component
class GoodsUpdater(
    private val goodsRepository: GoodsRepository
) {
    fun decrementStock(goodsId: Long): Boolean = goodsRepository.decrementStock(goodsId) > 0
    fun markSoldOut(goodsId: Long): Boolean = goodsRepository.markSoldOut(goodsId) > 0
}
