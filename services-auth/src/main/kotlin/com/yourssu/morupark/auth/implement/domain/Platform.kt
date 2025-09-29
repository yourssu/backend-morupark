package com.yourssu.morupark.auth.implement.domain

import java.util.*

class Platform(
    val id: Long? = null,
    val name: String,
    val redirectUri: String,
    val secretKey: String,
) {
    companion object {
        fun create(name: String, redirectUri: String): Platform {
            return Platform(
                name = name,
                redirectUri = redirectUri,
                secretKey = UUID.randomUUID().toString(),
            )
        }
    }
}
