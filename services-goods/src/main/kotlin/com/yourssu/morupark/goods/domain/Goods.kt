package com.yourssu.morupark.goods.domain

import jakarta.persistence.*

@Entity
@Table(name = "goods")
class Goods(
    @Id
    val id: Long = 1L,

    @Column(nullable = false)
    val stock: Int,

    @Column(nullable = false)
    val soldOut: Boolean = false
)
