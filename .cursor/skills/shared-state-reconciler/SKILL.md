---
name: shared-state-reconciler
description: Verifies and repairs cross-loop shared-state handoff correctness between loop_shared_events and loop_latest files. Use when loops run concurrently, when validating LOOP GATES end-state writes, or when debugging stale loop state.
---

# Shared State Reconciler

## Purpose

Ensure loops can reliably react to each other by keeping shared-state files fresh and aligned.

Primary references:

- `docs/automation/LOOP_DYNAMIC_SHARING.md`
- `docs/automation/LOOP_GATES.md`
- `docs/automation/loop_shared_events.jsonl`
- `docs/automation/loop_latest/`

---

## Trigger

Use this skill when requests mention:

- "dynamic data sharing"
- "loops running together"
- "shared state stale"
- "loop_latest mismatch"
- "verify loop handoff"

---

## Workflow

1. **Read recent state**
   - Read last events in `loop_shared_events.jsonl`.
   - Read `loop_latest/*.json` for all loops.

2. **Reconcile**
   - For each loop, compare latest finished event with corresponding `loop_latest/<loop>.json`.
   - Verify `summary_path`, timestamp freshness, and `next_steps` continuity.

3. **Detect drift**
   - Flag missing finished event, stale latest file, or mismatched summary path.

4. **Repair**
   - Update run-end instructions in loop docs if drift is systemic.
   - If run artifacts are available, correct stale `loop_latest/<loop>.json`.

5. **Report**
   - Provide concise status: healthy/degraded, loops impacted, exact fixes.

---

## Output format

```markdown
## Shared State Reconciliation
- loop: [name] | events-latest match: [yes/no]
- summary_path match: [yes/no]
- freshness: [ok/stale]
- action: [none/fix applied/follow-up]
```

---

## Guardrails

- Never invent missing evidence; only use actual files.
- Prefer small corrective updates over broad rewrites.
- Keep schema-compatible with `LOOP_DYNAMIC_SHARING.md`.
