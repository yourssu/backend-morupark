package com.yourssu.morupark.gateway.filter

import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys.hmacShaKeyFor
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class JwtAuthenticationFilter(
    @Value("\${security.jwt.secret}") private val jwtSecret: String
) : AbstractGatewayFilterFactory<Any>(Any::class.java) {

    override fun apply(config: Any?): GatewayFilter {
        return GatewayFilter { exchange, chain ->
            val authHeader = exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION)

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                exchange.response.statusCode = HttpStatus.UNAUTHORIZED
                return@GatewayFilter exchange.response.setComplete()
            }

            val token = authHeader.substring(7)

            val secretKey = hmacShaKeyFor(jwtSecret.toByteArray())
            val subject = try {
                Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .payload
                    .subject
            } catch (e: JwtException) {
                exchange.response.statusCode = HttpStatus.UNAUTHORIZED
                return@GatewayFilter exchange.response.setComplete()
            }

            chain.filter(
                exchange.mutate()
                    .request(
                        exchange.request.mutate()
                            .header("X-User-Id", subject)
                            .build()
                    )
                    .build()
            )
        }
    }
}
