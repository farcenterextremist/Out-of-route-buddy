# Loop Master Role — Authority for How Loops Run

**Purpose:** Single authority that defines how all loops (Improvement, Token, Cyber Security, Synthetic Data, or other) are run. Every agent running any loop must read this doc and, when the user says "start master loop", complete **Step 0.M** first.

**Hub (advice and rules from all agents):** [docs/agents/data-sets/hub/](../agents/data-sets/hub/) — index at [hub/README.md](../agents/data-sets/hub/README.md).  
**Full axis (all loops, triggers, outputs):** [LOOPS_AND_IMPROVEMENT_FULL_AXIS.md](./LOOPS_AND_IMPROVEMENT_FULL_AXIS.md).  
**Universal Loop Prompt (bind every loop):** [UNIVERSAL_LOOP_PROMPT.md](../agents/data-sets/hub/UNIVERSAL_LOOP_PROMPT.md) in the Hub.

---

## Who is the Loop Master?

When the user says **"start master loop"**, you are the **Loop Master**. You orchestrate the Improvement Loop and ensure all other loops are researched, compared, and aligned. The Loop Master does **Step 0.M** before running the full Improvement Loop.

When the user triggers **any other loop** (e.g. "run token loop", "GO", Cyber Security loop, etc.), you are **not** the Loop Master for that run, but you still **read this doc** and the **Hub** at loop start so you follow the same authority and advice.

---

## Step 0.M (only when user says "start master loop")

Before running the Improvement Loop, complete **Step 0.M**:

1. **Research all loops.**  
   Read [LOOPS_AND_IMPROVEMENT_FULL_AXIS.md](./LOOPS_AND_IMPROVEMENT_FULL_AXIS.md). Identify every loop in the project (Improvement, Token, Cyber Security, Synthetic Data, file-organizer, etc.), their triggers, outputs, and docs/scripts.

2. **Compare and scrutinize.**  
   Check for consistency: naming, ledger/summary format, use of Hub, use of Loop # and proof of work where applicable. Note drift or gaps.

3. **Update universal files (as needed).**  
   Update [IMPROVEMENT_LOOP_BEST_PRACTICES.md](./IMPROVEMENT_LOOP_BEST_PRACTICES.md) (if present), [IMPROVEMENT_LOOP_FOR_OTHER_AGENTS.md](./IMPROVEMENT_LOOP_FOR_OTHER_AGENTS.md) (if present), RUN_LEDGER template, [LOOP_TIERING.md](./LOOP_TIERING.md) (if present) so they reflect current loops and Hub usage.

4. **Read the Hub.**  
   Open [docs/agents/data-sets/hub/README.md](../agents/data-sets/hub/README.md). Scan the Hub index; read or skim each file relevant to the Improvement Loop and to other loops. Note **Hub consulted** and **Advice/rules applied** at the start of your run.

5. **Then run the full Improvement Loop.**  
   Proceed with the Improvement Loop (checkpoint → phases 0–4 → summary → append ledger) per the 2-hour-loop rule and [IMPROVEMENT_LOOP_INDEX.md](./IMPROVEMENT_LOOP_INDEX.md) (or equivalent routine doc).

---

## If the user did NOT say "start master loop"

- You still **read this doc** (LOOP_MASTER_ROLE.md) at the start of your run.
- You still **read the Hub** ([hub/README.md](../agents/data-sets/hub/README.md) and relevant files from the index) and note **Hub consulted** and **Advice/rules applied**.
- You follow [UNIVERSAL_LOOP_PROMPT.md](../agents/data-sets/hub/UNIVERSAL_LOOP_PROMPT.md): minimize slop, critique before depositing, consider "send to hub" when you have polished output.

---

## Paths to bookmark

| What | Path |
|------|------|
| This doc (Loop Master authority) | `docs/automation/LOOP_MASTER_ROLE.md` |
| Full axis (all loops) | `docs/automation/LOOPS_AND_IMPROVEMENT_FULL_AXIS.md` |
| Hub | `docs/agents/data-sets/hub/` |
| Hub index | `docs/agents/data-sets/hub/README.md` |
| Universal Loop Prompt | `docs/agents/data-sets/hub/UNIVERSAL_LOOP_PROMPT.md` |

---

*This doc is the authority for how loops run. Every loop run must refer to it and to the Hub.*
