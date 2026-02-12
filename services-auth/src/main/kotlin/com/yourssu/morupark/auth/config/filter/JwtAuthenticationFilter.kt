package com.yourssu.morupark.auth.config.filter

import com.yourssu.morupark.auth.config.properties.JwtProperties
import com.yourssu.morupark.auth.util.JwtUtil
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.web.filter.OncePerRequestFilter

class JwtAuthenticationFilter(
    jwtProperties: JwtProperties,
) : OncePerRequestFilter() {
    private val jwtUtil = JwtUtil(jwtProperties.secret, jwtProperties.accessTokenExpiration)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val authHeader = request.getHeader("Authorization")
        if (authHeader.isNullOrBlank()) {
            filterChain.doFilter(request, response)
            return
        }

        if (!authHeader.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Authorization header")
            return
        }

        val token = authHeader.substringAfter("Bearer ").trim()
        if (token.isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing token")
            return
        }

        if (!jwtUtil.validateToken(token)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token")
            return
        }

        val userId = jwtUtil.getUserIdFromToken(token)
        val authentication = UsernamePasswordAuthenticationToken(userId, null, emptyList())
        authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
        SecurityContextHolder.getContext().authentication = authentication

        filterChain.doFilter(request, response)
    }
}
