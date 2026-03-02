package com.yourssu.morupark.auth.sub.util

import com.yourssu.morupark.auth.sub.config.properties.JwtProperties
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys.hmacShaKeyFor
import org.springframework.stereotype.Component
import java.util.*

@Component
class JwtUtil(
    jwtProperties: JwtProperties
) {
    private val secretKey = hmacShaKeyFor(jwtProperties.secret.toByteArray())
    private val expiration = jwtProperties.accessTokenExpiration


    fun generateToken(studentId: String, phoneNumber: String): String {
        val token = Jwts.builder()
            .subject(studentId)
            .claim("phoneNumber", phoneNumber)
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(Date(System.currentTimeMillis() + expiration))
            .signWith(secretKey)
            .compact()

        return token
    }
}
