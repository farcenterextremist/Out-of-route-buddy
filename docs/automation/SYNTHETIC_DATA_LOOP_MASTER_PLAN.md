# Synthetic Data Loop — Master Plan

**Purpose:** Single place for the Synthetic Data Loop: trigger, scope, phases, data-tier linkage, and initiation. Update when steps or outputs change.

**Trigger:** User says **"Start Synthetic data loop"**, **"START DATA LOOP"**, or "run synthetic data loop" / "data loop".

**Scope:** Hybrid — trigger in Cursor; loop may invoke subagents/tasks; writes reports, synthetic data, and pruning lists into the project; user reviews and approves before applying tier changes or large edits.

**Related:** [docs/DATA_TIERS.md](../DATA_TIERS.md) (SILVER / PLATINUM / GOLD), [IMPROVEMENT_LOOP_FOR_OTHER_AGENTS.md](./IMPROVEMENT_LOOP_FOR_OTHER_AGENTS.md) (checkpoint, ledger pattern), [TOKEN_INITIATIVE_BRIEFING.md](../agents/TOKEN_INITIATIVE_BRIEFING.md) (token saving).

---

## 1. Goal

- **Create** synthetic/simulated data (PLATINUM or SILVER tier).
- **Prune** low-quality or redundant data (e.g. SILVER termination, demotion).
- **Mesh** data (combine, deduplicate, or align for quality).
- **Output** quality reports and data-analyzing session artifacts; user approves before tier changes.
- **Rule:** Keep synthetic data **verifiably separate** from human data (GOLD = human only; PLATINUM/SILVER = synthetic). Use synthetic data to **suggest improvements** to how we gather human data (e.g. validation, UX, export shape); do not auto-apply those suggestions to GOLD or production capture without approval. See [docs/DATA_TIERS.md](../DATA_TIERS.md) § Rule: Verifiable separation.

---

## 2. Data tier linkage

| Tier | Role in loop |
|------|----------------|
| **GOLD** | Human-only; never auto-created by loop. Loop may *suggest* promotion from PLATINUM after verification. |
| **PLATINUM** | Synthetic/simulated data lives here (and below). Loop can create, demote to SILVER, or suggest promotion to GOLD. |
| **SILVER** | Candidate for termination. Loop can create (unsure data), prune, or recommend delete. |

**APIs used:** `getTripsByTier`, `setTripTier`, `deleteTripsOlderThan(cutoff, maxTier)`, `copyForEdit()` for GOLD-safe edits. See [docs/DATA_TIERS.md](../DATA_TIERS.md).

---

## 3. Phases (high level)

| Phase | Name | Action |
|-------|------|--------|
| 0 | Research & checkpoint | **Research** current/popular data-loop and synthetic-data best practices; update [SYNTHETIC_DATA_LOOP_RESEARCH.md](./SYNTHETIC_DATA_LOOP_RESEARCH.md) (findings + suggested improvements). Read DATA_TIERS, routine, for-other-agents; checkpoint (git commit/tag). |
| 1 | Create / gather | Generate or gather synthetic data; write to project (e.g. `docs/automation/synthetic_data/` or agreed path). |
| 2 | Prune & mesh | Propose pruning (SILVER termination, demotions); propose meshing (merge/dedupe). Output proposal file. |
| 3 | Quality report | Produce quality report and data-analyzing session summary. |
| 4 | User approval & ledger | User approves tier changes / big edits; agent applies only approved items; append one block to [SYNTHETIC_DATA_LOOP_RUN_LEDGER.md](./SYNTHETIC_DATA_LOOP_RUN_LEDGER.md). |

Detail: [SYNTHETIC_DATA_LOOP_ROUTINE.md](./SYNTHETIC_DATA_LOOP_ROUTINE.md).

---

## 4. Inputs (read at loop start)

| Doc | Purpose |
|-----|--------|
| [SYNTHETIC_DATA_LOOP_FOR_OTHER_AGENTS.md](./SYNTHETIC_DATA_LOOP_FOR_OTHER_AGENTS.md) | Best practices; what to read; ledger at end. |
| [SYNTHETIC_DATA_LOOP_RESEARCH.md](./SYNTHETIC_DATA_LOOP_RESEARCH.md) | **Research & self-improvement:** findings from data-loop best practices; suggested improvements for routine/next run. Update at loop start (Phase 0). |
| [docs/DATA_TIERS.md](../DATA_TIERS.md) | Tier semantics, APIs, retention. |
| [SYNTHETIC_DATA_LOOP_ROUTINE.md](./SYNTHETIC_DATA_LOOP_ROUTINE.md) | Phases 0–4 step-by-step. |
| [SYNTHETIC_DATA_LOOP_RUN_LEDGER.md](./SYNTHETIC_DATA_LOOP_RUN_LEDGER.md) | Latest run; append after Phase 4. |

---

## 5. Outputs (per run)

| Output | Location / form |
|--------|------------------|
| Synthetic data (if created) | Project path TBD (e.g. `docs/automation/synthetic_data/` or export file). |
| Pruning proposal | Markdown or JSON list (trip IDs, tier changes, deletes). |
| Quality report | Markdown summary; optional data-analyzing session notes. |
| Ledger block | Appended to SYNTHETIC_DATA_LOOP_RUN_LEDGER.md. |

---

## 6. Initiation checklist (ready for first run)

- [x] Master plan created (this doc).
- [x] For-other-agents entry point and ledger template.
- [x] Routine (phases 0–4) defined.
- [x] Wired in README, AGENTS.md, IMPROVEMENT_LOOP_INDEX.
- [x] Optional: dedicated output folder (e.g. `docs/automation/synthetic_data/`) created.
- [ ] Optional: subagent/task types and data-in from other agents (per design choice; default: Cursor-only, this repo).

---

## 7. Completion tracking

| Item | Done |
|------|------|
| Master plan | [x] |
| For-other-agents + ledger | [x] |
| Routine (phases 0–4) | [x] |
| AGENTS.md + README + Index | [x] |
| First run (user says "Start Synthetic data loop") | [x] |

*Recompute when new items are added or completed.*

---

*For initiation: when user says "Start Synthetic data loop" or "START DATA LOOP", read [SYNTHETIC_DATA_LOOP_FOR_OTHER_AGENTS.md](./SYNTHETIC_DATA_LOOP_FOR_OTHER_AGENTS.md) and [SYNTHETIC_DATA_LOOP_ROUTINE.md](./SYNTHETIC_DATA_LOOP_ROUTINE.md); run phases 0–4; append to run ledger.*
