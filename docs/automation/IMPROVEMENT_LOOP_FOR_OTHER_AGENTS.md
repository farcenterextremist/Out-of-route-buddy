# Improvement Loop — For Other Agents

**Purpose:** When you (any agent) run the main improvement loop, read this doc first and follow the best practices here. At the end of every improvement loop run, append one block to the run ledger so we keep a shared record.

**When:** At **loop start** — read this doc. At **loop end** (Phase 4.3) — append one block to [IMPROVEMENT_LOOP_RUN_LEDGER.md](./IMPROVEMENT_LOOP_RUN_LEDGER.md).

---

## Upon initiation (first step)

**Read the Hub via the Loop Master.** Before any research or changes, read [UNIVERSAL_LOOP_PROMPT.md](../agents/data-sets/hub/UNIVERSAL_LOOP_PROMPT.md). Then open [hub/README.md](../agents/data-sets/hub/README.md) and the Hub index; read or skim the listed files relevant to your loop. Note "Hub consulted" and "Advice/rules applied" in your research. This ensures every loop run refers to the advice and rules in the Hub and minimizes slop.

**Check todos.** Before research or changes, check the relevant todo lists: [CRUCIAL_IMPROVEMENTS_TODO.md](../CRUCIAL_IMPROVEMENTS_TODO.md), [TOKEN_LOOP_NEXT_TASKS.md](./TOKEN_LOOP_NEXT_TASKS.md) (latest run), and any in-progress or conversation todos. Prioritize tasks from these lists.

**Read shared state (when loops may run together).** Per [LOOP_DYNAMIC_SHARING.md](./LOOP_DYNAMIC_SHARING.md): at loop start, read **loop_shared_events.jsonl** (tail: last 50 lines) and **loop_latest/token.json**, **loop_latest/cyber.json**, **loop_latest/synthetic_data.json**. Note in research: **Shared state (other loops):** [what you will use]. At loop end, append a **finished** event to loop_shared_events.jsonl and update **loop_latest/improvement.json**. This ensures data is shared dynamically when multiple loops run at the same time.

---

## Best practices (follow these)

1. **Read the routine first.** Follow [IMPROVEMENT_LOOP_ROUTINE.md](./IMPROVEMENT_LOOP_ROUTINE.md). Read [IMPROVEMENT_LOOP_COMMON_SENSE.md](./IMPROVEMENT_LOOP_COMMON_SENSE.md) and [IMPROVEMENT_LOOP_REASONING.md](./IMPROVEMENT_LOOP_REASONING.md) at loop start.

2. **Checkpoint before any changes.** Save a copy (git commit or tag) so the user can say "revert" if something breaks. Note the checkpoint in your summary and in the ledger block.

3. **Research includes self-improvement and loop-improvement.** Every loop run must include in Phase 0 research: [LOOP_LESSONS_LEARNED.md](./LOOP_LESSONS_LEARNED.md), [SELF_IMPROVING_LOOP_RESEARCH.md](./SELF_IMPROVING_LOOP_RESEARCH.md), [CURSOR_SELF_IMPROVEMENT.md](./CURSOR_SELF_IMPROVEMENT.md). Note what you applied in your research output. **Auto-implement Light and Medium**; put **drastic loop improvements** (routine changes, new phases, new loops) in **Heavy** — document only, require human approval. See [UNIVERSAL_LOOP_PROMPT](../agents/data-sets/hub/UNIVERSAL_LOOP_PROMPT.md) and [LOOP_TIERING.md](./LOOP_TIERING.md).

4. **Respect design intent.** Read [USER_PREFERENCES_AND_DESIGN_INTENT.md](./USER_PREFERENCES_AND_DESIGN_INTENT.md) before research or changes. No unwarranted UI changes without permission.

5. **Research before doing.** Read the docs listed in Phase 0.1 (CRUCIAL, last summary, FAILING_OR_IGNORED_TESTS, security, **self-improvement/loop-improvement**, etc.). Classify tasks as Light / Medium / Heavy per [LOOP_TIERING.md](./LOOP_TIERING.md). Do not implement Heavy without user approval.

5. **One improvement per category per loop (Kaizen).** Avoid overload. Timebox (e.g. no more than 10 min per test fix).

6. **Tests must pass.** If a change breaks tests, revert that change and report in the summary. Do not leave tests red.

7. **Write an A-grade summary.** Use [LOOP_METRICS_TEMPLATE.md](./LOOP_METRICS_TEMPLATE.md). Output `IMPROVEMENT_LOOP_SUMMARY_<date>.md` with metrics, what was done, reasoning, suggested next steps, next run focus, quality grade.

8. **Recommend new ideas every run (required).** In the summary, the "File Organizer: recommended new ideas" section must contain **at least 1–2 new ideas** (Light, Medium, or Heavy) **or** (when Heavy list ≥ 50) **judge/critique** 1–2 existing Heavy ideas. When Heavy list **< 50**, include **at least 1–2 new Heavy ideas** (in FUTURE_IDEAS and summary). Do not skip. See [HEAVY_IDEAS_FAVORITES.md](./HEAVY_IDEAS_FAVORITES.md) § Heavy list cap and [IMPROVEMENT_LOOP_TEAMS.md](./IMPROVEMENT_LOOP_TEAMS.md).

9. **Append one block to the run ledger.** At the end of every run, add a new section to [IMPROVEMENT_LOOP_RUN_LEDGER.md](./IMPROVEMENT_LOOP_RUN_LEDGER.md) using the template in that file. This keeps a shared, chronological record for us and for other agents.

---

## At loop end: ledger block (required)

After writing the summary file, open [IMPROVEMENT_LOOP_RUN_LEDGER.md](./IMPROVEMENT_LOOP_RUN_LEDGER.md) and **append** one block. Use this template (the ledger file contains the same template and examples):

```markdown
---
## Run YYYY-MM-DD (variant)

- **Focus:** [Security | UI/UX | Shipability | Code Quality | File Structure | Data/Metrics]
- **Variant:** [Quick | Standard | Full]
- **Summary:** [IMPROVEMENT_LOOP_SUMMARY_YYYY-MM-DD.md](./IMPROVEMENT_LOOP_SUMMARY_YYYY-MM-DD.md)
- **Metrics:** Tests [pass/fail], Lint [errors/warnings], Files changed [N], Checkpoint [commit or tag]
- **Next:** [1–2 bullets for next run]
---
```

Replace YYYY-MM-DD and the bracketed values with this run’s data. Then append the block to the bottom of the ledger file.

---

*Other agents: read this at start, follow best practices, append to the ledger at end.*
