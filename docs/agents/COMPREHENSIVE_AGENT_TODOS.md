# Comprehensive todo list by agent

**Created:** 2025-02-19  
**Source:** User-approved crucial improvements (`docs/CRUCIAL_IMPROVEMENTS_TODO.md`), worker todos and ideas (`docs/agents/WORKER_TODOS_AND_IDEAS.md`), and user preference: **statistics section — monthly only (remove weekly and yearly)**.

Each agent has a single checklist. When a task is done, check it off here and in the source doc if applicable.

---

## Project Design / Creative Manager

**Agent card:** `docs/agents/roles/design-creative-manager.md`  
**Data set:** `docs/agents/data-sets/design-creative.md`

- [ ] **CRUCIAL #6** — Create `docs/product/ROADMAP.md`: high-level themes and "what's next" (align with WORKER_TODOS_AND_IDEAS where useful).
- [ ] **CRUCIAL #6** — Create at least one `docs/product/FEATURE_BRIEF_<name>.md` (e.g. Auto drive or Offline persistence) so Back-end, QA, and UI/UX have a single source of truth.
- [ ] Draft a short product brief for **Auto drive detected** mode (value, when to use it).
- [ ] Prioritize next 3 features after emulator polish and IMAP (e.g. Auto drive, Reports screen, History improvements).
- [ ] Align workdays (Sunday 3–4 hr) with a biweekly "sprint" note so the team knows what to batch.

**One idea (backlog):** Driver check-in prompts — After a trip ends, optionally prompt once: "How was the route?" (thumbs up/down or optional note).

---

## UI/UX Specialist

**Agent card:** `docs/agents/roles/ui-ux-specialist.md`  
**Data set:** `docs/agents/data-sets/ui-ux.md`

- [ ] **CRUCIAL #9 (Statistics: monthly only)** — Update statistics section spec: one aggregate view (monthly). Remove any wireframe/spec for weekly or yearly. Document in `docs/ux/` or a short note for Front-end and QA.
- [ ] Propose wireframe for **Auto drive detected** mode/button: where it lives (toolbar vs trip screen), default state (auto-on vs opt-in), one-tap override.
- [ ] Add long-press for edit in the emulator (touch devices) so right-click isn't the only way.
- [ ] Review statistics section accessibility (labels, contrast, touch targets) and list 3 quick wins.

**One idea (backlog):** Trip summary card — When the user ends a trip, show one card: "X.X mi driven, Y.Y% OOR" with Share or Save option.

---

## Front-end Engineer

**Agent card:** `docs/agents/roles/frontend-engineer.md`  
**Data set:** `docs/agents/data-sets/frontend.md`

- [ ] **CRUCIAL #4** — TripHistoryByDateDialog: implement "Navigate to trip details" (line 108) or hand off to UI/UX if trip-detail screen doesn't exist.
- [ ] **CRUCIAL #9 (Statistics: monthly only)** — In `TripInputViewModel.kt`, load only monthly statistics; remove weekly/yearly from UI state and any layout/strings that show "Weekly" or "Yearly" (e.g. statistics row, period picker). Ref: `TripInputViewModel.kt` (~231–244, ~1012–1014), `presentation/viewmodel/`, `res/`.
- [ ] Implement **Auto drive detected** UI: button or indicator + optional settings toggle (from UI/UX spec).
- [ ] Emulator: Settings dialog (Mode, Templates, Help) and End Trip confirmation modal (End / Clear / Continue).
- [ ] Statistics "View" button: wire to a simple period picker (monthly / custom) in the emulator (no weekly/yearly).

**One idea (backlog):** Haptic on trip start/end for tactile confirmation.

---

## Back-end Engineer

**Agent card:** `docs/agents/roles/backend-engineer.md`  
**Data set:** `docs/agents/data-sets/backend.md`

