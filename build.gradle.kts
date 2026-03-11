
// This project is configured for Java 17. Ensure your JDK and Android Studio use Java 17 (not Java 21+).

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.navigation.safeargs) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.google.services) apply false
}

// Force consistent Kotlin version across all modules and dependencies
subprojects {
    configurations.all {
        resolutionStrategy.eachDependency {
            if (requested.group == "org.jetbrains.kotlin") {
                useVersion(libs.versions.kotlin.get())
                because("Force consistent Kotlin version to avoid metadata mismatch")
            }
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
} 