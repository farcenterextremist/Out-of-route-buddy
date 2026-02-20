package com.example.outofroutebuddy.workers

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SyncWorkerRobolectricTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()
        try {
            WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
        } catch (e: IllegalStateException) {
            // Already initialized in this process; proceed
        }
    }

    @Test
    fun doWork_fullSync_returnsSuccess() = runTest {
        val worker = androidx.work.testing.TestListenableWorkerBuilder<SyncWorker>(context)
            .setInputData(androidx.work.workDataOf(SyncWorker.KEY_SYNC_TYPE to SyncWorker.SYNC_TYPE_FULL))
            .build()

        val result = worker.doWork()

        assertThat(result).isEqualTo(androidx.work.ListenableWorker.Result.success())
    }

    @Test
    fun schedulePeriodicSync_enqueuesUniquePeriodicWork_withSyncTag() {
        SyncWorker.schedulePeriodicSync(context)

        val workInfos = WorkManager.getInstance(context).getWorkInfosByTag("sync").get()
        assertThat(workInfos.isNotEmpty()).isTrue()
        assertThat(workInfos.any { it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING }).isTrue()
    }

    @Test
    fun cancelAllWork_clearsScheduledWorkByTags() {
        SyncWorker.schedulePeriodicSync(context)
        SyncWorker.scheduleCacheCleanup(context)
        SyncWorker.scheduleDataIntegrityCheck(context)

        // Sanity check: work exists
        assertThat(WorkManager.getInstance(context).getWorkInfosByTag("sync").get().isNotEmpty()).isTrue()
        assertThat(WorkManager.getInstance(context).getWorkInfosByTag("cache").get().isNotEmpty()).isTrue()
        assertThat(WorkManager.getInstance(context).getWorkInfosByTag("integrity").get().isNotEmpty()).isTrue()

        SyncWorker.cancelAllWork(context)

        val syncInfos = WorkManager.getInstance(context).getWorkInfosByTag("sync").get()
        val cacheInfos = WorkManager.getInstance(context).getWorkInfosByTag("cache").get()
        val integrityInfos = WorkManager.getInstance(context).getWorkInfosByTag("integrity").get()

        // Verify nothing remains enqueued or running for these tags
        assertThat(syncInfos.none { it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING }).isTrue()
        assertThat(cacheInfos.none { it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING }).isTrue()
        assertThat(integrityInfos.none { it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING }).isTrue()
    }
}


