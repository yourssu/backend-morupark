package com.yourssu.morupark.auth.util

import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys.hmacShaKeyFor
import java.util.*

class JwtUtil(
    secret: String,
    val expiration: Long
) {
    private val secretKey = hmacShaKeyFor(secret.toByteArray())

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
