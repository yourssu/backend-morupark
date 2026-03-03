package com.yourssu.morupark.goods.implement

import com.yourssu.morupark.goods.domain.Goods
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface GoodsRepository : JpaRepository<Goods, Long> {

    @Modifying
    @Query("UPDATE Goods g SET g.stock = g.stock - 1 WHERE g.id = :id AND g.stock > 0")
    fun decrementStock(id: Long): Int
}
