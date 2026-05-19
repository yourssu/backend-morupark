package com.yourssu.morupark.goods.implement

interface ProcessedTicketRepository {
    fun existsByToken(waitingToken: String): Boolean
    fun save(waitingToken: String)
}
