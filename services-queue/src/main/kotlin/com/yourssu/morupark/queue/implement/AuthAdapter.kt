package com.yourssu.morupark.queue.implement

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class AuthAdapter(
    private val webClient : WebClient.Builder
) {
    private val AUTH_SERVICE_URL = "http://localhost:8081"
    private val webClientAuth = webClient.baseUrl(AUTH_SERVICE_URL).build()

    fun isTokenValid(accessToken: String) : Boolean {
        return webClientAuth.get()
            .uri { uriBuilder ->
                uriBuilder.path("/auth/token")
                    .queryParam("accessToken", accessToken)
                    .build()
            }
            .retrieve()
            .bodyToMono(Boolean::class.java)
            .block() == true
    }

    fun getWaitingToken(accessToken: String): String {
        return webClientAuth.get()
            .uri { uriBuilder ->
                // TODO: 레오에게 이거 엔드포인트 뭔지 물어보기
                uriBuilder.path("/auth/token")
                    .queryParam("accessToken", accessToken)
                    .build()
            }
            .retrieve()
            .bodyToMono(String::class.java)
            .block() ?: ""
    }
}
