# Purple Team Simulation — OutOfRouteBuddy

**exercise_id:** 2026-02-24-purple-simulations
**date:** 2026-02-24
**time:** 20:51
**target:** Sync service (key allowlist, size limit, validation); unit tests
**mode:** Purple (automated Red simulations + Blue detection check)

---

## Scenario 1: Unit tests (sync_service validation)

### Red action – Technical Ninja
- **Role:** Technical Ninja
- **Target:** sync_service.py validate_design_keys, MAX_REQUEST_BODY_SIZE
- **Action:** Run test_sync_service.py — tests key allowlist, unknown key rejection, size limit
- **Result:** Success
- **Blue visibility:** Yes — unit tests verify controls
- **Artifacts:** scripts/emulator-sync-service/test_sync_service.py

### Blue check – Scenario 1
- **Red action reviewed:** Unit test suite
- **Alarm went off?** Yes
- **What detected it:** test_validate_design_keys_* tests
- **Remediation:** N/A (controls working)

---

## Scenario 2: Validation logic (no HTTP)

### validate_unknown_top_level
- **Expected:** reject | **Actual:** reject | **Passed:** True
- **Blue visibility:** validate_design_keys in sync_service

### validate_unknown_nested
- **Expected:** reject | **Actual:** reject | **Passed:** True
- **Blue visibility:** validate_design_keys in sync_service

### validate_known_paths
- **Expected:** accept | **Actual:** accept | **Passed:** True
- **Blue visibility:** Key allowlist allows known paths

---

## Scenario 3: HTTP attack simulations

### valid_design
- **Expected:** 200 | **Actual:** 200 | **Passed:** True
- **Blue visibility:** SyncServiceAudit would log on success

### unknown_key_injection
- **Expected:** 400 | **Actual:** 400 | **Passed:** True
- **Blue visibility:** Key allowlist correctly rejected

### oversized_payload
- **Expected:** 413 | **Actual:** 413 | **Passed:** True
- **Blue visibility:** Size limit correctly rejected

---

## Summary

- Unit tests: PASS (7/7)
- Validation simulations: 3/3 passed
- HTTP simulations: 3/3 passed

**Run record:** See [artifacts/2026-02-24-purple-run-record.md](artifacts/2026-02-24-purple-run-record.md) for detailed payloads and timestamps.
