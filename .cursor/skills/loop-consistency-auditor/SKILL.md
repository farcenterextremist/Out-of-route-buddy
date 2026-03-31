---
name: loop-consistency-auditor
description: Audits loop runs against the universal consistency contract and produces a scored consistency check. Use when comparing loops, standardizing loop behavior, or validating run summaries/ledgers for consistency.
---

# Loop Consistency Auditor

## Purpose

Apply one scoring contract across all loops so consistency drift is detected early.

Primary references:

- `docs/automation/LOOP_CONSISTENCY_STANDARD.md`
- `docs/automation/LOOP_CONSISTENCY_LEDGER_SNIPPET.md`
- `docs/automation/LOOP_GATES.md`

---

## Trigger

Use this skill when requests mention:

- "compare loops"
- "consistency across loops"
- "standardize all loops"
- "audit loop quality"
- "consistency score"

---

## Workflow

1. **Read baseline**
   - Open loop docs relevant to the run (Improvement, Token, Cyber, Synthetic).
   - Open `LOOP_CONSISTENCY_STANDARD.md`.

2. **Score each contract item**
   - Evaluate pass/fail for all 10 items.
   - Do not mark pass without concrete evidence in summary/ledger/shared state.

3. **Write consistency block**
   - Add `Loop Consistency Check` using `LOOP_CONSISTENCY_LEDGER_SNIPPET.md`.
   - Compute `Consistency score: X/10`.

4. **Identify drift**
   - List 1-3 contract items failing or weak across loops.
   - Propose exact file-level fixes.

5. **Close**
   - Ensure loop docs point to the reusable consistency snippet.

---

## Output format

```markdown
## Loop Consistency Check
- Trigger + owner: [pass/fail]
- Start gates complete: [pass/fail]
- Checkpoint recorded: [pass/fail]
- Plan/phase scope explicit: [pass/fail]
- Validation evidence (liveness/readiness): [pass/fail]
- Proof of quality present: [pass/fail]
- Ledger updated: [pass/fail]
- Shared state updated: [pass/fail]
- Hub handoff/index handled: [pass/fail/not-applicable]
- Next-step continuity: [pass/fail]
- Consistency score: X/10
```

---

## Guardrails

- Keep scoring evidence-based and reproducible.
- If score is below 9/10, require a follow-up action.
- Respect frontend policy: non-master loops do not implement UI changes.
