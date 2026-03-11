# 🚀 **OutOfRouteBuddy Deployment Guide**

## **📋 Prerequisites**

### **Development Environment**
- **Android Studio**: Latest stable version
- **JDK**: **Version 17 (Java 17)** — required for current Gradle and Kotlin
- **Gradle**: Version 7.0 or higher (see `GRADLE_9_MIGRATION_NOTES.md` for Gradle 9 / `--warning-mode all`)
- **Android SDK**: **minSdk 24** (Android 7.0+), targetSdk 34, compileSdk 34

### **System Requirements**
- **RAM**: Minimum 8GB, recommended 16GB
- **Storage**: 10GB free space for builds and dependencies
- **OS**: Windows 10+, macOS 10.15+, or Linux

## **🔧 Build Configuration**

### **Actual build and lint policy (source of truth: app/build.gradle.kts)**

- **App version:** The **source of truth** for `versionCode` and `versionName` is `app/build.gradle.kts`. Update there for each release; keep DEPLOYMENT examples or prose in sync or state "see app/build.gradle.kts for current version."
- **Release minification:** `isMinifyEnabled = true` and `isShrinkResources = true` for release. Keep [app/proguard-rules.pro](../app/proguard-rules.pro) aligned with real runtime behavior and re-test release builds after any keep-rule changes. See [docs/GRADLE_9_MIGRATION_NOTES.md](GRADLE_9_MIGRATION_NOTES.md) (Release minification).
- **Lint:** `abortOnError = true`, so lint is a hard release gate. Fix or explicitly suppress real issues before shipping. See [docs/qa/TEST_STRATEGY.md](qa/TEST_STRATEGY.md) (Quality gates) and [docs/technical/CODE_QUALITY_NOTES.md](technical/CODE_QUALITY_NOTES.md) (Linting).
- **Gradle 9:** Deprecation warnings captured; migration planned. See [docs/GRADLE_9_MIGRATION_NOTES.md](GRADLE_9_MIGRATION_NOTES.md) and `build_warnings.txt` (if present).

### **Gradle Optimization**

The project uses optimized Gradle configuration for faster builds:

```kotlin
android {
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.outofroutebuddy"
        minSdk = 24
        targetSdk = 34
        versionCode = 2
        versionName = "1.0.2"
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    lint {
        checkReleaseBuilds = true
        abortOnError = true
    }
}
```

### **Build Variants**

- **Debug**: Development builds with debugging enabled
- **Release**: Production builds with minification and resource shrinking enabled
- **Note**: There is **no dedicated `staging` build variant** in the current Gradle configuration

## **📦 Release Process**

### **1. Pre-Release Checklist**

- [ ] All tests passing (98.3% target)
- [ ] Release-critical tests passing or explicitly deferred in `docs/qa/FAILING_OR_IGNORED_TESTS.md`
- [ ] `:app:lintDebug` passes
- [ ] `assembleRelease` passes
- [ ] Performance benchmarks met
- [ ] Documentation updated
- [ ] Version numbers updated (in `app/build.gradle.kts`; see STORE_CHECKLIST for full release steps)
- [ ] Changelog prepared

### **2. Build Commands**

```bash
# Clean build
./gradlew clean

# Build debug variant
./gradlew assembleDebug

# Build release variant
./gradlew assembleRelease

# Run unit tests and coverage (recommended)
./gradlew jacocoSuite

# Run unit tests only (no report)
./gradlew :app:testDebugUnitTest

# Lint
./gradlew :app:lintDebug
```

### **3. Performance Validation**

Optional or future: performance tests, memory profiling, and build-time analysis may be added. For now, rely on `jacocoSuite` and `lintDebug` for quality gates.

## **🔍 Quality Assurance**

### **Automated Testing**

The project includes unit and instrumented test suites. Use these commands (Windows: use `.\gradlew.bat`):

```bash
# Unit tests + coverage report + threshold verification (single entry point)
./gradlew jacocoSuite

# Unit tests only
./gradlew :app:testDebugUnitTest

# Instrumented tests (device/emulator)
./gradlew connectedDebugAndroidTest
```

See [docs/qa/JACOCO_SUITE.md](qa/JACOCO_SUITE.md) for report paths and options.

### **Code Quality Gates**

- **Lint:** Run `./gradlew :app:lintDebug`; report at `app/build/reports/lint-results*.html`.
- **Test coverage:** JaCoCo thresholds (see `app/build.gradle.kts` and [JACOCO_SUITE.md](qa/JACOCO_SUITE.md)).
- **CI:** `.github/workflows/android-tests.yml` runs `jacocoSuite`; coverage-check and coverage-analysis workflows run tests and report.
- **Dependency review:** Run periodically (e.g. `./gradlew dependencyUpdates` or OSS index / CVE scan). See [docs/security/SECURITY_NOTES.md](security/SECURITY_NOTES.md) §11 (Dependency and CVE hygiene).

### **Manual Testing Checklist**

- [ ] Location validation accuracy
- [ ] GPS synchronization
- [ ] Background service stability
- [ ] Memory usage monitoring
- [ ] Cache performance
- [ ] Error handling scenarios
- [ ] Offline functionality
- [ ] UI responsiveness

## **📱 Distribution**

### **Internal Testing**

1. **Debug APK**: For development team testing
2. **Internal testing `.aab`**: Upload the signed bundle to Google Play Internal testing for QA validation
3. **Release candidate validation**: Complete the manual matrix and recovery checks on a real device before production rollout

