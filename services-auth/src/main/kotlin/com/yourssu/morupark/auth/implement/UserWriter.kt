package com.yourssu.morupark.auth.implement

import com.yourssu.morupark.auth.implement.domain.User
import org.springframework.stereotype.Component

@Component
class UserWriter(
    private val userRepository: UserRepository,
) {
    fun save(user: User): User {
        return userRepository.save(user)
    }
}
