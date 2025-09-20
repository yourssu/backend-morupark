package com.yourssu.morupark.queue.implement

import com.yourssu.morupark.queue.business.TicketStatus

data class UserInfo(
    val accessToken: String,
    val status: TicketStatus,
){
    @Override
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as UserInfo
        return accessToken == other.accessToken
    }

    override fun hashCode(): Int {
        return accessToken.hashCode()
    }
}
