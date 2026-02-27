# Purple Team Simulation Run Record — 2026-02-24

**Run timestamp:** 2026-02-24 20:51 (local)  
**Command:** `python scripts/purple-team/run_purple_simulations.py --with-http`  
**Prerequisite:** Sync service running on `http://127.0.0.1:8765`

---

## Run Summary

| Phase | Result | Details |
|-------|--------|---------|
| Unit tests | PASS | 7/7 tests passed |
| Validation simulations | PASS | 3/3 passed |
| HTTP attack simulations | PASS | 3/3 passed |

**Overall:** All controls verified. Blue Team alarms would fire for all attack scenarios.

---

## Detailed Results

### Unit tests (test_sync_service.py)

| Test | Result |
|------|--------|
| test_validate_design_keys_allows_known_paths | OK |
| test_validate_design_keys_rejects_unknown_top_level | OK |
| test_validate_design_keys_rejects_unknown_nested | OK |
| test_validate_design_keys_rejects_non_dict | OK |
| test_validate_design_keys_empty_dict | OK |
| test_max_request_body_size | OK |
| test_get_paths_from_dict | OK |

### Validation simulations (no HTTP)

| Attack | Expected | Actual | Passed |
|--------|----------|--------|--------|
| validate_unknown_top_level | reject | reject | Yes |
| validate_unknown_nested | reject | reject | Yes |
| validate_known_paths | accept | accept | Yes |

### HTTP attack simulations

| Attack | Payload / Action | Expected | Actual | Passed |
|--------|------------------|----------|--------|--------|
| valid_design | POST `{"toolbar":{"title":"OOR"},"loadedMiles":{"hint":"Loaded Miles"}}` | 200 | 200 | Yes |
| unknown_key_injection | POST `{"toolbar":{"title":"OOR"},"evil_key":"malicious_value"}` | 400 | 400 | Yes |
| oversized_payload | POST body > 64KB | 413 | 413 | Yes |

---

## Blue Team Detection

- **valid_design:** SyncServiceAudit logs `sync_requested applied=N keys=...` on success
- **unknown_key_injection:** Key allowlist rejects before apply; 400 returned
- **oversized_payload:** Content-Length check returns 413 before body read

---

## Artifacts

- Exercise log: `docs/agents/data-sets/security-exercises/2026-02-24-purple-simulations.md`
- Training data: `docs/agents/data-sets/security-exercises/artifacts/2026-02-24-purple-training.json`
- This run record: `docs/agents/data-sets/security-exercises/artifacts/2026-02-24-purple-run-record.md`
