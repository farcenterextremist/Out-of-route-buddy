plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.detekt)
    id("kotlin-kapt")
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.google.services)
    id("jacoco")
}

// DB2: Room schema export for migration tracking
kapt {
    arguments {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
}

android {
    namespace = "com.example.outofroutebuddy"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.outofroutebuddy"
        minSdk = 24
        targetSdk = 34
        versionCode = 3
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Add PendingIntent flags for Android 12+ compatibility
        manifestPlaceholders["pendingIntentFlags"] = "FLAG_IMMUTABLE"
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

    buildFeatures {
        viewBinding = true
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    compileOptions {
        // CFG1: Aligned with root build.gradle.kts (Java 17)
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs +=
            listOf(
                "-Xjvm-default=all",
                "-opt-in=kotlin.RequiresOptIn"
            )
    }

    lint {
        checkReleaseBuilds = true
        // CFG3: abortOnError true so lint is a hard gate; fix or suppress issues (see TEST_STRATEGY).
        abortOnError = true
        disable +=
            setOf(
                "ObsoleteLintCustomCheck",
                "GradleDependency",
                "NewerVersionAvailable",
            )
    }

    testOptions {
        // REVERTED: Using HOST execution to avoid potential orchestration issues
        // execution = "ANDROID_TEST_ORCHESTRATOR" // Disabled for now to prevent cascading failures
        animationsDisabled = true
        
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
    // T4: Expose schema dir to androidTest so MigrationTestHelper finds 1.json, 2.json
    sourceSets {
        getByName("androidTest").assets.srcDirs("$projectDir/schemas")
    }
}

detekt {
    config.setFrom(files("${layout.projectDirectory}/config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
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
    
    // Material Components (for MaterialDatePicker - keeping for now)
    implementation("com.google.android.material:material:1.11.0")
    
    // MaterialCalendarView - Customizable calendar with decorators (exclude threetenbp: we use one version below)
    implementation("com.github.prolificinteractive:material-calendarview:2.0.1") {
        exclude(group = "org.threeten", module = "threetenbp")
    }

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

    // DataStore for offline persistence
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // ThreeTen Backport for MaterialCalendarView (requires LocalDate)
    implementation("org.threeten:threetenbp:1.6.8")

    // Testing dependencies
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.arch.core.testing)
    testImplementation(libs.truth)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.work.testing)
    testImplementation(libs.robolectric)
    debugImplementation(libs.fragment.testing)
    testImplementation("com.google.dagger:hilt-android-testing:${libs.versions.hilt.get()}")
    kaptTest("com.google.dagger:hilt-compiler:${libs.versions.hilt.get()}")

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.test.runner)
    androidTestImplementation(libs.test.rules)
    androidTestImplementation(libs.test.core)
    androidTestImplementation(libs.test.core.ktx)
    androidTestImplementation("androidx.room:room-testing:${libs.versions.room.get()}")

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    debugImplementation(libs.androidx.ui.test.junit4)
}

// Optimize Kotlin compilation
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
}

// Optimize Java compilation
tasks.withType<JavaCompile> {
    options.isIncremental = true
}

// Convenience alias to run only the device smoke test
tasks.register<org.gradle.api.tasks.GradleBuild>("connectedSmoke") {
    group = "verification"
    description = "Run only the device smoke test (MainActivityDeviceSmokeTest)"
    tasks = listOf("connectedDebugAndroidTest")
    startParameter.projectProperties = mapOf(
        "android.testInstrumentationRunnerArguments.class" to
            "com.example.outofroutebuddy.ui.MainActivityDeviceSmokeTest"
    )
}

// ==================== INSTRUMENTED TEST OPTIMIZATION ====================
// Note: Advanced task configuration removed due to Gradle API compatibility issues
// The test orchestrator configuration above provides the main benefits

// ==================== JACOCO COVERAGE SUITE ====================

// JaCoCo Advanced Configuration
jacoco {
    toolVersion = "0.8.10"
}

// Shared file filter for all JaCoCo reports and verification (excludes generated/Android/DI code)
val jacocoFileFilter = listOf(
    "**/R.class",
    "**/R\$*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*",
    "**/Manifest\$*.class",
    "**/*Test*.*",
    "**/*Tests*.*",
    "**/test/**",
    "**/androidTest/**",
    "android/**/*.*",
    "androidx/**/*.*",
    "**/databinding/**",
    "**/DataBinderMapper*.*",
    "**/DataBindingComponent*.*",
    "**/di/**",
    "**/*_Factory.*",
    "**/*_MembersInjector.*",
    "**/*Module*.*",
    "**/*Dagger*.*",
    "**/*Hilt*.*",
    "**/hilt_aggregated_deps/**",
    "**/dagger/**",
    "**/generated/**",
    "**/build/**",
    "**/tmp/**",
    "**/*Dao_Impl*.*",
    "**/*Database_Impl*.*",
    "**/*RoomDatabase*.*",
    "**/*Worker*.*",
    "**/*WorkManager*.*"
)

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

    // Advanced file filtering for accurate coverage (shared filter)
    val fileFilter = jacocoFileFilter

    val debugTree = fileTree("${layout.buildDirectory.get()}/tmp/kotlin-classes/debug") {
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

    val fileFilter = jacocoFileFilter

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

    val fileFilter = jacocoFileFilter

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

    val fileFilter = jacocoFileFilter

    val debugTree = fileTree("${layout.buildDirectory.get()}/tmp/kotlin-classes/debug") {
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

// Full JaCoCo suite: run unit tests, generate report, verify thresholds (single entry point for CI/local).
// "Full gate" = use this when you want to enforce coverage thresholds; fails if below 70% line / 60% branch.
tasks.register("jacocoSuite") {
    dependsOn("jacocoCoverageVerification")
    group = "Verification"
    description = "Full JaCoCo suite: unit tests + coverage report + threshold verification"

    doLast {
        // Use relative path to avoid configuration-cache serialization of Gradle script objects
        println("✅ JaCoCo suite finished: tests run, report generated, thresholds checked")
        println("📊 HTML report: app/build/reports/jacoco/jacocoTestReport/html/index.html")
        println("   XML: app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
        println("   CSV: app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.csv")
    }
}

// Run unit tests and generate coverage report only (no threshold verification).
// "Quick check" = use when jacocoSuite fails due to thresholds; CI may use this as the passing gate until coverage is raised. See docs/qa/JACOCO_SUITE.md.
tasks.register("jacocoSuiteTestsOnly") {
    dependsOn("jacocoTestReport")
    group = "Verification"
    description = "Unit tests + coverage report only (no verification). Use jacocoSuite for full gate."
    doLast {
        println("✅ Tests and report finished (thresholds not checked). Run jacocoSuite for full verification.")
        println("📊 HTML report: app/build/reports/jacoco/jacocoTestReport/html/index.html")
    }
}

// ==================== CYBER SECURITY LOOP ====================

// Run SecuritySimulationTest + run_purple_simulations.py. Single entry point for attack simulations.
tasks.register<Exec>("securitySimulations") {
    group = "Verification"
    description = "Run security attack simulations (SecuritySimulationTest + Purple training JSON)"
    workingDir = rootProject.projectDir
    commandLine(
        "python",
        "scripts/purple-team/run_purple_simulations.py",
        "--full"
    )
    isIgnoreExitValue = false
    doFirst {
        println("Running Purple Team simulations...")
    }
    doLast {
        println("✅ Security simulations complete. Check docs/agents/data-sets/security-exercises/artifacts/")
    }
}

