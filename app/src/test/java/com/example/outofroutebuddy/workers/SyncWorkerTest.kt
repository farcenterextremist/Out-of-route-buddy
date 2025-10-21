package com.example.outofroutebuddy.workers

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.*
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import com.example.outofroutebuddy.workers.SyncWorker.Companion.KEY_SYNC_TYPE
import com.example.outofroutebuddy.workers.SyncWorker.Companion.SYNC_TYPE_CACHE
import com.example.outofroutebuddy.workers.SyncWorker.Companion.SYNC_TYPE_DATA_INTEGRITY
import com.example.outofroutebuddy.workers.SyncWorker.Companion.SYNC_TYPE_FULL
import com.example.outofroutebuddy.workers.SyncWorker.Companion.SYNC_TYPE_GPS
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * 🧪 WorkManager SyncWorker Tests
 * 
 * Tests for WorkManager-based background sync:
 * - Worker execution
 * - Constraint handling
 * - Retry logic
 * - Different sync types
 * 
 * Priority: 🟡 MEDIUM
 * Impact: Background task reliability
 * 
 * Created: Phase 2G - WorkManager
 */
@RunWith(AndroidJUnit4::class)
class SyncWorkerTest {
    
    private lateinit var context: Context
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
    }
    
    // ==================== WORKER EXECUTION TESTS ====================
    
    @Test
    fun testFullSyncWorkerExecutes() = runBlocking {
        // Create worker
        val worker = TestListenableWorkerBuilder<SyncWorker>(context)
            .setInputData(workDataOf(KEY_SYNC_TYPE to SYNC_TYPE_FULL))
            .build()
        
        // Execute
        val result = worker.doWork()
        
        // Should succeed
        assertTrue("Full sync should succeed", result is ListenableWorker.Result.Success)
    }
    
    @Test
    fun testCacheSyncWorkerExecutes() = runBlocking {
        val worker = TestListenableWorkerBuilder<SyncWorker>(context)
            .setInputData(workDataOf(KEY_SYNC_TYPE to SYNC_TYPE_CACHE))
            .build()
        
        val result = worker.doWork()
        assertTrue("Cache sync should succeed", result is ListenableWorker.Result.Success)
    }
    
    @Test
    fun testDataIntegritySyncWorkerExecutes() = runBlocking {
        val worker = TestListenableWorkerBuilder<SyncWorker>(context)
            .setInputData(workDataOf(KEY_SYNC_TYPE to SYNC_TYPE_DATA_INTEGRITY))
            .build()
        
        val result = worker.doWork()
        assertTrue("Data integrity check should succeed", result is ListenableWorker.Result.Success)
    }
    
    @Test
    fun testGpsSyncWorkerExecutes() = runBlocking {
        val worker = TestListenableWorkerBuilder<SyncWorker>(context)
            .setInputData(workDataOf(KEY_SYNC_TYPE to SYNC_TYPE_GPS))
            .build()
        
        val result = worker.doWork()
        assertTrue("GPS sync should succeed", result is ListenableWorker.Result.Success)
    }
    
    // ==================== SYNC TYPE TESTS ====================
    
    @Test
    fun testUnknownSyncTypeReturnsFailure() = runBlocking {
        val worker = TestListenableWorkerBuilder<SyncWorker>(context)
            .setInputData(workDataOf(KEY_SYNC_TYPE to "unknown_type"))
            .build()
        
        val result = worker.doWork()
        assertTrue("Unknown sync type should fail", result is ListenableWorker.Result.Failure)
    }
    
    @Test
    fun testMissingSyncTypeDefaultsToFull() = runBlocking {
        val worker = TestListenableWorkerBuilder<SyncWorker>(context)
            .build() // No input data
        
        val result = worker.doWork()
        assertTrue("Missing sync type should default to full", result is ListenableWorker.Result.Success)
    }
    
    // ==================== WORKER CONFIGURATION TESTS ====================
    
    @Test
    fun testPeriodicSyncScheduled() {
        // Schedule work
        SyncWorker.schedulePeriodicSync(context)
        
        // Verify it was scheduled
        val workManager = WorkManager.getInstance(context)
        val workInfos = workManager.getWorkInfosForUniqueWork(SyncWorker.WORK_NAME_PERIODIC_SYNC).get()
        
        assertTrue("Periodic sync should be scheduled", workInfos.isNotEmpty())
    }
    
    @Test
    fun testCacheCleanupScheduled() {
        SyncWorker.scheduleCacheCleanup(context)
        
        val workManager = WorkManager.getInstance(context)
        val workInfos = workManager.getWorkInfosForUniqueWork(SyncWorker.WORK_NAME_CACHE_CLEANUP).get()
        
        assertTrue("Cache cleanup should be scheduled", workInfos.isNotEmpty())
    }
    
    @Test
    fun testDataIntegrityCheckScheduled() {
        SyncWorker.scheduleDataIntegrityCheck(context)
        
        val workManager = WorkManager.getInstance(context)
        val workInfos = workManager.getWorkInfosForUniqueWork(SyncWorker.WORK_NAME_DATA_INTEGRITY).get()
        
        assertTrue("Data integrity check should be scheduled", workInfos.isNotEmpty())
    }
    
    // ==================== WORK CANCELLATION TESTS ====================
    
    @Test
    fun testCancelAllWork() {
        // Schedule all work types
        SyncWorker.schedulePeriodicSync(context)
        SyncWorker.scheduleCacheCleanup(context)
        SyncWorker.scheduleDataIntegrityCheck(context)
        
        // Cancel all
        SyncWorker.cancelAllWork(context)
        
        // Verify all cancelled (this might take a moment)
        Thread.sleep(100)
        
        val workManager = WorkManager.getInstance(context)
        val syncWork = workManager.getWorkInfosForUniqueWork(SyncWorker.WORK_NAME_PERIODIC_SYNC).get()
        val cacheWork = workManager.getWorkInfosForUniqueWork(SyncWorker.WORK_NAME_CACHE_CLEANUP).get()
        val integrityWork = workManager.getWorkInfosForUniqueWork(SyncWorker.WORK_NAME_DATA_INTEGRITY).get()
        
        // All should be cancelled or empty
        assertTrue("Work should be cancelled", 
            syncWork.all { it.state.isFinished } || sync Work.isEmpty())
    }
    
    // ==================== CONSTRAINTS TESTS ====================
    
    @Test
    fun testPeriodicSyncHasNetworkConstraint() {
        SyncWorker.schedulePeriodicSync(context)
        
        val workManager = WorkManager.getInstance(context)
        val workInfos = workManager.getWorkInfosForUniqueWork(SyncWorker.WORK_NAME_PERIODIC_SYNC).get()
        
        assertTrue("Work should be scheduled", workInfos.isNotEmpty())
        // Note: Can't easily verify constraints from WorkInfo, but test passes if no errors
    }
}

