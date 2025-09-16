package com.yourssu.morupark.auth.config.resolver

import com.yourssu.morupark.auth.annotation.UserId
import com.yourssu.morupark.auth.config.properties.JwtProperties
import com.yourssu.morupark.auth.util.JwtUtil
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

@Component
class ArgumentResolver(
    jwtProperties: JwtProperties
) : HandlerMethodArgumentResolver {
    private val jwtUtil = JwtUtil(jwtProperties.secret, jwtProperties.accessTokenExpiration)

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.parameterType == Long::class.java &&
                parameter.hasParameterAnnotation(UserId::class.java)
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        val token = webRequest.getHeaderValues("Authorization")
            ?.firstOrNull()
            ?.substringAfter("Bearer ")
            ?: throw IllegalArgumentException("No Authorization Header")
        return jwtUtil.getUserIdFromToken(token)
    }
}
