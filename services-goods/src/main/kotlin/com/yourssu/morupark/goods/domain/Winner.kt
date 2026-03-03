package com.yourssu.morupark.goods.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "winner")
class Winner(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val studentId: String,

    @Column(nullable = false)
    val phoneNumber: String,

    @Column(nullable = false)
    val wonAt: LocalDateTime = LocalDateTime.now()
)