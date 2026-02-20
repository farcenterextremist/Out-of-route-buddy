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
- **Do not log PII:** Avoid logging full coordinates, trip IDs that could be linked to a user, or other PII. Prefer log levels and tags only (e.g. "sync started", "trip saved") or non-identifying metrics.
- **Encryption at rest:** Android app-private storage is already protected by the OS (per-app isolation; on many devices, encryption at rest is enabled). For extra hardening, consider EncryptedSharedPreferences or SQLCipher for the DB if handling higher-sensitivity data in the future.
- **Future back-ends:** If trip or location data are ever sent to a server, use HTTPS only and avoid logging request/response bodies that contain PII.

---

## 3. Coordinator email — .env and last_reply.txt

**Verification:**
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

*For follow-up tasks (e.g. Auto drive privacy, app PIN), see `docs/agents/COMPREHENSIVE_AGENT_TODOS.md` — Security Specialist section.*
