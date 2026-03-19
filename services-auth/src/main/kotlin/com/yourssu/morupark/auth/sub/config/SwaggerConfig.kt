package com.yourssu.morupark.auth.sub.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.servers.Server
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Value("\${app.server-url}")
    private lateinit var serverUrl: String

    @Bean
    fun openAPI(): OpenAPI = OpenAPI()
        .servers(listOf(Server().url(serverUrl)))
}
