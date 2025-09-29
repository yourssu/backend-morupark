package com.yourssu.morupark.auth.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf { csrf ->
            csrf.ignoringRequestMatchers("/external-servers/**")
        }
        .authorizeHttpRequests { requests ->
            requests
                .requestMatchers("/external-servers/**").permitAll()
                .anyRequest().authenticated()
        }

        return http.build()
    }
}