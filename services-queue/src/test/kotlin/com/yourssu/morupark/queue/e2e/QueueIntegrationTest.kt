package com.yourssu.morupark.queue.e2e

import com.ninjasquad.springmockk.MockkBean
import com.yourssu.morupark.queue.implement.AuthAdapter
import io.mockk.every
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.kafka.KafkaContainer
import org.testcontainers.utility.DockerImageName

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class QueueE2ETest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var authAdapter: AuthAdapter

    private val logger = LoggerFactory.getLogger(this::class.java)

    companion object {
        @Container
        @ServiceConnection
        val kafkaContainer = KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.6.1")
                .asCompatibleSubstituteFor("apache/kafka")
        )

        @Container
        @ServiceConnection(name = "redis")
        val redisContainer = GenericContainer(DockerImageName.parse("redis:latest"))
            .withExposedPorts(6379)
    }

    @BeforeEach
    fun setup() {
        // 1. 토큰 검증은 무조건 통과
        every { authAdapter.isTokenValid(any()) } returns true
        // 2. 인증 서버에서 대기 토큰 발급해주는 척 (UUID 반환)
        every { authAdapter.getWaitingToken(any()) } returns "mock-waiting-token-12345"
    }

    @Test
    fun `100명이_줄을_서고_마지막_사람이_자신의_순번을_확인한다`() {
        val totalUsers = 100
        val lastUserToken = "user-token-$totalUsers"

        logger.info("=== [Step 1] 100명 대기열 등록 요청 (POST /queues) ===")

        for (i in 1..totalUsers) {
            mockMvc.perform(
                post("/queues")
                    .header("Authorization", "user-token-$i")
            ).andExpect(status().isAccepted) // 202 OK 확인
        }

        logger.info("=== [Step 2] 비동기 처리 대기 (Kafka -> Redis) 5초 ===")
        Thread.sleep(5000)

        logger.info("=== [Step 3] 마지막 유저의 대기 토큰 발급 요청 (GET /queues) ===")

        // API 2: 대기 토큰 조회
        val result = mockMvc.perform(
            get("/queues")
                .header("Authorization", lastUserToken)
        )
            .andDo(print()) // 로그에 찍기
            .andExpect(status().isOk)
            .andReturn()

        val waitingToken = result.response.contentAsString
        logger.info("발급받은 대기 토큰: $waitingToken")


        logger.info("=== [Step 4] 마지막 유저의 대기 상태 조회 (GET /queues/status) ===")

        // API 3: 내 순번(Rank) 확인
        mockMvc.perform(
            get("/queues/status")
                .header("Authorization", lastUserToken)
                .header("X-Waiting-Token", waitingToken)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("WAITING")) // 아직 대기중이어야 함
            .andExpect(jsonPath("$.rank").exists()) // 랭크 값이 있어야 함
            .andExpect(jsonPath("$.estimatedWaitingTime").exists()) // 예상 시간도 있어야 함

        logger.info("테스트 성공: 3개의 API가 모두 유기적으로 동작했습니다.")
    }
}