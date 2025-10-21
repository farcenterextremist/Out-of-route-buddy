# 🚀 Configuration Quick Reference

## **Most Used Constants**

### **Location Validation**
```kotlin
import com.example.outofroutebuddy.core.config.ValidationConfig

// GPS Accuracy
ValidationConfig.MAX_ACCURACY          // 50f meters
ValidationConfig.MIN_ACCURACY          // 5f meters

// Speed Limits
ValidationConfig.VEHICLE_MAX_SPEED_MPH // 85f MPH
ValidationConfig.VEHICLE_MIN_SPEED_MPH // 2.5f MPH

// Distance Thresholds
ValidationConfig.MIN_DISTANCE_THRESHOLD // 25f meters
ValidationConfig.MAX_DISTANCE_BETWEEN_UPDATES // 1000f meters

// Time Limits
ValidationConfig.MAX_LOCATION_AGE      // 30000L ms (30s)
ValidationConfig.MAX_STATIONARY_TIME   // 300000L ms (5min)
```

### **Traffic Detection**
```kotlin
// Traffic Mode
ValidationConfig.TRAFFIC_SPEED_THRESHOLD     // 15f MPH
ValidationConfig.TRAFFIC_ACCURACY_THRESHOLD  // 25f meters
ValidationConfig.TRAFFIC_MIN_SPEED_MPH       // 0.5f MPH

// Heavy Traffic
ValidationConfig.HEAVY_TRAFFIC_MAX_SPEED_MPH // 10f MPH
```

### **Micro-movement Tracking**
```kotlin
ValidationConfig.MICRO_MOVEMENT_THRESHOLD        // 2f meters
ValidationConfig.MICRO_MOVEMENT_MIN_COUNT        // 3
ValidationConfig.MICRO_MOVEMENT_ACCUMULATION_LIMIT // 50f meters
```

### **GPS Data Flow**
```kotlin
ValidationConfig.GPS_BATCH_SIZE              // 5
ValidationConfig.GPS_MIN_DISTANCE_METERS     // 10.0
ValidationConfig.GPS_MAX_SPEED_MPH           // 85.0 (DEPRECATED - use VEHICLE_MAX_SPEED_MPH)
ValidationConfig.GPS_MIN_ACCURACY_METERS     // 20.0 (matches VEHICLE_MIN_ACCURACY)
ValidationConfig.GPS_MAX_ACCURACY_METERS     // 50.0 (matches MAX_ACCURACY)
```

### **Background Sync**
```kotlin
ValidationConfig.SYNC_CACHE_CLEANUP_INTERVAL_MS // 300000L (5min)
ValidationConfig.SYNC_STATE_INTERVAL_MS         // 30000L (30s)
ValidationConfig.SYNC_GPS_INTERVAL_MS           // 10000L (10s)
```

### **Build Configuration**
```kotlin
import com.example.outofroutebuddy.core.config.BuildConfig

// App Info
BuildConfig.VERSION_NAME    // App version
BuildConfig.MIN_SDK         // Minimum SDK version
BuildConfig.TARGET_SDK      // Target SDK version

// Database
BuildConfig.DATABASE_NAME   // Database name
BuildConfig.DATABASE_VERSION // Database version

// Services
BuildConfig.LOCATION_SERVICE_CLASS // Location service class name
BuildConfig.SYNC_SERVICE_CLASS     // Sync service class name
```

## **Common Usage Patterns**

### **Location Validation**
```kotlin
fun validateLocation(location: Location): Boolean {
    return location.accuracy <= ValidationConfig.MAX_ACCURACY &&
           location.speed <= ValidationConfig.VEHICLE_MAX_SPEED_MPH * ValidationConfig.MPH_TO_MPS &&
           System.currentTimeMillis() - location.time <= ValidationConfig.MAX_LOCATION_AGE
}
```

### **Traffic Mode Detection**
```kotlin
fun isTrafficMode(speed: Float): Boolean {
    return speed <= ValidationConfig.TRAFFIC_SPEED_THRESHOLD * ValidationConfig.MPH_TO_MPS
}
```

### **Micro-movement Detection**
```kotlin
fun isMicroMovement(distance: Float): Boolean {
    return distance <= ValidationConfig.MICRO_MOVEMENT_THRESHOLD
}
```

### **GPS Filtering**
```kotlin
fun shouldProcessGpsLocation(location: Location): Boolean {
    return location.accuracy >= ValidationConfig.GPS_MIN_ACCURACY_METERS &&
           location.accuracy <= ValidationConfig.GPS_MAX_ACCURACY_METERS
}
```

## **Test Constants**

### **Accuracy Values**
```kotlin
ValidationConfig.TEST_GOOD_ACCURACY      // 15f meters
ValidationConfig.TEST_POOR_ACCURACY      // 30f meters
ValidationConfig.TEST_CRITICAL_ACCURACY  // 60f meters
```

### **Speed Values**
```kotlin
ValidationConfig.TEST_NORMAL_SPEED_MPH      // 25f MPH
ValidationConfig.TEST_HIGH_SPEED_MPH        // 50f MPH
ValidationConfig.TEST_UNREALISTIC_SPEED_MPH // 100f MPH
ValidationConfig.TEST_TRAFFIC_SPEED_MPH     // 3f MPH
```

### **Timing Values**
```kotlin
ValidationConfig.TEST_SHORT_DELAY  // 1000L ms
ValidationConfig.TEST_MEDIUM_DELAY // 3000L ms
ValidationConfig.TEST_LONG_DELAY   // 5000L ms
```

## **Conversion Factors**
```kotlin
ValidationConfig.MPH_TO_MPS  // 0.44704f (MPH to m/s)
ValidationConfig.MPS_TO_MPH  // 2.23694f (m/s to MPH)
ValidationConfig.METERS_TO_FEET // 3.28084f
ValidationConfig.FEET_TO_METERS // 0.3048f
```

## **Quick Tips**

1. **Always import the config**: `import com.example.outofroutebuddy.core.config.ValidationConfig`
2. **Use descriptive names**: Constants are self-documenting
3. **Check the full documentation**: See README.md for detailed explanations
4. **Test your changes**: Always test when modifying constants
5. **Update documentation**: Keep this reference and README.md current

---

**📚 Full Documentation**: [README.md](README.md)
**🔧 Maintenance Guide**: See README.md for best practices 