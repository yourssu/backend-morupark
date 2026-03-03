package com.yourssu.morupark.goods.implement

import com.yourssu.morupark.goods.domain.Winner
import org.springframework.data.jpa.repository.JpaRepository

interface WinnerRepository : JpaRepository<Winner, Long>