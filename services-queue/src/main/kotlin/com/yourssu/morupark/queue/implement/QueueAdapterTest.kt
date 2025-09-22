package com.yourssu.morupark.queue.implement

import org.junit.jupiter.api.Assertions.*

@ExtendWith(MockKExtension::class)
class QueueAdapterTest {

    @MockK
    private val redisTemplate: RedisTemplate<String, String>

    @MockK
    private val zSetOps: ZSetOperations<String, String>

    @MockK
    private val setOps: SetOperations<String, String>

    @InjectMockKs
    private val queueAdapter: QueueAdapter

}