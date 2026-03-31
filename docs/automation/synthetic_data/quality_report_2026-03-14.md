# Synthetic Data Loop — Quality Report (2026-03-14)

**Provenance:** Source doc: SYNTHETIC_DATA_LOOP_ROUTINE; Run date: 2026-03-14; **Loop #4.**

---

## 1. Data quality summary

| Metric | Value |
|--------|--------|
| **Trip data (live)** | None (no DB in this environment) |
| **Gathered sources** | 15+ (tests, mocks, research, warm-up, usefulness) — see [gather_manifest_2026-03-14.md](./gather_manifest_2026-03-14.md) |
| **Tier separation** | Verified: GOLD = human-only; PLATINUM/SILVER = synthetic; DATA_TIERS and tests enforce. |
| **Pruning proposal** | N/A — [pruning_proposal_2026-03-14.md](./pruning_proposal_2026-03-14.md) |

---

## 2. Validation checklist

| Check | Status |
|-------|--------|
| **Schema consistency** | TripEntity.dataTier, DataTier enum, DataTierConverter aligned; tier tests pass. |
| **Tier separation verified** | GOLD never auto-created by loop; synthetic = PLATINUM/SILVER only. |
| **Business rules (GOLD human-only)** | KNOWN_TRUTHS, DATA_TIERS; saveCompletedTrip sets GOLD; loop does not set GOLD. |
| **Usefulness criteria** | Outputs actionable, traceable (Provenance), reusable (manifest, report). |
| **Provenance** | Every quality report includes: source doc, run date, Loop # (lightweight lineage). |

---

## 3. Gaps and next steps

- **Live data:** When device/emulator or test DB has trips, run getTripsByTier and propose SILVER/PLATINUM candidates; user approval before apply.
- **Quality metrics:** When generating synthetic trips, consider a short "quality metrics" subsection (tier mix, schema compliance) per Loop #4 research.
- **Generator/export:** PLATINUM/SILVER fixture generator or export-by-tier script when ready.

---

*Report produced by Synthetic Data Loop Phase 3. Loop #4. See [SYNTHETIC_DATA_LOOP_RUN_LEDGER.md](../SYNTHETIC_DATA_LOOP_RUN_LEDGER.md).*
