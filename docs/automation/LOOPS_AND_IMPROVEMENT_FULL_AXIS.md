# Loops and Improvement — Full Axis

**Purpose:** Single index of **all loops** in the project: triggers, outputs, and where to find docs/scripts. Used by the Loop Master (Step 0.M) and by any agent to see the full picture. Keep this doc updated when new loops are added.

**Loop Master:** [LOOP_MASTER_ROLE.md](./LOOP_MASTER_ROLE.md)  
**Hub:** [docs/agents/data-sets/hub/](../agents/data-sets/hub/)  
**Universal Loop Prompt:** [hub/UNIVERSAL_LOOP_PROMPT.md](../agents/data-sets/hub/UNIVERSAL_LOOP_PROMPT.md)

**LOOP GATES (required):** Every loop must pass [LOOP_GATES.md](./LOOP_GATES.md) at start (read LOOP_MASTER_ROLE, full axis, UNIVERSAL_LOOP_PROMPT, hub/README, loop_shared_events.jsonl tail, loop_latest/*.json; log Hub consulted + Advice applied) and at end (send to hub, update hub/README, append to loop_shared_events.jsonl, update loop_latest/&lt;your_loop&gt;.json). See [LOOP_DYNAMIC_SHARING.md](./LOOP_DYNAMIC_SHARING.md) for shared-state schema.

**Consistency contract (required):** Every loop run must pass [LOOP_CONSISTENCY_STANDARD.md](./LOOP_CONSISTENCY_STANDARD.md) and report `Consistency score: X/10` in summary/progress output.

**Global frontend policy:** Frontend/UI implementation is **Master Loop only**. Other loops may research and propose UI changes but do not apply them. Master Loop uses [FRONTEND_CHANGE_AUTOMATION_GATE.md](./FRONTEND_CHANGE_AUTOMATION_GATE.md) to decide if a UI change is obviously beneficial and subtle enough for automation.

**Unified future direction:** The long-term design is one `Loopmaster` super-loop with shared gates, shared health monitoring, and scoped internal lanes or replica tabs. See [LOOPMASTER_SUPER_LOOP_BLUEPRINT.md](./LOOPMASTER_SUPER_LOOP_BLUEPRINT.md).
**Default neighboring-tab model:** When the user opens worker tabs manually, use the role-based pattern from [LOOPMASTER_TAB_AND_SPAWN_MODEL.md](./LOOPMASTER_TAB_AND_SPAWN_MODEL.md).

---

## Loop register

| Loop | Trigger(s) | Main output(s) | Docs / scripts |
|------|------------|-----------------|----------------|
| **Loopmaster super-loop** | "loopmaster", "super loop", unified loop requests | Loop design, gated execution plan, bucket-backed evidence, optional role-based tab model when user opens tabs | [LOOPMASTER_SUPER_LOOP_BLUEPRINT.md](./LOOPMASTER_SUPER_LOOP_BLUEPRINT.md), `.cursor/skills/loopmaster-orchestrator/SKILL.md` |
| **Improvement (2-hour)** | "GO", "start improvement loop", "run improvement loop" | 120_MINUTE_LOOP_SUMMARY_*.md, ledger, pulse_log | [120_MINUTE_IMPROVEMENT_LOOP.md](./120_MINUTE_IMPROVEMENT_LOOP.md), [LOOP_MASTER_ALLOWLIST.md](./LOOP_MASTER_ALLOWLIST.md), improvement-loop-wizard skill |
| **Master loop** | "start master loop" | Step 0.M (research all loops, update universal files, read Hub); then full Improvement Loop | [LOOP_MASTER_ROLE.md](./LOOP_MASTER_ROLE.md), [2-hour-loop rule](.cursor/rules/2-hour-loop.mdc) |
| **LLM / Token audit** | "start llm loop", "start token loop", "run token reduction loop", "token audit" | Snapshot JSON, token_loop_events.jsonl, TOKEN_LOOP_RUN_LEDGER, TOKEN_LOOP_NEXT_TASKS, progress report (Loop #, proof of work, benefits), permanent local-first provider guidance | [LLM_LOOP.md](./LLM_LOOP.md), [TOKEN_REDUCTION_LOOP.md](./TOKEN_REDUCTION_LOOP.md), run_llm_loop.ps1, run_token_loop.ps1, token_loop_listener.ps1, token_loop_state_snapshot.ps1 |
| **Cyber Security** | Red/Blue/Purple loop, security validation | Proof of work, validation_simulations, training data, data summary | Hub: cyber-security reports, purple-training.json |
| **Synthetic Data** | When generating or curating synthetic/training data | Training datasets, summaries; deposit to Hub when polished | Hub index; align with UNIVERSAL_LOOP_PROMPT |
| **File-organizer / data-sets** | "organize data", data-sets index | Index of data locations, organization; deposit to Hub | Hub: file-organizer index, data-organized index |

---

## Triggers quick reference

- **loopmaster** / **super loop** → Unified Loopmaster architecture and run-plan; other loops become internal lanes; optional role-based replica tabs when user opens them (no fixed count or synchronous automation).
- **start master loop** → Loop Master; Step 0.M then full Improvement Loop.
- **GO** / **start improvement loop** / **run improvement loop** → Improvement Loop (2-hour).
- **start llm loop** → LLM Loop (permanent local-first loop; current stable lane = token audit; provider guidance in `LLM_LOOP.md`).
- **start token loop** / **run token reduction loop** / **token audit** → Token Loop (steps 0–7, listener, ledger, NEXT_TASKS).
- **send to hub** → Deposit completed polished output to `docs/agents/data-sets/hub/` with `YYYY-MM-DD_<role-or-topic>_<short-description>.<ext>`; add one-line entry to hub/README.md.

---

## Outputs and Hub

All loops should, when they produce **completed, polished** output (proof of work, report, index, data summary), consider depositing to **docs/agents/data-sets/hub/** and adding a one-line entry to **hub/README.md**. Hub = this data folder; not GitHub. See [SEND_TO_HUB_PROMPT.md](../agents/data-sets/hub/SEND_TO_HUB_PROMPT.md).

---

*Update this axis when new loops or triggers are added. Loop Master uses it in Step 0.M.*
