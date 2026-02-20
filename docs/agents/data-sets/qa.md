# QA Engineer — data set

## Consumes (reads / references)

### Primary
- **Unit tests:** `app/src/test/` — JVM tests (Robolectric, coroutines, ViewModels, services, repositories). Use existing patterns (`runBlocking`, test fixtures, naming).
- **Instrumented tests:** `app/src/androidTest/` — device/emulator UI and integration tests. Use to add or extend coverage.
- **Worker todos (test-related):** `docs/agents/WORKER_TODOS_AND_IDEAS.md` — test-related items (e.g. “Add tests for Auto drive,” “Fix or document failing test,” “Smoke-test email scripts”).
- **Feature briefs:** `docs/product/FEATURE_BRIEF_*.md` — use to derive test scenarios and acceptance criteria (happy path, edge cases, failure cases).

### Secondary
- **Email scripts (smoke):** `scripts/coordinator-email/send_email.py`, `read_replies.py` — for smoke or dry-run tests so we don’t break the open line; mock or stub where needed.
- **Production code (read-only):** `app/src/main/` — to understand behavior when writing tests and bug reports; do not implement features here.
- **Deployment / run instructions:** `docs/DEPLOYMENT.md`, `run_tests.ps1` — how to run tests locally and in CI.

## Produces (writes / owns)

### Primary
- **New/updated tests** in `app/src/test/` and `app/src/androidTest/` — unit tests, integration tests, UI tests. Follow existing style and patterns.
- **Test plans:** `docs/qa/TEST_PLAN_<feature>.md` for major features (e.g. Auto drive) — scenarios, priority, and how to run.
- **Bug reports:** Clear reproduction steps, expected vs actual, environment; hand off to **Front-end** or **Back-end** with that info.

### Secondary
- **Test strategy (optional):** `docs/qa/TEST_STRATEGY.md` — what we test (unit vs integration vs UI), when, and quality gates. Can be short.
- **Documentation of known issues:** In test files or `docs/qa/` — e.g. “Test X ignored because of dispatcher conflict; see ticket or WORKER_TODOS.”

## Delegation (when to assign to this role)

- “Add tests for [feature]” (from feature brief or spec).
- “Fix or document the failing test in [file]” (e.g. TripInputViewModelIntegrationTest, TripHistoryByDateViewModelTest).
- “Smoke-test the email scripts” (read/send without sending real mail if possible).
- “Write a test plan for Auto drive” (or another feature).
- “Define acceptance criteria for [feature]” (can feed into Design or UI/UX).

## Handoffs

- **Failing test due to bug in production code:** Hand off to **Front-end** or **Back-end** with steps and expected behavior.
- **Flaky or environment-related:** Hand off to **DevOps** (e.g. CI, Gradle, device).
- **Security-related scenarios:** Hand off to **Security Specialist** for review; you may still implement the test.

## Out of scope (do not assign here)

- Implementing production feature code → **Front-end / Back-end**.
- CI pipeline configuration (YAML, Gradle tasks) → **DevOps** (you may say “we need a step that runs X”).
- Product prioritization or scope → **Design/Creative Manager**.
