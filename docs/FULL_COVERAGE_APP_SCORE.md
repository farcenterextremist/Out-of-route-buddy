# OutOfRouteBuddy — Full Coverage App Score

**Purpose:** A single scoring framework that analyzes the app across **performance/functionality**, **health**, **testing**, **workflow**, **security**, **data layer**, **build**, **code quality**, **UX/accessibility**, and **documentation**. Use for release readiness, prioritization, and tracking improvement over time.

**Related:** [COVERAGE_SCORE_AND_CRITICAL_AREAS](qa/COVERAGE_SCORE_AND_CRITICAL_AREAS.md), [PROJECT_AUDIT_2025_02_27](archive/PROJECT_AUDIT_2025_02_27.md), [RECENT_FEATURES_ASSESSMENT_AND_IMPROVEMENT_PROMPT](archive/prompts/RECENT_FEATURES_ASSESSMENT_AND_IMPROVEMENT_PROMPT.md), [SECURITY_PLAN](security/SECURITY_PLAN.md), [TEST_STRATEGY](qa/TEST_STRATEGY.md).

---

## 1. Scoring scale

- **Per dimension:** Score **1–5** (1 = critical gaps / poor, 5 = exemplary).
- **Two sub-scores per dimension:**
  - **Performance (P):** How well the app performs in that dimension — correctness, reliability, speed, absence of ANR/crashes, and proper use of APIs. "Does it work well?"
  - **Completion (C):** How complete the implementation is — features shipped, gaps filled, deferred items documented, and acceptance criteria met. "Is it done?"
- **Dimension score:** Use the average of P and C, or the lower of the two, for weighted overall (recommended: average).
- **Overall:** Weighted average of dimension scores, then mapped to a **letter grade** and **percentage** (e.g. 4.2/5 → 84% → B+).
- **Re-score:** After major releases or quarterly; update "Current" column and "Last scored" date below.

**Last scored:** _______________

---

## 2. Dimensions and weights

| # | Dimension | Weight | Description |
|---|-----------|--------|-------------|
| 1 | **Performance & functionality** | 18% | Feature correctness, trip lifecycle, GPS/drive detect, notifications, calendar/periods, no ANR, resource use |
| 2 | **Health & resilience** | 12% | Startup, DB init, service lifecycle, error handling, recovery, `isHealthy()` / `getDatabaseError()` |
| 3 | **Testing** | 15% | Effective coverage score, critical-path tests, deferred/ignored tests, JaCoCo gate, stability |
| 4 | **User workflow** | 14% | End-to-end flows (start → track → end, calendar, history, settings, overlay), navigation, recovery UX |
| 5 | **Security** | 10% | Secrets, data protection, permissions, PII in logs, audit logging, SECURITY_PLAN alignment |
| 6 | **Data layer** | 10% | Adapter error handling (D1), delete/update propagation (D2), StateCache (D3), DB suspend/migration |
| 7 | **Build & configuration** | 8% | Java 17, minify/ProGuard, lint gate, Gradle 9 readiness |
| 8 | **Code quality** | 6% | Dead code, logging policy, unsafe `!!`, maintainability |
| 9 | **UX & accessibility** | 5% | Overlay a11y, notifications, terminology, touch targets |
| 10 | **Documentation** | 4% | Architecture, QA strategy, security docs, technical ADRs |

**Total weight:** 100%.

---

## 3. Dimension scorecards

### 3.1 Performance & functionality (18%)

| Criterion | 1 (Poor) | 3 (Adequate) | 5 (Strong) | How to verify |
|-----------|-----------|---------------|------------|---------------|
| Trip lifecycle | Start/end broken or data lost | Works; minor edge cases | Start → track → end → save reliable; recovery works | Manual / instrumented trip flow |
| GPS & drive detect | Miles wrong or always count walking | Mostly correct; some drift | Drive state strict; walking auto-excluded; highway context | DriveStateClassifierTest; manual |
| Notifications | Missing or wrong text | Shade + overlay work | Shade "Trip in progress" / "Trip ending"; overlay bubble; fallback when overlay denied | TripTrackingPullDownNotificationTest; overlay tests |
| Calendar & periods | Current period wrong; calendar broken | Correct in common cases | Current Period = period containing today; CUSTOM boundaries correct | PeriodCalculationServiceTest; manual |
| ANR / responsiveness | ANR on start or during trip | Occasional jank | No ANR; DB/network on background threads | StrictMode; profile |
| Resource use | High battery/memory | Acceptable | Foreground service and GPS scoped; cleanup on stop | Battery stats; LeakCanary (optional) |

