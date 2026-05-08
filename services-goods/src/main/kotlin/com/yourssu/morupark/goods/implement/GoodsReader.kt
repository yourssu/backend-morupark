package com.yourssu.morupark.goods.implement

import org.springframework.stereotype.Component

@Component
class GoodsReader(
    private val goodsRepository: GoodsRepository
) {
    fun isSoldOut(goodsId: Long): Boolean = goodsRepository.isSoldOut(goodsId)
}
