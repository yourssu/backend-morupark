package com.yourssu.morupark.goods.implement

interface GoodsRepository {
    fun decrementStock(id: Long): Int
    fun markSoldOut(id: Long): Int
}