**Performance (1–5):** 4 — Trip lifecycle, drive detect, notifications, and calendar behave correctly; no ANR; minor edge cases (e.g. 0 speed, period boundaries).  
**Completion (1–5):** 4 — Core features shipped; drive-detect settings, overlay fallback, notification i18n/tap and period fix done; some edge-case docs remain.  
**Notes:** _______________________________________________

---

### 3.2 Health & resilience (12%)

| Criterion | 1 (Poor) | 3 (Adequate) | 5 (Strong) | How to verify |
|-----------|-----------|---------------|------------|---------------|
| Startup | Crash or stuck | Starts; some init failures unhandled | Clean start; DB/prefs init with fallback | ApplicationInitializationTest; manual |
| DB & services | No health API; failures silent | isHealthy() exists; not always used | isHealthy() / getDatabaseError() used; recovery paths tested | OutOfRouteApplication; instrumented |
| Error handling | Exceptions crash app | Most paths handle; some swallowed | Errors logged; UI shows snackbar or retry | D1/D2; handleEvent in TripInputFragment |
| Recovery | No trip recovery | Recovery exists; edge cases | Recovery dialog; persist/restore state; detector reset on end | TripRecoveryDialog; TripStatePersistence |

**Performance (1–5):** 4.5 — Startup runs DatabaseHealthCheck; isHealthy/getDatabaseError used; health checked before Start trip; errors logged and shown.  
**Completion (1–5):** 4.5 — Startup health and start-trip guard implemented; recovery and detector reset documented; error-handling audit done.  
**Notes:** _______________________________________________

---

### 3.3 Testing (15%)

| Criterion | 1 (Poor) | 3 (Adequate) | 5 (Strong) | How to verify |
|-----------|-----------|---------------|------------|---------------|
| Suite runnability | testDebugUnitTest fails | Passes with known skips | testDebugUnitTest + jacocoSuiteTestsOnly pass | `.\gradlew jacocoSuiteTestsOnly` |
| Unit test breadth | Few tests; many files untested | Most layers have tests | ViewModels, services, repos, utils, key fragments covered | Test file count vs main files |
| Critical-path coverage | Startup/trip/recovery untested | Partially covered | Application, trip lifecycle, persistence, recovery have tests | COVERAGE_SCORE_AND_CRITICAL_AREAS |
| Edge-case & failure | No failure-path tests | Some adapter/delete tests | D1 error paths; insert/delete failure; empty list; invalid ID | DomainTripRepositoryAdapterTest; DriveStateClassifierTest |
| Deferred/ignored tests | Many @Ignore without reason | Documented in FAILING_OR_IGNORED_TESTS | Zero or explicitly deferred with owner and reason | FAILING_OR_IGNORED_TESTS.md |
| JaCoCo & thresholds | No coverage or gate | Report generated; verification fails | jacocoCoverageVerification passes or threshold documented | JACOCO_SUITE.md |

**Performance (1–5):** 4 — Suite runs (jacocoSuiteTestsOnly); stable; critical-path and edge-case coverage good; TestDispatcher used.  
**Completion (1–5):** 4.3 — Effective coverage 4.3/5; FAILING_OR_IGNORED_TESTS current; jacocoCoverageVerification fails until per-class thresholds met.  
**Notes:** _______________________________________________

---

### 3.4 User workflow (14%)

| Criterion | 1 (Poor) | 3 (Adequate) | 5 (Strong) | How to verify |
|-----------|-----------|---------------|------------|---------------|
| Start trip | Permission or start broken | Works with manual steps | Permission check → start → shade + overlay ready | MainActivity; TripInputFragment |
| Track & auto-detect | No drive state or wrong | Drive state works; no Settings tuning | Drive state + optional Settings; "Not counting" shown when walking | TripTrackingService; DriveStateClassifier |
| End trip | End loses data or no dialog | End works; overlay optional | End from app or overlay; dialog; detector reset | TripInputViewModel.endTrip; TripEndedOverlayService |
| Calendar & stats | Wrong period or no stats | Standard/Custom work | Current Period correct; stats by period; calendar navigates | TripInputFragment; PeriodCalculationService |
| History & details | No history or no details | List works; details missing | List + tap → TripDetailsFragment with trip | TripHistoryFragment; TripHistoryByDateDialog; nav |
| Settings | Missing or broken | Core settings work | Period mode, overlay permission, drive-detect (if added) | SettingsFragment |

