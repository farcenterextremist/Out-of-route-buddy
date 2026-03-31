# Simulations and Lightweight Mocks

**Purpose:** Document the simulation and mock strategy for OutOfRouteBuddy tests. Use simulations and lightweight mocks to test behavior without device/emulator or heavy dependencies.

**References:** [TEST_STRATEGY.md](./TEST_STRATEGY.md), [FAILING_OR_IGNORED_TESTS.md](./FAILING_OR_IGNORED_TESTS.md)

---

## Strategy

| Approach | When to use | Location |
|----------|-------------|----------|
| **Simulations** | Verify data flow, wiring, period logic without real services | `app/src/test/.../simulation/` |
| **Lightweight mocks** | Stub interfaces for unit tests; configurable behavior | `app/src/test/.../utils/` |
| **MockK / Mockito** | One-off mocks in individual tests | Inline in test classes |

---

## Existing Simulations

| Simulation | Purpose |
|------------|---------|
| **SimulatedTripTest** | Trip lifecycle: start → track → end → save. Uses MockGpsSynchronizationService. |
| **StatCardCalendarWiringSimulationTest** | Period → stats → datesWithTrips. Verifies calendar/stat card wiring. |
| **StatCardCalendarWithMockRepositorySimulationTest** | Same wiring as above, but uses MockTripRepository (lightweight mock) instead of MockK. |
| **test_loop_listener.ps1** | Simulates loop events and validates JSONL listener output for automation telemetry. |
| **test_loop_desktop_guide_export.ps1** | Simulates export of the human-readable Loopmaster desktop guide from repo source of truth. |

---

## Existing Mocks

| Mock | Purpose |
|------|---------|
| **MockGpsSynchronizationService** | Controlled GPS data; `simulateDriving()`, `emitDistance()`. No device. |
| **MockServices.MockLocationValidationService** | Configurable validation results; `setShouldFail()`, `setValidationResult()`. |
| **MockServices.MockLocationCache** | Configurable hit rate; `setHitRate()`, `setShouldFail()`. |
| **MockServices.MockPerformanceMonitor** | Track validation times; `setMockUptime()`, `setOperationTimes()`. |
| **MockServices.MockUnifiedTripService** | Trip calculations, period stats; `calculateTrip()`, `calculatePeriodStatistics()`. |
| **MockServices.UnifiedMockServiceFactory** | Create full mock suite; `createUnifiedServiceSuite()`, `createMockTripService()`. |
| **TestValidationUtils** | `createMockLocation()`, `createMockRouteLocations()`, `createMockTripEntity()`. |
| **TestLocationUtils** | `createMockLocation()` with params; used by TrafficStateMachineTest, AdaptiveGpsAccuracyTest. |
| **MockTripRepository** | In-memory TripRepository; `setTrips()`, `setPeriodStats()`, `setYearStats()`, `setMonthlyStats()`, `setTodayStats()`, `setShouldFail()`. Has unit tests in MockTripRepositoryTest. |

---

## Adding New Simulations

1. **Place in** `app/src/test/.../simulation/`.
2. **Use** MockGpsSynchronizationService, MockServices, or mockk for dependencies.
3. **Verify** data flow or wiring — not implementation details.
4. **No instrumented tests** — unit/simulation only in this environment.

---

## Adding New Lightweight Mocks

1. **Place in** `app/src/test/.../utils/` (e.g. `MockTripRepository.kt`).
2. **Implement** the interface with configurable behavior.
3. **Expose** setters for `setShouldFail()`, `setReturnValue()`, etc.
4. **Keep** mocks simple — no real I/O, no network.

---

## Quick Reference

```
Simulations:  app/src/test/.../simulation/*.kt
Mocks:        app/src/test/.../utils/Mock*.kt, Test*Utils.kt
MockK:        mockk<T>(relaxed = true) for one-off stubs
Loop ops:     scripts/automation/test_loop_*.ps1
```

---

*Use simulations and mocks to maximize coverage without instrumented tests.*
