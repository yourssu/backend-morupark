package com.yourssu.morupark.queue.implement

import com.yourssu.morupark.queue.business.UserInfo
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class AuthAdapter(
    webClient: WebClient.Builder
) {
    private val AUTH_SERVICE_URL = "http://localhost:8081"
    private val webClientAuth = webClient.baseUrl(AUTH_SERVICE_URL).build()

    fun getUserInfo(accessToken: String): UserInfo {
        return webClientAuth.get()
            .uri { uriBuilder ->
                uriBuilder.path("/auth/me")
                    .queryParam("accessToken", accessToken)
                    .build()
            }
            .retrieve()
            .bodyToMono(UserInfo::class.java)
            .block() ?: throw IllegalStateException("Failed to get user info")
    }

    fun getWaitingToken(accessToken: String): String {
        return webClientAuth.get()
            .uri { uriBuilder ->
                uriBuilder.path("/auth/waiting-token")
                    .queryParam("accessToken", accessToken)
                    .build()
            }
            .retrieve()
            .bodyToMono(String::class.java)
            .block() ?: ""
    }

    fun getExternalServerToken(waitingToken: String): String {
        return webClientAuth.get()
            .uri { uriBuilder ->
                uriBuilder.path("/platforms/url")
                    .queryParam("waitingToken", waitingToken)
                    .build()
            }
            .retrieve()
            .bodyToMono(String::class.java)
            .block() ?: ""
    }
}
