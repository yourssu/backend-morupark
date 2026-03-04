package com.yourssu.morupark.goods.storage

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface JpaGoodsRepository : JpaRepository<GoodsEntity, Long> {

    @Modifying
    @Query("UPDATE GoodsEntity g SET g.stock = g.stock - 1 WHERE g.id = :id AND g.stock > 0")
    fun decrementStock(id: Long): Int

    @Modifying
    @Query("UPDATE GoodsEntity g SET g.soldOut = true WHERE g.id = :id AND g.soldOut = false")
    fun markSoldOut(id: Long): Int
}
