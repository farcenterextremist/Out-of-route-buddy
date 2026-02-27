# QA

This folder holds **test strategy and test plans** owned by the QA Engineer.

- **TEST_STRATEGY.md** — What we test (unit vs integration vs UI), when, quality gates.
- **TEST_PLAN_TEMPLATE.md** — Template for TEST_PLAN_<feature>.md (Board-adopted: use per major feature).
- **TEST_PLAN_<feature>.md** — Per-feature test scenarios and how to run (e.g. Auto drive).
- **SSOT_TEST_SCENARIOS.md** — Scenarios that verify Known Truths (Clear doesn't insert; recovery order; live miles source; etc.); add or extend tests to cover these.
- **FAILING_OR_IGNORED_TESTS.md** — Tracking for failing/ignored tests; fix or document.
- **COORDINATOR_EMAIL_SMOKE.md** — Smoke test for send/read email; run when we change coordinator-email scripts (dry-run or mock where needed).
- **REGESSION_CHECKLIST.md** — Manual (or automated) checklist: Trip start/end, History, Statistics.

**CI (verified):** `.github/workflows/android-tests.yml` runs unit tests (`testDebugUnitTest`), coverage, lint, and instrumented tests (`connectedDebugAndroidTest`). No change needed; QA defines what "right" means; DevOps owns the YAML.

See `docs/agents/data-sets/qa.md` for the full data set.
