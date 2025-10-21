package com.example.outofroutebuddy

import android.os.StrictMode
import android.util.Log

/**
 * 🐛 Debug Application Class
 * 
 * Enhanced version of OutOfRouteApplication specifically for debug builds.
 * Enables StrictMode to detect ANR-causing operations and performance issues.
 * 
 * ✅ NEW (#27): ANR Prevention with StrictMode
 * 
 * StrictMode detects:
 * - Disk reads/writes on main thread
 * - Network operations on main thread
 * - Slow operations (>100ms) on main thread
 * - Custom slow code execution
 * 
 * This helps us catch performance issues during development before they
 * reach production and cause ANRs (Application Not Responding).
 * 
 * Note: @HiltAndroidApp is inherited from OutOfRouteApplication
 */
class DebugApplication : OutOfRouteApplication() {
    
    companion object {
        private const val TAG = "DebugApplication"
    }
    
    override fun onCreate() {
        // ✅ NEW (#27): Enable StrictMode in debug builds only
        enableStrictMode()
        
        super.onCreate()
        
        Log.d(TAG, "✅ Debug application initialized with StrictMode enabled")
    }
    
    /**
     * ✅ NEW (#27): Enable StrictMode for ANR detection
     */
    private fun enableStrictMode() {
        try {
            // Thread policy - detects operations that shouldn't be on main thread
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll() // Detect all violations
                    .penaltyLog() // Log violations to Logcat
                    .build()
            )
            
            // VM policy - detects memory leaks and resource issues
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectAll() // Detect all violations
                    .penaltyLog() // Log violations to Logcat
                    .build()
            )
            
            Log.i(TAG, "✅ StrictMode enabled - watching for ANR-causing operations")
            Log.i(TAG, "   - Disk I/O on main thread will be logged")
            Log.i(TAG, "   - Network on main thread will be logged")
            Log.i(TAG, "   - Slow operations (>100ms) will be logged")
            Log.i(TAG, "   - Memory leaks will be detected")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to enable StrictMode", e)
        }
    }
} 
