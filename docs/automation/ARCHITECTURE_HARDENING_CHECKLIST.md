# Architecture Hardening Checklist (Production #20)

**Purpose:** Incremental architecture/toolchain hardening checklist that can be advanced across multiple Light/Medium loop runs.

**Tracks:** Production-stage item **#20** (Architecture / major toolchain).

---

## Core checks

| Area | Check | Status |
|------|-------|--------|
| Toolchain baseline | Gradle/Kotlin/AGP versions documented and verified in migration notes | Done (baseline) |
| Lint gate | `:app:lintDebug` passes without new blocking issues | In progress (per run) |
| Unit test gate | `:app:testDebugUnitTest` passes | In progress (per run) |
| Shared-state schema | `loop_shared_events.jsonl` and `loop_latest/<loop>.json` remain valid and updated | In progress (per run) |
| Prompt-gate parity | LOOP_GATES and UNIVERSAL_LOOP_PROMPT remain aligned | In progress (per run) |

---

## Incremental rollout plan

1. **Stabilize gates:** Keep liveness/readiness checks green each run.
2. **Reduce warnings debt:** Triage warning-heavy hotspots in small, safe slices.
3. **Schema/process hardening:** Keep checklist + summaries synchronized with actual behavior.
4. **Regression protection:** Add/maintain focused tests around newly approved production-stage features.

---

## Current run note

- This checklist was created as incremental #20 progress.
- Use one row update per loop summary so progress is visible even when not complete in one go.
- Mini-loop increment (2026-03-13): added focused `MainActivityRobolectricTest` regression coverage for drawer/menu wiring (`drawer_layout`, `nav_view`, `menu_button`, START-gravity) and verified with focused tests + `:app:lintDebug`.
