# Test plan template — TEST_PLAN_<feature>.md

**Use for:** Auto drive, Export, or any major feature. Copy this template and fill in the sections below.

---

## Feature

- **Name:** [e.g. Auto drive detected]
- **Brief:** Link to `docs/product/FEATURE_BRIEF_<name>.md`.
- **Owner (tests):** QA Engineer; failures handed off to Front-end or Back-end with steps.

---

## Scenarios (what to test)

| # | Scenario | Type (unit / UI / integration) | Priority | How to run |
|---|----------|-------------------------------|----------|------------|
| 1 | [e.g. Happy path: start trip when movement detected] | unit + UI | P0 | `./gradlew test`; instrumented for UI |
| 2 | [Edge case] | unit | P1 | … |
| 3 | [Failure case] | unit | P1 | … |

---

## Acceptance criteria (from brief)

- [ ] Criterion 1
- [ ] Criterion 2

---

## How to run

- **Unit:** `./gradlew testDebugUnitTest` (or `./gradlew test`).
- **Instrumented:** `./gradlew connectedDebugAndroidTest` (device/emulator).
- **Single test class:** `./gradlew testDebugUnitTest --tests "*.MyTest"`.

---

## Handoff on failure

When a test fails due to a bug in production code: hand off to **Front-end** or **Back-end** with reproduction steps, expected vs actual, and environment. QA does not implement the fix; they describe the failure.
