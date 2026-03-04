package com.yourssu.morupark.goods.implement

import java.time.LocalDateTime

class Winner(
    val id: Long? = null,
    val studentId: String,
    val phoneNumber: String,
    val wonAt: LocalDateTime = LocalDateTime.now(),
)
