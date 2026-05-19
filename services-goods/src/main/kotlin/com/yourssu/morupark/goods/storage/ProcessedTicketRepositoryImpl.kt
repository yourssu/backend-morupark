package com.yourssu.morupark.goods.storage

import com.yourssu.morupark.goods.implement.ProcessedTicketRepository
import org.springframework.stereotype.Repository

@Repository
class ProcessedTicketRepositoryImpl(
    private val jpaProcessedTicketRepository: JpaProcessedTicketRepository,
) : ProcessedTicketRepository {

    override fun existsByToken(waitingToken: String): Boolean =
        jpaProcessedTicketRepository.existsById(waitingToken)

    override fun save(waitingToken: String) {
        jpaProcessedTicketRepository.save(ProcessedTicketEntity(waitingToken))
    }
}
