package com.yourssu.morupark.auth.implement

import com.yourssu.morupark.auth.implement.domain.User

interface UserRepository {
    fun save(user: User): User
}
