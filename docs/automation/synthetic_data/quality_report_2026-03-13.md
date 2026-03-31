# Synthetic Data Loop — Quality Report (2026-03-13)

**Loop #:** 3  
**Provenance:** Source doc: SYNTHETIC_DATA_LOOP_ROUTINE; Run date: 2026-03-13; Loop #3.

---

## 1. Data quality summary

| Metric | Value |
|--------|--------|
| **Trip data (live)** | None in this environment (no DB) |
| **Gathered sources** | 15 (tests, mocks, research, warm-up, usefulness doc) — see [gather_manifest_2026-03-13.md](./gather_manifest_2026-03-13.md) |
| **Tier separation** | Verified in code: GOLD = human-only (TripStatePersistence); PLATINUM/SILVER = synthetic; DATA_TIERS and tests enforce. |
| **Pruning proposal** | N/A — [pruning_proposal_2026-03-13.md](./pruning_proposal_2026-03-13.md) |

---

## 2. Validation checklist (Phase 3)

| Check | Status |
|-------|--------|
| **Schema consistency** | TripEntity.dataTier, domain DataTier enum, Room DataTierConverter aligned; tests (DataTierTest, DataTierConverterTest, DataLoopTierWiringTest) pass. |
| **Tier separation verified** | GOLD never auto-created by loop; synthetic = PLATINUM/SILVER only. DATA_TIERS § Rule: Verifiable separation. |
| **Business rules (GOLD human-only)** | KNOWN_TRUTHS and DATA_TIERS document; saveCompletedTrip sets GOLD; loop does not call setTripTier(_, GOLD). |
| **Usefulness criteria** | Per DATA_USEFULNESS_AND_PRUNING_RESEARCH: outputs are actionable (paths, Loop #), traceable (run date, ledger), reusable (manifest, report). |

---

## 3. Gaps and next steps

- **Live data:** When device/emulator or test DB has trips, run getTripsByTier and propose SILVER (and optionally PLATINUM) candidates for termination; user approves before apply.
- **Provenance:** Add "Provenance" line to every quality report (source doc, run date, Loop #) — proposed in SYNTHETIC_DATA_LOOP_RESEARCH for next run.
- **Generator/export:** Consider PLATINUM/SILVER fixture generator or export-by-tier script when ready (from Loop #2 Next).

---

*Report produced by Synthetic Data Loop Phase 3. Loop #3. See [SYNTHETIC_DATA_LOOP_RUN_LEDGER.md](../SYNTHETIC_DATA_LOOP_RUN_LEDGER.md).*
