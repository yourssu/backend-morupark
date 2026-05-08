package com.yourssu.morupark.goods.implement

import com.yourssu.morupark.goods.sub.constants.GoodsConstants
import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class TicketValidator(
    private val goodsReader: GoodsReader
) {
    fun isSoldOut(goodsId: Long): Boolean = goodsReader.isSoldOut(goodsId)

    fun isWinner(): Boolean = Random.nextFloat() < GoodsConstants.WIN_PROBABILITY
}
