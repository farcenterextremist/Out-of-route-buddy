package com.example.outofroutebuddy.util

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import kotlin.time.Duration.Companion.milliseconds

/**
 * RetryPolicy Tests
 * 
 * Unit tests for exponential backoff retry logic.
 * 
 * Priority: MEDIUM
 * Coverage Target: 90%
 * 
 * Created: December 2024
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RetryPolicyTest {
    
    private lateinit var retryPolicy: RetryPolicy
    
    @Before
    fun setup() {
        retryPolicy = RetryPolicy()
    }
    
    // ==================== SUCCESS TESTS ====================
    
    @Test
    fun `execute should succeed on first attempt`() = runTest {
        // Given: Operation that succeeds immediately
        var attemptCount = 0
        
        // When: Execute with retry
        val result = retryPolicy.execute("test") {
            attemptCount++
            "success"
        }
        
        // Then: Should succeed without retries
        assertTrue("Should be success", result.isSuccess)
        assertEquals("success", result.getOrNull())
        assertEquals(1, attemptCount)
    }
    
    @Test
    fun `execute should succeed after retry`() = runTest {
        // Given: Operation that fails then succeeds
        var attemptCount = 0
        
        // When: Execute with retry
        val result = retryPolicy.execute("test") {
            attemptCount++
            if (attemptCount < 2) {
                throw RuntimeException("Fail on first attempt")
            }
            "success"
        }
        
        // Then: Should succeed after retry
        assertTrue("Should be success", result.isSuccess)
        assertEquals("success", result.getOrNull())
        assertEquals(2, attemptCount)
    }
    
    @Test
    fun `execute should eventually succeed after multiple retries`() = runTest {
        // Given: Operation that succeeds on third attempt
        var attemptCount = 0
        
        // When: Execute with retry
        val result = retryPolicy.execute("test") {
            attemptCount++
            if (attemptCount < 3) {
                throw RuntimeException("Fail")
            }
            "success"
        }
        
        // Then: Should succeed
        assertTrue("Should be success", result.isSuccess)
        assertEquals("success", result.getOrNull())
        assertEquals(3, attemptCount)
    }
    
    // ==================== FAILURE TESTS ====================
    
    @Test
    fun `execute should fail after max attempts`() = runTest {
        // Given: Operation that always fails
        var attemptCount = 0
        
        // When: Execute with retry
        val result = retryPolicy.execute("test") {
            attemptCount++
            throw RuntimeException("Always fail")
        }
        
        // Then: Should fail after max attempts
        assertTrue("Should be failure", result.isFailure)
        assertEquals(5, attemptCount) // Default max attempts
    }
    
    @Test
    fun `execute should respect max attempts limit`() = runTest {
        // Given: Custom retry policy with fewer attempts
        val customPolicy = RetryPolicy(maxAttempts = 3)
        var attemptCount = 0
        
        // When: Execute with retry
        val result = customPolicy.execute("test") {
            attemptCount++
            throw RuntimeException("Always fail")
        }
        
        // Then: Should fail after 3 attempts
        assertTrue("Should be failure", result.isFailure)
        assertEquals(3, attemptCount)
    }
    
    // ==================== EXCEPTION HANDLING TESTS ====================
    
    @Test
    fun `execute should preserve exception on failure`() = runTest {
        // Given: Specific exception
        val expectedException = IllegalStateException("Test exception")
        
        // When: Execute with retry
        val result = retryPolicy.execute("test") {
            throw expectedException
        }
        
        // Then: Should preserve exception
        assertTrue("Should be failure", result.isFailure)
        assertTrue("Should contain expected exception", result.exceptionOrNull() is IllegalStateException)
    }
    
    // ==================== BACKOFF TESTS ====================
    
    @Test
    fun `getBackoffSequence should return correct exponential delays`() {
        // Given: Standard retry policy
        val policy = RetryPolicy(baseDelayMs = 1000L)
        
        // When: Get backoff sequence
        val sequence = policy.getBackoffSequence()
        
        // Then: Should be exponential (2^attempt * baseDelay)
        assertEquals(1000L, sequence[0]) // 1s
        assertEquals(2000L, sequence[1]) // 2s
        assertEquals(4000L, sequence[2]) // 4s
        assertEquals(8000L, sequence[3]) // 8s
        assertEquals(16000L, sequence[4]) // 16s
    }
    
    @Test
    fun `getBackoffSequence should cap at max delay`() {
        // Given: Policy with max delay
        val policy = RetryPolicy(baseDelayMs = 1000L, maxDelayMs = 5000L)
        
        // When: Get backoff sequence
        val sequence = policy.getBackoffSequence()
        
        // Then: Should not exceed max delay
        assertTrue("All delays should be <= max", sequence.all { it <= 5000L })
        assertTrue("Should include max delay", sequence.contains(5000L))
    }
    
    // ==================== CONFIGURATION TESTS ====================
    
    @Test
    fun `getConfig should return current configuration`() {
        // Given: Custom retry policy
        val policy = RetryPolicy(
            maxAttempts = 3,
            baseDelayMs = 500L,
            maxDelayMs = 2000L,
            jitterPercentage = 5
        )
        
        // When: Get config
        val config = policy.getConfig()
        
        // Then: Should match configuration
        assertEquals(3, config.maxAttempts)
        assertEquals(500L, config.baseDelayMs)
        assertEquals(2000L, config.maxDelayMs)
        assertEquals(5, config.jitterPercentage)
    }
    
    @Test
    fun `default network retry should have correct config`() {
        // Given: Default network retry policy
        val config = RetryPolicy.NETWORK_RETRY.getConfig()
        
        // Then: Should have network-appropriate settings
        assertEquals(3, config.maxAttempts)
        assertEquals(2000L, config.baseDelayMs)
        assertEquals(30000L, config.maxDelayMs)
    }
    
    @Test
    fun `default GPS retry should have correct config`() {
        // Given: Default GPS retry policy
        val config = RetryPolicy.GPS_RETRY.getConfig()
        
        // Then: Should have GPS-appropriate settings
        assertEquals(5, config.maxAttempts)
        assertEquals(1000L, config.baseDelayMs)
        assertEquals(60000L, config.maxDelayMs)
    }
    
    @Test
    fun `default database retry should have correct config`() {
        // Given: Default database retry policy
        val config = RetryPolicy.DATABASE_RETRY.getConfig()
        
        // Then: Should have database-appropriate settings
        assertEquals(3, config.maxAttempts)
        assertEquals(500L, config.baseDelayMs)
        assertEquals(5000L, config.maxDelayMs)
    }
    
    // ==================== EXECUTE WITH RETRY TESTS ====================
    
    @Test
    fun `executeWithRetry should pass attempt number to operation`() = runTest {
        // Given: Operation that checks attempt number
        val capturedAttempts = mutableListOf<Int>()
        
        // When: Execute with retry
        val result = retryPolicy.executeWithRetry("test") { attempt ->
            capturedAttempts.add(attempt)
            if (attempt < 2) {
                throw RuntimeException("Fail")
            }
            "success"
        }
        
        // Then: Should pass correct attempt numbers
        assertTrue("Should succeed", result.isSuccess)
        assertEquals(listOf(0, 1, 2), capturedAttempts)
    }
    
    // ==================== EDGE CASE TESTS ====================
    
    @Test
    fun `execute should handle single attempt policy`() = runTest {
        // Given: Single attempt policy
        val singleAttemptPolicy = RetryPolicy(maxAttempts = 1)
        var attemptCount = 0
        
        // When: Execute with failing operation
        val result = singleAttemptPolicy.execute("test") {
            attemptCount++
            throw RuntimeException("Fail")
        }
        
        // Then: Should fail immediately
        assertTrue("Should be failure", result.isFailure)
        assertEquals(1, attemptCount)
    }
    
    @Test
    fun `execute should handle null return value`() = runTest {
        // Given: Operation returning null
        val result = retryPolicy.execute("test") {
            null
        }
        
        // Then: Should succeed with null
        assertTrue("Should be success", result.isSuccess)
        assertNull("Should be null", result.getOrNull())
    }
    
    @Test
    fun `execute should handle operations returning numbers`() = runTest {
        // Given: Operation returning number
        var attemptCount = 0
        val result = retryPolicy.execute("test") {
            attemptCount++
            if (attemptCount < 2) {
                throw RuntimeException("Fail")
            }
            42
        }
        
        // Then: Should succeed with number
        assertTrue("Should be success", result.isSuccess)
        assertEquals(42, result.getOrNull())
    }
    
    @Test
    fun `execute should handle operations returning lists`() = runTest {
        // Given: Operation returning list
        val result = retryPolicy.execute("test") {
            listOf(1, 2, 3)
        }
        
        // Then: Should succeed with list
        assertTrue("Should be success", result.isSuccess)
        assertEquals(listOf(1, 2, 3), result.getOrNull())
    }
    
    // ==================== PERFORMANCE TESTS ====================
    
    @Test
    fun `execute should complete quickly on first success`() = runTest {
        // Given: Quick operation
        val startTime = System.currentTimeMillis()
        
        // When: Execute
        val result = retryPolicy.execute("test") {
            "success"
        }
        
        // Then: Should complete quickly (< 100ms)
        val duration = System.currentTimeMillis() - startTime
        assertTrue("Should complete quickly: ${duration}ms", duration < 100)
        assertTrue("Should be success", result.isSuccess)
    }
}