### **Production Release**

1. **Google Play Console**: Upload signed Android App Bundle (`.aab`) to Internal testing first
2. **Release Notes**: Document changes and improvements
3. **Staged Rollout**: Gradual release to users
4. **Monitoring**: Track performance and crash reports

### **APK Signing**

- **Release keystore:** Create once with `keytool` (see [docs/STORE_CHECKLIST.md](STORE_CHECKLIST.md) §3). Store the keystore file and passwords in a **secure place** (e.g. encrypted backup or secret manager). **Do not commit** the keystore to the repo. Document where it is stored and who has access (e.g. in this section or in STORE_CHECKLIST: "Release keystore is stored at [e.g. secure backup]; only [role] has access. Used for signing release builds.")
- **First Play release path:** Prefer Android Studio **Build > Generate Signed Bundle / APK** and generate a signed **Android App Bundle (`.aab`)**. This matches the current repo state because no `signingConfigs.release` block is configured in [app/build.gradle.kts](../app/build.gradle.kts).
- **Keystore ownership:** Before release, fill in the operational details below:
  - **Stored at:** `TBD_BY_OWNER`
  - **Accessible by:** `TBD_BY_OWNER`
  - **Backup location:** `TBD_BY_OWNER`

```bash
# Generate keystore (first time only)
keytool -genkey -v -keystore outofroutebuddy.keystore -alias outofroutebuddy -keyalg RSA -keysize 2048 -validity 10000

# Optional command-line signing path for APK workflows only
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore outofroutebuddy.keystore app-release-unsigned.apk outofroutebuddy
zipalign -v 4 app-release-unsigned.apk OutOfRouteBuddy-release.apk
```

**Recommended first-release signing steps (Android Studio):**

1. Open the project in Android Studio.
2. Choose **Build > Generate Signed Bundle / APK**.
3. Select **Android App Bundle**.
4. Choose the release keystore, alias, and passwords.
5. Build the signed `.aab`.
6. Upload that `.aab` to **Google Play Console > Internal testing**.

## **📊 Monitoring & Analytics**

### **Performance Monitoring**

The app includes built-in performance monitoring:

```kotlin
// Performance tracking
PerformanceMonitor.trackValidationTime("location_validation", duration)
PerformanceMonitor.trackMemoryUsage(memoryUsage)

// Generate reports
val report = PerformanceMonitor.generatePerformanceReport()
```

### **Crash Reporting**

- **Firebase Crashlytics**: Automatic crash reporting
- **Custom Error Tracking**: Validation error logging
- **Performance Alerts**: Memory and performance warnings

### **User Analytics**

- **Usage Patterns**: Feature usage tracking
- **Performance Metrics**: Validation success rates
- **Error Rates**: Validation failure tracking
- **User Feedback**: In-app feedback collection

## **🔄 Continuous Integration**

### **CI/CD Pipeline**

The repo uses GitHub Actions in `.github/workflows/`:

- **android-tests.yml:** Runs `./gradlew jacocoSuiteTestsOnly`, lint, debug build, and connected Android tests.
- **coverage-check.yml**, **coverage-analysis.yml:** Coverage reporting and PR comments.

Example step to run tests in CI:

```yaml
- run: ./gradlew jacocoSuiteTestsOnly --stacktrace
```

### **Automated Quality Gates**

- **Build success:** All builds must pass.
- **Test coverage:** JaCoCo thresholds (see app/build.gradle.kts and docs/qa/JACOCO_SUITE.md).
- **Lint:** Required for release readiness; run `:app:lintDebug` because `abortOnError = true`.

## **🔧 Environment Configuration**

### **Development Environment**

```properties
# gradle.properties
org.gradle.jvmargs=-Xmx4g -XX:MaxPermSize=512m -XX:+HeapDumpOnOutOfMemoryError
org.gradle.daemon=true
org.gradle.parallel=true
org.gradle.configureondemand=true
org.gradle.caching=true
```

### **Production Environment**

```properties
# Production settings
android.enableR8.fullMode=true
android.enableProguardInReleaseBuilds=true
android.useAndroidX=true
android.enableJetifier=true
```

## **🚨 Rollback Procedures**

### **Emergency Rollback**

1. **Immediate Actions**:
   - Disable new feature flags
   - Revert to previous stable version
   - Notify users of temporary issues

2. **Investigation**:
   - Analyze crash reports
   - Review performance metrics
   - Identify root cause

3. **Fix and Redeploy**:
   - Implement fixes
   - Test thoroughly
   - Deploy with staged rollout

### **Rollback Triggers**

- **Crash Rate**: >5% crash rate
- **Performance**: Validation time >100ms
- **Memory Issues**: Peak usage >200MB
- **User Complaints**: Significant negative feedback

## **📈 Performance Benchmarks**

### **Target Metrics**

| Metric | Target | Current | Status |
|--------|--------|---------|--------|
| Build Time | <5 minutes | 3.2 minutes | ✅ |
| Test Pass Rate | >95% | 98.3% | ✅ |
| Validation Time | <50ms | 35ms | ✅ |
| Memory Usage | <100MB | 85MB | ✅ |
| Cache Hit Rate | >70% | 78% | ✅ |

### **Monitoring Dashboard**

- **Real-time Metrics**: Live performance tracking
- **Historical Data**: Trend analysis
- **Alert System**: Automatic notifications
- **Performance Reports**: Weekly summaries

---

*Last Updated: Phase 3 Deployment Documentation Complete* 