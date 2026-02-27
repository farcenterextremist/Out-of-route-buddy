# Security Notes — OutOfRouteBuddy

**Owner:** Security Specialist  
**Created:** 2025-02-19  
**Source:** CRUCIAL #7 (COMPREHENSIVE_AGENT_TODOS.md)

This document captures security review findings and recommendations. Implementation is delegated to Back-end, DevOps, or QA as noted.

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
- **Trip metadata:** Store trip metadata in structured fields (e.g. Room columns). Avoid free-form text blobs for notes or descriptors that could contain PII; if adding notes, prefer bounded/typed fields or sanitization.
- **Future back-ends:** If trip or location data are ever sent to a server, use HTTPS only and avoid logging request/response bodies that contain PII.

---

## 3. Coordinator email — .env and last_reply.txt (Board 2025-03-15 verified)

**Verification (2025-02):** Confirmed `.env` and `last_reply` are never committed.

- **`.env`** and **`last_reply.txt`** are listed in the **root `.gitignore`** under:
  - `scripts/coordinator-email/.env`
  - `scripts/coordinator-email/last_reply.txt`
- They are **not** committed to the repo as long as this `.gitignore` is in use.

**One-time checklist for new dev machines:**
1. Clone the repo as usual.
2. **Never commit** `scripts/coordinator-email/.env` or `scripts/coordinator-email/last_reply.txt`.
3. For coordinator email scripts to work:
   - Copy `scripts/coordinator-email/.env.example` to `scripts/coordinator-email/.env`.
   - Fill in real values only in `.env` (e.g. SMTP/IMAP credentials); keep `.env` local and out of version control.
4. Optionally run a one-line health check (when implemented by DevOps) that verifies `.env` exists for coordinator email, without committing it.

---

## 4. Lost device — threat note

**Scenario:** The device is lost or stolen while a trip is active (or while the app holds trip history).

**Risks:** Unauthorized access to the app could expose trip history, last known location, and out-of-route statistics.

**Recommendations:**
- **Lock screen:** Rely on device lock (PIN, pattern, or biometric). Encourage users to use a strong lock and short auto-lock timeout.
- **App-level:** Consider (backlog) an optional **app PIN or biometric** to open the app or to end a trip, so that a found device with an unlocked screen still protects the app.
- **Remote wipe:** If the app later adds cloud sync or remote management, consider documenting or supporting **remote wipe** of app data (e.g. via device management or a signed “wipe my data” action from a trusted back-end). For current local-only storage, standard **Android “Find My Device”** (or similar) wipe restores the device to factory state and removes app data.

---

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

## 7. Emulator sync service (Purple Team 2025-02-20)

**Context:** `scripts/emulator-sync-service/sync_service.py` exposes GET `/design` and POST `/sync` on 127.0.0.1 for local dev. Any process on the same machine can call it.

**Hardening implemented:**
- **Key allowlist:** POST `/sync` rejects design objects containing any path not in `EMULATOR_TO_PROJECT`. Prevents injection of unexpected keys.
- **Request size limit:** 64KB max body size. Prevents DoS or memory exhaustion.
- **Audit log:** `[SyncServiceAudit] sync_requested applied=N keys=...` printed on each successful sync for audit trail.

**Optional future hardening:** For stricter setups, add an optional API key or token (e.g. `OORB_SYNC_TOKEN` env var) and require it in a header. Document in this file if adopted.

*Added: Security Plan Multi-Agent exercise 2025-02-20.*

---

*For follow-up tasks (e.g. Auto drive privacy, app PIN), see `docs/agents/COMPREHENSIVE_AGENT_TODOS.md` — Security Specialist section.*

---

## 8. When Export (or new sensitive feature) lands (Board-adopted)

When **Export to PDF/CSV** (or another sensitive flow) is added or significantly changed:

- **Security Specialist:** Review FileProvider paths, what's in the file, share scope, and path traversal. Document in this file or in `REVIEW_export.md` (or `REVIEW_<feature>.md`). Update this section with a short summary and link.
- **Red Team:** Run a short Purple exercise targeting that flow (e.g. path traversal, share scope). Document what Blue should check; log to `docs/agents/data-sets/security-exercises/`.
- **Blue Team:** Define what "alarm" means for that surface (what we log or enforce). After Red's action, answer "did the alarm go off?"; if not, document the gap and propose or implement a fix. Every fix should be auditable (file or config we can point to). Where possible, Red re-runs the same attack to verify the fix.
- **User:** Will be emailed when the review and optional Purple exercise are done (Human-in-the-Loop).
