# Google Play first release packet

**Purpose:** Single place to prepare the first Google Play submission for OutOfRouteBuddy using the current package name and Android Studio manual signing.

---

## Current release facts

- **App name:** `OutOfRouteBuddy`
- **Package name:** `com.example.outofroutebuddy`
- **Current version in code:** `versionCode = 2`, `versionName = "1.0.2"`
- **Build target:** `minSdk 24`, `targetSdk 34`, `compileSdk 34`
- **Release config:** `isMinifyEnabled = true`, `isShrinkResources = true`
- **Lint gate:** `abortOnError = true`
- **Firebase config present:** `app/google-services.json`

Source of truth: [app/build.gradle.kts](../../app/build.gradle.kts), [app/google-services.json](../../app/google-services.json), [docs/security/SECURITY_NOTES.md](../security/SECURITY_NOTES.md).

---

## User input still needed

- Final privacy policy public URL
- Support or contact email to publish in privacy policy or store contact fields
- Decision: add an in-app privacy policy link before first release, or defer unless Play review requires it
- Final store listing text if you want wording different from the draft below
- Confirmation of who owns the release keystore and where it is stored

---

## Draft privacy policy content

Use this as the base text for the public privacy policy page.

### Privacy Policy for OutOfRouteBuddy

**Effective date:** `YYYY-MM-DD`

OutOfRouteBuddy is an Android app that helps users track trips and out-of-route miles on their device.

### Information the app uses

- **Trip and settings data stored on device**
  - Trip history
  - In-progress trip recovery data
  - App preferences such as theme and settings
- **Diagnostics and crash reporting**
  - Firebase Crashlytics may receive crash reports and non-fatal diagnostic events
- **Analytics**
  - Firebase Analytics may receive limited app usage and performance events

### Analytics and diagnostics events

The app may record non-PII app events such as:

- `app_open`
- `low_memory_warning`
- `critical_memory_trim`
- `moderate_memory_trim`
- `performance_metrics`
- `potential_memory_leak`
- `trip_ended`
- `trip_paused`
- `trip_resumed`

These events are used for app reliability, crash investigation, and performance monitoring. They are not intended to include personally identifying information.

### Data sharing and sale

- The app does **not sell personal data**.
- The app is designed to keep trip history and preferences **on-device**.
- Crash and diagnostic data may be processed by Firebase services to help monitor app stability.

### Location data

- The app uses location to support trip tracking features.
- Trip and recovery-related location data are stored locally on the device as part of trip functionality.
- The current app design does not intentionally send raw trip history or route history to a custom remote backend.

### Backup and restore

- Trip and trip-related preference data are excluded from Android Auto Backup so they do not leave the device through standard cloud backup or device transfer.

### User controls

- Users can clear app data and trip data from within the app.
- Users can uninstall the app to remove local app data from the device.

### Contact

- **Support email:** `YOUR_SUPPORT_EMAIL`
- **Privacy policy URL:** `YOUR_PUBLIC_POLICY_URL`

Before publishing, replace placeholders and verify the final text against [docs/security/SECURITY_NOTES.md](../security/SECURITY_NOTES.md).

---

## Play Console Data safety draft

Use this as a prep sheet when filling out Google Play forms. Final categorization must match the current Play Console wording at submission time.

| Area | Draft answer |
|------|--------------|
| Does the app collect or share data? | Yes, limited diagnostics/analytics via Firebase; trip data is primarily stored on device |
| Location | Used for app functionality; trip tracking depends on location access |
| Personal info | No intentional collection of name, email, phone number, or account identity in-app |
| App activity / analytics | Yes, limited non-PII analytics and app performance events |
| Crash logs / diagnostics | Yes, Crashlytics crash and non-fatal diagnostic reporting |
| Data sharing | No sale of user data; no intentional sharing of raw trip history with a custom backend |
| Data processing purpose | App functionality, diagnostics, analytics, stability, fraud/abuse not primary |
| Is data encrypted in transit? | Yes for Firebase network traffic |
| Can users request deletion? | Local data can be cleared on device within the app |

Notes:

- Align final answers with the exact language in [docs/security/SECURITY_NOTES.md](../security/SECURITY_NOTES.md).
- If Play asks whether location is collected, answer based on actual feature behavior and current Firebase payloads, not on general app permissions alone.

---

## Store listing draft

### App name

`OutOfRouteBuddy`

### Short description

`Track trips and out-of-route miles with recovery and trip history.`

### Full description

`OutOfRouteBuddy helps you track trips, calculate out-of-route miles, and review trip history from your Android device.

Built for practical trip tracking, the app focuses on reliable local trip recording, recovery after interruptions, and fast access to history and statistics.

Key features:
- Track trips with GPS support
- Record and review out-of-route miles
- Resume active trips after app restarts or interruptions
- Review trip history and calendar-based statistics
- Manage preferences such as theme and notification behavior

The app is designed to keep trip history and settings on your device. Crash reporting and limited diagnostics may be used to improve stability.

Before release, verify this description against the final app behavior and remove anything not supported in the current build.`

---

## Asset checklist

- App icon: `512 x 512`
- Feature graphic: `1024 x 500`
- Phone screenshots: at least 2
- Optional tablet screenshots: 7-inch and 10-inch
- Privacy policy URL ready
- Release notes ready

---

## Content rating and audience guidance

- **Target audience draft:** adults or general audience who use trip tracking features
- **Sensitive area to answer carefully:** location usage for trip tracking
- **Content rating expectation:** likely low, but complete the IARC questionnaire with actual app behavior

---

## Release path for first upload

1. Finalize privacy policy URL.
2. Confirm keystore ownership.
3. Bump release version in [app/build.gradle.kts](../../app/build.gradle.kts) when ready.
4. Update [CHANGELOG.md](../../CHANGELOG.md).
5. Run unit tests, lint, instrumented tests if device available, and release build.
6. Generate a signed **Android App Bundle (`.aab`)** from Android Studio.
7. Upload to **Internal testing** first.
8. Review crash monitoring, then promote to production with staged rollout.
