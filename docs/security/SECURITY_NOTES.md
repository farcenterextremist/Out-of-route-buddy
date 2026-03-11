# Security Notes — OutOfRouteBuddy

**Owner:** Security Specialist  
**Created:** 2025-02-19  
**Source:** CRUCIAL #7 (COMPREHENSIVE_AGENT_TODOS.md)

This document captures security review findings and recommendations. Implementation is delegated to Back-end, DevOps, or QA as noted.

---

## Security checklist (Weakest Areas Plan Phase 4.1)

| Item | Status | Section |
|------|--------|---------|
| **google-services.json** in repo | Accepted for this project if API key restricted in GCP; optional: move to CI | §1 |
| **Location/trip storage and transmission** | Local only; no PII to server; do not log PII | §2 |
| **StandaloneOfflineService encryption key** | Key in SharedPreferences; remediation planned (Keystore + EncryptedSharedPreferences) | §2 |
| **Local secret files** in .gitignore | Verified in root `.gitignore` | §3 |
| **One-time dev machine checklist** | Documented in §3 | §3 |

---

## 1. google-services.json in repository

**Status:** File is currently committed at `app/google-services.json`.

**Assessment:**
- **Project ID** (`project_id`: `oor-145a0`) and **application IDs** (e.g. `mobilesdk_app_id`, `package_name`) are generally considered **non-secret** for client apps. They identify the project/app and are often embedded in client builds.
- **API key** in the file is a **restricted secret**: it can be extracted from the APK. For Firebase/Google client configs, this is a common and accepted tradeoff **provided** the key is **restricted** in Google Cloud Console (e.g. by package name, API restrictions) so it cannot be abused from other apps or domains.

**Recommendations:**
- **Acceptable to keep in repo** for this project if the API key is restricted in GCP (Android apps only, limited APIs). Document that in this file or in a one-line README note.
- **Optional hardening:** For stricter setups (e.g. multiple environments, compliance): use **CI secrets** or **build-time injection** — e.g. store `google-services.json` in CI secrets and inject it during build so it is never committed. Uncomment `google-services.json` in root `.gitignore` and add `app/google-services.json` if you adopt this.

---

## 2. Location and PII — storage and transmission

**Where trip/location data lives:**
- **Local only:** Trip and location-related data are stored on-device:
  - **Room database** (`AppDatabase`, table `trips`) — file: `outofroutebuddy_database` (app-private storage). Contains trip records including aggregated GPS metadata and, for in-progress/recovery, last known lat/lng (`lastLocationLat`, `lastLocationLng`, `lastLocationTime`).
  - **Trip state / persistence:** `TripStatePersistence`, `TripPersistenceManager`, `TripStateManager` — state and recovery data are persisted locally (e.g. to the same DB or app preferences).
  - **Preferences / settings:** `PreferencesManager`, `SettingsManager` — user preferences and app settings (device-only).
- **Transmission:** No evidence that trip or raw location data are sent to any remote server. Background sync (e.g. `SyncWorker`, `OfflineSyncService`) is used for local operations (cache cleanup, data integrity), not for uploading PII off-device.

**Recommendations:**
- **Do not log PII:** Avoid logging full coordinates, trip IDs that could be linked to a user, or other PII. Prefer log levels and tags only (e.g. "sync started", "trip saved") or non-identifying metrics. For monthly-stats and End/Clear trip flows, log only that a trip was saved or cleared—not coordinates or user-identifying trip content.
- **Encryption at rest:** Android app-private storage is already protected by the OS (per-app isolation; on many devices, encryption at rest is enabled). For extra hardening, consider EncryptedSharedPreferences or SQLCipher for the DB if handling higher-sensitivity data in the future.
- **StandaloneOfflineService (S-1):** `StandaloneOfflineService` stores its AES encryption key in SharedPreferences, which is not encrypted. This weakens the security of the encrypted cache. **Remediation:** Migrate to Android Keystore (KeyStore + EncryptedSharedPreferences) for key storage. See SECURITY_PLAN Section 9 (Gap Register).
- **Remediation status (S-1):** Planned. Migrate encryption key to Android Keystore and EncryptedSharedPreferences in a future sprint. Tracked here; Back-end owns implementation.
- **Trip metadata:** Store trip metadata in structured fields (e.g. Room columns). Avoid free-form text blobs for notes or descriptors that could contain PII; if adding notes, prefer bounded/typed fields or sanitization.
- **Future back-ends:** If trip or location data are ever sent to a server, use HTTPS only and avoid logging request/response bodies that contain PII.

