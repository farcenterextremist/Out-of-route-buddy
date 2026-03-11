# Startup and database failure

**Purpose:** Document user-visible behavior when Room or critical initialization fails. Ref: Blind Spot Plan §7.

---

## When DB or critical init fails

- **Reporting:** `OutOfRouteApplication` exposes `isHealthy()` and `getDatabaseError()`. If the database (or other critical init such as preferences) fails, `isDatabaseInitialized` is false and `databaseError` is set; `isHealthy()` returns false.
- **User-visible behavior:** `MainActivity` catches exceptions during initialization (e.g. when testing database connection or navigation setup) and calls `showInitializationError(error)`. The user sees an initialization error state (no crash; error is logged and reported to Crashlytics).
- **Recovery:** The user can **clear app data** (Settings → Apps → OutOfRouteBuddy → Clear storage) or **reinstall** the app. There is no in-app "retry" or "clear data and continue" flow; recovery is manual.

---

## Manual test (optional)

- **Corrupt or delete DB file;** launch app; expect an error message and no crash. (DB path is app-private; use root or adb run-as to remove or corrupt the file for testing.)

---

*See [RECOVERY_WIRING.md](RECOVERY_WIRING.md) for trip recovery; [SECURITY_NOTES.md](../security/SECURITY_NOTES.md) for data and backup.*
