# Loop audit — wiring and build/tests

**Date:** 2026-03-12  
**Scope:** Improvement Loop (and universal loop rules) wiring, doc references, build, unit tests.

---

## 1. Build and tests

| Check | Result |
|-------|--------|
| **Build** `.\gradlew.bat assembleDebug --no-daemon` | **PASS** (exit 0) |
| **Unit tests** `.\gradlew.bat :app:testDebugUnitTest --no-daemon` | **PASS** (exit 0) |

No code changes were made; audit is doc-only. Build and tests confirm the project compiles and unit tests pass.

---

## 2. Doc wiring (references and existence)

### Required loop docs (Phase 0.1 / universal rules)

| Doc | Exists | Referenced from |
|-----|--------|------------------|
| `docs/automation/LOOP_LESSONS_LEARNED.md` | Yes | IMPROVEMENT_LOOP_ROUTINE Phase 0.1, UNIVERSAL_LOOP_PROMPT, IMPROVEMENT_LOOP_FOR_OTHER_AGENTS, universal-loop.mdc |
| `docs/automation/SELF_IMPROVING_LOOP_RESEARCH.md` | Yes | Same as above |
| `docs/automation/CURSOR_SELF_IMPROVEMENT.md` | Yes | Same; Phase 0.3 |
| `docs/automation/LOOP_MASTER_ROLE.md` | Yes | UNIVERSAL_LOOP_PROMPT, IMPROVEMENT_LOOP_ROUTINE trigger, universal-loop.mdc |
| `docs/agents/data-sets/hub/README.md` | Yes | UNIVERSAL_LOOP_PROMPT, IMPROVEMENT_LOOP_ROUTINE § Apply to your loop |
| `docs/agents/data-sets/hub/UNIVERSAL_LOOP_PROMPT.md` | Yes | Loop Master Step 0.M, IMPROVEMENT_LOOP_ROUTINE, IMPROVEMENT_LOOP_FOR_OTHER_AGENTS, LOOP_TIERING, LOOPS_AND_IMPROVEMENT_FULL_AXIS |
| `docs/automation/LOOP_TIERING.md` | Yes | Routine Phase 0.1b, UNIVERSAL_LOOP_PROMPT, IMPROVEMENT_LOOP_BEST_PRACTICES, HEAVY_IDEAS_FAVORITES |
| `docs/automation/HEAVY_IDEAS_FAVORITES.md` | Yes | IMPROVEMENT_LOOP_ROUTINE Phase 0.1 table and § 4.3 item 11, LOOP_TIERING Medium row, IMPROVEMENT_LOOP_INDEX § 4 |

All required self-improvement/loop-improvement and Hub/Loop Master docs exist and are linked consistently.

### HEAVY_TIER_IDEAS vs HEAVY_IDEAS_FAVORITES

| File | Status |
|------|--------|
| `HEAVY_IDEAS_FAVORITES.md` | **Canonical** — used in IMPROVEMENT_LOOP_ROUTINE, LOOP_TIERING (Medium + Heavy cap), IMPROVEMENT_LOOP_INDEX § 4 |
| `HEAVY_TIER_IDEAS.md` | **Present** in `docs/automation/` (legacy or duplicate). No references found in routine, tiering, or index; index § 4 points to HEAVY_IDEAS_FAVORITES. Safe to leave as-is or remove/redirect later. |

### UNIVERSAL_LOOP_PROMPT

- Duplicate table row **"Full prompt (this file)"** in Paths table was removed (one row kept).
- "Apply to your loop" section and six obligations are consistent with universal-loop.mdc and IMPROVEMENT_LOOP_ROUTINE.

---

## 3. Loop flow wiring

| Step | Wired |
|------|--------|
| Trigger **GO** / **start master loop** | IMPROVEMENT_LOOP_ROUTINE; LOOP_MASTER_ROLE Step 0.M (read Hub, then run Improvement Loop) |
| Phase 0.1 research | Includes self-improvement/loop-improvement (LOOP_LESSONS_LEARNED, SELF_IMPROVING_LOOP_RESEARCH, CURSOR_SELF_IMPROVEMENT); Hub (Apply to your loop); CRUCIAL, FAILING_OR_IGNORED_TESTS, etc. |
| Phase 0.1b tiering | LOOP_TIERING; Light/Medium auto-implement; Heavy = approval; drastic loop improvements = Heavy |
| Phase 2.3 sandboxing | SANDBOX_TESTING, SANDBOX_COMPLETION_PERCENTAGE (optional), HEAVY_IDEAS_FAVORITES / FUTURE_IDEAS |
| Phase 4.3 summary | LOOP_METRICS_TEMPLATE; File Organizer recommended new ideas (cap 50); append RUN_LEDGER; optional send to hub |
| Universal rule (any loop) | UNIVERSAL_LOOP_PROMPT + universal-loop.mdc: Hub, research self-improvement/loop-improvement, auto Light/Medium, drastic = Heavy, slop-minimize, send to hub when done |

---

## 4. Findings and fixes applied

1. **UNIVERSAL_LOOP_PROMPT.md** — Removed duplicate "Full prompt (this file)" row from the Paths table.
2. **Build** — Succeeds; no changes made to code.
3. **Unit tests** — All pass; no changes made to code.
4. **HEAVY_TIER_IDEAS.md** — File still present; all wiring uses HEAVY_IDEAS_FAVORITES. No fix required; optional future cleanup (redirect or delete HEAVY_TIER_IDEAS if unused).

---

## 5. Summary

| Area | Status |
|------|--------|
| Build | PASS |
| Unit tests | PASS |
| Loop doc references | All required docs exist; references consistent |
| Universal rules (research, tiers, Hub) | Wired in UNIVERSAL_LOOP_PROMPT, universal-loop.mdc, IMPROVEMENT_LOOP_ROUTINE, IMPROVEMENT_LOOP_FOR_OTHER_AGENTS, LOOP_TIERING, LOOPS_AND_IMPROVEMENT_FULL_AXIS |
| Sandbox / Heavy | HEAVY_IDEAS_FAVORITES used throughout; HEAVY_TIER_IDEAS present but unused in current wiring |
| Minor fix | Duplicate row removed from UNIVERSAL_LOOP_PROMPT Paths table |

---

*Audit complete. Re-run after major loop or doc changes.*
