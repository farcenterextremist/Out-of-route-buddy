# Loop health check — 2026-03-12

**Purpose:** Verify loop wiring, build, tests, and overall loop health. Run periodically or when changing automation docs.

---

## 1. Loop wiring

| Check | Status | Notes |
|-------|--------|-------|
| IMPROVEMENT_LOOP_ROUTINE.md | OK | Phase 0.0c (Read Hub), 0.1 (research + LOOP_LESSONS_LEARNED, SELF_IMPROVING_LOOP_RESEARCH, CURSOR_SELF_IMPROVEMENT), Phase 4.3c (consider send to hub) present |
| LOOP_MASTER_ROLE.md | OK | Exists in docs/automation |
| IMPROVEMENT_LOOP_RUN_LEDGER.md | OK | Exists; template and run blocks present |
| UNIVERSAL_LOOP_PROMPT.md | OK | hub/UNIVERSAL_LOOP_PROMPT.md exists; "Apply to your loop" + six obligations |
| Hub README + SEND_TO_HUB_PROMPT | OK | hub/README.md, hub/SEND_TO_HUB_PROMPT.md exist |
| DATA_USEFULNESS_AND_PRUNING_RESEARCH.md | OK | In Phase 0.1 research table |
| USER_PREFERENCES_AND_DESIGN_INTENT.md | OK | Referenced 0.0a |
| LOOP_TIERING, COMMON_SENSE, REASONING, FOR_OTHER_AGENTS | OK | All referenced in routine; files exist |
| AGENTS.md → Improvement Loop + Send to hub | OK | Links to routine, ledger, hub |

---

## 2. Scripts

| Script | Status | Notes |
|--------|--------|-------|
| pulse_check.ps1 | OK | scripts/automation |
| loop_listener.ps1 | OK | scripts/automation |
| test_loop_listener.ps1 | OK | **PASS** — 5 events recorded, valid JSON, loop_start/loop_end present |

---

## 3. Build & tests

| Check | Status | Notes |
|-------|--------|-------|
| `.\gradlew.bat assembleDebug --no-daemon` | **PASS** | Exit code 0 |
| `.\gradlew.bat :app:testDebugUnitTest --no-daemon` | **PASS** | Exit code 0 |

---

## 4. Hub index

- Hub index in README lists 6 files (file-organizer, token-loop, data-organized, cyber-security x3, Codey).
- Links use `./` relative paths; files in same folder.

---

## 5. Summary

- **Wiring:** Core loop docs, Hub, and Universal Loop Prompt are in place and cross-linked. Phase 0.0c (Read Hub) and 4.3c (consider send to hub) are in the routine.
- **Scripts:** Listener and test_loop_listener pass.
- **Build & tests:** Green.

**Next:** Run a full Improvement Loop (GO) or pulse_check.ps1 when ready; use this file as a baseline for future health checks.
