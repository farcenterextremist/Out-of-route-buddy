# Improvement Loop — OutOfRouteBuddy

**Objective:** Obvious improvements only. No drastic frontend changes. Sharpen, pop, beautify, or add useful information where clearly beneficial.  
**Strategy:** Slow, decisive, low-risk. Use subagents for parallelizable work. Call on more capable models for complex reasoning.  
**Trigger:** Run when user says **GO**.

---

## Phase 0: Pre-loop (before any changes)

- **Checkpoint:** `git add -A && git commit -m "Pre-improvement-loop checkpoint YYYY-MM-DD"` or `git tag improvement-loop-pre-$(date +%Y%m%d-%H%M)`. Record commit hash or tag in summary for revert.
- **Research note:** One-line: design intent, last loop, this focus. Add to summary.

---

## Overview

| Phase | Time | Focus | Subagent use |
|-------|------|-------|--------------|
| 0 | — | Checkpoint, research note | — |
| 1 | 0–30 min | Quick wins: dead code, unused constants, obvious fixes | Shell (tests), GeneralPurpose (code search) |
| 2 | 30–60 min | Test health, documentation alignment | GeneralPurpose (test fixes), Shell (run tests) |
| 3 | 60–90 min | UI polish: sharpen, pop, useful info (minimal) | GeneralPurpose (UI tweaks) |
| 4 | 90–120 min | Final pulse, lint, summary | Shell (pulse, lint) |

---

## Phase 1: Quick Wins (0–30 min)

**Goal:** Low-risk, obvious improvements from REDUNDANT_DEAD_CODE_REPORT and CRUCIAL_IMPROVEMENTS.

### 1.1 BuildConfig alignment (5–10 min)

- **Task:** Align `TripTrackingService` with `BuildConfig` constants (ACTION_START_TRIP, ACTION_END_TRIP, NOTIFICATION_CHANNEL_ID) per REDUNDANT_DEAD_CODE_REPORT §1.
- **Action:** Either refactor TripTrackingService to use BuildConfig, or document in BuildConfig README that these are "future use" and remove misleading references.
- **Subagent:** GeneralPurpose — "In OutOfRouteBuddy, align TripTrackingService with BuildConfig constants per docs/REDUNDANT_DEAD_CODE_REPORT.md §1. Use BuildConfig.ACTION_START_TRIP, ACTION_END_TRIP, NOTIFICATION_CHANNEL_ID in TripTrackingService. Attach: app/src/main/java/.../services/TripTrackingService.kt, app/src/main/java/.../core/config/BuildConfig.kt."

### 1.2 Remove obviously dead private members (10–15 min)

- **Task:** Remove or suppress only the **safest** dead code from REDUNDANT_DEAD_CODE_REPORT §2:
  - `TripRecoveryDialog.TAG` (unused constant — remove or use)
  - `Trip.kt MIN_ACTUAL_MILES` (unused — remove if no tests reference)
  - `CustomCalendarDialog.normalizeToStartOfDay`, `isSelectionLocked` — verify no callers, then remove or add @Suppress with reason
- **Action:** Grep for each before removing. Do NOT touch injected params (OfflineDataManager, TripInputViewModel) — those need DI review.
- **Subagent:** GeneralPurpose — "In OutOfRoutebuddy, remove only the safest dead code from docs/REDUNDANT_DEAD_CODE_REPORT.md §2: TripRecoveryDialog.TAG, Trip.kt MIN_ACTUAL_MILES. First grep to confirm no references. Do NOT touch injected constructor params."

### 1.3 Pulse check (end of Phase 1)

- Run: `.\scripts\automation\pulse_check.ps1 -Note "Phase 1: BuildConfig alignment, dead code cleanup"`
- Ensure tests still pass.

---

## Phase 2: Test Health & Documentation (30–60 min)

**Goal:** Fix or document one obvious test gap; align docs.

### 2.1 Test health triage (15–20 min)

- **Task:** Per FAILING_OR_IGNORED_TESTS.md — ensure all @Ignore tests have a clear reason and owner. Add a one-line doc update if any test was recently fixed.
- **Action:** Review LocationValidationServiceTest ignored test; add @Suppress or improve assertion if trivial. Do NOT spend >10 min on any single test.
- **Subagent:** GeneralPurpose — "In OutOfRoutebuddy, review app/src/test/.../LocationValidationServiceTest.kt. The test 'validateVehicleLocation with good vehicle data returns Valid' is ignored. Either fix the assertion (if trivial) or ensure @Ignore has a clear reason. Do not spend more than 10 minutes. Attach: docs/qa/FAILING_OR_IGNORED_TESTS.md."

### 2.2 Documentation cross-links (10 min)

- **Task:** CRUCIAL_IMPROVEMENTS §8 — ensure CRUCIAL_IMPROVEMENTS_TODO.md is linked from docs/README.md and docs/agents/WORKER_TODOS_AND_IDEAS.md. Quick verification only.
- **Action:** Grep for "CRUCIAL_IMPROVEMENTS" in docs; add link if missing.

### 2.3 Run full unit test suite (5 min)

- **Subagent:** Shell — "cd c:\Users\brand\OutofRoutebuddy && .\gradlew.bat :app:testDebugUnitTest --no-daemon"
- Log result to pulse.

### 2.4 Pulse check (end of Phase 2)

- Run: `.\scripts\automation\pulse_check.ps1 -Note "Phase 2: Test health, doc links"`

