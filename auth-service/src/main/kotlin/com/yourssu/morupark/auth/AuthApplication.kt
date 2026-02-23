package com.yourssu.morupark.auth

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class AuthApplication

fun main(args: Array<String>) {
    runApplication<AuthApplication>(*args)
}