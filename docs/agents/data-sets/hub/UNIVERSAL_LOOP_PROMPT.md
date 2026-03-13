# Universal Loop Prompt — Hub, Loop Master, and Anti-Slop

**Give this to all agents.** Paste into context, rules, or onboarding so that **every time any agent runs any loop**, they follow the advice and rules in the Hub via the Loop Master and produce data that is critiqued, purposeful, and free of AI slop.

---

## Apply to your loop

**Research** = include [LOOP_LESSONS_LEARNED](../../../automation/LOOP_LESSONS_LEARNED.md), [SELF_IMPROVING_LOOP_RESEARCH](../../../automation/SELF_IMPROVING_LOOP_RESEARCH.md), [CURSOR_SELF_IMPROVEMENT](../../../automation/CURSOR_SELF_IMPROVEMENT.md).  
**Auto-implement** Light & Medium.  
**Drastic loop changes** = Heavy (document only).  
**Read Hub at start.**  
**Consider send to hub when done.**

**Full:** `docs/agents/data-sets/hub/UNIVERSAL_LOOP_PROMPT.md` (this file).

---

## The one rule that binds every loop

**Before you run any loop (Improvement, Token, Cyber Security, Synthetic Data, or other), you must:**

1. **Consult the Hub via the Loop Master.**  
   Read **`docs/automation/LOOP_MASTER_ROLE.md`**. If the user said **"start master loop"**, complete **Step 0.M** first: research all loops, compare and scrutinize, update universal files — and **read the Hub** (see below).  
   If the user said **GO** or triggered another loop without "start master loop", you still **read the Hub** at the start of your run: the Hub holds the latest advice, proof of work, and rules that the Loop Master and other roles have deposited. The Loop Master is the authority that sets how loops run; the Hub is where that authority’s outputs and other agents’ polished data live. **Every loop run must refer to the advice and rules implicated in the Hub.**

2. **Read the Hub at loop start.**  
   Open **`docs/agents/data-sets/hub/README.md`**. Scan the **Hub index** table. For each listed file that is relevant to your loop (e.g. token-loop report for Token Loop, cyber-security proof for Cyber Security Loop, file-organizer index for any loop), **read or skim it**. Note in your research or first step:  
   - **Hub consulted:** [list files or roles you read].  
   - **Advice/rules applied:** [1–3 bullets: what from the Hub you are using this run — e.g. "§4.4 requires Loop # and proof of work"; "sandbox: only polished output"; "validation_simulations for regression"].  
   This ensures you are not reinventing the wheel and you are aligning with what other agents have already established as the standard.

3. **Include self-improvement and loop-improvement in research.**  
   Every loop run must include in its **research** (e.g. Phase 0 or Step 0) the **self-improvement and loop-improvement related articles and docs**. At minimum read or skim: **`docs/automation/LOOP_LESSONS_LEARNED.md`**, **`docs/automation/SELF_IMPROVING_LOOP_RESEARCH.md`**, and **`docs/automation/CURSOR_SELF_IMPROVEMENT.md`**. Note in your research output what you applied or will apply from these (e.g. "Avoid X per LOOP_LESSONS_LEARNED"; "Single highest-impact change per SELF_IMPROVING_LOOP_RESEARCH"). This keeps the loop improving over time and avoids repeating past mistakes.

4. **Auto-implement Light and Medium; put drastic loop improvements in Heavy.**  
   **Light and Medium tier** improvements (per LOOP_TIERING) run **without stopping** — auto-implement them. **Drastic loop improvements** (e.g. changing the routine, adding/removing phases, changing tier definitions, new loops, or major process changes) are **Heavy tier**: do not implement them automatically; document them, add to backlog or FUTURE_IDEAS, and require human approval before applying. This keeps incremental improvements automatic while guarding against unapproved process changes.

5. **Minimize AI slop and critique your data.**  
   The Hub exists for **completed, polished** data — not drafts or generic filler. When you run a loop:  
   - **Produce output that is specific, traceable, and human-useful.** Prefer concrete metrics, file paths, and clear next steps over vague summaries.  
   - **Critique before you deposit.** If you would "send to hub" or write a report, ask: Is this **actionable**? Does it **reference real artifacts** (files, commits, test results)? Would a human or another agent **reuse** it without guessing? If the answer is no, tighten the output before considering it done.  
   - **Avoid slop.** Do not add padding, generic best-practice paragraphs, or unspecific "consider X" without tying X to this project. Do not copy-paste boilerplate without filling in project-specific content. Prefer one sharp sentence over three vague ones.  
   - **Use your critiquing skills.** If you have access to skills or tools for critiquing data or minimizing slop, invoke them on your loop outputs (summaries, reports, training data) before you finalize or send to hub.

6. **When you finish a loop (or a major artifact), consider "send to hub."**  
   If your run produced something **completed and polished** that other agents or the next run should use (e.g. proof of work, loop report, index, data summary), deposit it in **`docs/agents/data-sets/hub/`** with naming **`YYYY-MM-DD_<role-or-topic>_<short-description>.<ext>`** and optionally add a one-line entry to **`hub/README.md`**. Hub = this data folder; not GitHub. That way the next agent running a loop will see your advice and rules in the Hub.