**Performance (1–5):** 4 — Start→track→end→history→details and overlay flow work; permission before start; "Not counting" and overlay fallback behave correctly.  
**Completion (1–5):** 4 — All main flows wired; Settings (period, overlay, drive-detect); history→details navigation; overlay dismiss and detector reset.  
**Notes:** _______________________________________________

---

### 3.5 Security (10%)

| Criterion | 1 (Poor) | 3 (Adequate) | 5 (Strong) | How to verify |
|-----------|-----------|---------------|------------|---------------|
| Secrets & PII | Keys in code; PII in logs | Some gaps (S-1–S-5) | No PII in release logs; secrets doc'd; .gitignore | SECURITY_PLAN; L1 logging |
| Data protection | Sensitive data in plain prefs | Room OK; prefs unencrypted | EncryptedSharedPreferences or documented risk | SECURITY_GRADE Data Protection |
| Permissions | Tracking without permission | Permission asked; not always checked before start | S3: permission verified before TripTrackingService start | MainActivity; TripTrackingService |
| Audit & docs | No audit or security docs | Trip insert/export/delete audit | Audit logs + SECURITY_NOTES + SECURITY_PLAN updated | SECURITY_PLAN Section 8 |

**Performance (1–5):** 4 — Permissions checked before tracking; no PII in release logs; audit logging and input validation in place.  
**Completion (1–5):** 4 — SECURITY_GRADE B+ (~4.0); Data Protection 3; S3 permission before start; SECURITY_PLAN and SECURITY_NOTES current.  
**Notes:** _______________________________________________

---

### 3.6 Data layer (10%)

| Criterion | 1 (Poor) | 3 (Adequate) | 5 (Strong) | How to verify |
|-----------|-----------|---------------|------------|---------------|
| D1 Adapter errors | Exceptions swallowed; no UI | Logged; no exposure | loadErrors Flow; ViewModels show snackbar | DomainTripRepositoryAdapter; TripHistoryViewModel |
| D2 Delete/update | Return dropped; no UI feedback | Boolean returned; not always shown | Delete/update failure → snackbar or error state | TripHistoryByDateViewModel deleteError |
| D3 StateCache | Stale cache after mutations | Sometimes invalidated | Single owner invalidates on insert/update/delete | StateCache.invalidateAll in adapter |
| DB3 / migration | getTripById blocks main; no schema export | Suspend or export only | getTripById suspend + IO; exportSchema true; migration path | TripDao; AppDatabase; schemas/ |

**Performance (1–5):** 4 — Adapter errors surface to UI; delete/update feedback; StateCache invalidated; DB on IO; no main-thread block.  
**Completion (1–5):** 4 — D1/D2/D3 and DB3 addressed; getTripById suspend + IO; exportSchema; adapter loadErrors and invalidateAll.  
**Notes:** _______________________________________________

---

### 3.7 Build & configuration (8%)

| Criterion | 1 (Poor) | 3 (Adequate) | 5 (Strong) | How to verify |
|-----------|-----------|---------------|------------|---------------|
| CFG1 Java | Java 8 or mixed | Java 17 in app only | Java 17 everywhere; no VERSION_1_8 | app/build.gradle.kts |
| CFG2 Minify | Release minify off | Minify on; no rules | Minify + shrinkResources; ProGuard rules for Room/Hilt | proguard-rules.pro |
| CFG3 Lint | abortOnError false | Lint runs; some suppressed | abortOnError true; issues fixed or suppressed | app/build.gradle.kts lint block |
| CFG4 Gradle 9 | Many deprecations | Some documented | --warning-mode all clean or GRADLE_9_MIGRATION_NOTES | GRADLE_9_MIGRATION_NOTES.md |

**Performance (1–5):** 4 — Build succeeds; Java 17; minify/shrinkResources on; lint runs; no blocking issues.  
**Completion (1–5):** 4 — Java 17 everywhere; minify + ProGuard rules; lint abortOnError; Gradle 9 migration in progress/documented.  
**Notes:** _______________________________________________

---

### 3.8 Code quality (6%)

| Criterion | 1 (Poor) | 3 (Adequate) | 5 (Strong) | How to verify |
|-----------|-----------|---------------|------------|---------------|
| Dead code | R1/R2/R3 unresolved | Some removed or documented | saveCompletedTrip wired or deferred with owner; no unused entry points | QUALITY_AND_ROBUSTNESS_PLAN |
| Logging | Log everywhere; PII risk | AppLogger in some places | Logging facade; no PII in release; LOGGING_POLICY | docs/technical/LOGGING_POLICY.md |
| Safety | Unsafe !! or silent failures | Few !!; most paths safe | No unnecessary !!; invalid ID handled | L2/L3 PROJECT_AUDIT |
| Maintainability | No structure or docs | Layered; some docs | Clear layers; config in one place; KDoc where needed | ARCHITECTURE.md |

