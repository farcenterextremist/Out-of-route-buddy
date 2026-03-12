# Improvement Loop — For Other Agents

**Purpose:** When you (any agent) run the main improvement loop, read this doc first and follow the best practices here. At the end of every improvement loop run, append one block to the run ledger so we keep a shared record.

**When:** At **loop start** — read this doc. At **loop end** (Phase 4.3) — append one block to [IMPROVEMENT_LOOP_RUN_LEDGER.md](./IMPROVEMENT_LOOP_RUN_LEDGER.md).

---

## Upon initiation (first step)

**Check todos.** Before research or changes, check the relevant todo lists: [CRUCIAL_IMPROVEMENTS_TODO.md](../CRUCIAL_IMPROVEMENTS_TODO.md), [TOKEN_LOOP_NEXT_TASKS.md](./TOKEN_LOOP_NEXT_TASKS.md) (latest run), and any in-progress or conversation todos. Prioritize tasks from these lists.

---

## Best practices (follow these)

1. **Read the routine first.** Follow [IMPROVEMENT_LOOP_ROUTINE.md](./IMPROVEMENT_LOOP_ROUTINE.md). Read [IMPROVEMENT_LOOP_COMMON_SENSE.md](./IMPROVEMENT_LOOP_COMMON_SENSE.md) and [IMPROVEMENT_LOOP_REASONING.md](./IMPROVEMENT_LOOP_REASONING.md) at loop start.

2. **Checkpoint before any changes.** Save a copy (git commit or tag) so the user can say "revert" if something breaks. Note the checkpoint in your summary and in the ledger block.

3. **Respect design intent.** Read [USER_PREFERENCES_AND_DESIGN_INTENT.md](./USER_PREFERENCES_AND_DESIGN_INTENT.md) before research or changes. No unwarranted UI changes without permission.

4. **Research before doing.** Read the docs listed in Phase 0.1 (CRUCIAL, last summary, FAILING_OR_IGNORED_TESTS, security, etc.). Classify tasks as Light / Medium / Heavy per [LOOP_TIERING.md](./LOOP_TIERING.md). Do not implement Heavy without user approval.

5. **One improvement per category per loop (Kaizen).** Avoid overload. Timebox (e.g. no more than 10 min per test fix).

6. **Tests must pass.** If a change breaks tests, revert that change and report in the summary. Do not leave tests red.

7. **Write an A-grade summary.** Use [LOOP_METRICS_TEMPLATE.md](./LOOP_METRICS_TEMPLATE.md). Output `IMPROVEMENT_LOOP_SUMMARY_<date>.md` with metrics, what was done, reasoning, suggested next steps, next run focus, quality grade.

8. **Append one block to the run ledger.** At the end of every run, add a new section to [IMPROVEMENT_LOOP_RUN_LEDGER.md](./IMPROVEMENT_LOOP_RUN_LEDGER.md) using the template in that file. This keeps a shared, chronological record for us and for other agents.

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
