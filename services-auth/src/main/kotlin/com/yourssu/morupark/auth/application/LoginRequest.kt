package com.yourssu.morupark.auth.application

data class LoginRequest(
    val studentId: String,
    val phoneNumber: String,
)