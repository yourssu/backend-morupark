package com.yourssu.morupark.auth.util

import com.yourssu.morupark.auth.config.properties.JwtProperties
import io.jsonwebtoken.JwtException
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


    fun generateToken(userId: Long): String {
        val token = Jwts.builder()
            .subject(userId.toString())
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(Date(System.currentTimeMillis() + expiration))
            .signWith(secretKey)
            .compact()

        return token
    }

    fun validateToken(token: String): Boolean {
        try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
        } catch (_: JwtException) {
            return false
        }
        return true
    }

    fun getUserIdFromToken(token: String): Long {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
            .subject
            .toLong()
    }
}
