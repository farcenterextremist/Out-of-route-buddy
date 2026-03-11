# OutOfRouteBuddy

Android app for tracking out-of-route miles. Built with Kotlin, MVVM, Hilt, and Room.

## Build

```bash
./gradlew assembleDebug
```

For release:
```bash
./gradlew assembleRelease
```

## Run Tests

**Quick check** (unit tests only; passing gate when coverage verification is not yet met):

```bash
./gradlew testDebugUnitTest
```

Or use the CI gate (unit tests + report, no threshold verification):

```bash
./gradlew jacocoSuiteTestsOnly
```

**Full verification** (unit tests + coverage report + threshold verification). May fail until per-class coverage is raised; see [docs/qa/COVERAGE_SCORE_AND_CRITICAL_AREAS.md](docs/qa/COVERAGE_SCORE_AND_CRITICAL_AREAS.md):

```bash
./gradlew jacocoSuite
```

See [docs/qa/JACOCO_SUITE.md](docs/qa/JACOCO_SUITE.md) for all JaCoCo tasks and report paths.

## Documentation

See [docs/](docs/) for:

- [Project overview](docs/README.md)
- [CRUCIAL improvements](docs/CRUCIAL_IMPROVEMENTS_TODO.md)
- [Quality and robustness plan](docs/QUALITY_AND_ROBUSTNESS_PLAN.md)
- [Technical documentation](docs/technical/)
- [QA and testing](docs/qa/)

## Requirements

- JDK 17
- Android Studio or compatible IDE
- Android SDK 34
