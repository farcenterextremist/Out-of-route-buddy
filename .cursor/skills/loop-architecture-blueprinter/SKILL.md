---
name: loop-architecture-blueprinter
description: Designs resilient loop/gate architecture upgrades using deterministic workflow patterns, idempotent actions, and contract-first shared state. Use when evolving loop orchestration or gate semantics.
---

# Loop Architecture Blueprinter

## Purpose

Design and stage architecture improvements to loop and gate systems without causing continuity regressions.

Primary references:
- `docs/automation/LOOP_GATE_ARCHITECTURE_BLUEPRINT.md`
- `docs/automation/LOOP_GATES.md`
- `docs/automation/LOOP_CONSISTENCY_STANDARD.md`
- `docs/automation/LOOP_DYNAMIC_SHARING.md`

## Trigger

Use this skill when requests mention:
- "loop architecture"
- "gate redesign"
- "orchestration model"
- "workflow/state machine"
- "make loops more reliable"

## Workflow

1. **Baseline current architecture**
   - Identify current states, gate steps, and outputs.
   - Identify where state can drift.

2. **Apply respected patterns**
   - Deterministic state transitions
   - Idempotent gate actions
   - Contract-first shared-state schemas
   - Additive evolution and versioning strategy

3. **Classify implementation risk**
   - Light: docs/rules/tests only
   - Medium: script behavior changes with fallback
   - Heavy: orchestration engine/state machine runner

4. **Define continuity controls**
   - Which continuity tests must pass before merge
   - Which fallback behavior is required on failure

5. **Produce implementation slice**
   - Give 1 immediate low-risk step
   - Give 1 medium step
   - Give 1 heavy step requiring approval

## Output format

```markdown
## Architecture Upgrade Slice
- Pattern adopted: [name]
- Why now: [reason]
- Scope: [light/medium/heavy]
- Continuity tests required: [list]
- Rollback/fallback: [short]
```

## Guardrails

- Do not bypass existing loop gates.
- Do not introduce breaking shared-state changes without version strategy.
- Respect frontend policy: architecture work does not auto-ship major UI changes.
