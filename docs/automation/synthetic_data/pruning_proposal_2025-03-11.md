# Pruning Proposal (2025-03-11)

**Run:** First Synthetic Data Loop run.  
**Status:** Proposed only — not applied. User approval required before any tier changes or deletes.

---

## Prune (termination / demotion)

| Action | Candidate | Reason |
|--------|-----------|--------|
| *None this run* | — | No live device/emulator trip database was queried this run. Pruning applies to persisted trips (e.g. SILVER termination, PLATINUM→SILVER demotion). |

**Next run:** If the loop runs in a context with access to the app database (e.g. instrumented test or exported DB), run `getTripsByTier(DataTier.SILVER)` and `getTripsByTier(DataTier.PLATINUM)` and list candidates for termination or demotion here.

---

## Mesh (merge / deduplicate)

| Action | Candidates | Reason |
|--------|-------------|--------|
| *None this run* | — | No duplicate or same-route trip set was identified. Meshing can be proposed when multiple synthetic trips represent the same logical trip or redundant test data. |

---

## Applied

- **Tier changes applied:** None (no user approval requested this run).
- **Deletes applied:** None.

---

*Proposal produced by Synthetic Data Loop Phase 2. Do not apply without user approval. See [SYNTHETIC_DATA_LOOP_FOR_OTHER_AGENTS.md](../SYNTHETIC_DATA_LOOP_FOR_OTHER_AGENTS.md).*