**Performance (1–5):** 4.5 — No unsafe !!; logging facade; R1 wired; L1 done; KDoc on key APIs.  
**Completion (1–5):** 4.5 — saveCompletedTrip wired; L1 DONE; MainActivity !! removed; TripStatePersistence/ARCHITECTURE config docs.  
**Notes:** _______________________________________________

---

### 3.9 UX & accessibility (5%)

| Criterion | 1 (Poor) | 3 (Adequate) | 5 (Strong) | How to verify |
|-----------|-----------|---------------|------------|---------------|
| Overlay | No content description or focus | Description present | Focusable; clear description; long-press dismiss for a11y | TripEndedOverlayService; ACCESSIBILITY_CHECKLIST |
| Notifications | Hardcoded strings; no tap | String resources | i18n title/content; content intent (tap opens app) | TripTrackingService; strings.xml |
| Terminology | Inconsistent or confusing | Mostly consistent | TERMINOLOGY_AND_COPY; consistent labels | ux/TERMINOLOGY_AND_COPY.md |
| Touch targets | Too small or overlapping | Minimum 48dp where possible | TOUCH_TARGETS_TRIP; no tiny tap areas | ux/TOUCH_TARGETS_TRIP.md |

**Performance (1–5):** 4 — Overlay focus/description/dismiss work; notification strings i18n; terminology consistent in UI.  
**Completion (1–5):** 4 — Overlay a11y (focus, description, long-press/drag dismiss); notification content intent; TERMINOLOGY_AND_COPY and touch-target docs.  
**Notes:** _______________________________________________

---

### 3.10 Documentation (4%)

| Criterion | 1 (Poor) | 3 (Adequate) | 5 (Strong) | How to verify |
|-----------|-----------|---------------|------------|---------------|
| Architecture & technical | No overview | ARCHITECTURE exists | ARCHITECTURE + WIRING_MAP + ADRs where needed | docs/ARCHITECTURE.md; technical/ |
| QA | No test strategy | TEST_STRATEGY; some gaps | TEST_STRATEGY + JACOCO_SUITE + FAILING_OR_IGNORED_TESTS current | qa/ |
| Security | No security docs | SECURITY_PLAN exists | SECURITY_PLAN + SECURITY_NOTES + SECURITY_GRADE; gaps documented | security/ |
| Product/roadmap | No roadmap | CRUCIAL_IMPROVEMENTS only | ROADMAP.md + FEATURE_BRIEF_* where needed | docs/product/ |

**Performance (1–5):** 4 — Docs are current and usable; ARCHITECTURE, QA, and security docs support verification and onboarding.  
**Completion (1–5):** 4 — ARCHITECTURE, TEST_STRATEGY, JACOCO_SUITE, FAILING_OR_IGNORED_TESTS, SECURITY_PLAN, SECURITY_GRADE, product/roadmap present.  
**Notes:** _______________________________________________

---

## 4. Overall score formula

**Weighted average:**

```
Overall = Σ (Dimension_score × Weight) / 100
```

Example (all dimensions 4.0): Overall = 4.0 → **80%**.

**Letter grade and percentage:**

| Weighted average (1–5) | Percentage | Letter |
|-------------------------|------------|--------|
| 4.5 – 5.0               | 90–100%    | A      |
| 4.0 – 4.4               | 80–88%     | B+     |
| 3.5 – 3.9               | 70–78%     | B      |
| 3.0 – 3.4               | 60–68%     | C      |
| 2.5 – 2.9               | 50–58%     | D      |
| &lt; 2.5                 | &lt; 50%   | F      |

---

## 5. Quick reference — current state (template)

Fill after each scoring run. **Dimension score** = average of Performance and Completion (or use the lower for conservative grading).

