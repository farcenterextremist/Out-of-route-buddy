# Loop blind spots and fortification

**Purpose:** Name the reliability blind spots that can still weaken the loop system even when gates, health checks, and summaries exist.

**Related:** `LOOP_HEALTH_CHECKS.md`, `LOOP_DIAGNOSTIC_SWEEP.md`, `LOOPMASTER_TAB_AND_SPAWN_MODEL.md`, `LOOPMASTER_DESKTOP_GUIDE_SYSTEM.md`, `LOOP_CONTINUITY_TEST_PLAN.md`.

---

## Why this exists

Loop systems usually fail in the gaps between:

- ownership and execution
- health checks and proof
- documentation and actual operator behavior
- mocks and real workflows

This doc keeps those gaps visible so future fortification work stays targeted.

---

## Current blind spots

### 1. Parent-child handoff drift

Risk:

- child workers return outputs in inconsistent shapes
- parent has to interpret vague conclusions
- proof gets weaker during fan-in

Fortification:

- require concise output blocks for workers
- keep `Watcher` independent
- keep one parent authority

### 2. File ownership collisions

Risk:

- multiple workers touch the same file family
- changes conflict silently
- blame and rollback become messy

Fortification:

- assign file-family ownership up front
- treat one-writer-per-file-family as a hard rule
- add contract checks for topology docs

### 3. Health green, proof weak

Risk:

- scripts pass
- tests pass
- but claims remain under-evidenced

Fortification:

- separate `Watcher` from implementation
- require claim -> evidence -> residual risk -> next step
- add proof-oriented review notes to summaries

### 4. Desktop guide drift

Risk:

- desktop copy becomes easier to read than repo docs
- then quietly becomes outdated

Fortification:

- keep a repo source file
- export to desktop
- test the export
- update the guide when topology, health, or portability rules change

### 5. Simulation gap

Risk:

- continuity tests validate text and schema only
- operator workflow changes are not simulated enough

Fortification:

- keep listener simulations
- add export/update simulations
- add future scenario tests for parent/worker evidence flow

### 6. Stale operational memory

Risk:

- latest-state files can exist but contain stale or placeholder data
- future loops may trust them too much

Fortification:

- refresh stale loop_latest files during relevant runs
- name stale shared-state findings explicitly
- avoid "all green" language when placeholders remain

### 7. Portable flywheel not actually portable

Risk:

- guidance works only for this repo
- another app cannot copy the system cleanly

Fortification:

- separate universal operating ideas from repo-specific commands
- keep a cross-project flywheel section in the desktop guide
- add skill support for portability-minded upkeep

### 8. Design sharpening overreach

Risk:

- screenshots exist
- beautification starts to outrun evidence
- UI changes become opinion-driven

Fortification:

- require connected-device evidence first
- use pleasantness scoring
- keep major UI changes approval-gated

---

## Recommended fortification order

1. Protect the source-of-truth docs and desktop export path
2. Protect role topology and parent/child guardrails
3. Protect proof quality, not just health checks
4. Expand simulations around operator behavior and evidence flow
5. Add trend-based watchdogs only after the fundamentals are stable

---

## Research notes applied

This fortification view is informed by cautious research themes:

- supervisor-worker orchestration works best when handoffs are structured
- living operational guides should be versioned and updated from one source
- reliability improves more from balanced fast checks plus a few good integration/simulation checks than from an oversized brittle test stack

---

## Short summary

The biggest loop risks are no longer "missing a script."

They are:

- vague handoffs
- weak proof
- drifting human-readable guidance
- and missing simulations for real operator behavior
