# Failing or ignored tests — tracking

**Owner:** QA Engineer  
**Purpose:** Document known failing or ignored tests and plan to fix or adjust.  
**Related:** 25-point #17, `docs/agents/APP_IMPROVEMENT_25_POINT_BRAINSTORM.md`, TEST_STRATEGY (when created).

---

## Tests to fix or document

| Test (class or name) | Status | Action |
|---------------------|--------|--------|
| **TripInputViewModelIntegrationTest** | Failing or flaky | Fix or document in TEST_STRATEGY; mark with @Ignore only if documented here. |
| **TripHistoryByDateViewModelTest** | Failing or flaky | Same as above. |
| **LocationValidationServiceTest** | Failing or flaky | Same as above. |
| **ThemeScreenshotTest** | Failing or flaky | Same as above; screenshot tests often need stable environment. |

---

## How to update this doc

1. Run `.\gradlew.bat test` (or `./gradlew test`) and note any failing tests.
2. Add or update the row: status (e.g. "Fail", "Ignore") and planned action (fix, document, or ignore with reason).
3. When a test is fixed, remove it from the table or set status to "Fixed" and date.

---

## TEST_STRATEGY / test plan

When updating test strategy or test plans, reference this file for “known issues” and point to it from `docs/qa/README.md`.

---

*Goal: No silent failures; every failing or ignored test is documented and has an owner/action.*
