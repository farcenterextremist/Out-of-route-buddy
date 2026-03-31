# Loop Consistency Ledger Snippet

**Purpose:** One reusable block for Token, Cyber Security, and Synthetic Data loop ledgers.

**Use:** Copy this into each run block to keep consistency reporting identical across loops.

**Reference:** [LOOP_CONSISTENCY_STANDARD.md](./LOOP_CONSISTENCY_STANDARD.md)

---

## Copy/Paste Block

```markdown
### Loop Consistency Check
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

## Fill Rules

- Use `pass` only when evidence exists in the same run block or linked summary.
- Use `not-applicable` only for hub handoff when no polished artifact exists.
- If score is below `9/10`, add one follow-up action in `Next`.
