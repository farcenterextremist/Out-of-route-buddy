# Synthetic Data Loop — For Other Agents

**Purpose:** When you (any agent) run the Synthetic Data Loop, read this doc first and follow it. At the end of every run, append one block to the data-loop run ledger.

**When:** User says **"Start Synthetic data loop"**, **"START DATA LOOP"**, or "run synthetic data loop" / "data loop". At **loop start** — read this doc and the routine. At **loop end** (Phase 4) — append one block to [SYNTHETIC_DATA_LOOP_RUN_LEDGER.md](./SYNTHETIC_DATA_LOOP_RUN_LEDGER.md).

---

## What to read at loop start

1. **[LOOP_GATES.md](./LOOP_GATES.md) (required):** At loop start read in order: LOOP_MASTER_ROLE.md, LOOPS_AND_IMPROVEMENT_FULL_AXIS.md, [UNIVERSAL_LOOP_PROMPT.md](../agents/data-sets/hub/UNIVERSAL_LOOP_PROMPT.md), and [LOOP_CONSISTENCY_STANDARD.md](./LOOP_CONSISTENCY_STANDARD.md); then hub/README.md, loop_shared_events.jsonl (tail), loop_latest/*.json. Log **Hub consulted** and **Advice applied** before execution.
2. **This doc** — best practices and ledger requirement.
3. [SYNTHETIC_DATA_LOOP_MASTER_PLAN.md](./SYNTHETIC_DATA_LOOP_MASTER_PLAN.md) — trigger, scope, phases, data-tier linkage.
4. [SYNTHETIC_DATA_LOOP_ROUTINE.md](./SYNTHETIC_DATA_LOOP_ROUTINE.md) — phases 0–4 step-by-step (Phase 0 includes **research**).
5. [docs/DATA_TIERS.md](../DATA_TIERS.md) — SILVER / PLATINUM / GOLD semantics and APIs.
6. **Hub at start (Universal Loop):** Open [docs/agents/data-sets/hub/README.md](../agents/data-sets/hub/README.md); scan the Hub index; read or skim files relevant to data loop (e.g. data-sets index, DATA_USEFULNESS_AND_PRUNING_RESEARCH). Note **Hub consulted:** [list] and **Advice/rules applied:** [1–3 bullets]. See [UNIVERSAL_LOOP_PROMPT.md](../agents/data-sets/hub/UNIVERSAL_LOOP_PROMPT.md).
7. **Phase 0.0 — Research:** Before checkpoint, search for **current/popular data-loop and synthetic-data best practices** (e.g. 2024–2025); update [SYNTHETIC_DATA_LOOP_RESEARCH.md](./SYNTHETIC_DATA_LOOP_RESEARCH.md) with findings and 1–2 suggested improvements so the loop is **self-improving**. Use that research to inform the run.

---

## Best practices (follow these)

1. **Checkpoint before changes.** Git commit or tag so the user can revert. Note checkpoint in the ledger block.

2. **Tier changes need user approval.** Do not call `setTripTier` or `deleteTripsOlderThan` (or apply pruning) until the user has approved the proposal. Output a pruning/mesh proposal file; wait for approval; then apply only approved items.

3. **GOLD is human-only.** Do not create or auto-promote to GOLD. Loop may *suggest* promotion from PLATINUM after verification.

4. **Verifiable separation.** Keep synthetic data verifiably separate from human data (see [docs/DATA_TIERS.md](../DATA_TIERS.md) § Rule: Verifiable separation). Synthetic = PLATINUM/SILVER only; human = GOLD only. Use synthetic to *suggest* improvements to how we gather human data (e.g. validation, UX, export shape); do not auto-apply those changes to GOLD or production capture without approval.

5. **Write outputs into the project.** Synthetic data, pruning proposal, and quality report go to agreed paths (e.g. `docs/automation/synthetic_data/` or paths noted in the master plan). Reference them in the ledger block.

6. **Append one block to the run ledger.** At the end of every run, append a new block to [SYNTHETIC_DATA_LOOP_RUN_LEDGER.md](./SYNTHETIC_DATA_LOOP_RUN_LEDGER.md) using the template in that file. **Include Loop #** (increment from last run); report to user: Loop #, proof of work, benefits.

7. **Research at start, self-improve.** In Phase 0, research current data-loop/synthetic-data best practices and update [SYNTHETIC_DATA_LOOP_RESEARCH.md](./SYNTHETIC_DATA_LOOP_RESEARCH.md); add 1–2 suggested improvements so the next run or the routine can be updated. Include [QUALITY_STANDARDS_AND_PROOF.md](./QUALITY_STANDARDS_AND_PROOF.md) and define the run’s proof-of-quality evidence bundle.

8. **Consider send to hub when done.** If the run produced **completed, polished** output (e.g. quality report, data-sets index, or pruning summary) that other agents or the next run should use, consider depositing to [docs/agents/data-sets/hub/](../agents/data-sets/hub/) with naming `YYYY-MM-DD_data-loop_<short-description>.<ext>` and optionally add a one-line entry to hub/README.md. See [SEND_TO_HUB_PROMPT.md](../agents/data-sets/hub/SEND_TO_HUB_PROMPT.md).

9. **LOOP GATES at loop end (required):** Per [LOOP_GATES.md](./LOOP_GATES.md): send polished outputs to hub (YYYY-MM-DD_<role-or-topic>_<short-description>.<ext>), add/update one line in hub/README.md, append finished event to [loop_shared_events.jsonl](./loop_shared_events.jsonl), update [loop_latest/synthetic_data.json](./loop_latest/synthetic_data.json).
10. **Report consistency score.** In quality report or ledger include `Loop Consistency Check` and `Consistency score: X/10` per [LOOP_CONSISTENCY_STANDARD.md](./LOOP_CONSISTENCY_STANDARD.md).

---

## At loop end: ledger block (required)

After producing the quality report and (if applicable) applying approved tier changes, open [SYNTHETIC_DATA_LOOP_RUN_LEDGER.md](./SYNTHETIC_DATA_LOOP_RUN_LEDGER.md) and **append** one block. Use the template in that file; fill Focus, Outputs, Metrics, **Proof of quality**, Next. Then complete LOOP GATES (above): hub deposit, hub/README.md, loop_shared_events.jsonl, loop_latest/synthetic_data.json.

---

*Other agents: read this at start; run phases 0–4 per routine; append to the data-loop ledger at end.*
