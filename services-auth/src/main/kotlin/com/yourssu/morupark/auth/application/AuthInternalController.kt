package com.yourssu.morupark.auth.application

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.sql.DriverManager.println

@RestController
@RequestMapping("/api/internal/auth")
class AuthInternalController {

    // 예시: userId를 받아 유효성만 검증 후 true/false 반환
    @GetMapping("/validate")
    fun validateUser(@RequestParam userId: String): Boolean {
        // 실제로는 DB 조회, 토큰 검증 등의 로직이 들어갑니다.
        println("Validating user: $userId")
        return true // 일단 무조건 통과라고 가정
    }
}
