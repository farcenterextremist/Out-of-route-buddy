# Loop Consistency Standard (Universal Contract)

**Purpose:** Keep all loops consistent, auditable, and interoperable.

**Scope:** Improvement, Master, Token, Cyber Security, Synthetic Data, and any new loops.

---

## Why this standard exists

Cross-loop comparison found recurring drift risks:

- Different levels of rigor in start/end checks
- Inconsistent proof of quality and rollback reporting
- Inconsistent handoff between ledger, hub, and shared-state files

Research-backed process guidance supports using short, repeatable checklists and explicit runbook metadata (owner, trigger, validation, rollback, and last-tested behavior) to reduce missed steps and improve reliability.

---

## Universal loop contract (must pass every run)

Every loop run must satisfy all 10 items:

1. **Trigger + owner:** Record what triggered the run and which loop role is executing.
2. **Start gates complete:** Follow `LOOP_GATES.md` start steps and log **Hub consulted** + **Advice applied**.
3. **Checkpoint ready:** Create/record checkpoint or explicitly state why checkpoint is not applicable.
4. **Plan is explicit:** Use named phases/steps with clear goal and scope.
5. **Validation evidence:** Run and record liveness + readiness evidence relevant to the loop.
6. **Proof of quality:** Include evidence bundle per `QUALITY_STANDARDS_AND_PROOF.md`.
7. **Ledger updated:** Append run block to the loop's run ledger.
8. **Shared state updated:** Append `loop_shared_events.jsonl` finished event + update `loop_latest/<loop>.json`.
9. **Hub handoff handled:** If output is polished, deposit to hub and update hub index.
10. **Next-step continuity:** Record 1–3 concrete next steps for next run.

---

## Consistency score

Use a simple compliance score per run:

- **Consistency Score = passed contract items / 10**
- Target: **10/10**
- Minimum acceptable: **9/10**
- If <9/10, next run must prioritize consistency gaps before new enhancements.

---

## Required summary block

Add this section to loop summaries or progress reports:

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

## Enforcement

- This contract is mandatory for all loops.
- If a loop cannot satisfy an item, record the reason and add a follow-up task.
- New loop docs must include this standard before adoption.
