// This project is configured for Java 17. Ensure your JDK and Android Studio use Java 17 (not Java 21+).

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.navigation.safeargs) apply false
    id("com.google.firebase.crashlytics") version "2.9.9" apply false
    id("com.google.dagger.hilt.android") version "2.48.1" apply false
    id("com.google.devtools.ksp") version "1.8.22-1.0.11" apply false
    id("com.google.gms.google-services") version "4.4.0" apply false
}

// Force consistent Kotlin version across all modules and dependencies
subprojects {
    configurations.all {
        resolutionStrategy.eachDependency {
            if (requested.group == "org.jetbrains.kotlin") {
                useVersion("1.9.22")
                because("Force consistent Kotlin version to avoid metadata mismatch")
            }
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
} 