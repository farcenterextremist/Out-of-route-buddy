# ========================================
# ✅ ProGuard Rules for OutOfRouteBuddy
# ✅ NEW (#25): Comprehensive rules for release builds
# ========================================
#
# These rules prevent runtime crashes in release builds by:
# - Keeping classes used via reflection
# - Preserving annotation processors (Hilt, Room)
# - Keeping data models for serialization (Gson)
# - Preventing obfuscation of critical code
#
# Priority: HIGH
# Impact: Release build stability
# ========================================

# ========================================
# General Android Rules
# ========================================

# Keep source file names and line numbers for better crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep annotations
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep generic type information for better debugging
-keepattributes Signature

# ========================================
# Hilt / Dagger Rules
# ========================================

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Keep classes annotated with Hilt annotations
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.Module class * { *; }
-keep @dagger.hilt.InstallIn class * { *; }

# Keep Hilt-generated code
-keep class **_HiltModules { *; }
-keep class **_HiltComponents { *; }
-keep class **_Factory { *; }
-keep class **_MembersInjector { *; }

# Dagger specific rules
-dontwarn com.google.errorprone.annotations.**

# ========================================
# Room Database Rules
# ========================================

# Keep Room classes
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keep @androidx.room.Database class * { *; }

# Keep Room generated implementations
-keep class * implements androidx.room.RoomDatabase.Callback { *; }
-keep class **_Impl { *; }

# Keep database entities and DAOs
-keep class com.example.outofroutebuddy.data.entities.** { *; }
-keep class com.example.outofroutebuddy.data.dao.** { *; }

# ========================================
# Gson / JSON Serialization Rules
# ========================================

# Keep Gson classes
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class sun.misc.Unsafe { *; }

# Keep all data models used for serialization
-keep class com.example.outofroutebuddy.domain.models.** { *; }
-keep class com.example.outofroutebuddy.models.** { *; }
-keep class com.example.outofroutebuddy.data.entities.** { *; }
-keep class com.example.outofroutebuddy.services.TripCrashRecoveryManager$RecoverableTripState { *; }

# Prevent obfuscation of fields in data classes
-keepclassmembers class com.example.outofroutebuddy.domain.models.** {
    <fields>;
    <init>(...);
}

# ========================================
# Firebase Rules
# ========================================

# Keep Firebase classes
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Firebase Crashlytics
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# Firebase Analytics
-keep class com.google.android.gms.measurement.** { *; }

# ========================================
# Kotlin Coroutines Rules
# ========================================

# Keep coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keep class kotlinx.coroutines.** { *; }

# Keep Flow and StateFlow
-keep class kotlinx.coroutines.flow.** { *; }

# ========================================
# Location Services Rules
# ========================================

# Keep Google Play Services Location
-keep class com.google.android.gms.location.** { *; }
-dontwarn com.google.android.gms.**

# ========================================
# App-Specific Rules
# ========================================

# Keep Application class
-keep class com.example.outofroutebuddy.OutOfRouteApplication { *; }
-keep class com.example.outofroutebuddy.DebugApplication { *; }

# Keep MainActivity
-keep class com.example.outofroutebuddy.MainActivity { *; }

# Keep ViewModels (used via reflection by Hilt)
-keep class com.example.outofroutebuddy.presentation.viewmodel.** { *; }

# Keep Services
-keep class com.example.outofroutebuddy.services.** { *; }

# Keep fragments
-keep class com.example.outofroutebuddy.presentation.ui.** { *; }

# Keep validation framework
-keep class com.example.outofroutebuddy.validation.** { *; }

# Keep utilities
-keep class com.example.outofroutebuddy.util.** { *; }

# ========================================
# Reflection Rules
# ========================================

# Keep classes accessed via reflection (BuildConfig)
-keep class com.example.outofroutebuddy.BuildConfig { *; }
-keep class com.example.outofroutebuddy.core.config.** { *; }

# ========================================
# Enum Rules
# ========================================

# Keep all enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ========================================
# Parcelable Rules
# ========================================

# Keep Parcelable CREATOR fields
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# ========================================
# Debugging Rules (Optional - Disable in production)
# ========================================

# Keep class names for better stack traces
-keepnames class com.example.outofroutebuddy.** { *; }

# Log removed code (helps optimize)
-printconfiguration build/outputs/mapping/configuration.txt

# ========================================
# Warnings to Ignore
# ========================================

# Ignore warnings about missing classes from libraries
-dontwarn org.slf4j.**
-dontwarn javax.annotation.**
-dontwarn org.jetbrains.annotations.**

# ========================================
# Optimization Rules
# ========================================

# Enable optimization
-optimizationpasses 5
-dontusemixedcaseclassnames
-verbose

# ========================================
# End of OutOfRouteBuddy ProGuard Rules
# ========================================