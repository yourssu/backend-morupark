package com.yourssu.morupark.auth.config.filter

import com.yourssu.morupark.auth.config.properties.JwtProperties
import com.yourssu.morupark.auth.util.JwtUtil
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import jakarta.servlet.FilterChain
import jakarta.servlet.DispatcherType
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.core.context.SecurityContextHolder

@ExtendWith(MockKExtension::class)
class JwtAuthenticationFilterTest {

    @MockK(relaxUnitFun = true)
    private lateinit var request: HttpServletRequest

    @MockK(relaxUnitFun = true)
    private lateinit var response: HttpServletResponse

    @MockK(relaxUnitFun = true)
    private lateinit var filterChain: FilterChain

    private val jwtProperties = JwtProperties(
        secret = "a".repeat(64),
        accessTokenExpiration = 3_600_000,
    )
    private val jwtUtil = JwtUtil(jwtProperties.secret, jwtProperties.accessTokenExpiration)
    private val filter = JwtAuthenticationFilter(jwtProperties)

    @BeforeEach
    fun setup() {
        SecurityContextHolder.clearContext()
        every { request.getAttribute(any()) } returns null
        every { request.dispatcherType } returns DispatcherType.REQUEST
        every { request.remoteAddr } returns "127.0.0.1"
        every { request.session } returns null
        every { request.getSession(any()) } returns null
    }

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `유효한 Bearer 토큰이면 SecurityContext에 userId가 설정되고 체인이 실행된다`() {
        val userId = 123L
        val token = jwtUtil.generateToken(userId)

        every { request.getHeader("Authorization") } returns "Bearer $token"

        filter.doFilter(request, response, filterChain)

        val authentication = SecurityContextHolder.getContext().authentication
        assert(authentication?.principal == userId)
        verify(exactly = 1) { filterChain.doFilter(request, response) }
        verify(exactly = 0) { response.sendError(any(), any()) }
    }

    @Test
    fun `Authorization 헤더가 없으면 인증 없이 체인이 그대로 실행된다`() {
        every { request.getHeader("Authorization") } returns null

        filter.doFilter(request, response, filterChain)

        val authentication = SecurityContextHolder.getContext().authentication
        assert(authentication == null)
        verify(exactly = 1) { filterChain.doFilter(request, response) }
        verify(exactly = 0) { response.sendError(any(), any()) }
    }

    @Test
    fun `유효하지 않은 토큰이면 401을 반환하고 체인이 중단된다`() {
        every { request.getHeader("Authorization") } returns "Bearer invalid.token.value"

        filter.doFilter(request, response, filterChain)

        verify(exactly = 1) { response.sendError(HttpServletResponse.SC_UNAUTHORIZED, any()) }
        verify(exactly = 0) { filterChain.doFilter(request, response) }
        val authentication = SecurityContextHolder.getContext().authentication
        assert(authentication == null)
    }
}
