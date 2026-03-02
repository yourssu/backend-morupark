package com.yourssu.morupark.auth.business

import com.yourssu.morupark.auth.application.LoginResponse
import com.yourssu.morupark.auth.sub.util.JwtUtil
import org.springframework.stereotype.Service

@Service
class UserService(
    private val jwtUtil: JwtUtil,
) {
    fun login(studentId: String, phoneNumber: String): LoginResponse {
        return LoginResponse(accessToken = jwtUtil.generateToken(studentId, phoneNumber))
    }
}
