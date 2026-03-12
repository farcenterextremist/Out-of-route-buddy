# Synthetic Data Loop — Routine

**Objective:** Create, prune, and mesh synthetic/simulated data; produce quality reports; user approves tier changes before application.

**Trigger:** User says **"Start Synthetic data loop"**, **"START DATA LOOP"**, or "run synthetic data loop" / "data loop".

**For other agents:** Read [SYNTHETIC_DATA_LOOP_FOR_OTHER_AGENTS.md](./SYNTHETIC_DATA_LOOP_FOR_OTHER_AGENTS.md) at loop start. At the end of every run, append one block to [SYNTHETIC_DATA_LOOP_RUN_LEDGER.md](./SYNTHETIC_DATA_LOOP_RUN_LEDGER.md).

**Data tiers:** [docs/DATA_TIERS.md](../DATA_TIERS.md). GOLD = human-only; PLATINUM/SILVER = synthetic. No auto tier changes without user approval. **Rule:** Keep synthetic verifiably separate from human data; use synthetic to suggest improvements to how we gather human data (see DATA_TIERS § Rule: Verifiable separation).

---

## Phase 0: Research & checkpoint

| Step | Action |
|------|--------|
| **0.0** | **Research data-loop best practices.** Search for current/popular practices (e.g. "synthetic data pipeline best practices", "data quality loop", "human-in-the-loop data" 2024–2025). Update [SYNTHETIC_DATA_LOOP_RESEARCH.md](./SYNTHETIC_DATA_LOOP_RESEARCH.md): append § Findings (2–4 bullets with "Applies: …" to our loop) and § Suggested improvements (1–2 items for routine or next run). Use findings to inform this run. |
| 0.1 | Read [SYNTHETIC_DATA_LOOP_MASTER_PLAN.md](./SYNTHETIC_DATA_LOOP_MASTER_PLAN.md), [SYNTHETIC_DATA_LOOP_FOR_OTHER_AGENTS.md](./SYNTHETIC_DATA_LOOP_FOR_OTHER_AGENTS.md), [docs/DATA_TIERS.md](../DATA_TIERS.md). |
| 0.2 | Check [SYNTHETIC_DATA_LOOP_RUN_LEDGER.md](./SYNTHETIC_DATA_LOOP_RUN_LEDGER.md) for last run and "Next" items; check [SYNTHETIC_DATA_LOOP_RESEARCH.md](./SYNTHETIC_DATA_LOOP_RESEARCH.md) for latest suggested improvements. |
| 0.3 | Checkpoint: `git add -A && git commit -m "Pre-synthetic-data-loop YYYY-MM-DD"` or `git tag synthetic-data-loop-pre-YYYYMMDD`. Note checkpoint for ledger. |

**Output:** Research doc updated; ready to run; checkpoint recorded.

---

## Phase 1: Create / gather

| Step | Action |
|------|--------|
| 1.1 | Decide focus: create new synthetic data, or gather existing (e.g. from simulations, exports). |
| 1.2 | If creating: generate synthetic/simulated trips (PLATINUM or SILVER tier per DATA_TIERS). Write to project path (e.g. `docs/automation/synthetic_data/` or agreed path). |
| 1.3 | If gathering: collect from repo (e.g. test fixtures, simulation outputs); document source and tier. |
| 1.4 | List created/gathered artifacts in a short manifest (path + tier + count). |

**Output:** Synthetic data files and/or manifest.

---

## Phase 2: Prune & mesh

| Step | Action |
|------|--------|
| 2.1 | **Prune:** Identify candidates for termination (SILVER) or demotion. Produce a **pruning proposal** (e.g. trip IDs, tier changes, or delete list). Do **not** apply yet. |
| 2.2 | **Mesh:** Propose merges, deduplication, or alignment (e.g. same-route duplicates). Add to proposal or separate mesh proposal file. |
| 2.3 | Write proposal to `docs/automation/synthetic_data/pruning_proposal_YYYY-MM-DD.md` (or similar). Clearly separate "proposed" vs "applied." |

**Output:** Pruning/mesh proposal file(s).

---

## Phase 3: Quality report

| Step | Action |
|------|--------|
| 3.1 | Summarize data quality: counts by tier, coverage, gaps, anomalies. |
| 3.2 | Optional: data-analyzing session notes (what was explored, findings). |
| 3.3 | Write report to `docs/automation/synthetic_data/quality_report_YYYY-MM-DD.md` (or path in master plan). |

**Output:** Quality report (and optional session notes).

---

## Phase 4: User approval & ledger

| Step | Action |
|------|--------|
| 4.1 | Present to user: pruning/mesh proposal and quality report. Ask: "Approve tier changes / deletes? (yes / no / partial list)." |
| 4.2 | If user approves (all or partial): apply **only** approved items (`setTripTier`, `deleteTripsOlderThan`, or listed deletes). If no approval: do not apply; note in ledger. |
| 4.3 | Append one block to [SYNTHETIC_DATA_LOOP_RUN_LEDGER.md](./SYNTHETIC_DATA_LOOP_RUN_LEDGER.md) using the template there. Include: Focus, Outputs, Tier changes applied, Metrics (checkpoint), Next. |

**Output:** Ledger block appended; tier changes applied only if approved.

---

*Loop complete. Next run: use "Next" from ledger or master plan.*
