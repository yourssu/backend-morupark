package com.yourssu.morupark.queue.implement

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class AuthApiAdapter(
    // WebClient.Builder를 주입받아 설정
    webClientBuilder: WebClient.Builder
) {
    // 호출할 서비스의 기본 URL을 설정하여 WebClient 인스턴스 생성
    private val webClient = webClientBuilder.baseUrl("http://localhost:8081").build() // service-auth의 주소

    /**
     * auth 서비스에 유저의 유효성을 질의합니다.
     * @param accessToken 검증할 유저 ID
     * @return 유효하면 true, 아니면 false
     */
    fun isTokenValid(accessToken: String): Boolean {
        return webClient.get()
            .uri { uriBuilder ->
                uriBuilder.path("/api/internal/auth/validate")
                    .queryParam("accessToken", accessToken)
                    .build()
            }
            .retrieve() // 응답을 받아옴
            .bodyToMono(Boolean::class.java) // 응답 body를 Boolean 타입으로 변환 (Mono는 0-1개의 결과를 담는 비동기 객체)
            .block() ?: false // 동기식으로 결과를 기다림. 실제 운영 코드에서는 block() 사용을 최소화해야 함.
    }
}