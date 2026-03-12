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
