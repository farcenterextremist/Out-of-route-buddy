# Purple Team Exercise – Security Plan Comprehensive

**exercise_id:** 2025-02-20-security-plan-comprehensive  
**date:** 2025-02-20  
**target:** Sync API, Android service boundaries, export/delete flows  
**mode:** Purple (Red + Blue run together)

---

## Scenario 1: Sync service POST /sync – no key allowlist, no size limit

### Red action – Technical Ninja
- **Role:** Technical Ninja
- **Target:** Sync service POST /sync – `scripts/emulator-sync-service/sync_service.py`
- **Action:** Simulated POST with (1) unexpected keys not in EMULATOR_TO_PROJECT, (2) oversized payload (>64KB). Checked whether key validation or size limit existed.
- **Result:** Partial (code review; no live run). Unknown keys were ignored (only mapped keys processed) but not explicitly rejected. No size limit.
- **Blue visibility:** No – no audit log for sync requests; no rejection of unknown keys or oversized bodies.
- **Artifacts:** `docs/agents/data-sets/security-exercises/artifacts/2025-02-20-attack-surface-summary.md`

### Blue check – Scenario 1
- **Red action reviewed:** Sync service POST /sync – key validation, size limit, audit
- **Alarm went off?** No
- **If no (gap):** No key allowlist (explicit rejection of unknown keys); no request size limit; no audit trail for sync requests.
- **Remediation:** (1) Added `validate_design_keys()` – reject request if design contains any path not in EMULATOR_TO_PROJECT. (2) Added `MAX_REQUEST_BODY_SIZE` (64KB) – return 413 if body exceeds. (3) Added `[SyncServiceAudit] sync_requested applied=N keys=...` print on successful sync.
- **Artifacts:** `scripts/emulator-sync-service/sync_service.py`

---

## Scenario 2: Android service boundaries – trip insert audit

### Red action – Technical Ninja
- **Role:** Technical Ninja
- **Target:** TripRepository.insertTrip(), Trip domain model, InputValidator
- **Action:** Traced input from TripInputViewModel.calculateTrip() through to insertTrip(). Checked if NaN, negative, or out-of-range values are rejected at Trip init, InputValidator, ValidationFramework, and entity validation.
- **Result:** Partial (code review). Trip init throws for invalid values; InputValidator.sanitizeMiles() enforces range; ValidationFramework validates entity. No dedicated audit log for trip insert – defenders could not easily detect bulk or anomalous inserts.
- **Blue visibility:** No – success logged as Log.d(TAG, ...) only; no filterable audit tag for "trip_inserted".
- **Artifacts:** `docs/agents/data-sets/security-exercises/artifacts/2025-02-20-attack-surface-summary.md`

### Blue check – Scenario 2
- **Red action reviewed:** Trip insert flow and validation layers
- **Alarm went off?** No (for audit – validation layers exist but no insert audit)
- **If no (gap):** No dedicated audit log for "trip_inserted" with trip_id and result; defenders could not detect bulk or anomalous inserts.
- **Remediation:** Added `Log.w("TripInsertAudit", "trip_inserted trip_id=$tripId result=true")` in TripRepository after successful insert. Tag `TripInsertAudit` is filterable for log aggregation.
- **Artifacts:** `app/src/main/java/com/example/outofroutebuddy/data/repository/TripRepository.kt`

---

## Scenario 3: Export / Delete (existing 2025-02-22)

Already remediated in 2025-02-22-purple-outofroutebuddy.md:
- TripExportAudit, TripDeleteAudit in TripHistoryViewModel
- FileProvider scope documentation

---

## Summary

| Scenario | Red target | Alarm before? | Blue remediation |
|----------|-----------|---------------|------------------|
| 1 | Sync API POST /sync | No | Key allowlist, 64KB limit, SyncServiceAudit |
| 2 | Trip insert | No | TripInsertAudit in TripRepository |
| 3 | Export/Delete | No (2025-02-22) | TripExportAudit, TripDeleteAudit (existing) |

All remediations implemented. Proof of work: this file; sync_service.py; TripRepository.kt; SECURITY_NOTES.md Sections 6 and 7.
