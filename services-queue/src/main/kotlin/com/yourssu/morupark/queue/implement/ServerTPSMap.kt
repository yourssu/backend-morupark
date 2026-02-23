package com.yourssu.morupark.queue.implement

object ServerTPSMap {
    private val tpsMap = mutableMapOf<Long, Long>()

    fun put(platformId: Long, tps: Long) {
        tpsMap[platformId] = tps
    }

    fun get(platformId: Long): Long? {
        return tpsMap[platformId]
    }
}