# Data loop warm-up — prepare before "Start Synthetic data loop"

**Purpose:** Read this before running the Synthetic Data Loop so the run is aligned with Hub, tiers, and ledger. Use when you say "prepare and warm up for data loop" or before "START DATA LOOP".

**Trigger for the loop itself:** "Start Synthetic data loop", "START DATA LOOP", or "run synthetic data loop" / "data loop".

---

## 1. Pre-flight checklist

| Item | Status |
|------|--------|
| [SYNTHETIC_DATA_LOOP_FOR_OTHER_AGENTS.md](./SYNTHETIC_DATA_LOOP_FOR_OTHER_AGENTS.md) | Entry point; read first when loop starts |
| [SYNTHETIC_DATA_LOOP_MASTER_PLAN.md](./SYNTHETIC_DATA_LOOP_MASTER_PLAN.md) | Trigger, scope, phases, tier linkage |
| [SYNTHETIC_DATA_LOOP_ROUTINE.md](./SYNTHETIC_DATA_LOOP_ROUTINE.md) | Phases 0–4 step-by-step |
| [docs/DATA_TIERS.md](../DATA_TIERS.md) | SILVER / PLATINUM / GOLD; verifiable separation; no tier changes without approval |
| [SYNTHETIC_DATA_LOOP_RUN_LEDGER.md](./SYNTHETIC_DATA_LOOP_RUN_LEDGER.md) | Last run = Loop #2 (2026-03-12); **next run = Loop #3** |
| [SYNTHETIC_DATA_LOOP_RESEARCH.md](./SYNTHETIC_DATA_LOOP_RESEARCH.md) | Findings + suggested improvements; update in Phase 0 |
| [docs/agents/data-sets/hub/README.md](../agents/data-sets/hub/README.md) | Read at loop start; note Hub consulted + Advice applied |
| [UNIVERSAL_LOOP_PROMPT.md](../agents/data-sets/hub/UNIVERSAL_LOOP_PROMPT.md) | Research = LOOP_LESSONS_LEARNED, SELF_IMPROVING_LOOP_RESEARCH, CURSOR_SELF_IMPROVEMENT; read Hub; consider send to hub when done |
| [DATA_USEFULNESS_AND_PRUNING_RESEARCH.md](./DATA_USEFULNESS_AND_PRUNING_RESEARCH.md) | Usefulness criteria; what to keep/prune; optional for Phase 0 |
| Output folder `docs/automation/synthetic_data/` | Exists; use for gather manifest, pruning proposal, quality report |

---

## 2. Rules to keep in mind

- **Checkpoint before changes.** Git commit or tag at start of Phase 0; note in ledger.
- **No tier changes without user approval.** Produce pruning/mesh **proposal** only; do not call `setTripTier` or `deleteTripsOlderThan` until user approves.
- **GOLD = human-only.** Do not create or auto-promote to GOLD. Synthetic = PLATINUM/SILVER only.
- **Verifiable separation.** Keep synthetic data separate from human data; use synthetic to *suggest* improvements to human data gathering, do not auto-apply to GOLD.
- **Every run:** Append one block to the ledger with **Loop #**, proof of work, benefits; report Loop # to the user at end.
- **Consider send to hub when done.** If quality report or data summary is polished and reusable, deposit to `docs/agents/data-sets/hub/` with `YYYY-MM-DD_data-loop_<short-description>.<ext>` and optional index entry.

---

## 3. Phase summary (when you run the loop)

| Phase | Action |
|-------|--------|
| **0** | Research (data-loop best practices; update SYNTHETIC_DATA_LOOP_RESEARCH); read Master Plan, DATA_TIERS, ledger, **Hub**; checkpoint |
| **1** | Create/gather synthetic data (PLATINUM/SILVER); write to `synthetic_data/`; manifest |
| **2** | Prune & mesh proposal (do not apply); write proposal file |
| **3** | Quality report (counts by tier, gaps, optional validation checklist) |
| **4** | Present to user; apply only approved tier changes; append ledger block (Loop #3); consider send to hub |

---

## 4. Next Loop #

**Next run is Loop #3.** Increment from the last block in [SYNTHETIC_DATA_LOOP_RUN_LEDGER.md](./SYNTHETIC_DATA_LOOP_RUN_LEDGER.md). In the new block include: Loop #3, Focus, Outputs, Tier changes applied, Metrics, Proof of work, Benefits, Next.

---

*Warm-up complete. When ready, say "Start Synthetic data loop" or "START DATA LOOP" and the agent will read FOR_OTHER_AGENTS + routine and run phases 0–4.*
