# Synthetic Data Loop — Gather Manifest (2026-03-14)

**Loop #:** 4  
**Run:** Fourth Synthetic Data Loop run.  
**Focus:** Gather existing sources; implement Provenance in quality report; no live DB.

**Hub consulted:** hub/README.md, 2026-03-13_data-loop_loop3-quality-summary.md, DATA_USEFULNESS_AND_PRUNING_RESEARCH.md.  
**Advice/rules applied:** Provenance in every quality report (Loop #3 Next); usefulness criteria; tier separation; Loop # + proof of work + benefits.

---

## Sources gathered (this repo)

| Source | Location | Tier / type | Notes |
|--------|----------|-------------|--------|
| **SimulatedTripTest** | `app/src/test/.../simulation/SimulatedTripTest.kt` | N/A | 7 scenarios |
| **MockTripRepository** / **MockTripRepositoryTest** | `app/src/test/.../utils/` | Configurable / tier tests | getTripsByTier, setTripTier, deleteTripsOlderThan |
| **DataTierConverterTest**, **DataTierTest**, **DataLoopTierWiringTest** | `app/src/test/.../data/`, `domain/models/` | Room / enum / wiring | SILVER/PLATINUM/GOLD |
| **DomainTripRepositoryAdapterTest**, **TripRepositoryTest** | `app/src/test/.../data/repository/` | DataTier, entity | getTripsByTier, setTripTier, deleteTripsOlderThan |
| **SYNTHETIC_ATTACK_SCENARIOS**, **security-exercises/** | `docs/agents/data-sets/` | Security synthetic | Purple, attack library |
| **SYNTHETIC_DATA_LOOP_RESEARCH.md** | `docs/automation/` | Self-improvement | Loop #4: quality metrics/feedback finding |
| **DATA_LOOP_WARMUP.md**, **DATA_USEFULNESS_AND_PRUNING_RESEARCH.md** | `docs/automation/` | Prep / usefulness | Checklist, criteria |

---

## Created this run

- None (gather-only). Research doc updated (Loop #4: quality metrics/feedback finding; Provenance suggestion marked Done). Quality report now includes **Provenance** (source doc, run date, Loop #) by default. Checkpoint tag: `synthetic-data-loop-pre-20260314`.

---

*Manifest produced by Synthetic Data Loop Phase 1. Loop #4. See [SYNTHETIC_DATA_LOOP_RUN_LEDGER.md](../SYNTHETIC_DATA_LOOP_RUN_LEDGER.md).*
