package com.yourssu.morupark.auth.config

import com.yourssu.morupark.auth.config.filter.JwtAuthenticationFilter
import com.yourssu.morupark.auth.config.properties.JwtProperties
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun filterChain(
        http: HttpSecurity,
        jwtProperties: JwtProperties,
    ): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { requests ->
                requests
                    .requestMatchers("/external-servers/**", "/auth/token/**").permitAll()
                    .anyRequest().authenticated()
            }
            .exceptionHandling { exceptions ->
                exceptions
                    .authenticationEntryPoint { _, response, _ ->
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
                    }
                    .accessDeniedHandler { _, response, _ ->
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden")
                    }
            }
            .addFilterBefore(
                JwtAuthenticationFilter(jwtProperties),
                UsernamePasswordAuthenticationFilter::class.java,
            )

        return http.build()
    }
}
