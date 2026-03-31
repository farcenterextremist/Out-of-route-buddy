# Synthetic Data Loop — Pruning Proposal (2026-03-13)

**Loop #:** 3  
**Status:** Proposed only. **Do not apply** until user approves.

---

## Environment

- **No live trip DB** in this run (Cursor/repo only). No device or emulator database was queried.

## Pruning proposal

- **Trip-tier changes:** N/A (no trip data to prune).
- **Demotions / terminations:** None proposed.
- **Mesh (merge/dedupe):** None proposed.

When a device or emulator DB is available with trip data: run `getTripsByTier(SILVER)` (and optionally PLATINUM), produce a list of candidate IDs for termination or demotion, and present in a future pruning proposal for user approval. Apply only after user says "approve" (all or partial list).

---

## Applied

- **Tier changes applied:** None (no approval step this run).

---

*Proposal produced by Synthetic Data Loop Phase 2. Loop #3. See [SYNTHETIC_DATA_LOOP_RUN_LEDGER.md](../SYNTHETIC_DATA_LOOP_RUN_LEDGER.md).*
