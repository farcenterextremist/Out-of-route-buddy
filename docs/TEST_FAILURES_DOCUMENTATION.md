# Test Failures Documentation

**Purpose:** Document known test failure patterns and workarounds. See [FAILING_OR_IGNORED_TESTS.md](./qa/FAILING_OR_IGNORED_TESTS.md) for the current list of ignored/failing tests.

---

## Known patterns

| Pattern | Notes |
|---------|-------|
| **PerformanceTestSuite** | Environment-dependent thresholds; may fail on slower CI/machines. Relaxed for CI. |
| **Dispatcher-related flakiness** | MockGpsSynchronizationServiceTest, TripInputViewModelIntegrationTest. ioDispatcher injection applied where fixed. |

---

*See [FAILING_OR_IGNORED_TESTS.md](./qa/FAILING_OR_IGNORED_TESTS.md) for full tracking.*

---

## Active remediation plan (debugging pass)

To **fix the current 10 unit-test failures + Detekt/Kotlin mismatch + Windows test lock**, follow **[DEBUGGING_REMEDIATION_PLAN.md](./DEBUGGING_REMEDIATION_PLAN.md)** (phased checklist and commands).
