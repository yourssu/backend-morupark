package com.yourssu.morupark.auth.business

import com.yourssu.morupark.auth.application.dto.UserRegisterResponse
import com.yourssu.morupark.auth.business.command.TokenRefreshCommand
import com.yourssu.morupark.auth.business.command.UserRegisterCommand
import com.yourssu.morupark.auth.config.properties.JwtProperties
import com.yourssu.morupark.auth.implement.PlatformReader
import com.yourssu.morupark.auth.implement.RefreshTokenReader
import com.yourssu.morupark.auth.implement.RefreshTokenWriter
import com.yourssu.morupark.auth.implement.UserReader
import com.yourssu.morupark.auth.implement.UserWriter
import com.yourssu.morupark.auth.implement.domain.Platform
import com.yourssu.morupark.auth.implement.domain.RefreshToken
import com.yourssu.morupark.auth.implement.domain.User
import com.yourssu.morupark.auth.util.JwtUtil
import io.mockk.CapturingSlot
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant

class UserServiceTest {

    @MockK
    private lateinit var userReader: UserReader

    @MockK(relaxUnitFun = true)
    private lateinit var userWriter: UserWriter

    @MockK
    private lateinit var platformReader: PlatformReader

    @MockK(relaxUnitFun = true)
    private lateinit var refreshTokenWriter: RefreshTokenWriter

    @MockK
    private lateinit var refreshTokenReader: RefreshTokenReader

    private val jwtProperties = JwtProperties(
        secret = "a".repeat(64),
        accessTokenExpiration = 3_600_000,
        refreshTokenExpiration = 604_800_000,
    )

    private val jwtUtil = JwtUtil(jwtProperties)

    @InjectMockKs
    private lateinit var userService: UserService

    @BeforeEach
    fun init() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `회원 등록 시 AccessToken과 RefreshToken을 반환하고 RefreshToken이 저장된다`() {
        val platform = Platform(id = 1L, name = "p", redirectUrl = "r", secretKey = "s".repeat(64))
        val user = User(id = 10L, platformId = platform.id!!)
        val refreshSlot: CapturingSlot<RefreshToken> = slot()

        every { platformReader.getByName(any()) } returns platform
        every { userWriter.save(any()) } returns user
        every { refreshTokenWriter.save(capture(refreshSlot)) } answers { refreshSlot.captured }

        val response: UserRegisterResponse = userService.register(UserRegisterCommand(platformName = platform.name))

        assert(response.accessToken.isNotBlank())
        assert(response.refreshToken == refreshSlot.captured.token)
        assert(response.expiredIn == jwtProperties.accessTokenExpiration)
        assert(response.refreshExpiredIn == jwtProperties.refreshTokenExpiration)

        val expectedExpiryAfter = Instant.now().plusMillis(jwtProperties.refreshTokenExpiration)
        val actualExpiry = refreshSlot.captured.expiresAt
        assert(Duration.between(Instant.now(), actualExpiry).seconds in 0..jwtProperties.refreshTokenExpiration / 1000)
        verify(exactly = 1) { refreshTokenWriter.save(any()) }
    }

    @Test
    fun `RefreshToken으로 토큰을 재발급하면 AccessToken과 새로운 RefreshToken을 반환한다`() {
        val platform = Platform(id = 2L, name = "p2", redirectUrl = "r2", secretKey = "s".repeat(64))
        val existing = RefreshToken(
            id = 1L,
            userId = 20L,
            token = "old-refresh",
            expiresAt = Instant.now().plusSeconds(60),
            platformId = platform.id!!,
        )
        val refreshSlot: CapturingSlot<RefreshToken> = slot()

        every { refreshTokenReader.getByToken(existing.token) } returns existing
        every { platformReader.getById(platform.id!!) } returns platform
        every { refreshTokenWriter.deleteByToken(existing.token) } just runs
        every { refreshTokenWriter.save(capture(refreshSlot)) } answers { refreshSlot.captured }

        val response = userService.refresh(TokenRefreshCommand(refreshToken = existing.token))

        assert(response.accessToken.isNotBlank())
        assert(response.refreshToken == refreshSlot.captured.token)
        verify(exactly = 1) { refreshTokenWriter.deleteByToken(existing.token) }
        verify(exactly = 1) { refreshTokenWriter.save(any()) }
    }
}
