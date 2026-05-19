package com.yourssu.morupark.goods.storage

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.domain.Persistable
import java.time.LocalDateTime

@Entity
@Table(name = "processed_ticket")
class ProcessedTicketEntity(
    @Id
    @Column(name = "waiting_token")
    val waitingToken: String,

    @Column(name = "processed_at", nullable = false)
    val processedAt: LocalDateTime = LocalDateTime.now(),
) : Persistable<String> {
    override fun getId(): String = waitingToken
    override fun isNew(): Boolean = true
}