- [ ] **CRUCIAL #2** — OfflineDataManager: implement `loadOfflineStorage()` and `saveOfflineStorage()` (actual load/save to local storage), or document as future phase and add `docs/product/FEATURE_BRIEF_offline_persistence.md`.
- [ ] **CRUCIAL #3** — TripStateManager: implement or document location jump detection (line 231).
- [ ] **CRUCIAL #9 (Statistics: monthly only)** — In `TripRepository` and `DomainTripRepositoryAdapter`, remove or deprecate `getWeeklyTripStatistics()` and `getYearlyTripStatistics()`; keep `getMonthlyTripStatistics()`. Update all implementations and callers. Ref: `domain/repository/TripRepository.kt`, `data/repository/DomainTripRepositoryAdapter.kt`.
- [ ] Define **Auto drive detected** logic: what triggers it (e.g. movement + speed threshold, or geofence), and how it starts a trip or shows the button.
- [ ] Implement trip state persistence so an active trip survives app kill (verify and document).
- [ ] Optional: persist "Driver check-in" (thumbs up/down) if Design approves.

**One idea (backlog):** Smart default for bounce miles — remember last used and prefill.

---

## DevOps Engineer

**Agent card:** `docs/agents/roles/devops-engineer.md`  
**Data set:** `docs/agents/data-sets/devops.md`

- [ ] **CRUCIAL #1** — Gradle 9 readiness: run build with `--warning-mode all`, document sources in `GRADLE_9_MIGRATION_NOTES.md`, plan plugin updates. Ref: `build_warnings.txt`, `GRADLE_9_MIGRATION_NOTES.md`.
- [ ] **CRUCIAL #1** — Update `docs/DEPLOYMENT.md`: set minSdk 24 and Java 17 (currently says minSdk 21 and JDK 11).
- [ ] Document how to run the emulator in CI (e.g. static build or smoke test) if we want a "no regressions" gate.
- [ ] Add a one-line "health" script or target that checks: build, unit tests, and (optional) coordinator email .env present.
- [ ] Keep Gradle and JDK versions in README or DEPLOYMENT.md up to date.

**One idea (backlog):** Nightly digest — optional script that summarizes the week's changes and emails a short digest.

---

## QA Engineer

**Agent card:** `docs/agents/roles/qa-engineer.md`  
**Data set:** `docs/agents/data-sets/qa.md`

- [ ] **CRUCIAL #5** — TripInputViewModelIntegrationTest: fix or document the ignored test (dispatcher conflict with Dispatchers.IO).
- [ ] **CRUCIAL #5** — TripHistoryByDateViewModelTest: complete with Robolectric/DI or document as integration-only and add to test plan.
- [ ] **CRUCIAL #5** — LocationValidationServiceTest: triage PHASE 1 / INSTRUMENTED / HEAVY TRAFFIC — fix, ignore with reason, or move to instrumented suite.
- [ ] **CRUCIAL #5** — ThemeScreenshotTest: add Paparazzi and enable, or document in `docs/qa/` that screenshot tests are deferred.
- [ ] **CRUCIAL #9 (Statistics: monthly only)** — Update tests: remove or adjust assertions for weekly/yearly; mock only `getMonthlyTripStatistics()` where relevant. Ref: `TripStatisticsWiringTest.kt`, `TripInputViewModelIntegrationTest.kt`, `TestConfig.kt` (statistics_button description).
- [ ] Add test cases for **Auto drive detected** once the flow is defined.
- [ ] Smoke-test read_replies.py and send_email.py (e.g. mock IMAP/SMTP or dry-run) so we don't break the open line.

**One idea (backlog):** Screenshot on trip end (debug builds, no PII) for visual regression.

---

## Security Specialist

**Agent card:** `docs/agents/roles/security-specialist.md`  
**Data set:** `docs/agents/data-sets/security.md`

