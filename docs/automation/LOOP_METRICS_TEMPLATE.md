# Improvement Loop — Metrics Template

**Purpose:** Define which metrics to capture per run to close the improvement loop. Research: "Close the loop with measurable checks."

**References:** [IMPROVEMENT_LOOP_RESEARCH_2025-03.md](./IMPROVEMENT_LOOP_RESEARCH_2025-03.md), [IMPROVEMENT_LOOP_ROUTINE.md](./IMPROVEMENT_LOOP_ROUTINE.md)

---

## Required Metrics (Every Run)

| Metric | Source | How to capture |
|--------|--------|----------------|
| **Test count** | `.\gradlew.bat :app:testDebugUnitTest` | Pass/fail from output; total count |
| **Lint** | `.\gradlew.bat :app:lintDebug` | Errors / warnings from report |
| **Files changed** | Git | `git diff --stat` or `git status --short` count |
| **Focus area** | This run | Security, UI/UX, Shipability, Code Quality, File Structure, Data/Metrics |
| **Variant** | Trigger | Quick (30 min), Standard (90 min), Full (2 hr) |
| **Sandboxing status** | Phase 2.4 | Pass/fail + action + artifact path (required for main loop runs) |

---

## Optional Metrics (When Available)

| Metric | Source | How to capture |
|--------|--------|----------------|
| **Coverage %** | `.\gradlew.bat jacocoSuite` | From report `app/build/reports/jacoco/` |
| **Build time** | Gradle | `.\gradlew.bat assembleDebug --profile` |
| **Checkpoint** | Pre-loop | Commit hash or tag name for revert |
| **User metadata** | App (opt-in) | If metadata collection/display was added: note in summary. See [USER_METADATA_USAGE_GUIDE.md](./USER_METADATA_USAGE_GUIDE.md). |

---

## Before/After Tracking

Capture baseline **before** any changes; compare **after** loop:

| Metric | BEFORE | AFTER | Delta |
|--------|--------|-------|-------|
| Tests | pass/fail count | pass/fail count | No change / improved / regressed |
| Lint | status | status | — |
| Build | status | status | — |

---

## Summary Block Format

Copy into every summary:

```markdown
## Metrics

| Metric | Value |
|--------|-------|
| Tests | Pass / Fail (count) |
| Lint | 0 errors, X warnings |
| Files changed | N |
| Focus | [Security \| UI/UX \| Shipability \| Code Quality \| File Structure \| Data/Metrics] |
| Variant | [Quick \| Standard \| Full] |
| Sandboxing | [Pass/Fail], action, artifact path |
| Checkpoint | `commit` or `tag` |
```

---

## Neat Details Block (for next-loop report)

Use this block at the end of each loop summary so users get the richer view they asked for.

```markdown
## Loop Effectiveness

- Planned tasks completed: X/Y
- Most effective action: ...
- Biggest blocker: ...

## Loop Efficiency Score

- Efficiency score: X/100
- Progress bar: [################----] XX%
- Grade: [A | B | C]
- Why it moved: [1 short sentence tied to loop design or automation hardening]

## Useful Data Generated

- [artifact/path] — why it is useful/reusable
- [artifact/path] — why it is useful/reusable

## Loop Performance & Health

- Liveness checks: pass/fail (count)
- Readiness checks: pass/fail (tests/lint)
- Test duration: ...
- Lint duration: ...
- Gate status (LOOP GATES): start pass / end pass

## Interesting Metrics

- Warnings delta: ...
- Hub artifacts added this run: ...
- Shared-state freshness (`loop_latest/<loop>.json`): ...
- Production-stage incremental progress count: ...
```

### Loop efficiency scoring guide

Use the automation score when the loop design itself changes:

- **Run contract wiring** — stable `run_id` propagation across listeners/runners
- **Gate wrappers** — reusable start/finish wrappers in use
- **Shared-state audit** — drift check is clean
- **Continuity protection** — continuity suite is green
- **Health signals** — liveness + pulse/readiness evidence present
- **Documentation alignment** — docs reflect the current automation helpers

Script:

```powershell
.\scripts\automation\measure_loop_efficiency.ps1
```

State output:
- `docs/automation/loop_efficiency_state.json`

Persistence:
- `scripts/automation/finish_loop_run.ps1` now writes the `## Loop Efficiency Score` block into the summary automatically.
- `scripts/automation/write_loop_efficiency_block.ps1` keeps the block idempotent for the same `run_id`, so reruns replace the block instead of appending duplicates.

---

## Proof of Quality Block (required)

Use this in every loop summary. No A-grade without this evidence.

```markdown
## Proof of Quality

- Standards used: ISO/IEC 25010 + DORA + SRE golden signals (project-adapted)
- Liveness evidence: [command + result]
- Readiness evidence: [tests command/result], [lint command/result]
- Change evidence: [key files/tests proving behavior]
- Residual risks: [known risks and why acceptable this run]
- Traceability: [summary path], [ledger path], [shared-state files updated]
```

Reference: `docs/automation/QUALITY_STANDARDS_AND_PROOF.md`

---

## File Organizer Use

File Organizer uses metrics to recommend next focus (e.g., "Lint warnings increased → next focus: Code Quality").
