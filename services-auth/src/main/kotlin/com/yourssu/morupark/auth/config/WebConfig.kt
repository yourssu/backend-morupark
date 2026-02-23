package com.yourssu.morupark.auth.config

import com.yourssu.morupark.auth.config.resolver.ArgumentResolver
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.method.support.HandlerMethodArgumentResolver

@Configuration
class WebConfig(
    val argumentResolver: ArgumentResolver
) : WebMvcConfigurer {
    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(argumentResolver)
    }
}
