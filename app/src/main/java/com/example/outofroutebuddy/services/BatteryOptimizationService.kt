package com.example.outofroutebuddy.services

import android.content.Context
import android.location.Location
import android.os.BatteryManager
import android.util.Log
import com.example.outofroutebuddy.core.config.ValidationConfig
import com.example.outofroutebuddy.data.SettingsManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * ✅ Battery Optimization Service
 * 
 * Intelligently manages GPS polling to save battery:
 * - Reduces update frequency when stationary
 * - Adapts based on battery level
 * - Disables features when battery critical
 * - Monitors battery impact
 */
@Singleton
class BatteryOptimizationService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsManager: SettingsManager
) {
    companion object {
        private const val TAG = "BatteryOptimization"
        private const val CRITICAL_BATTERY_LEVEL = 15 // 15%
        private const val LOW_BATTERY_LEVEL = 20 // 20%
        private const val STATIONARY_THRESHOLD_METERS = 10.0 // 10 meters
        private const val STATIONARY_TIME_MS = 60000L // 1 minute
    }
    
    private val _optimizationState = MutableStateFlow(OptimizationState())
    val optimizationState: StateFlow<OptimizationState> = _optimizationState.asStateFlow()
    
    private var lastLocation: Location? = null
    private var lastMovementTime: Long = System.currentTimeMillis()
    private var stationaryStartTime: Long? = null
    
    data class OptimizationState(
        val batteryLevel: Int = 100,
        val isLowBattery: Boolean = false,
        val isCriticalBattery: Boolean = false,
        val isStationary: Boolean = false,
        val recommendedUpdateInterval: Long = 10000L, // 10 seconds default
        val powerSavingMode: PowerSavingMode = PowerSavingMode.NORMAL,
        val batteryImpact: String = "Low"
    )
    
    enum class PowerSavingMode {
        NORMAL,      // Full GPS tracking
        ECO,         // Reduced frequency when battery low
        CRITICAL     // Minimal tracking when battery critical
    }
    
    /**
     * Get recommended GPS update interval based on conditions
     */
    fun getRecommendedGpsInterval(): Long {
        if (!settingsManager.isBatteryOptimizationEnabled()) {
            return ValidationConfig.NORMAL_UPDATE_FREQUENCY // 5 seconds
        }
        
        val batteryLevel = getCurrentBatteryLevel()
        val isStationary = _optimizationState.value.isStationary
        
        return when {
            batteryLevel < CRITICAL_BATTERY_LEVEL -> 30000L // 30 seconds - critical
            batteryLevel < LOW_BATTERY_LEVEL -> 15000L // 15 seconds - low battery
            isStationary -> 20000L // 20 seconds - not moving
            else -> 10000L // 10 seconds - normal
        }
    }
    
    /**
     * Update battery status
     */
    fun updateBatteryStatus() {
        val batteryLevel = getCurrentBatteryLevel()
        val isCharging = isBatteryCharging()
        
        val powerMode = when {
            batteryLevel < CRITICAL_BATTERY_LEVEL && !isCharging -> PowerSavingMode.CRITICAL
            batteryLevel < LOW_BATTERY_LEVEL && !isCharging -> PowerSavingMode.ECO
            else -> PowerSavingMode.NORMAL
        }
        
        val batteryImpact = when (powerMode) {
            PowerSavingMode.NORMAL -> "Normal"
            PowerSavingMode.ECO -> "Reduced"
            PowerSavingMode.CRITICAL -> "Minimal"
        }
        
        _optimizationState.value = _optimizationState.value.copy(
            batteryLevel = batteryLevel,
            isLowBattery = batteryLevel < LOW_BATTERY_LEVEL,
            isCriticalBattery = batteryLevel < CRITICAL_BATTERY_LEVEL,
            powerSavingMode = powerMode,
            recommendedUpdateInterval = getRecommendedGpsInterval(),
            batteryImpact = batteryImpact
        )
        
        Log.d(TAG, "Battery: $batteryLevel%, Mode: $powerMode, Interval: ${getRecommendedGpsInterval()}ms")
    }
    
    /**
     * Check if device is stationary
     */
    fun checkStationaryStatus(currentLocation: Location) {
        lastLocation?.let { last ->
            val distance = currentLocation.distanceTo(last)
            
            if (distance < STATIONARY_THRESHOLD_METERS) {
                if (stationaryStartTime == null) {
                    stationaryStartTime = System.currentTimeMillis()
                } else if (System.currentTimeMillis() - (stationaryStartTime ?: 0) > STATIONARY_TIME_MS) {
                    // Been stationary for more than 1 minute
                    if (!_optimizationState.value.isStationary) {
                        Log.d(TAG, "Device detected as stationary - reducing GPS polling")
                        _optimizationState.value = _optimizationState.value.copy(
                            isStationary = true,
                            recommendedUpdateInterval = 20000L // 20 seconds
                        )
                    }
                }
            } else {
                // Movement detected
                if (_optimizationState.value.isStationary) {
                    Log.d(TAG, "Movement detected - resuming normal GPS polling")
                }
                stationaryStartTime = null
                _optimizationState.value = _optimizationState.value.copy(
                    isStationary = false,
                    recommendedUpdateInterval = getRecommendedGpsInterval()
                )
                lastMovementTime = System.currentTimeMillis()
            }
        }
        
        lastLocation = currentLocation
    }
    
    /**
     * Get current battery level
     */
    private fun getCurrentBatteryLevel(): Int {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }
    
    /**
     * Check if battery is charging
     */
    private fun isBatteryCharging(): Boolean {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return batteryManager.isCharging
    }
}


