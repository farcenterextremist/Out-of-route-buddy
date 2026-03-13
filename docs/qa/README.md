# QA

This folder holds **test strategy and test plans** owned by the QA Engineer.

- **TEST_STRATEGY.md** — What we test (unit vs integration vs UI), when, quality gates.
- **TEST_PLAN_TEMPLATE.md** — Template for `TEST_PLAN_<feature>.md`.
- **TEST_PLAN_<feature>.md** — Per-feature test scenarios and how to run.
- **SSOT_TEST_SCENARIOS.md** — Scenarios that verify Known Truths and core behavior.
- **FAILING_OR_IGNORED_TESTS.md** — Tracking for failing or ignored tests.
- **TEST_FAILURES_DOCUMENTATION.md** — Known test failures, flaky tests, dispatcher/perf notes and resolutions.
- **REGESSION_CHECKLIST.md** — Manual or automated checklist for core app flows.

**CI (verified):** `.github/workflows/android-tests.yml` runs unit tests, coverage, lint, and instrumented tests. QA defines what "right" means; DevOps owns the workflow wiring.

See `docs/agents/data-sets/qa.md` for the full data set.
