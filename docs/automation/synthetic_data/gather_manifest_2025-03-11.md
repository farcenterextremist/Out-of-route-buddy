# Synthetic Data Loop — Gather Manifest (2025-03-11)

**Run:** First Synthetic Data Loop run.  
**Focus:** Gather existing synthetic/simulated data sources; no new trip files created this run.

---

## Sources gathered (this repo)

| Source | Location | Tier / type | Count / notes |
|--------|----------|-------------|----------------|
| **SimulatedTripTest** | `app/src/test/.../simulation/SimulatedTripTest.kt` | N/A (mocked save) | 7 scenarios: short commute, long haul 10% OOR, negative OOR, multiple trips, pause/resume, extreme OOR, real-time UI |
| **StatCardCalendarWiringSimulationTest** | `app/src/test/.../simulation/StatCardCalendarWiringSimulationTest.kt` | N/A | Period selection, datesWithTripsInPeriod, end-trip refresh |
| **StatCardCalendarWithMockRepositorySimulationTest** | `app/src/test/.../simulation/StatCardCalendarWithMockRepositorySimulationTest.kt` | N/A | Same wiring with MockTripRepository |
| **MockTripRepository** | `app/src/test/.../utils/MockTripRepository.kt` | Configurable | In-memory domain TripRepository; used by simulation tests |
| **DomainTripRepositoryAdapterTest** | `app/src/test/.../data/repository/DomainTripRepositoryAdapterTest.kt` | DataTier.GOLD/PLATINUM used | getTripsByTier, setTripTier, deleteTripsOlderThan covered |
| **TripRepositoryTest** | `app/src/test/.../data/repository/TripRepositoryTest.kt` | DataTier in entities | setTripTier, insert with dataTier |
| **SYNTHETIC_ATTACK_SCENARIOS** | `docs/agents/data-sets/SYNTHETIC_ATTACK_SCENARIOS.md` | Security synthetic | App validation + prompt-injection scenarios; training JSON format |
| **Security exercises / artifacts** | `docs/agents/data-sets/security-exercises/` | Security synthetic | Purple training, attack library, artifacts (e.g. JSON) |
| **SIMULATIONS_AND_MOCKS** | `docs/qa/SIMULATIONS_AND_MOCKS.md` | Doc | Index of simulations and mocks |

---

## Tier API usage (codebase)

| API | Where | Notes |
|-----|--------|--------|
| `getTripsByTier(tier)` | Domain TripRepository, adapter, data TripRepository (getTripEntitiesByTier, getTripIdsByTier) | Used in tests (DomainTripRepositoryAdapterTest) |
| `setTripTier(tripId, tier)` | Domain + data layer | Tests: adapter, TripRepositoryTest |
| `deleteTripsOlderThan(cutoff, maxTier)` | Domain + data layer | Tests: adapter (SILVER only) |

---

## Created this run

- None (gather-only). Next run may add: exported PLATINUM/SILVER trip fixtures or a small generated set in `docs/automation/synthetic_data/`.

---

*Manifest produced by Synthetic Data Loop Phase 1. See [SYNTHETIC_DATA_LOOP_RUN_LEDGER.md](../SYNTHETIC_DATA_LOOP_RUN_LEDGER.md).*
