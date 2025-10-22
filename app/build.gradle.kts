plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android") version "2.48.1"
    id("com.google.gms.google-services")
    id("jacoco")
}

android {
    namespace = "com.example.outofroutebuddy"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.outofroutebuddy"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Add PendingIntent flags for Android 12+ compatibility
        manifestPlaceholders["pendingIntentFlags"] = "FLAG_IMMUTABLE"
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
        }
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

// ==================== JACOCO COVERAGE SUITE ====================

// JaCoCo Advanced Configuration
jacoco {
    toolVersion = "0.8.10"
}

// Enhanced Test Configuration with JaCoCo
tasks.withType<Test> {
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf(
            "jdk.internal.*",
            "jdk.jfr.*",
            "sun.*",
            "com.sun.*",
            "org.gradle.*"
        )
    }
    
    // Enable parallel test execution for faster builds
    maxParallelForks = Runtime.getRuntime().availableProcessors().div(2).coerceAtLeast(1)
    
    // Add test result reporting
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = false
        showStackTraces = true
        showCauses = true
    }
}

// ==================== COVERAGE REPORTS ====================

// Main Coverage Report (Unit Tests)
tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")
    group = "Reporting"
    description = "Generate comprehensive JaCoCo coverage reports for unit tests"

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(true)
    }

    // Advanced file filtering for accurate coverage
    val fileFilter = listOf(
        // Android generated files
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/Manifest$*.class",
        
        // Test files
        "**/*Test*.*",
        "**/*Tests*.*",
        "**/test/**",
        "**/androidTest/**",
        
        // Android framework
        "android/**/*.*",
        "androidx/**/*.*",
        
        // Data binding
        "**/databinding/**",
        "**/DataBinderMapper*.*",
        "**/DataBindingComponent*.*",
        
        // Dependency injection (Hilt/Dagger)
        "**/di/**",
        "**/*_Factory.*",
        "**/*_MembersInjector.*",
        "**/*Module*.*",
        "**/*Dagger*.*",
        "**/*Hilt*.*",
        "**/hilt_aggregated_deps/**",
        "**/dagger/**",
        
        // Generated code
        "**/generated/**",
        "**/build/**",
        "**/tmp/**",
        
        // Room database
        "**/*Dao_Impl*.*",
        "**/*Database_Impl*.*",
        "**/*RoomDatabase*.*",
        
        // WorkManager
        "**/*Worker*.*",
        "**/*WorkManager*.*"
    )

    val debugTree = fileTree("${layout.buildDirectory.get()}/tmp/kotlin-classes/debugUnitTest") {
        exclude(fileFilter)
    }

    val mainSrc = "${project.projectDir}/src/main/java"

    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree))
    executionData.setFrom(fileTree(layout.buildDirectory) {
        include("outputs/unit_test_code_coverage/debugUnitTest/*.exec")
    })
}

// Instrumented Test Coverage Report
tasks.register<JacocoReport>("jacocoAndroidTestReport") {
    dependsOn("connectedAndroidTest")
    group = "Reporting"
    description = "Generate JaCoCo coverage reports for instrumented tests"

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(true)
    }

    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
        "**/databinding/**",
        "**/di/**",
        "**/*_Factory.*",
        "**/*_MembersInjector.*",
        "**/*Module*.*",
        "**/*Dagger*.*",
        "**/*Hilt*.*",
        "**/hilt_aggregated_deps/**"
    )

    val debugTree = fileTree("${layout.buildDirectory.get()}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }

    val mainSrc = "${project.projectDir}/src/main/java"

    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree))
    executionData.setFrom(fileTree(layout.buildDirectory) {
        include("outputs/code_coverage/connected/*coverage.ec")
    })
}

// Combined Coverage Report (Unit + Instrumented)
tasks.register<JacocoReport>("jacocoCombinedReport") {
    dependsOn("jacocoTestReport", "jacocoAndroidTestReport")
    group = "Reporting"
    description = "Generate combined JaCoCo coverage reports for all tests"

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(true)
    }

    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
        "**/databinding/**",
        "**/di/**",
        "**/*_Factory.*",
        "**/*_MembersInjector.*",
        "**/*Module*.*",
        "**/*Dagger*.*",
        "**/*Hilt*.*",
        "**/hilt_aggregated_deps/**"
    )

    val debugTree = fileTree("${layout.buildDirectory.get()}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }

    val mainSrc = "${project.projectDir}/src/main/java"

    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree))
    executionData.setFrom(
        fileTree(layout.buildDirectory) {
            include("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
        },
        fileTree(layout.buildDirectory) {
            include("outputs/code_coverage/connected/*coverage.ec")
        }
    )
}

// ==================== COVERAGE THRESHOLDS ====================

// Coverage Verification Task
tasks.register<JacocoCoverageVerification>("jacocoCoverageVerification") {
    dependsOn("jacocoTestReport")
    group = "Verification"
    description = "Verify code coverage meets minimum thresholds"

    violationRules {
        rule {
            limit {
                minimum = "0.70".toBigDecimal() // 70% minimum coverage
            }
        }
        
        rule {
            element = "CLASS"
            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                minimum = "0.60".toBigDecimal() // 60% branch coverage
            }
        }
        
        rule {
            element = "CLASS"
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.75".toBigDecimal() // 75% line coverage
            }
        }
    }

    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
        "**/databinding/**",
        "**/di/**",
        "**/*_Factory.*",
        "**/*_MembersInjector.*",
        "**/*Module*.*",
        "**/*Dagger*.*",
        "**/*Hilt*.*",
        "**/hilt_aggregated_deps/**"
    )

    val debugTree = fileTree("${layout.buildDirectory.get()}/tmp/kotlin-classes/debugUnitTest") {
        exclude(fileFilter)
    }

    val mainSrc = "${project.projectDir}/src/main/java"

    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree))
    executionData.setFrom(fileTree(layout.buildDirectory) {
        include("outputs/unit_test_code_coverage/debugUnitTest/*.exec")
    })
}

// ==================== COVERAGE ANALYSIS ====================

// Coverage Analysis Task
tasks.register("coverageAnalysis") {
    dependsOn("jacocoTestReport")
    group = "Analysis"
    description = "Analyze coverage reports and generate insights"

    doLast {
        val reportDir = file("${layout.buildDirectory.get()}/reports/jacoco/jacocoTestReport/html")
        if (reportDir.exists()) {
            println("📊 Coverage Report Generated: ${reportDir.absolutePath}")
            println("📈 Open index.html in your browser to view detailed coverage")
            println("🎯 Coverage thresholds: 70% overall, 60% branch, 75% line")
        } else {
            println("❌ Coverage report not found. Run './gradlew jacocoTestReport' first")
        }
    }
}

// Quick Coverage Check
tasks.register("coverageCheck") {
    dependsOn("jacocoCoverageVerification")
    group = "Verification"
    description = "Quick coverage check with thresholds"

    doLast {
        println("✅ Coverage verification completed")
        println("📊 Check build/reports/jacoco/jacocoTestReport/ for detailed reports")
    }
}
