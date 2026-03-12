# Loop Master — Role and Master Loop

**Purpose:** Define the Loop Master role and the **Master Loop** (trigger: **"start master loop"**). The Loop Master researches all loops in the project, compares and scrutinizes them, sets the standard, and updates universal files so all loop-bearing agents can follow and improve.

**Trigger:** User says **"start master loop"**.

---

## Loop Master role

- **Authority:** Sets the standard for how loops run and how they are documented. Universal files (best practices, for-other-agents entry point, run ledger template, tier definitions) are updated by the Loop Master so other agents have one source of truth.
- **Duty:** At the start of every Master Loop run, **research all other loops** in the project, **compare, analyze, and scrutinize** them, then **update universal files** so loop-bearing agents can follow and improve.
- **Scope:** Improvement Loop is the primary loop the Loop Master runs; the Master step (research + sync) precedes the normal Improvement Loop phases.

---

## Loops in this project (research these)

| Loop | Trigger | Key docs |
|------|---------|----------|
| **Improvement Loop** | GO, run improvement loop | IMPROVEMENT_LOOP_ROUTINE.md, LOOP_TIERING.md, IMPROVEMENT_LOOP_BEST_PRACTICES.md, IMPROVEMENT_LOOP_RUN_LEDGER.md |
| **Token Loop** | start token loop | TOKEN_REDUCTION_LOOP.md, TOKEN_LOOP_NEXT_TASKS.md, TOKEN_LOOP_MASTER_PLAN.md |
| **Cyber Security Loop** | Run Cyber Security Loop, GO security | CYBER_SECURITY_LOOP_ROUTINE.md, CYBER_SECURITY_LOOP_COMMON_SENSE.md |
| **Synthetic Data Loop** | Start Synthetic data loop, START DATA LOOP | SYNTHETIC_DATA_LOOP_ROUTINE.md, SYNTHETIC_DATA_LOOP_FOR_OTHER_AGENTS.md, SYNTHETIC_DATA_LOOP_RUN_LEDGER.md |

*(Add or remove rows as the project adds or retires loops.)*

---

## Master Loop flow (when user says "start master loop")

### Step 0.M — Loop Master step (before Phase 0)

Do this **first**, before checkpoint and Phase 0 research:

1. **Research all other loops** — Read the key docs for each loop listed above (routines, common sense, run ledgers, next-tasks). Identify triggers, phases, what each loop records, and where it writes.
2. **Compare and analyze** — Note overlaps (e.g. checkpoint, run ledger), gaps (e.g. one loop has no ledger), and drift (e.g. different tier names or phase counts). Scrutinize for consistency with IMPROVEMENT_LOOP_BEST_PRACTICES and IMPROVEMENT_LOOP_FOR_OTHER_AGENTS.
3. **Set the standard** — Decide what the canonical pattern is (e.g. checkpoint first, tiering Light/Medium/Heavy, append run ledger every run, one entry point for other agents). If another loop’s docs contradict the standard, note it for sync.
4. **Update universal files** — So all loop-bearing agents can follow and improve:
   - [IMPROVEMENT_LOOP_BEST_PRACTICES.md](./IMPROVEMENT_LOOP_BEST_PRACTICES.md) — Add or adjust recommendations so they apply across loops where relevant (e.g. “every loop records a run in a ledger”).
   - [IMPROVEMENT_LOOP_FOR_OTHER_AGENTS.md](./IMPROVEMENT_LOOP_FOR_OTHER_AGENTS.md) — Ensure entry point and links are current; add a one-line list of “other loops in this project” if helpful.
   - [IMPROVEMENT_LOOP_RUN_LEDGER.md](./IMPROVEMENT_LOOP_RUN_LEDGER.md) — Ensure template and “how to append” are clear; no change if already good.
   - [LOOP_TIERING.md](./LOOP_TIERING.md) — Keep tier definitions and approval rules the standard for improvement loop; note in doc if other loops use different tiering.
   - Optional: Add a short “Loop Master findings” subsection to the run summary (e.g. “Other loops reviewed: X. Universal files updated: Y.”).

5. **Then run the Improvement Loop** — Proceed to checkpoint (0.0), Phase 0 research (0.1), tiering (0.1b), and Phases 1–4 per [IMPROVEMENT_LOOP_ROUTINE.md](./IMPROVEMENT_LOOP_ROUTINE.md). Append this run to IMPROVEMENT_LOOP_RUN_LEDGER.md at the end.

---

## Universal files (Loop Master maintains)

| File | Purpose |
|------|--------|
| [IMPROVEMENT_LOOP_BEST_PRACTICES.md](./IMPROVEMENT_LOOP_BEST_PRACTICES.md) | Best practices for any loop; what to record every run; checklist. |
| [IMPROVEMENT_LOOP_FOR_OTHER_AGENTS.md](./IMPROVEMENT_LOOP_FOR_OTHER_AGENTS.md) | Entry point for other agents; links to best practices and recorded data. |
| [IMPROVEMENT_LOOP_RUN_LEDGER.md](./IMPROVEMENT_LOOP_RUN_LEDGER.md) | Template and instructions for appending each improvement loop run. |
| [LOOP_TIERING.md](./LOOP_TIERING.md) | Light / Medium / Heavy definitions and approval rules (improvement loop standard). |
| This file | Loop Master role and Master Loop flow. |

---

## Trigger summary

| User says | Action |
|-----------|--------|
| **start master loop** | Run Step 0.M (research all loops, compare/analyze/scrutinize, update universal files), then run full Improvement Loop (checkpoint → phases 0–4 → summary → append ledger). |
| **GO** / run improvement loop | Run Improvement Loop only (no Loop Master step). |

---

*Loop Master sets the standard. Other loop-bearing agents follow the universal files updated by the Master Loop.*
