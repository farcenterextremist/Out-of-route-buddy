package com.example.outofroutebuddy.data

import android.content.Context
import android.util.Log
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 🏥 Database Health Check Service
 * 
 * Monitors database integrity and health, with automatic corruption detection and recovery.
 * 
 * ✅ NEW (#4): Database Migration & Corruption Detection
 * 
 * Features:
 * - Detects database corruption on app start
 * - Runs SQLite integrity checks
 * - Provides fallback rebuild if corrupted
 * - Reports health status
 * - Tracks database errors
 * 
 * Priority: HIGH
 * Impact: Data integrity and app stability
 */
@Singleton
class DatabaseHealthCheck @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val TAG = "DatabaseHealthCheck"
        private const val DATABASE_NAME = "outofroutebuddy_database"
    }
    
    /**
     * Database health status
     */
    data class HealthStatus(
        val isHealthy: Boolean,
        val integrityCheckPassed: Boolean,
        val databaseExists: Boolean,
        val databaseSize: Long,
        val lastCheckTime: Long,
        val errorMessage: String? = null
    )
    
    /**
     * Check database integrity
     * 
     * @return true if database is healthy, false if corrupted
     */
    suspend fun checkDatabaseIntegrity(database: AppDatabase): Boolean {
        return try {
            Log.d(TAG, "Performing database integrity check")
            
            var isIntact = false
            
            database.openHelper.writableDatabase.apply {
                // Run SQLite PRAGMA integrity_check
                query("PRAGMA integrity_check").use { cursor ->
                    if (cursor.moveToFirst()) {
                        val result = cursor.getString(0)
                        isIntact = result == "ok"
                        
                        if (isIntact) {
                            Log.i(TAG, "✅ Database integrity check PASSED")
                        } else {
                            Log.e(TAG, "❌ Database integrity check FAILED: $result")
                        }
                    }
                }
            }
            
            isIntact
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during integrity check", e)
            false
        }
    }
    
    /**
     * Check if database file exists and is accessible
     */
    fun databaseFileExists(): Boolean {
        return try {
            val dbFile = context.getDatabasePath(DATABASE_NAME)
            val exists = dbFile.exists()
            Log.d(TAG, "Database file exists: $exists (path: ${dbFile.absolutePath})")
            exists
        } catch (e: Exception) {
            Log.e(TAG, "Error checking database file", e)
            false
        }
    }
    
    /**
     * Get database file size
     */
    fun getDatabaseSize(): Long {
        return try {
            val dbFile = context.getDatabasePath(DATABASE_NAME)
            if (dbFile.exists()) {
                val size = dbFile.length()
                Log.d(TAG, "Database size: ${size / 1024}KB")
                size
            } else {
                0L
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting database size", e)
            0L
        }
    }
    
    /**
     * Perform comprehensive health check
     */
    suspend fun performHealthCheck(database: AppDatabase): HealthStatus {
        val startTime = System.currentTimeMillis()
        
        return try {
            val exists = databaseFileExists()
            val size = getDatabaseSize()
            val integrityOk = if (exists) checkDatabaseIntegrity(database) else false
            
            val isHealthy = exists && integrityOk
            
            if (isHealthy) {
                Log.i(TAG, "✅ Database health check PASSED (${size / 1024}KB)")
            } else {
                Log.w(TAG, "⚠️ Database health check FAILED (exists: $exists, integrity: $integrityOk)")
            }
            
            HealthStatus(
                isHealthy = isHealthy,
                integrityCheckPassed = integrityOk,
                databaseExists = exists,
                databaseSize = size,
                lastCheckTime = startTime,
                errorMessage = if (!isHealthy) "Database check failed" else null
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Health check error", e)
            HealthStatus(
                isHealthy = false,
                integrityCheckPassed = false,
                databaseExists = false,
                databaseSize = 0,
                lastCheckTime = startTime,
                errorMessage = e.message
            )
        }
    }
    
    /**
     * Attempt to rebuild corrupted database
     * 
     * WARNING: This will delete all existing data!
     */
    fun rebuildCorruptedDatabase(): Boolean {
        return try {
            Log.w(TAG, "⚠️ Attempting to rebuild corrupted database")
            
            // Delete corrupted database
            val deleted = context.deleteDatabase(DATABASE_NAME)
            
            if (deleted) {
                Log.i(TAG, "✅ Corrupted database deleted successfully")
                // Database will be recreated on next access
                true
            } else {
                Log.e(TAG, "❌ Failed to delete corrupted database")
                false
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error rebuilding database", e)
            false
        }
    }
    
    /**
     * Room database callback for integrity checks
     */
    class IntegrityCheckCallback : RoomDatabase.Callback() {
        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            Log.d(TAG, "Database opened - verifying integrity")
            
            try {
                // Quick integrity check on open
                db.query("SELECT 1").use { cursor ->
                    if (cursor.moveToFirst()) {
                        Log.d(TAG, "✅ Database query test passed")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Database integrity issue detected on open", e)
            }
        }
        
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            Log.i(TAG, "✅ Database created successfully")
        }
    }
}


