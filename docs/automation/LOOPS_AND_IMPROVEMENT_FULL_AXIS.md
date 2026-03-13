# Loops and Improvement — Full Axis

**Purpose:** Single index of **all loops** in the project: triggers, outputs, and where to find docs/scripts. Used by the Loop Master (Step 0.M) and by any agent to see the full picture. Keep this doc updated when new loops are added.

**Loop Master:** [LOOP_MASTER_ROLE.md](./LOOP_MASTER_ROLE.md)  
**Hub:** [docs/agents/data-sets/hub/](../agents/data-sets/hub/)  
**Universal Loop Prompt:** [hub/UNIVERSAL_LOOP_PROMPT.md](../agents/data-sets/hub/UNIVERSAL_LOOP_PROMPT.md)

**Universal rules (every loop run):** Research must include **self-improvement and loop-improvement** docs (LOOP_LESSONS_LEARNED, SELF_IMPROVING_LOOP_RESEARCH, CURSOR_SELF_IMPROVEMENT). **Light and Medium** = auto-implement; **drastic loop improvements** (routine changes, new phases, new loops) = **Heavy** — document only, require human approval. **Shared state:** At start read [loop_shared_events.jsonl](./loop_shared_events.jsonl) (tail) and [loop_latest/](./loop_latest/) (other loops’ latest); at end append event and update loop_latest/&lt;your_loop&gt;.json so data is shared dynamically when loops run together. See [LOOP_DYNAMIC_SHARING.md](./LOOP_DYNAMIC_SHARING.md) and UNIVERSAL_LOOP_PROMPT.

---

## Loop register

| Loop | Trigger(s) | Main output(s) | Docs / scripts |
|------|------------|-----------------|----------------|
| **Improvement (2-hour)** | "GO", "start improvement loop", "run improvement loop" | 120_MINUTE_LOOP_SUMMARY_*.md, ledger, pulse_log | [120_MINUTE_IMPROVEMENT_LOOP.md](./120_MINUTE_IMPROVEMENT_LOOP.md), [LOOP_MASTER_ALLOWLIST.md](./LOOP_MASTER_ALLOWLIST.md), improvement-loop-wizard skill |
| **Master loop** | "start master loop" | Step 0.M (research all loops, update universal files, read Hub); then full Improvement Loop | [LOOP_MASTER_ROLE.md](./LOOP_MASTER_ROLE.md), [2-hour-loop rule](.cursor/rules/2-hour-loop.mdc) |
| **Token** | "start token loop", "run token reduction loop", "token audit" | Snapshot JSON, token_loop_events.jsonl, TOKEN_LOOP_RUN_LEDGER, TOKEN_LOOP_NEXT_TASKS, progress report (Loop #, proof of work, benefits) | [TOKEN_REDUCTION_LOOP.md](./TOKEN_REDUCTION_LOOP.md), run_token_loop.ps1, token_loop_listener.ps1, token_loop_state_snapshot.ps1 |
| **Cyber Security** | Red/Blue/Purple loop, security validation | Proof of work, validation_simulations, training data, data summary | Hub: cyber-security reports, purple-training.json |
| **Synthetic Data** | When generating or curating synthetic/training data | Training datasets, summaries; deposit to Hub when polished | Hub index; align with UNIVERSAL_LOOP_PROMPT |
| **File-organizer / data-sets** | "organize data", data-sets index | Index of data locations, organization; deposit to Hub | Hub: file-organizer index, data-organized index |

---

## Triggers quick reference

- **start master loop** → Loop Master; Step 0.M then full Improvement Loop.
- **GO** / **start improvement loop** / **run improvement loop** → Improvement Loop (2-hour).
- **start token loop** / **run token reduction loop** / **token audit** → Token Loop (steps 0–7, listener, ledger, NEXT_TASKS).
- **send to hub** → Deposit completed polished output to `docs/agents/data-sets/hub/` with `YYYY-MM-DD_<role-or-topic>_<short-description>.<ext>`; add one-line entry to hub/README.md.

---

## Outputs and Hub

All loops should, when they produce **completed, polished** output (proof of work, report, index, data summary), consider depositing to **docs/agents/data-sets/hub/** and adding a one-line entry to **hub/README.md**. Hub = this data folder; not GitHub. See [SEND_TO_HUB_PROMPT.md](../agents/data-sets/hub/SEND_TO_HUB_PROMPT.md).

---

*Update this axis when new loops or triggers are added. Loop Master uses it in Step 0.M.*
