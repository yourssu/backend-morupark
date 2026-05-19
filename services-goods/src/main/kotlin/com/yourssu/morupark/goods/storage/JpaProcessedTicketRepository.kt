package com.yourssu.morupark.goods.storage

import org.springframework.data.jpa.repository.JpaRepository

interface JpaProcessedTicketRepository : JpaRepository<ProcessedTicketEntity, String>
