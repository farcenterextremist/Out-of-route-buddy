# 📚 **OutOfRouteBuddy API Documentation**

## **🔧 Core Services**

### **LocationValidationService**

The main service for validating GPS location data in vehicle tracking applications.

#### **Primary Methods**

##### `validateLocation(location: Location, lastLocation: Location?, lastUpdateTime: Long, lastSpeed: Float, config: ValidationConfig): ValidationResult`

Validates a GPS location update for vehicle tracking applications.

**Parameters:**
- `location`: The GPS location to validate (required)
- `lastLocation`: The previous valid location (optional)
- `lastUpdateTime`: Timestamp of the last update in milliseconds
- `lastSpeed`: The last known speed in mph
- `config`: Validation configuration parameters

**Returns:** `ValidationResult` indicating if the location is valid

**Throws:** `IllegalArgumentException` if location is null

**Example:**
```kotlin
val result = validationService.validateLocation(
    location = currentLocation,
    lastLocation = previousLocation,
    lastUpdateTime = System.currentTimeMillis(),
    lastSpeed = 25.0f,
    config = ValidationConfig
)
```

##### `validateVehicleLocation(location: Location, lastLocation: Location?, lastUpdateTime: Long, lastSpeed: Float): ValidationResult`

Vehicle-specific location validation with stricter thresholds.

**Parameters:**
- `location`: The GPS location to validate
- `lastLocation`: The previous valid location
- `lastUpdateTime`: Timestamp of the last update
- `lastSpeed`: The last known speed in mph

**Returns:** `ValidationResult` with vehicle-specific validation

### **LocationCache**

Performance-optimized cache for location validation results.

#### **Primary Methods**

##### `getCachedValidation(location: Location, context: ValidationContext): ValidationResult?`

Retrieves a cached validation result if available.

**Parameters:**
- `location`: The GPS location to validate
- `context`: Additional validation context (speed, accuracy, etc.)

**Returns:** Cached validation result or null if not found

##### `cacheValidation(location: Location, context: ValidationContext, result: ValidationResult)`

Caches a validation result for future use.

**Parameters:**
- `location`: The GPS location that was validated
- `context`: Additional validation context
- `result`: The validation result to cache

##### `getCacheStats(): CachePerformanceStats`

Gets cache performance statistics.

**Returns:** `CachePerformanceStats` with hit rate and usage information

### **PerformanceMonitor**

Tracks performance metrics for location validation operations.

#### **Primary Methods**

##### `trackValidationTime(operation: String, duration: Long)`

Tracks the duration of a validation operation.

**Parameters:**
- `operation`: Name of the operation being tracked
- `duration`: Duration in milliseconds

##### `trackMemoryUsage(memoryUsage: Long, context: String = "validation")`

Tracks memory usage at a specific point in time.

**Parameters:**
- `memoryUsage`: Current memory usage in bytes
- `context`: Context for the memory snapshot

##### `generatePerformanceReport(): PerformanceReport`

Generates a comprehensive performance report.

**Returns:** `PerformanceReport` with detailed metrics and recommendations

## **📊 Data Models**

### **ValidationResult**

Represents the result of a location validation operation.

```kotlin
data class ValidationResult(
    val isValid: Boolean,
    val distance: Float,
    val speed: Float,
    val accuracy: Float,
    val age: Long,
    val errors: List<ValidationError>,
    val warnings: List<ValidationWarning>
)
```

**Properties:**
- `isValid`: Whether the location is valid for tracking
- `distance`: Calculated distance from previous location
- `speed`: Calculated speed in mph
- `accuracy`: GPS accuracy in meters
- `age`: Age of the location data in milliseconds
- `errors`: List of validation errors
- `warnings`: List of validation warnings

### **ValidationError**

Represents a validation error with details.

```kotlin
data class ValidationError(
    val type: ErrorType,
    val message: String,
    val severity: ErrorSeverity,
    val timestamp: Long
)
```

**Error Types:**
- `LOCATION_TOO_OLD`: Location data is older than threshold
- `ACCURACY_TOO_POOR`: GPS accuracy is below acceptable level
- `SPEED_TOO_HIGH`: Calculated speed exceeds vehicle limits
- `DISTANCE_TOO_LARGE`: Distance from previous location is unrealistic

### **CachePerformanceStats**

Performance statistics for the location cache.

```kotlin
data class CachePerformanceStats(
    val cacheHits: Long,
    val cacheMisses: Long,
    val hitRate: Double,
    val lruCacheSize: Int,
    val highFrequencyCacheSize: Int,
    val maxCacheSize: Int
)
```

### **PerformanceReport**

Comprehensive performance report with metrics and recommendations.

```kotlin
data class PerformanceReport(
    val uptime: Long,
    val totalValidations: Long,
    val averageValidationTime: Long,
    val peakMemoryUsage: Long,
    val averageMemoryUsage: Long,
    val operationAverages: Map<String, Long>,
    val performanceAlerts: List<PerformanceAlert>,
    val recommendations: List<String>
)
```

## **⚙️ Configuration**

### **ValidationConfig**

Centralized configuration for validation parameters.

```kotlin
object ValidationConfig {
    // Location validation
    const val MAX_LOCATION_AGE = 30000L // 30 seconds
    const val MAX_ACCURACY = 50f // 50 meters
    const val MIN_ACCURACY = 5f // 5 meters
    
    // Vehicle-specific
    const val VEHICLE_MAX_SPEED_MPH = 85f
    const val VEHICLE_MIN_SPEED_MPH = 2.5f
    
    // Traffic detection
    const val TRAFFIC_SPEED_THRESHOLD = 15f
    const val TRAFFIC_ACCURACY_THRESHOLD = 25f
}
```

## **🔧 Error Handling**

### **Validation Exceptions**

```kotlin
class ValidationException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

class ConfigurationException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
```

### **Error Recovery**

The system implements graceful error handling:

1. **Validation Failures**: Continue with degraded accuracy
2. **Cache Failures**: Fall back to direct validation
3. **Memory Issues**: Clear cache and continue
4. **Service Failures**: Automatic retry with exponential backoff

## **📈 Performance Guidelines**

### **Target Metrics**

- **Validation Time**: <50ms per validation
- **Memory Usage**: <100MB peak
- **Cache Hit Rate**: >70%
- **Error Rate**: <5%

### **Optimization Tips**

1. **Use LocationCache**: Reduces redundant calculations
2. **Monitor Performance**: Use PerformanceMonitor for insights
3. **Configure Appropriately**: Adjust ValidationConfig for your use case
4. **Handle Errors Gracefully**: Implement proper error recovery

## **🧪 Testing**

### **Unit Testing**

```kotlin
@Test
fun `validateLocation with valid location returns Valid`() {
    val location = createTestLocation()
    val result = validationService.validateLocation(location, null, 0L, 0f, ValidationConfig)
    assertTrue(result.isValid)
}
```

### **Performance Testing**

```kotlin
@Test
fun `location validation should complete within 10ms`() {
    val startTime = System.currentTimeMillis()
    validationService.validateLocation(testLocation, null, 0L, 0f, ValidationConfig)
    val duration = System.currentTimeMillis() - startTime
    assertTrue("Validation took too long: ${duration}ms", duration < 10)
}
```

---

*Last Updated: Phase 3 Documentation Complete* 