# Improvement Loop — Prompt for Other Agents

**Purpose:** Copy-paste prompt you can give to other agents (in this repo or elsewhere) so they build or follow improvement loops using our best practices and recorded data.

---

## Option A — "Follow our loop and best practices"

```
You're helping with an Improvement Loop. Follow OutOfRouteBuddy's approach:

1. **Read the entry point:** docs/automation/IMPROVEMENT_LOOP_FOR_OTHER_AGENTS.md — it links to best practices, our run ledger (recorded data), and key docs.

2. **Apply best practices:** docs/automation/IMPROVEMENT_LOOP_BEST_PRACTICES.md — checkpoint before any changes, research first, tier tasks (Light/Medium/Heavy), reason before each change, timebox per task, tests must pass. At the end of every run, append one block to a run ledger (date, focus, summary link, metrics, next steps).

3. **Record every run:** Append this run to docs/automation/IMPROVEMENT_LOOP_RUN_LEDGER.md using the template in that file (or create your own RUN_LEDGER in your repo and append there).

4. **Full routine (if running our loop):** docs/automation/IMPROVEMENT_LOOP_ROUTINE.md — phases 0–4, common sense and reasoning first. When the user says GO, start with docs/automation/IMPROVEMENT_LOOP_COMMON_SENSE.md and docs/automation/IMPROVEMENT_LOOP_REASONING.md.

If you're in a different repo, adopt the same structure: one run ledger, append every run; use the best-practices doc as your template.
```

---

## Option B — "Build your own loop (short)"

```
Build an improvement loop for this project using these practices:

- **Entry point & best practices:** Read docs/automation/IMPROVEMENT_LOOP_FOR_OTHER_AGENTS.md and docs/automation/IMPROVEMENT_LOOP_BEST_PRACTICES.md (from the OutOfRouteBuddy repo or ask for the content). Key rules: checkpoint before changes, research first, tier tasks, reason before each change, timebox 10 min per task, tests must pass. At the end of every run, append one block to a run ledger (date, focus, summary link, metrics, next steps).

- **Record every run:** Maintain a RUN_LEDGER file and append one section per run with: date, focus, summary link, metrics one-liner, checkpoint, next steps. Use the template in OutOfRouteBuddy's docs/automation/IMPROVEMENT_LOOP_RUN_LEDGER.md as reference.

Deliver: a short routine (phases 0–4), a run ledger, and a first run appended to the ledger.
```

---

## Option C — "Just record this run"

```
This was an Improvement Loop run. Append it to the run ledger:

- **Ledger file:** docs/automation/IMPROVEMENT_LOOP_RUN_LEDGER.md (or the project's run ledger).
- **Template:** Add a "Run YYYY-MM-DD" section with: Focus, Variant, Summary (link to the summary file), Metrics one-liner (tests, lint, files changed, checkpoint), Progress bar one-liner (if used), Next (1–2 bullets). See the "How to append" section in the ledger file.

Do it now for this run.
```

---

## Option D — "One-liner for chat"

```
Read docs/automation/IMPROVEMENT_LOOP_FOR_OTHER_AGENTS.md and follow the best practices there; at the end of every improvement loop run, append one block to docs/automation/IMPROVEMENT_LOOP_RUN_LEDGER.md (or our run ledger).
```

---

*Use Option A when the agent is in this repo and should follow our full loop. Use Option B when they're in another repo building their own. Use Option C when you only want them to log a run. Use Option D for a quick reminder in chat.*