---

## 3. Local secret files and machine setup

**Verification:** Local secret/config files are kept out of version control through the root `.gitignore`.

**One-time checklist for new dev machines:**
1. Clone the repo as usual.
2. **Never commit** local secret files or machine-specific configs.
3. Keep real credentials out of the repository and use local-only env/config files when needed.

---

## 4. Lost device — threat note

**Scenario:** The device is lost or stolen while a trip is active (or while the app holds trip history).

**Risks:** Unauthorized access to the app could expose trip history, last known location, and out-of-route statistics.

**Recommendations:**
- **Lock screen:** Rely on device lock (PIN, pattern, or biometric). Encourage users to use a strong lock and short auto-lock timeout.
- **App-level:** Consider (backlog) an optional **app PIN or biometric** to open the app or to end a trip, so that a found device with an unlocked screen still protects the app.
- **Remote wipe:** If the app later adds cloud sync or remote management, consider documenting or supporting **remote wipe** of app data (e.g. via device management or a signed “wipe my data” action from a trusted back-end). For current local-only storage, standard **Android “Find My Device”** (or similar) wipe restores the device to factory state and removes app data.

---

## 5. FileProvider scope (Purple Team hardening)

**Context:** Trip export uses `FileProvider` to share CSV/report files from app cache. `res/xml/file_paths.xml` exposes `cache-path name="exported_files" path="."`.

**Recommendation:** Do **not** use user-controlled paths or filenames when building URIs for FileProvider. `TripExporter` uses fixed patterns (`trips_export_*.csv`, `trips_report_*.txt`) and `context.cacheDir` only—no user input in paths. If adding new export or share features, keep file paths and names derived from app-controlled values only, to avoid path traversal or unintended file exposure. For defense in depth, consider a dedicated subfolder (e.g. `cache/exports/`) and a corresponding `<cache-path>` entry with `path="exports"` so the provider scope is minimal.

*Added: Purple Team exercise 2025-02-22 (see `docs/agents/data-sets/security-exercises/`).*

---

## 6. Service-to-service validation (Purple Team 2025-02-20)

**Context:** Android services communicate via Flow/StateFlow and DI. Malicious input could flow from one "worker" (e.g. ViewModel, external input) to another (TripRepository, TripStateManager).

**Defense in depth:**
- **Trip domain model** (`domain/models/Trip.kt`): Constructor validates and rejects NaN, negative values, out-of-range miles. Throws `IllegalArgumentException` for invalid data.
- **InputValidator** (`util/InputValidator.kt`): `sanitizeMiles()` enforces 0–10000 range; `sanitizeFilePath()` prevents path traversal.
- **ValidationFramework** (`validation/ValidationFramework.kt`): `validateTripData()`, `validateTripEntity()` for business logic and DB entity validation.
- **TripRepository** (`data/repository/TripRepository.kt`): Validates entity before `tripDao.insertTrip()`. Audit log `TripInsertAudit` fires on successful insert (`trip_inserted trip_id=X result=true`) for detection of bulk or anomalous inserts.

**Recommendation:** When adding new services or data flows, ensure validation at each layer. Do not trust internal callers without validation if the data originates from user input or external sources.

*Added: Security Plan Multi-Agent exercise 2025-02-20 (see `docs/agents/data-sets/security-exercises/`).*

---

## 7. Removed local tooling note

The custom emulator sync service was removed during repository cleanup. Security guidance in this file now focuses on the shipped Android app and any active tooling still present in the repo.

---

*For follow-up tasks (e.g. Auto drive privacy, app PIN), see `docs/agents/COMPREHENSIVE_AGENT_TODOS.md` — Security Specialist section.*

---

## 9. Backup and restore (Blind Spot Plan §1)