- [ ] **CRUCIAL #7** — Review google-services.json in repo: confirm acceptable or recommend CI secrets / build-time injection; document in `docs/security/SECURITY_NOTES.md` or similar.
- [ ] **CRUCIAL #7** — Brief review of location/PII: where stored and transmitted; recommend hardening (e.g. encryption at rest, no logging of PII).
- [ ] **CRUCIAL #7** — Confirm `.env` and `last_reply.txt` are in `.gitignore` and document one-time checklist for new dev machines.
- [ ] Review **Auto drive detected** for privacy: location/movement data usage, storage/send beyond device.
- [ ] Short threat note: "What if the device is lost while a trip is active?" (lock screen, optional remote wipe, etc.).

**One idea (backlog):** App PIN or biometric to end trip.

---

## Email Editor / Market Guru

**Agent card:** `docs/agents/roles/email-editor-market-guru.md`  
**Data set:** `docs/agents/data-sets/email-editor.md`

- [ ] Draft 2–3 subject-line templates for "we need your decision" vs "here's an update" vs "please confirm" for the Human-in-the-Loop Manager.
- [ ] One-sentence value prop for OutOfRouteBuddy (for future app store or outreach).
- [ ] Suggest a short sign-off for coordinator emails (e.g. "— OutOfRouteBuddy Team" vs "— Coordinator").

**One idea (backlog):** Weekly one-liner — Human-in-the-Loop sends one line: "This week: [X]. Next: [Y]. Reply with any changes."

---

## File Organizer

**Agent card:** `docs/agents/roles/file-organizer.md`  
**Data set:** `docs/agents/data-sets/file-organizer.md`

- [ ] **CRUCIAL #8** — Ensure `docs/CRUCIAL_IMPROVEMENTS_TODO.md` is referenced from `docs/agents/WORKER_TODOS_AND_IDEAS.md` (already linked) and optionally from coordinator instructions or README.
- [ ] Optionally add one-line in `docs/agents/team-parameters.md` or README: "Current crucial improvement list: docs/CRUCIAL_IMPROVEMENTS_TODO.md."
- [ ] Propose a small reorg: e.g. `docs/agents/` vs `docs/product/` for roadmaps vs agent instructions, and where "future plans" live.
- [ ] Ensure EMULATOR_PERFECTION_PLAN, WORKER_TODOS, OPEN_LINE doc, and this COMPREHENSIVE_AGENT_TODOS are linked from README or a single docs index.
- [ ] Naming: decide whether worker todos are dated or a single WORKER_TODOS.md that gets updated.

**One idea (backlog):** Changelog for you — CHANGELOG_USER.md in plain language: "What we did this week / What's next."

---

## Human-in-the-Loop Manager

**Agent card:** `docs/agents/roles/human-in-the-loop-manager.md`  
**Data set:** `docs/agents/data-sets/human-in-the-loop.md`

- [ ] After user replies or next session: run `read_replies.py` when the user says they replied; update team-parameters or backlog from `last_reply.txt`.
- [ ] Use the Email Editor's subject-line templates once they're drafted.
- [ ] When coordinator requests: send status emails (e.g. "Statistics change and comprehensive todos are in place; please review COMPREHENSIVE_AGENT_TODOS when you can").

**One idea (backlog):** Reply with a number — when we need a quick priority, ask the user to reply with just the number (e.g. 1=Auto drive, 2=Reports, 3=Emulator polish).

---

## Quick reference: CRUCIAL improvement # → agents

| # | Area                         | Agents |
|---|------------------------------|--------|
| 1 | Build & docs alignment       | DevOps |
| 2 | Offline data persistence    | Back-end |
| 3 | Location jump detection      | Back-end |
| 4 | Trip history → details       | Front-end |
| 5 | Test health                  | QA |
| 6 | ROADMAP + FEATURE_BRIEF(s)   | Design / Creative Manager |
| 7 | Security and secrets review  | Security Specialist |
| 8 | Cross-link improvement list  | File Organizer |
| 9 | Statistics: monthly only     | UI/UX, Front-end, Back-end, QA |

---

*When tasks are completed, check the box in this file and update the source doc (CRUCIAL_IMPROVEMENTS_TODO or WORKER_TODOS_AND_IDEAS) if the task came from there.*
