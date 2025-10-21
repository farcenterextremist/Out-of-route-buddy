package com.example.outofroutebuddy.util

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * 📝 Log Rotation Manager
 * 
 * Manages log file rotation to prevent disk space issues.
 * 
 * ✅ NEW (#16): Log Rotation
 * 
 * Features:
 * - Rotates logs daily
 * - Keeps max 7 days of logs
 * - Limits total size to 10MB
 * - Compresses old logs (optional)
 * 
 * Priority: LOW (but useful)
 * Impact: Disk space management, debugging
 */
class LogRotationManager(private val context: Context) {
    
    companion object {
        private const val TAG = "LogRotationManager"
        private const val MAX_LOG_FILES = 7
        private const val MAX_TOTAL_SIZE_MB = 10
        private const val LOG_DIR_NAME = "logs"
    }
    
    private val logDir: File by lazy {
        File(context.filesDir, LOG_DIR_NAME).apply {
            if (!exists()) {
                mkdirs()
                Log.d(TAG, "Created log directory: $absolutePath")
            }
        }
    }
    
    /**
     * Rotate logs if needed
     * 
     * Call this on app startup or periodically
     */
    fun rotateLogsIfNeeded() {
        try {
            Log.d(TAG, "Checking if log rotation needed")
            
            val logFiles = getLogFiles()
            
            if (logFiles.isEmpty()) {
                Log.d(TAG, "No log files to rotate")
                return
            }
            
            // Delete old files (keep last MAX_LOG_FILES)
            deleteOldLogFiles(logFiles)
            
            // Check total size and delete if over limit
            enforceSizeLimit(logFiles)
            
            Log.d(TAG, "Log rotation completed")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during log rotation", e)
        }
    }
    
    /**
     * Get all log files sorted by modification time (newest first)
     */
    private fun getLogFiles(): List<File> {
        return try {
            logDir.listFiles()
                ?.filter { it.isFile && it.name.endsWith(".log") }
                ?.sortedByDescending { it.lastModified() }
                ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting log files", e)
            emptyList()
        }
    }
    
    /**
     * Delete old log files (keep only MAX_LOG_FILES newest)
     */
    private fun deleteOldLogFiles(logFiles: List<File>) {
        if (logFiles.size <= MAX_LOG_FILES) {
            Log.d(TAG, "Log file count (${logFiles.size}) within limit ($MAX_LOG_FILES)")
            return
        }
        
        val filesToDelete = logFiles.drop(MAX_LOG_FILES)
        
        Log.i(TAG, "Deleting ${filesToDelete.size} old log files")
        
        filesToDelete.forEach { file ->
            try {
                val deleted = file.delete()
                if (deleted) {
                    Log.d(TAG, "Deleted old log: ${file.name}")
                } else {
                    Log.w(TAG, "Failed to delete log: ${file.name}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting file: ${file.name}", e)
            }
        }
    }
    
    /**
     * Enforce total size limit
     */
    private fun enforceSizeLimit(logFiles: List<File>) {
        val totalSizeBytes = logFiles.sumOf { it.length() }
        val totalSizeMb = totalSizeBytes / (1024 * 1024)
        
        Log.d(TAG, "Total log size: ${totalSizeMb}MB (limit: ${MAX_TOTAL_SIZE_MB}MB)")
        
        if (totalSizeMb <= MAX_TOTAL_SIZE_MB) {
            return
        }
        
        Log.w(TAG, "Log size (${totalSizeMb}MB) exceeds limit (${MAX_TOTAL_SIZE_MB}MB) - deleting oldest files")
        
        var currentSizeMb = totalSizeMb
        val sortedFiles = logFiles.sortedBy { it.lastModified() } // Oldest first
        
        for (file in sortedFiles) {
            if (currentSizeMb <= MAX_TOTAL_SIZE_MB) {
                break
            }
            
            try {
                val fileSizeMb = file.length() / (1024 * 1024)
                val deleted = file.delete()
                
                if (deleted) {
                    currentSizeMb -= fileSizeMb
                    Log.d(TAG, "Deleted log to reduce size: ${file.name} (${fileSizeMb}MB)")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting file: ${file.name}", e)
            }
        }
    }
    
    /**
     * Create a new log file for today
     */
    fun createLogFile(): File {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val filename = "app_log_${dateFormat.format(Date())}.log"
        return File(logDir, filename)
    }
    
    /**
     * Get current log file (creates if doesn't exist)
     */
    fun getCurrentLogFile(): File {
        val todayLog = createLogFile()
        
        if (!todayLog.exists()) {
            try {
                todayLog.createNewFile()
                Log.d(TAG, "Created new log file: ${todayLog.name}")
            } catch (e: Exception) {
                Log.e(TAG, "Error creating log file", e)
            }
        }
        
        return todayLog
    }
    
    /**
     * Get log statistics
     */
    fun getLogStatistics(): LogStatistics {
        return try {
            val files = getLogFiles()
            val totalSize = files.sumOf { it.length() }
            val totalSizeMb = totalSize / (1024 * 1024)
            
            LogStatistics(
                totalFiles = files.size,
                totalSizeMb = totalSizeMb.toInt(),
                oldestLogDate = files.lastOrNull()?.let { Date(it.lastModified()) },
                newestLogDate = files.firstOrNull()?.let { Date(it.lastModified()) },
                isWithinLimits = files.size <= MAX_LOG_FILES && totalSizeMb <= MAX_TOTAL_SIZE_MB
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting log statistics", e)
            LogStatistics(
                totalFiles = 0,
                totalSizeMb = 0,
                oldestLogDate = null,
                newestLogDate = null,
                isWithinLimits = true
            )
        }
    }
    
    /**
     * Delete all log files
     */
    fun clearAllLogs() {
        try {
            val files = getLogFiles()
            Log.w(TAG, "Deleting all ${files.size} log files")
            
            files.forEach { file ->
                file.delete()
            }
            
            Log.i(TAG, "All logs cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing logs", e)
        }
    }
    
    /**
     * Log statistics data class
     */
    data class LogStatistics(
        val totalFiles: Int,
        val totalSizeMb: Int,
        val oldestLogDate: Date?,
        val newestLogDate: Date?,
        val isWithinLimits: Boolean
    )
}

