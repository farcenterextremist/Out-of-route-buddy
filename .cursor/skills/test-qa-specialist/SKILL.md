---
name: test-qa-specialist
description: >-
  Specializes in test strategy, unit tests, Robolectric, and QA for OutOfRouteBuddy.
  Use when fixing ignored tests, writing test plans, running tests, or when the user
  asks for QA or test help.
---

# Test/QA Specialist

## Quick Reference

Read `docs/qa/TEST_STRATEGY.md` and `docs/qa/FAILING_OR_IGNORED_TESTS.md` before test work.

---

## Test Layers

| Layer | Location | When |
|-------|----------|------|
| Unit | app/src/test/ | Every commit; CI |
| Robolectric | app/src/test/ | Fragments, dialogs, ViewModels |
| Instrumented | app/src/androidTest/ | Pre-release; device/emulator |

**Improvement Loop:** Unit tests only. No instrumented tests in loop. Do not suggest "move to instrumented" for ignored tests.

---

## Quality Gates

- `./gradlew testDebugUnitTest` — must pass
- `./gradlew lint` — abortOnError = true
- `jacocoSuiteTestsOnly` — coverage report (accepted CI gate)
- Tests verify Known Truths per `docs/qa/SSOT_TEST_SCENARIOS.md`

---

## Fixing Ignored Tests

1. Check `docs/qa/FAILING_OR_IGNORED_TESTS.md` for reason and owner
2. **Options:** Fix in unit suite, add @Ignore with reason, or defer
3. For ViewModels: use TestDispatcher, inject ioDispatcher
4. For Robolectric: ApplicationProvider, mock repository
5. Document resolution in FAILING_OR_IGNORED_TESTS

---

## Test Patterns

- **Names:** `should_doSomething_whenCondition` or `givenX_whenY_thenZ`
- **Doubles:** Prefer fakes/stubs over mocks
- **ViewModels:** runTest, TestScope, advanceTimeBy for delays
- **Simulations:** MockGpsSynchronizationService, MockTripRepository per `docs/qa/SIMULATIONS_AND_MOCKS.md`

---

## Per-Feature Test Plans

For major features (Auto drive, Export): create `TEST_PLAN_<feature>.md` using `docs/qa/TEST_PLAN_TEMPLATE.md`.

---

## Additional Resources

- Strategy: [docs/qa/TEST_STRATEGY.md](../../../docs/qa/TEST_STRATEGY.md)
- Ignored tests: [docs/qa/FAILING_OR_IGNORED_TESTS.md](../../../docs/qa/FAILING_OR_IGNORED_TESTS.md)
- Simulations: [docs/qa/SIMULATIONS_AND_MOCKS.md](../../../docs/qa/SIMULATIONS_AND_MOCKS.md)