- **Auto Backup:** The app has `allowBackup="true"` and `fullBackupContent="@xml/backup_rules"`. In `backup_rules.xml`, **database** and **sharedpref** are excluded so trip and location data are not included in Google Auto Backup or device transfer.
- **Decision:** Trip/location data must not leave the device via backup. See [docs/technical/BACKUP_AND_RESTORE.md](../technical/BACKUP_AND_RESTORE.md) for what is excluded and why.

---

## 10. Analytics and Crashlytics (Blind Spot Plan §4)

**Firebase Analytics** and **Firebase Crashlytics** are used for app-open and performance/memory events, and for crash/non-fatal error reporting. No PII is sent in event parameters.

**Analytics — event names and parameter keys (no PII):**

| Event | Parameter keys | Purpose |
|-------|-----------------|---------|
| `app_open` (Firebase `APP_OPEN`) | `timestamp`, `crash_recovered` | App launch |
| `low_memory_warning` | `timestamp`, `available_memory` | Low-memory callback |
| `critical_memory_trim` | `level`, `available_memory` | Critical trim level |
| `moderate_memory_trim` | `level`, `available_memory` | Moderate trim |
| `performance_metrics` | `total_memory`, `free_memory`, `used_memory`, `max_memory`, `memory_usage_percentage`, `timestamp`, `database_initialized`, `database_error` | Startup metrics |
| `potential_memory_leak` | `memory_usage_percentage`, `used_memory`, `max_memory`, `timestamp` | Debug-only potential leak |
| `trip_ended`, `trip_paused`, `trip_resumed` (from TripTrackingService) | (minimal or none) | Trip lifecycle |

**Crashlytics:** Used for crashes and non-fatal errors (e.g. database init failure, WorkManager init failure, memory trimming errors). Custom keys may include `app_version`, `build_type`; no user identifiers or trip content.

**Store listing:** When publishing to a store, complete the Data safety disclosure (e.g. crash data, diagnostics) and link to your privacy policy. See [docs/STORE_CHECKLIST.md](../STORE_CHECKLIST.md) for the release and privacy checklist.

---

## 11. Dependency and CVE hygiene (Blind Spot Plan §5)

**Policy:** Run a dependency review periodically to catch outdated or vulnerable libraries. There is no automated Dependabot or CI CVE check yet.

**Recommended actions:**
- **One-time or periodic audit:** Run `./gradlew dependencyUpdates` (with the [Ben Manes plugin](https://github.com/ben-manes/gradle-versions-plugin) if added) or use an OSS index / CVE scan (e.g. OWASP Dependency-Check, or GitHub Dependabot). Document the result and date (e.g. in this section or in a `docs/security/LAST_DEPENDENCY_AUDIT.md`).
- **Optional:** Enable Dependabot (or similar) or a CI job that fails on high/critical CVEs for direct dependencies.
- **Reference:** [SECURITY_GRADE.md](SECURITY_GRADE.md) notes "no known vulnerable deps in scan; regular updates recommended." This section defines the process; last audit date and tool can be updated here when run.

---

## 12. Data export and deletion (Blind Spot Plan §11)

- **Export:** Users can **export trip data** (CSV/report) via in-app export flows (e.g. TripExporter). Export is local (file generated on device and shared via system share sheet); no server upload.
- **Deletion:** Users can **clear all trips and data** via in-app **Data Management / Clear All**. This removes local trip history and related persisted state. There is **no server-side data**; all data is local only. For store or privacy reviewers: the app supports user-initiated export and full deletion of app-held data.

---

## 8. When Export (or new sensitive feature) lands (Board-adopted)

When **Export to PDF/CSV** (or another sensitive flow) is added or significantly changed:

- **Security Specialist:** Review FileProvider paths, what's in the file, share scope, and path traversal. Document in this file or in `REVIEW_export.md` (or `REVIEW_<feature>.md`). Update this section with a short summary and link.
- **Red Team:** Run a short Purple exercise targeting that flow (e.g. path traversal, share scope). Document what Blue should check; log to `docs/agents/data-sets/security-exercises/`.
- **Blue Team:** Define what "alarm" means for that surface (what we log or enforce). After Red's action, answer "did the alarm go off?"; if not, document the gap and propose or implement a fix. Every fix should be auditable (file or config we can point to). Where possible, Red re-runs the same attack to verify the fix.
- **User:** Will be emailed when the review and optional Purple exercise are done (Human-in-the-Loop).
