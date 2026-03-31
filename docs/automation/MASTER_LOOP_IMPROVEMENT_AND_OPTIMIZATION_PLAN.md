# Master Loop Improvement And Optimization Plan

**Purpose:** Make the loop system lighter, more repeatable, and easier to prove by converting the right repeated tasks into reusable scripts while keeping judgment-heavy work with the agent or user.

---

## Loop Architecture Review

- Area: Loopmaster and neighboring loop routines
- Current structure: Strong doc coverage, growing script coverage, but several repeatable run steps are still described in prose more often than they are executed through standard wrappers.
- Drift or weakness: Repeated debugging, readiness proof, diagnostics, and run summaries are not yet normalized into enough reusable commands, which increases variance and token spend.
- Safe next step: Use a script-first approach for deterministic checks while keeping planning, classification, critique, and UI judgment human/agent-led.
- Required tests or checks: `run_simple_debug_cleanup.ps1`, `pulse_check.ps1`, `loop_health_check.ps1`, `run_loop_continuity_tests.ps1`, targeted doc updates, and lint review on changed files.
- Approval level: Medium for docs plus non-destructive automation wiring. Heavy only if future work changes gate semantics or auto-executes risky build/deployment actions.

---

## Goals

1. Reduce loop repetition without reducing loop quality.
2. Make proof-of-work easier to generate and compare between runs.
3. Lower token cost by replacing repeated instruction text with stable scripts.
4. Keep dangerous or judgment-heavy steps outside blind automation.

## Guardrails

- Do not automate architectural decisions, Heavy-tier approvals, or UI changes that still need taste/judgment.
- Prefer additive wrappers over replacing proven scripts.
- New automation should emit simple, grep-friendly output and be safe to rerun.
- Scripts should support dry-run or low-risk modes whenever practical.
- One script should do one repeatable job well instead of becoming a giant do-everything loop blob.

---

## What Gets Automated

| Category | Keep manual / agent-led | Good candidate for scripting |
|----------|--------------------------|------------------------------|
| Health and readiness | Deciding whether failure blocks the run | Running tests, lint, detekt, continuity checks |
| Diagnostics | Interpreting root cause and prioritizing fixes | Collecting lints, known failing tests, hotspot searches, report paths |
| Loop bookkeeping | Writing final judgment and recommendations | Creating run IDs, timestamps, baseline summaries, proof file paths |
| Cross-loop sharing | Deciding what matters from another loop | Reading latest-state files, validating schema, writing standardized outputs |
| Documentation hygiene | Deciding wording and policy changes | Checking required sections, broken links, stale references |
| Build cleanup | Deciding when a rebuild is warranted | Clean, assemble, test, lint, detekt wrappers |

---

## Repeatable Tasks To Lighten

| Task | Current state | Target improvement | Tier |
|------|---------------|-------------------|------|
| Readiness proof for substantial runs | Split between `pulse_check.ps1` and manual Gradle commands | Standardize on `run_simple_debug_cleanup.ps1` for deeper proof and `pulse_check.ps1` for lightweight pulses | Light |
| Phase-end pulse evidence | Present but limited to tests and lint by default | Keep lightweight default; optionally use `pulse_check.ps1 -UseSimpleDebugCleanup` when automation/build changes occur | Light |
| Diagnostic baseline collection | Mostly doc-driven and manual | Create a future `run_loop_diagnostic_baseline.ps1` wrapper for lints, ignored-tests snapshot, hotspot scan, and residual-risk prompts | Medium |
| Loop start research checklist | Mostly prose in routine docs | Create a future `invoke_loop_start_checklist.ps1` that prints required docs, health commands, and latest-state file locations | Light |
| Run summary scaffolding | Manual summary writing each run | Create a future summary template generator with placeholders for consistency score, diagnostic sweep, proof, and next steps | Medium |
| Cross-loop proof validation | Partially scripted today | Expand continuity/shared-state checks into one future `run_loop_governance_suite.ps1` wrapper | Medium |
| Report-path surfacing | Scattered across scripts | Standardize common report output blocks across loop scripts | Light |
| Stale artifact cleanup | Usually manual judgment | Add an opt-in future cleanup helper for safe report/build artifact pruning only | Medium |

---

## Scripts Already Helping

- `scripts/automation/pulse_check.ps1`
  Lightweight readiness pulse for normal phase endings.
- `scripts/automation/run_simple_debug_cleanup.ps1`
  Consolidated deeper debugging pass for tests, lint, and detekt.
- `scripts/automation/loop_health_check.ps1`
  Fast liveness gate so loops do not start blind.
- `scripts/automation/run_loop_continuity_tests.ps1`
  Contract proof for loop/gate/shared-state edits.

## Changes Made In This Pass

- `pulse_check.ps1` now supports `-UseSimpleDebugCleanup` so loops can opt into the consolidated debug sweep without changing the default lightweight behavior.
- `run_120min_loop.ps1` now supports `-UseSimpleDebugCleanupFirstPulse` so a substantial run can front-load a deeper proof pass and then stay lighter afterward.
- Improvement-loop docs now explicitly describe when to use the lightweight pulse versus the deeper consolidated debug sweep.

---

## Rollout Plan

### Slice 1: Normalize the debugging path

- Keep `pulse_check.ps1` as the default phase-end pulse.
- Use `run_simple_debug_cleanup.ps1` once early in substantial runs.
- Record which mode was used in pulse logs and listener metrics.

### Slice 2: Add script wrappers for repeated evidence gathering

- Create a diagnostic baseline wrapper for lints, ignored tests, hotspots, and known report paths.
- Create a governance wrapper that runs continuity and shared-state contract checks together.

### Slice 3: Standardize loop output scaffolding

- Generate run-summary starter blocks with required sections.
- Standardize proof blocks so improvement, token, cyber, and synthetic loops are easier to compare.

### Slice 4: Prune and consolidate

- Remove duplicate loop commands once wrappers are proven stable.
- Move repeated command examples out of docs when a single maintained script can replace them.

---

## Priority Backlog

1. Add a `run_loop_diagnostic_baseline.ps1` script for repeatable non-destructive diagnostics.
2. Add a `run_loop_governance_suite.ps1` wrapper for continuity, shared-state, and contract validation.
3. Add a run-summary template generator so ledger and summary sections stay consistent.
4. Add an opt-in safe artifact cleanup helper for stale reports and build outputs.
5. Benchmark which loop scripts are actually used so dead or overlapping automation can be trimmed.

---

## Success Measures

- Fewer ad hoc Gradle commands typed during loops.
- More consistent pulse logs and proof blocks between runs.
- Lower token spend because repeated operational instructions move into stable scripts.
- Less drift between what loop docs say and what operators actually run.

---

## Recommended Usage

For a normal run:

```powershell
.\scripts\automation\pulse_check.ps1 -Note "Phase N"
```

For a substantial run with build, lint, or automation changes:

```powershell
.\scripts\automation\run_simple_debug_cleanup.ps1
.\scripts\automation\pulse_check.ps1 -UseSimpleDebugCleanup -Note "Phase 1: consolidated debug cleanup"
```

For the timer-based 120-minute loop when you want one deeper first pulse:

```powershell
.\scripts\automation\run_120min_loop.ps1 -UseSimpleDebugCleanupFirstPulse
```