---

## Phase 3: UI Polish — Minimal (60–90 min)

**Goal:** Sharpen, pop, beautify, or add useful information. NO layout/flow changes unless critical.

### 3.1 Strings and copy (10 min)

- **Task:** Review strings.xml for one clear improvement: typo fix, accessibility hint, or clearer label per TERMINOLOGY_AND_COPY.md.
- **Action:** One small copy tweak only. Example: add contentDescription to a key icon if missing.

### 3.2 Stat card / trip history visual polish (15–20 min)

- **Task:** Per UI_CONSISTENCY.md — ensure stat cards use 6dp elevation, 12dp corner radius, 16dp padding. If already correct, consider one subtle improvement: slightly increase contrast for OOR miles, or add a subtle divider.
- **Action:** Read item_trip_history_stat_card.xml; if values are off, align. Otherwise, one small color or spacing tweak (e.g. text_secondary for metadata).
- **Subagent:** GeneralPurpose — "In OutOfRoutebuddy, review app/src/main/res/layout/item_trip_history_stat_card.xml. Ensure it follows docs/ux/UI_CONSISTENCY.md: 6dp elevation, 12dp corner radius, 16dp padding. If already correct, suggest ONE subtle improvement (e.g. contrast for OOR value, or divider). Do NOT change layout structure. Attach: docs/ux/UI_CONSISTENCY.md."

### 3.3 Useful information (10 min)

- **Task:** Add one small piece of useful info to the UI: e.g. "Last updated" on a settings row, or a hint in TripInputFragment for period mode. Must be non-intrusive.
- **Action:** Pick ONE location. Example: SettingsFragment — add a small "Version X.X" or "Last sync: never" if it helps. Or TripInputFragment — ensure period mode label is clear.
- **Constraint:** User rule: "DO NOT MAKE ANY UNWARRANTED CHANGES TO THE UI WITHOUT MY PERMISSION" — so this must be obviously useful, not decorative.

### 3.4 Pulse check (end of Phase 3)

- Run: `.\scripts\automation\pulse_check.ps1 -Note "Phase 3: UI polish, strings, useful info"`

---

## Phase 4: Final Pulse & Summary (90–120 min)

**Goal:** Lint, final test run, write summary.

### 4.1 Lint run (5 min)

- **Subagent:** Shell — "cd c:\Users\brand\OutofRoutebuddy && .\gradlew.bat :app:lintDebug --no-daemon"
- Fix only **obvious** new lint issues introduced this session. Do not refactor for pre-existing warnings.

### 4.2 Final pulse (5 min)

- Run: `.\scripts\automation\pulse_check.ps1 -Note "Phase 4: Final. Lint reviewed."`

### 4.3 Write improvement summary (10 min)

- **Output:** `docs/automation/IMPROVEMENT_LOOP_SUMMARY_<date>.md` (or `120_MINUTE_LOOP_SUMMARY_<date>.md` for compatibility)
- **Contents (A-grade format):**
  - Phase 0 research note (one-line: design intent, last loop, this focus)
  - What was done (checkboxes from this plan)
  - What was skipped and why
  - Files modified
  - Build status
  - **Metrics block** — Tests, Lint, Files changed, Focus, Variant, Checkpoint (see [LOOP_METRICS_TEMPLATE.md](./LOOP_METRICS_TEMPLATE.md))
  - Suggested next steps (actionable: include commands where helpful)
  - **Quality Grade** — A/B/C with rationale and one improvement for next run

---

## Subagent Spawn Reference

When user says **GO**, the host (main session) should:

1. **Phase 1:** Spawn 2 subagents in parallel:
   - GeneralPurpose: BuildConfig + TripTrackingService alignment
   - GeneralPurpose: Safe dead code removal (TAG, MIN_ACTUAL_MILES)
   - Meanwhile: Host can do manual grep/verification

2. **Phase 2:** Spawn:
   - GeneralPurpose: LocationValidationServiceTest @Ignore review
   - Shell: Run unit tests (after Phase 1 changes land)

3. **Phase 3:** Spawn:
   - GeneralPurpose: Stat card layout review + one subtle improvement

4. **Phase 4:** Spawn:
   - Shell: Lint run

**Model selection:** Use default/fast for Shell tasks. Use a more capable model for GeneralPurpose when the task requires code reasoning (e.g. test fix, BuildConfig alignment).

---

## Files to Attach When Spawning Subagents

| Task | Attachments |
|------|-------------|
| BuildConfig alignment | TripTrackingService.kt, BuildConfig.kt, REDUNDANT_DEAD_CODE_REPORT.md |
| Dead code removal | REDUNDANT_DEAD_CODE_REPORT.md, TripRecoveryDialog.kt, Trip.kt (models) |
| Test health | LocationValidationServiceTest.kt, FAILING_OR_IGNORED_TESTS.md |
| Stat card polish | item_trip_history_stat_card.xml, UI_CONSISTENCY.md |

---

## Out of Scope (Do Not Do)

- OfflineDataManager load/save implementation (CRUCIAL §2) — too large
- Location jump detection (CRUCIAL §3) — requires design
- Trip history → TripDetails navigation (CRUCIAL §4) — UI flow change
- Statistics monthly-only refactor (CRUCIAL §9) — user approval needed
- Gradle 9 migration — too large for 2 hours
- New features or major refactors

---

*Created for a focused 2-hour improvement run. Run when user says GO.*
