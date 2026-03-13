# Synthetic Data Loop — Gather Manifest (2026-03-12)

**Loop #:** 2  
**Run:** Second Synthetic Data Loop run.  
**Focus:** Gather existing + new tier-wiring sources; no live DB.

---

## Sources gathered (this repo)

| Source | Location | Tier / type | Count / notes |
|--------|----------|-------------|----------------|
| **SimulatedTripTest** | `app/src/test/.../simulation/SimulatedTripTest.kt` | N/A (mocked save) | 7 scenarios |
| **StatCardCalendarWiringSimulationTest** | `app/src/test/.../simulation/StatCardCalendarWiringSimulationTest.kt` | N/A | Period, datesWithTripsInPeriod |
| **StatCardCalendarWithMockRepositorySimulationTest** | `app/src/test/.../simulation/StatCardCalendarWithMockRepositorySimulationTest.kt` | N/A | MockTripRepository wiring |
| **MockTripRepository** | `app/src/test/.../utils/MockTripRepository.kt` | Configurable | getTripsByTier, setTripTier, deleteTripsOlderThan |
| **MockTripRepositoryTest** | `app/src/test/.../utils/MockTripRepositoryTest.kt` | Tier tests | getTripsByTier, setTripTier (Loop #2 added) |
| **DataTierConverterTest** | `app/src/test/.../data/util/DataTierConverterTest.kt` | Room converter | SILVER/PLATINUM/GOLD, null→GOLD default |
| **DataTierTest** | `app/src/test/.../domain/models/DataTierTest.kt` | Enum | Three tiers, GOLD last |
| **DataLoopTierWiringTest** | `app/src/test/.../data/DataLoopTierWiringTest.kt` | Loop wiring | Tier API through repository |
| **DomainTripRepositoryAdapterTest** | `app/src/test/.../data/repository/DomainTripRepositoryAdapterTest.kt` | DataTier | getTripsByTier, setTripTier, deleteTripsOlderThan |
| **TripRepositoryTest** | `app/src/test/.../data/repository/TripRepositoryTest.kt` | dataTier in entity | insertTrip with GOLD/PLATINUM in gpsMetadata |
| **SYNTHETIC_ATTACK_SCENARIOS** | `docs/agents/data-sets/SYNTHETIC_ATTACK_SCENARIOS.md` | Security synthetic | Scenarios + training format |
| **Security exercises** | `docs/agents/data-sets/security-exercises/` | Security synthetic | Purple, attack library |
| **SYNTHETIC_DATA_LOOP_RESEARCH.md** | `docs/automation/SYNTHETIC_DATA_LOOP_RESEARCH.md` | Self-improvement | Findings + suggested improvements (Loop #2 appended) |

---

## Created this run

- None (gather-only). Research doc updated (Phase 0). Ledger template updated to include **Loop #** and user-facing proof of work/benefits.

---

*Manifest produced by Synthetic Data Loop Phase 1. Loop #2. See [SYNTHETIC_DATA_LOOP_RUN_LEDGER.md](../SYNTHETIC_DATA_LOOP_RUN_LEDGER.md).*
