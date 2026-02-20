package com.example.outofroutebuddy.util

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import kotlin.time.Duration.Companion.milliseconds

/**
 * RateLimiter Tests
 * 
 * Unit tests for rate limiting with token bucket algorithm.
 * 
 * Priority: MEDIUM
 * Coverage Target: 90%
 * 
 * Created: December 2024
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RateLimiterTest {
    
    private lateinit var rateLimiter: RateLimiter
    
    @Before
    fun setup() {
        rateLimiter = RateLimiter(maxRequests = 5, timeWindowMs = 1000L)
    }
    
    // ==================== BASIC ACQUIRE TESTS ====================
    
    @Test
    fun `acquire should allow requests within limit`() = runTest {
        // Given: Rate limiter with limit of 5
        var allowedCount = 0
        
        // When: Acquire 5 times
        repeat(5) {
            if (rateLimiter.acquire()) {
                allowedCount++
            }
        }
        
        // Then: All should be allowed
        assertEquals(5, allowedCount)
    }
    
    @Test
    fun `acquire should block requests over limit`() = runTest {
        // Given: Rate limiter with limit of 5
        
        // When: Acquire 7 times
        var allowedCount = 0
        repeat(7) {
            if (rateLimiter.acquire()) {
                allowedCount++
            }
        }
        
        // Then: Only 5 should be allowed
        assertEquals(5, allowedCount)
    }
    
    @Test
    fun `acquire should block when at limit`() = runTest {
        // Given: Rate limiter with limit of 2
        val limiter = RateLimiter(maxRequests = 2, timeWindowMs = 1000L)
        
        // When: Acquire 2 times (at limit)
        assertTrue("First should be allowed", limiter.acquire())
        assertTrue("Second should be allowed", limiter.acquire())
        
        // Then: Should block third request
        assertFalse("Third should be blocked", limiter.acquire())
    }
    
    // ==================== TRY ACQUIRE TESTS ====================
    
    @Test
    fun `tryAcquire should allow with operation name`() = runTest {
        // Given: Fresh rate limiter
        
        // When: Try acquire
        val allowed = rateLimiter.tryAcquire("test operation")
        
        // Then: Should be allowed
        assertTrue("Should be allowed", allowed)
    }
    
    @Test
    fun `tryAcquire should block when at limit`() = runTest {
        // Given: Rate limiter at limit
        val limiter = RateLimiter(maxRequests = 1, timeWindowMs = 1000L)
        
        // When: Acquire then try acquire again
        assertTrue("First should be allowed", limiter.tryAcquire("first"))
        assertFalse("Second should be blocked", limiter.tryAcquire("second"))
    }
    
    // ==================== CURRENT USAGE TESTS ====================
    
    @Test
    fun `getCurrentUsage should return correct count`() = runTest {
        // Given: Rate limiter
        
        // When: Acquire some requests
        assertEquals(0, rateLimiter.getCurrentUsage())
        
        rateLimiter.acquire()
        assertEquals(1, rateLimiter.getCurrentUsage())
        
        rateLimiter.acquire()
        rateLimiter.acquire()
        assertEquals(3, rateLimiter.getCurrentUsage())
    }
    
    @Test
    fun `getCurrentUsage should increase with more requests`() = runTest {
        // Given: Rate limiter with window
        val limiter = RateLimiter(maxRequests = 5, timeWindowMs = 1000L)
        
        // When: Acquire requests
        limiter.acquire()
        limiter.acquire()
        
        // Then: Should return correct count
        assertEquals(2, limiter.getCurrentUsage())
        
        limiter.acquire()
        
        // Then: Should update count
        assertEquals(3, limiter.getCurrentUsage())
    }
    
    // ==================== SUCCESS RATE TESTS ====================
    
    @Test
    fun `getSuccessRate should return 100 percent when all allowed`() = runTest {
        // Given: Rate limiter with high limit
        
        // When: Acquire within limit
        repeat(5) {
            rateLimiter.acquire()
        }
        
        // Then: Should be 100%
        assertEquals(100.0, rateLimiter.getSuccessRate(), 0.1)
    }
    
    @Test
    fun `getSuccessRate should calculate partial blocking correctly`() = runTest {
        // Given: Rate limiter with limit of 2
        val limiter = RateLimiter(maxRequests = 2, timeWindowMs = 1000L)
        
        // When: Try to acquire 4 times
        repeat(4) {
            limiter.acquire()
        }
        
        // Then: Should be 50% (2 allowed out of 4)
        assertEquals(50.0, limiter.getSuccessRate(), 0.1)
    }
    
    @Test
    fun `getSuccessRate should handle zero requests`() = runTest {
        // Given: Fresh rate limiter
        
        // When: Get success rate without requests
        val rate = rateLimiter.getSuccessRate()
        
        // Then: Should be 100%
        assertEquals(100.0, rate, 0.1)
    }
    
    // ==================== STATISTICS TESTS ====================
    
    @Test
    fun `getStatistics should return correct stats`() = runTest {
        // Given: Rate limiter with limit of 3
        val limiter = RateLimiter(maxRequests = 3, timeWindowMs = 1000L)
        
        // When: Acquire some requests
        repeat(5) {
            limiter.acquire()
        }
        
        // When: Get statistics
        val stats = limiter.getStatistics()
        
        // Then: Should have correct stats
        assertEquals(5L, stats.totalRequests)
        assertEquals(3L, stats.totalAllowed)
        assertEquals(2L, stats.totalRateLimited)
        assertEquals(3, stats.maxRequests)
        assertEquals(1000L, stats.timeWindowMs)
    }
    
    @Test
    fun `getStatistics should track correct success rate`() = runTest {
        // Given: Rate limiter
        val limiter = RateLimiter(maxRequests = 2, timeWindowMs = 1000L)
        
        // When: Make requests
        repeat(4) {
            limiter.acquire()
        }
        
        // When: Get statistics
        val stats = limiter.getStatistics()
        
        // Then: Success rate should be 50%
        assertEquals(50.0, stats.successRate, 0.1)
    }
    
    // ==================== RESET TESTS ====================
    
    @Test
    fun `reset should clear all statistics`() = runTest {
        // Given: Rate limiter with some usage
        repeat(5) {
            rateLimiter.acquire()
        }
        assertTrue("Should have usage", rateLimiter.getCurrentUsage() > 0)
        
        // When: Reset
        rateLimiter.reset()
        
        // Then: Should be cleared
        assertEquals(0, rateLimiter.getCurrentUsage())
        assertEquals(0L, rateLimiter.getStatistics().totalRequests)
        assertEquals(100.0, rateLimiter.getSuccessRate(), 0.1)
    }
    
    @Test
    fun `reset should allow fresh requests`() = runTest {
        // Given: Rate limiter at limit
        val limiter = RateLimiter(maxRequests = 2, timeWindowMs = 1000L)
        
        // When: Fill up and reset
        repeat(2) { limiter.acquire() }
        assertFalse("Should be blocked", limiter.acquire())
        
        limiter.reset()
        
        // Then: Should allow new requests
        assertTrue("Should allow after reset", limiter.acquire())
        assertTrue("Should allow again", limiter.acquire())
    }
    
    // ==================== TIME UNTIL NEXT TOKEN TESTS ====================
    
    @Test
    fun `getTimeUntilNextToken should return zero when not at limit`() = runTest {
        // Given: Rate limiter below limit
        rateLimiter.acquire()
        rateLimiter.acquire()
        
        // When: Get time until next token
        val time = rateLimiter.getTimeUntilNextToken()
        
        // Then: Should be zero
        assertEquals(0L, time)
    }
    
    @Test
    fun `getTimeUntilNextToken should return positive when at limit`() = runTest {
        // Given: Rate limiter with short window at limit
        val limiter = RateLimiter(maxRequests = 2, timeWindowMs = 500L)
        
        // When: Fill up
        assertTrue(limiter.acquire())
        assertTrue(limiter.acquire())
        assertFalse(limiter.acquire())
        
        // When: Get time until next token
        val time = limiter.getTimeUntilNextToken()
        
        // Then: Should be positive and <= window
        assertTrue("Should be positive", time > 0)
        assertTrue("Should be <= window", time <= 500L)
    }
    
    
    // ==================== EDGE CASE TESTS ====================
    
    @Test
    fun `acquire should work with single request limit`() = runTest {
        // Given: Rate limiter with limit of 1
        val limiter = RateLimiter(maxRequests = 1, timeWindowMs = 1000L)
        
        // When: Acquire
        assertTrue("Should allow", limiter.acquire())
        assertFalse("Should block", limiter.acquire())
    }
    
    @Test
    fun `acquire should work with custom window`() = runTest {
        // Given: Rate limiter with custom window
        val limiter = RateLimiter(maxRequests = 2, timeWindowMs = 1000L)
        
        // When: Acquire up to limit
        assertTrue(limiter.acquire())
        assertTrue(limiter.acquire())
        
        // Then: Should block when at limit
        assertFalse(limiter.acquire())
    }
    
    @Test
    fun `statistics should track all requests`() = runTest {
        // Given: Rate limiter with limit
        val limiter = RateLimiter(maxRequests = 2, timeWindowMs = 1000L)
        
        // When: Make many requests (some will be rate limited)
        repeat(5) { limiter.acquire() }
        
        // When: Get statistics
        val stats = limiter.getStatistics()
        
        // Then: Should track all requests
        assertEquals(5L, stats.totalRequests)
        assertEquals(2L, stats.totalAllowed)
        assertEquals(3L, stats.totalRateLimited)
    }
    
    @Test
    fun `current usage should not exceed max requests`() = runTest {
        // Given: Rate limiter
        
        // When: Acquire beyond limit
        repeat(10) {
            rateLimiter.acquire()
        }
        
        // Then: Current usage should not exceed max
        val usage = rateLimiter.getCurrentUsage()
        assertTrue("Should not exceed max", usage <= 5)
    }
}