| Dimension | Weight | Performance (1–5) | Completion (1–5) | Score (1–5) | Weighted |
|-----------|--------|-------------------|-------------------|-------------|----------|
| 1. Performance & functionality | 18% | 4 | 4 | 4.0 | 0.72 |
| 2. Health & resilience | 12% | 4.5 | 4.5 | 4.5 | 0.54 |
| 3. Testing | 15% | 4 | 4.3 | 4.15 | 0.62 |
| 4. User workflow | 14% | 4 | 4 | 4.0 | 0.56 |
| 5. Security | 10% | 4 | 4 | 4.0 | 0.40 |
| 6. Data layer | 10% | 4 | 4 | 4.0 | 0.40 |
| 7. Build & configuration | 8% | 4 | 4 | 4.0 | 0.32 |
| 8. Code quality | 6% | 4.5 | 4.5 | 4.5 | 0.27 |
| 9. UX & accessibility | 5% | 4 | 4 | 4.0 | 0.20 |
| 10. Documentation | 4% | 4 | 4 | 4.0 | 0.16 |
| **Total** | **100%** | — | — | — | **3.99** |

**Overall:** 3.99 / 5 → **~80%** → Grade **B+**  
**Last scored:** 2025-02-28 (Health & Code quality 4.5 plan)

---

## 6. Suggested initial scores (from existing docs)

Use these as a starting point; adjust after walking through each dimension. **P** = Performance, **C** = Completion.

| Dimension | P (1–5) | C (1–5) | Basis |
|-----------|---------|---------|--------|
| Performance & functionality | 4 | 4 | Trip/overlay/notification work; period fix; drive-detect settings; some edge cases remain. |
| Health & resilience | 4.5 | 4.5 | Startup health check and start-trip guard; recovery and error audit (Health & Code quality 4.5 plan). |
| Testing | 4 | 4.3 | COVERAGE_SCORE effective 4.3/5; jacocoSuiteTestsOnly gate; deferred tests documented. |
| User workflow | 4 | 4 | Start→end→history→details wired; Settings; overlay fallback; permission before start. |
| Security | 4 | 4 | SECURITY_GRADE B+; Data Protection 3; S3 permission before start. |
| Data layer | 4 | 4 | D1/D2/D3 and DB3 addressed; schema export; adapter loadErrors and StateCache. |
| Build & configuration | 4 | 4 | Java 17; minify on; lint abortOnError; Gradle 9 in progress. |
| Code quality | 4.5 | 4.5 | R1 wired; L1 DONE; !! removed; KDoc and ARCHITECTURE config (Health & Code quality 4.5 plan). |
| UX & accessibility | 4 | 4 | Overlay a11y; notification strings/content intent; terminology docs. |
| Documentation | 4 | 4 | ARCHITECTURE, QA, security, product docs present. |

**Suggested overall (avg of P and C per dimension, then weighted):** ~4.2 → **84% → B+** (after Health & Code quality 4.5 plan)

---

## 7. How to run a full score

1. **Checklist:** For each dimension, open the "How to verify" column and run the referenced tests/docs.
2. **Score:** Assign **Performance (1–5)** and **Completion (1–5)** per dimension (or per criterion then average). Combine as (P+C)/2 for dimension score, or use the lower for conservative grading.
3. **Weight:** Multiply each dimension score by its weight; sum.
4. **Record:** Fill Section 5 (Performance, Completion, Score, Weighted) and update "Last scored."
5. **Prioritize:** Use low-scoring dimensions and low P or C for the next improvement plan.

---

## 8. References

- [COVERAGE_SCORE_AND_CRITICAL_AREAS](qa/COVERAGE_SCORE_AND_CRITICAL_AREAS.md) — testing dimension detail
- [PROJECT_AUDIT_2025_02_27](archive/PROJECT_AUDIT_2025_02_27.md) — audit IDs (C1–C4, D1–D3, DB, CFG, R, L, S, T)
- [RECENT_FEATURES_ASSESSMENT_AND_IMPROVEMENT_PROMPT](RECENT_FEATURES_ASSESSMENT_AND_IMPROVEMENT_PROMPT.md) — feature-level scores (78%, 82%, 72%)
- [SECURITY_PLAN](security/SECURITY_PLAN.md) and [SECURITY_GRADE](security/SECURITY_GRADE.md) — security dimension
- [QUALITY_AND_ROBUSTNESS_PLAN](QUALITY_AND_ROBUSTNESS_PLAN.md) — D1–D3, L1, R1
- [CRUCIAL_IMPROVEMENTS_TODO](CRUCIAL_IMPROVEMENTS_TODO.md) — product/tech backlog
- [HEALTH_AND_CODE_QUALITY_IMPROVEMENT_PROMPT](HEALTH_AND_CODE_QUALITY_IMPROVEMENT_PROMPT.md) — prompt to plan raising Health & resilience and Code quality from 3.5 to 4.5
