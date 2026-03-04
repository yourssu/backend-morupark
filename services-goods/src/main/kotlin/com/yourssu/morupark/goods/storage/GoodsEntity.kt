package com.yourssu.morupark.goods.storage

import com.yourssu.morupark.goods.implement.Goods
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "goods")
class GoodsEntity(
    @Id
    val id: Long = 1L,

    @Column(nullable = false)
    val stock: Int,

    @Column(nullable = false)
    val soldOut: Boolean = false,
) {
    companion object {
        fun from(goods: Goods): GoodsEntity = GoodsEntity(
            id = goods.id,
            stock = goods.stock,
            soldOut = goods.soldOut,
        )
    }

    fun toDomain(): Goods = Goods(
        id = id,
        stock = stock,
        soldOut = soldOut,
    )
}
