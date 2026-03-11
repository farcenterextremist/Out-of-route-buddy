# First release go / no-go

**Purpose:** Record what is already release-ready in the repo, what is explicitly deferred, and what still blocks the first Google Play upload.

**Checked on:** 2026-03-10

---

## Verified in repo

- `:app:testDebugUnitTest` passes
- `OfflineDataManagerPersistenceTest` now runs without `@Ignore`; the prior DataStore timing race was fixed in the manager/test pair
- `:app:lintDebug` passes
- `assembleRelease` passes
- `assembleDebug --warning-mode all` passes and exposes the current Gradle 9 deprecation path
- Release doc set now matches the actual Gradle configuration in [app/build.gradle.kts](../../app/build.gradle.kts)
- A release-build blocker caused by an invalid drawable asset has been fixed
- Hot-path logging no longer prints raw coordinates or completed-trip database IDs in the reviewed save/tracking paths

---

## Current blockers for first Play upload

These items still block an actual upload even though the repo now builds cleanly.

| Area | Status | Why it blocks ship |
|------|--------|--------------------|
| Privacy policy URL | **Open** | Play listing needs a stable public privacy policy URL |
| Release keystore ownership | **Open** | Android Studio signing cannot be completed without the release keystore, alias, and password ownership process |
| Internal testing upload | **Open** | Requires Google Play Console access and manual upload/signing steps |
| Instrumented release validation | **Open** | `adb devices` returned no connected device or emulator, so `connectedDebugAndroidTest` and manual on-device release smoke testing were not run |
| Manual release matrix M1-M15 | **Open** | Recovery, overlay, permission, and data-clear flows still need human sign-off on a real device |

---

## Explicitly deferred for first release

These are acceptable to defer **only if they are documented in release notes or internal ship notes**.

| Item | Decision | Notes |
|------|----------|-------|
| Gradle 9 migration cleanup | **Defer** | Current build works on Gradle 8.13; migration tracked in [docs/GRADLE_9_MIGRATION_NOTES.md](../GRADLE_9_MIGRATION_NOTES.md) |
| Theme screenshot tests | **Defer** | Paparazzi is not configured yet |
| Robolectric recovery/application ignores | **Defer** | Covered by instrumented scenarios once a device/emulator is available |
| Trip history to details navigation | **Defer** | Product backlog item; not required for first Play upload |

---

## Needs a final release decision

These items should be treated as a yes/no release decision before production.

| Item | Recommended decision | Why |
|------|----------------------|-----|
| OfflineDataManager persistence confidence | **Allow** | Startup/save race was hardened and the previously ignored Robolectric persistence round-trip now passes; still confirm restart behavior during manual matrix work |
| LocationValidationService ignored test | **Allow if manual/device checks pass** | The suite passes except for one deferred validation case |
| In-app privacy link | **Optional unless required by review or jurisdiction** | No UI change was made yet; final policy URL must exist first |

---

## Recommended launch bar

Use this as the minimum safe first-release bar:

1. Public privacy policy URL exists.
2. Release keystore is confirmed and stored securely outside the repo.
3. Internal testing upload succeeds with a signed `.aab`.
4. Manual matrix M1-M15 is completed on a real device.
5. Crashlytics is confirmed for the uploaded build.
6. Deferred items are listed in ship notes or release notes.

If any of the five blockers above remain open, treat the release as **no-go** for production.
