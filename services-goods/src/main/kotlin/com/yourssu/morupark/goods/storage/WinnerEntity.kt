package com.yourssu.morupark.goods.storage

import com.yourssu.morupark.goods.implement.Winner
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "winner")
class WinnerEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val studentId: String,

    @Column(nullable = false)
    val phoneNumber: String,

    @Column(nullable = false)
    val wonAt: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        fun from(winner: Winner): WinnerEntity = WinnerEntity(
            id = winner.id,
            studentId = winner.studentId,
            phoneNumber = winner.phoneNumber,
            wonAt = winner.wonAt,
        )
    }

    fun toDomain(): Winner = Winner(
        id = id,
        studentId = studentId,
        phoneNumber = phoneNumber,
        wonAt = wonAt,
    )
}
