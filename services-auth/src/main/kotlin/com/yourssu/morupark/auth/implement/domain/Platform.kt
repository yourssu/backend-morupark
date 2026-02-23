package com.yourssu.morupark.auth.implement.domain

import java.util.*

class Platform(
    val id: Long? = null,
    val name: String,
    val redirectUrl: String,
    val secretKey: String,
) {
    companion object {
        fun create(name: String, redirectUrl: String): Platform {
            return Platform(
                name = name,
                redirectUrl = redirectUrl,
                secretKey = UUID.randomUUID().toString(),
            )
        }
    }
}
