# 8-Hour Improvement Plan — OutOfRouteBuddy

**Objective:** Shippable product.  
**Strategy:** Slow, decisive, wise improvements. **~98% backend, ~2% frontend** (minimal UI).  
**Pulse:** Every 30 minutes — check progress and log to `docs/automation/pulse_log.txt`.  
**Final 2 hours:** Plan next moves and produce shipping instructions → `Desktop\OUTOFROUTEBUDDY_SHIP_INSTRUCTIONS.txt`.

---

## Backend-focused improvements (priority order)

1. **Data & persistence**
   - [ ] Ensure `TripStatePersistence` / `TripPersistenceManager` edge cases are covered (null, empty, corrupt state).
   - [ ] Add or tighten null-safety and logging in `OfflineDataManager` and `TripRepository` adapters.
   - [ ] Verify Room migrations and schema exports are up to date; no silent failures on upgrade.

2. **Services**
   - [ ] Review `TripTrackingService`, `UnifiedTripService`, `LocationValidationService` for unhandled exceptions and add defensive logs or fallbacks where appropriate.
   - [ ] Ensure `TripEndedDetector` / overlay service don’t leak or crash on rapid start/stop.
   - [ ] Add or improve logging in one critical path (e.g. trip start → save) for easier production debugging.

3. **Validation & config**
   - [ ] Review `ValidationFramework` and `ValidationConfig` for edge values (0, negative, very large miles).
   - [ ] Ensure `PeriodCalculationService` and timezone handling are consistent with `DATE_AND_TIME_ASSUMPTIONS.md`.

4. **Tests & quality**
   - [ ] Keep unit test suite green; fix any new failures from the above changes.
   - [ ] Add or extend one unit test for a critical backend path (e.g. save trip, load state).
   - [ ] Run `:app:lintDebug` and fix or document any new issues; no regressions.

5. **Error handling & resilience**
   - [ ] Ensure `ErrorHandler` or equivalent is used in key flows (trip save, recovery).
   - [ ] Review `OutOfRouteApplication` health check and recovery flow; no silent swallows of critical errors.

---

## Frontend (minimal, ~2%)

- [ ] At most one small copy/UX tweak (e.g. string in `strings.xml`, or one accessibility hint) if clearly needed for ship.
- [ ] No layout or flow changes unless critical for store/accessibility compliance.
- [ ] **Design research:** Per [DESIGN_AND_UX_RESEARCH.md](./DESIGN_AND_UX_RESEARCH.md), research color schemes, templates, state flows, color matching, popular designs, beautification standards, professionalism—apply one subtle improvement if clearly needed for ship.

---

## Pulse checklist (every 30 minutes)

Run `scripts\automation\pulse_check.ps1` from repo root. It will:

1. **Tests:** Run `.\gradlew.bat :app:testDebugUnitTest` (or quick smoke if configured).
2. **Lint:** Run `.\gradlew.bat :app:lintDebug` (optional, can be every 2nd pulse).
3. **Log:** Append to `docs/automation/pulse_log.txt`:
   - Timestamp
   - Tests: pass/fail count (or "skipped")
   - Lint: errors/warnings count (or "skipped")
   - One-line progress note (from plan checkboxes or free text).
4. **Objective distance:** Note how close to shippable (e.g. "Tests green, lint clean, N items left on plan").

---

## Final 2 hours (planning and ship instructions)

1. **Review** `pulse_log.txt` and the plan checkboxes; list what was done and what remains.
2. **Decide** next moves: what must be done before first store upload vs. what can follow in a patch.
3. **Write** `C:\Users\brand\Desktop\OUTOFROUTEBUDDY_SHIP_INSTRUCTIONS.txt` with:
   - Pre-ship checklist (version, changelog, tests, lint, manual smoke).
   - Build and sign commands (from DEPLOYMENT.md and STORE_CHECKLIST.md).
   - Upload steps (Play Console or other store).
   - Post-ship: monitoring, rollback, first patch plan.

The automation runner can call a small "planning" script that generates this file from a template plus current repo state (e.g. version from `app/build.gradle.kts`, test/lint status from last pulse).

---

## How to run the automation

**Option A — Pulse every 30 min manually**  
- Every 30 min run: `.\scripts\automation\pulse_check.ps1`  
- In the last 2 hours, run the planning step and create the ship instructions file.

**Option B — 8-hour runner (recommended)**  
- Run once: `.\scripts\automation\run_8hr_automation.ps1`  
- It loops for 8 hours, runs the pulse every 30 min, and in the last 2 hours generates the desktop ship instructions.

**Option C — Windows Task Scheduler**  
- Create a task that runs `pulse_check.ps1` every 30 minutes for 8 hours.  
- Separately run the planning step once at the end (or use the runner).

---

*Created for slow, decisive progress toward a shippable OutOfRouteBuddy release.*
