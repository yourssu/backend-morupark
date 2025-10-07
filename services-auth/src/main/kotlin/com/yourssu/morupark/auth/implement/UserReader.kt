package com.yourssu.morupark.auth.implement

import com.yourssu.morupark.auth.implement.domain.User
import org.springframework.stereotype.Component

@Component
class UserReader(
    private val userRepository: UserRepository,
) {
    fun getById(userId: Long): User {
        return userRepository.getById(userId)
    }
}
