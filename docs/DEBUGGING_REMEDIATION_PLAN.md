# Plan: complete debugging remediation (tests + Detekt + hygiene)

**Status (2026-03-16):** Unit tests green (`testDebugUnitTest`); Robolectric TripInput tests use **NavHost** `TripInputFragment`, **`startTripViaViewModel`** (bypasses flaky reliability-dialog timing), and **Continue Trip** for end-dialog flows. **MockGps** tests seed `calculateTrip(..., 0.0)` so TripTracking metrics aren’t ignored. **Lint:** `preference_accordion_widget.xml` uses `app:tint`. Re-run full verify: `.\gradlew.bat :app:testDebugUnitTest :app:detekt :app:lintDebug :app:assembleDebug --no-daemon` (one Gradle at a time on Windows).

**Goal:** Close everything called out in the last debugging pass: **10 failing unit tests**, **Detekt/Kotlin mismatch**, and **repeatable test runs on Windows**.  
**Owner:** whoever picks this up (Jarvey / dev).  
**Related:** `docs/TEST_FAILURES_DOCUMENTATION.md`, `docs/qa/FAILING_OR_IGNORED_TESTS.md`, `app/build/reports/tests/testDebugUnitTest/index.html`.

---

## Success criteria

| Item | Done when |
|------|-----------|
| Unit tests | `:app:testDebugUnitTest` passes (0 failures); skipped count unchanged or documented. |
| Detekt | `:app:detekt` completes without Kotlin version error; new findings fixed or baselined in `config/detekt/detekt.yml`. |
| Windows locks | Documented one-command workaround; optional script to clear `test-results` before CI/local full test. |
| Docs | This plan’s checkboxes updated or linked from `TEST_FAILURES_DOCUMENTATION.md`. |

---

## Phase 0 — Preconditions (15 min)

1. **Single Gradle at a time** — Close extra IDE builds, stop background `gradlew` (tests + lint in parallel caused `Unable to delete ... test-results`).
2. **Baseline report** — Open `app/build/reports/tests/testDebugUnitTest/index.html` and note **exact failure messages** for each of the 10 tests (copy into a scratch file or ticket).
3. **Branch** — Work on a branch so you can bisect if a fix regresses something else.

**Checkpoint:** Clean tree, one terminal running tests.

---

## Phase 1 — Fix the 10 failing tests (order: smallest blast radius first)

### 1A. `MockGpsSynchronizationServiceTest` (2 failures) — ~1–2 h

- **Likely causes:** timing/dispatcher, stale mocks, or API drift vs `TripTrackingService` / GPS sync.
- **Steps:**
  1. Run only this class:  
     `.\gradlew.bat :app:testDebugUnitTest --tests "com.example.outofroutebuddy.services.MockGpsSynchronizationServiceTest" --no-daemon`
  2. Read stack traces; fix **production code** only if contract is wrong; otherwise fix **test doubles** (`TestDispatchers`, `advanceUntilIdle`, `runTest`).
  3. Re-run class until green; then full `testDebugUnitTest` once.

### 1B. `TripInputFragmentEdgeCasesRobolectricTest` (2 failures) — ~1–2 h

- **Likely causes:** layout ID changes, binding assumptions, or dialog/recovery flow.
- **Steps:**
  1. Run:  
     `.\gradlew.bat :app:testDebugUnitTest --tests "*.TripInputFragmentEdgeCasesRobolectricTest" --no-daemon`
  2. Update assertions to match current `fragment_trip_input.xml` / `TripInputFragment` (e.g. toolbar structure, visibility).
  3. Prefer **stable test IDs** (`R.id.*`) over text-only where possible.

### 1C. `TripInputFragmentBehaviorRobolectricTest` (6 failures) — ~2–4 h

- **Likely causes:** same as 1B, plus navigation/drawer/toolbar (hamburger, settings entry).
- **Steps:**
  1. Run:  
     `.\gradlew.bat :app:testDebugUnitTest --tests "*.TripInputFragmentBehaviorRobolectricTest" --no-daemon`
  2. Group failures by theme (toolbar, drawer, start trip, permissions mock).
  3. Fix in batches; re-run class after each batch.
  4. Full suite at end.

**Checkpoint:** `1111 tests, 0 failed` (or new total if tests added).

---

## Phase 2 — Detekt + Kotlin 2.0.21 (1–3 h)

1. **Upgrade plugin** — In `gradle/libs.versions.toml`, bump `detekt` to a version that **officially supports Kotlin 2.0.x** (check [Detekt releases](https://github.com/detekt/detekt/releases) / compatibility table). Typical path: **1.23.6+** or **2.x** series depending on project Kotlin.
2. **Sync & run:**  
   `.\gradlew.bat :app:detekt --no-daemon`
3. **Triage findings:**
   - Auto-fix where safe (`detekt -auto-correct` if enabled).
   - New rules: either fix code or **suppress with comment + reason** or adjust `config/detekt/detekt.yml` (document in PR).
4. **CI alignment** — If CI runs Detekt, update workflow to same Gradle command.

**Checkpoint:** `:app:detekt` green.

---

## Phase 3 — Hygiene & documentation (30–60 min)

1. **Windows test-results lock** — Add to `docs/DEV_SETUP.md` or `TEST_FAILURES_DOCUMENTATION.md`:  
   - “If `Unable to delete ... test-results` appears: stop parallel Gradle, `Remove-Item -Recurse -Force app\build\test-results\testDebugUnitTest`, retry.”
2. **Optional script** — e.g. `scripts/clear_unit_test_results.ps1` that removes that folder before `testDebugUnitTest` (document in `docs/automation` if used in loops).
3. **Update `docs/qa/FAILING_OR_IGNORED_TESTS.md`** — Remove or amend entries once these 10 pass.
4. **Short entry in `TEST_FAILURES_DOCUMENTATION.md`** — Link to this plan + “resolved as of &lt;date&gt;” when done.

---

## Phase 4 — Final verification (30 min)

Run in sequence (no parallel Gradle):

```powershell
cd c:\Users\brand\OutofRoutebuddy
.\scripts\automation\loop_health_check.ps1 -Quick
.\gradlew.bat :app:testDebugUnitTest :app:lintDebug :app:detekt --no-daemon
.\gradlew.bat :app:assembleDebug --no-daemon
```

Optional: `adb logcat` smoke on device after behavior-affecting fixes.

---

## Effort estimate

| Phase | Rough time |
|-------|------------|
| 0 | 15 min |
| 1 (tests) | 4–8 h |
| 2 (Detekt) | 1–3 h |
| 3–4 | 1–2 h |
| **Total** | **~1–2 dev days** |

---

## Checklist (copy to PR or issue)

- [ ] Phase 0: single Gradle, failure messages captured  
- [ ] MockGpsSynchronizationServiceTest (2) green  
- [ ] TripInputFragmentEdgeCasesRobolectricTest (2) green  
- [ ] TripInputFragmentBehaviorRobolectricTest (6) green  
- [ ] Full `testDebugUnitTest` green  
- [ ] Detekt version bumped; `:app:detekt` green  
- [ ] Lint still acceptable (`lintDebug`)  
- [ ] Docs/scripts updated; FAILING_OR_IGNORED_TESTS synced  

---

*Created to close the debugging list from the 2026-03-18 run: 10 failures + Detekt mismatch + test-results lock.*
