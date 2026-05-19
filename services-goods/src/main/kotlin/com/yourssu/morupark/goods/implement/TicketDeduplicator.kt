package com.yourssu.morupark.goods.implement

import org.springframework.stereotype.Component

@Component
class TicketDeduplicator(
    private val processedTicketRepository: ProcessedTicketRepository,
) {
    /**
     * 토큰의 중복 처리 여부를 확인한다.
     * 처음 보는 토큰이면 처리 기록을 저장하고 false를 반환한다.
     * 이미 처리된 토큰이면 true를 반환한다.
     */
    fun isDuplicate(waitingToken: String): Boolean {
        if (processedTicketRepository.existsByToken(waitingToken)) return true
        processedTicketRepository.save(waitingToken)
        return false
    }
}