7. **Read and write shared state so data is dynamic across loops.**  
   When multiple loops run at the same time (or in sequence), **at loop start** read **`docs/automation/loop_shared_events.jsonl`** (tail: last 50 lines) and **`docs/automation/loop_latest/<other_loop>.json`** for every other loop (e.g. if you are Improvement, read token.json, cyber.json, synthetic_data.json). Note in your research: **Shared state (other loops):** [what you will use from others — summary paths, next_steps]. **At loop end** append one **finished** event to `loop_shared_events.jsonl` (ts, loop, event, run_id, summary_path, next_steps, optional checkpoint) and overwrite **`docs/automation/loop_latest/<your_loop>.json`** with your latest output (last_run_ts, summary_path, suggested_next_steps). This ensures 100% that when all loops run, data is shared dynamically and other agents can react/act. See [LOOP_DYNAMIC_SHARING.md](../../../automation/LOOP_DYNAMIC_SHARING.md).

---

## Short form (paste this into a rule or short context)

**Every loop run:**  
(1) Read **LOOP_MASTER_ROLE.md**; if "start master loop", do Step 0.M first.  
(2) **Research:** Include **self-improvement and loop-improvement** docs (LOOP_LESSONS_LEARNED, SELF_IMPROVING_LOOP_RESEARCH, CURSOR_SELF_IMPROVEMENT).  
(3) **Tiers:** Auto-implement **Light and Medium**; **drastic loop changes = Heavy (document only)** — do not implement without approval.  
(4) Read **hub/README.md** and relevant Hub files from the index; note "Hub consulted" and "Advice/rules applied" at run start.  
(5) Minimize slop: output specific, traceable, actionable; critique before depositing; use critiquing/slop-minimization skills if available.  
(6) When you have polished output, consider depositing to **docs/agents/data-sets/hub/** and indexing in hub/README.md.  
(7) **Shared state:** At start read **loop_shared_events.jsonl** (tail) and **loop_latest/*.json** (other loops); at end append finished event to loop_shared_events.jsonl and update **loop_latest/&lt;your_loop&gt;.json**. See docs/automation/LOOP_DYNAMIC_SHARING.md.

## Prompt to relay to other agents (apply to their loops)

Copy this to other agents so they apply the same rules to **their** loops (Token, Cyber, Synthetic Data, or any other):

```
When you run your loop, apply these rules: (1) **Research** — Include self-improvement and loop-improvement docs in your run: LOOP_LESSONS_LEARNED.md, SELF_IMPROVING_LOOP_RESEARCH.md, CURSOR_SELF_IMPROVEMENT.md. Note what you applied. (2) **Tiers** — Auto-implement Light and Medium improvements. Drastic loop improvements (routine changes, new phases, new loops) = Heavy: document only, do not implement without human approval. (3) **Hub** — At run start read hub/README.md and relevant Hub files; note "Hub consulted" and "Advice/rules applied." When you have polished output, consider depositing to docs/agents/data-sets/hub/. (4) **Shared state** — At start read docs/automation/loop_shared_events.jsonl (tail) and docs/automation/loop_latest/*.json (other loops); at end append finished event to loop_shared_events.jsonl and update loop_latest/<your_loop>.json. See docs/automation/LOOP_DYNAMIC_SHARING.md. Full prompt: docs/agents/data-sets/hub/UNIVERSAL_LOOP_PROMPT.md.
```

| What | Path |
|------|------|
| **Full prompt (this file)** | `docs/agents/data-sets/hub/UNIVERSAL_LOOP_PROMPT.md` |
| Loop Master (authority for how loops run) | `docs/automation/LOOP_MASTER_ROLE.md` |
| Hub (advice and rules from all agents) | `docs/agents/data-sets/hub/` |
| Hub index | `docs/agents/data-sets/hub/README.md` |
| Full axis (all loops, triggers, outputs) | `docs/automation/LOOPS_AND_IMPROVEMENT_FULL_AXIS.md` |
| Send-to-hub prompt | `docs/agents/data-sets/hub/SEND_TO_HUB_PROMPT.md` |
| Self-improvement & loop-improvement (required in research) | `docs/automation/LOOP_LESSONS_LEARNED.md`, `docs/automation/SELF_IMPROVING_LOOP_RESEARCH.md`, `docs/automation/CURSOR_SELF_IMPROVEMENT.md` |
| Data usefulness & pruning (evaluate what to keep/deposit) | `docs/automation/DATA_USEFULNESS_AND_PRUNING_RESEARCH.md` |
| Tiering (Light/Medium auto; Heavy = document only; drastic loop changes = Heavy) | `docs/automation/LOOP_TIERING.md` |
| Dynamic sharing (read/write shared state when running with other loops) | `docs/automation/LOOP_DYNAMIC_SHARING.md` |

---

*This prompt ensures every loop run refers to the Hub via the Loop Master and produces data that is critiqued and slop-minimized. Update when new loops or Hub conventions are added.*
