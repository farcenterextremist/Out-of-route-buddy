plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android") version "2.48.1"
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.outofroutebuddy"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.outofroutebuddy"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Add PendingIntent flags for Android 12+ compatibility
        manifestPlaceholders["pendingIntentFlags"] = "FLAG_IMMUTABLE"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    buildFeatures {
        viewBinding = true
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs +=
            listOf(
                "-Xjvm-default=all",
                "-opt-in=kotlin.RequiresOptIn"
            )
    }

    lint {
        checkReleaseBuilds = true
        abortOnError = false
        disable +=
            setOf(
                "ObsoleteLintCustomCheck",
                "GradleDependency",
                "NewerVersionAvailable",
            )
    }

    testOptions {
        // Run in host mode (no orchestrator)
        execution = "HOST"
        animationsDisabled = true
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    // Core Android dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Hilt Dependencies for dependency injection
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.work) // Hilt integration for WorkManager
    
    // WorkManager for background task optimization
    implementation(libs.work.runtime.ktx)

    // Firebase (restrict Crashlytics to release to avoid androidTest crash when plugin disabled)
    implementation(platform("com.google.firebase:firebase-bom:32.7.2"))
    // Keep Analytics in debug to satisfy FirebaseApp and analytics references; no Crashlytics in debug
    debugImplementation("com.google.firebase:firebase-analytics")
    releaseImplementation("com.google.firebase:firebase-crashlytics")
    releaseImplementation("com.google.firebase:firebase-analytics")

    // Compose dependencies
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    
    // Preferences
    implementation("androidx.preference:preference-ktx:1.2.1")

    // Room Database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)

    // Location services
    implementation(libs.play.services.location)

    // Logging
    implementation(libs.slf4j.api)
    implementation(libs.slf4j.simple)

    // Coroutines
    implementation(libs.coroutines.play.services)

    // JSON Serialization
    implementation(libs.gson)

    // Testing dependencies
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.arch.core.testing)
    testImplementation(libs.truth)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.work.testing)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.test.runner)
    androidTestImplementation(libs.test.rules)
    androidTestImplementation(libs.test.core)
    androidTestImplementation(libs.test.core.ktx)
    androidTestImplementation(libs.fragment.testing)
    androidTestImplementation(libs.fragment.testing.manifest)
    androidTestImplementation(libs.navigation.testing)
    androidTestImplementation(libs.espresso.contrib)
    androidTestImplementation(libs.espresso.intents)
    androidTestImplementation(libs.espresso.accessibility)
    androidTestImplementation(libs.espresso.web)
    androidTestImplementation(libs.idling.concurrent)
    androidTestImplementation(libs.idling.net)
    androidTestImplementation(libs.mockito.android)
    androidTestImplementation(libs.mockito.kotlin)
    androidTestImplementation("com.google.dagger:hilt-android-testing:${libs.versions.hilt.get()}")
    kaptAndroidTest("com.google.dagger:hilt-compiler:${libs.versions.hilt.get()}")
    androidTestImplementation("androidx.test.espresso:espresso-core:${libs.versions.espresso.core.get()}")
    androidTestImplementation("org.hamcrest:hamcrest-library:1.3")

    // Hilt testing dependencies
    androidTestImplementation(libs.hilt.android)
    kaptAndroidTest(libs.hilt.compiler)

    // Additional testing dependencies
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    debugImplementation(libs.androidx.ui.test.junit4)
}

// Align Android Test dependencies to avoid version conflicts
configurations.all {
    resolutionStrategy {
        force("androidx.test.espresso:espresso-core:${libs.versions.espresso.core.get()}")
        force("androidx.test.espresso:espresso-contrib:${libs.versions.espresso.contrib.get()}")
        force("androidx.test.espresso:espresso-intents:${libs.versions.espresso.intents.get()}")
        force("androidx.test.espresso:espresso-accessibility:${libs.versions.espresso.accessibility.get()}")
        force("androidx.test.espresso:espresso-web:${libs.versions.espresso.web.get()}")
    }
}

// Optimize Kotlin compilation
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

// Optimize Java compilation
tasks.withType<JavaCompile> {
    options.isIncremental = true
}
