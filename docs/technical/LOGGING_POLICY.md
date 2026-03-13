# Logging policy (polish)

**Purpose:** Short checklist for log levels, message quality, and PII so logging stays consistent and safe. Ref: PROJECT_AUDIT L1; CRUCIAL_IMPROVEMENTS_TODO §7.

---

## Checklist

1. **Log levels and L1 facade**
   - Use **AppLogger** (see `util/AppLogger.kt`) for hot paths: MainActivity, TripPersistenceManager, OfflineDataManager, PreferencesManager.
   - **AppLogger.d** / **AppLogger.v**: debug/verbose; no-op in release to avoid PII leakage.
   - **AppLogger.w** / **AppLogger.e**: warn/error; logged in all builds; do not include PII in messages.
   - Legacy: Use `Log.d` / `Log.v` for debug and verbose only where AppLogger is not yet used.
   - Use `Log.w` for recoverable issues (e.g. invalid input, fallback used).
   - Use `Log.e` for real errors (exceptions, failed operations).

2. **Message quality**
   - Messages should be actionable: include key IDs or state where helpful (e.g. trip id, screen name).
   - Avoid vague one-word messages; prefer "getTripById failed for id=…" over "Load failed" when context is useful.

3. **No PII or secrets**
   - Do not log coordinates, user identifiers, or other PII in log text.
   - Do not log tokens, API keys, or secrets. Align with [security/SECURITY_NOTES.md](../security/SECURITY_NOTES.md) if present.

4. **Hot paths**
   - Avoid log-per-frame or log-per-tiny-callback in hot paths (e.g. location updates, draw loops).
   - Add logs in hot paths only when they add clear diagnostic value and are gated (e.g. debug build or rate-limited).

---

## Tags

Use a consistent `TAG` per class (e.g. `companion object { private const val TAG = "ClassName" }`) so logcat filtering works.

---

*Part of the Polishing Plan; see [POLISHING_PLAN_PROMPT.md](../archive/prompts/POLISHING_PLAN_PROMPT.md).*
