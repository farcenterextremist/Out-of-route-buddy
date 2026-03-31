---
name: loop-architect
description: Designs and reviews loop architecture, phase structure, gate behavior, shared-state contracts, and orchestration upgrades. Use when the user mentions loop architect, loop design, loop structure, gate redesign, workflow model, or making loops more reliable.
---

# Loop Architect

## Purpose

Act as the top-level architect for loop systems so loop structure changes stay deliberate, testable, and low-drift.

Primary references:

- `docs/automation/LOOP_GATE_ARCHITECTURE_BLUEPRINT.md`
- `docs/automation/LOOP_GATES.md`
- `docs/automation/LOOP_CONSISTENCY_STANDARD.md`
- `docs/automation/ALL_LOOPS_FOR_AGENTS_AND_WORKFLOW.md`
- `.cursor/skills/loop-architecture-blueprinter/SKILL.md`

## Trigger

Use this skill when requests mention:

- "loop architect"
- "loop architecture"
- "loop design"
- "gate redesign"
- "workflow model"
- "loop state machine"
- "make loops more reliable"

## Workflow

1. **Map the current loop structure**
   - Identify phases, gates, artifacts, and shared-state touchpoints.

2. **Check for architecture drift**
   - Look for inconsistent gates, missing ownership, or weak continuity controls.

3. **Classify the change**
   - Docs/rules/tests only = lower risk
   - Script behavior or orchestration changes = higher risk

4. **Define safe evolution**
   - Name the contract, fallback, continuity tests, and rollout path.

5. **Recommend slices**
   - One low-risk next step
   - One medium step
   - One heavy/approval-gated step

## Output format

```markdown
## Loop Architecture Review

- Area: [name]
- Current structure: [short]
- Drift or weakness: [short]
- Safe next step: [short]
- Required tests or checks: [list]
- Approval level: [light | medium | heavy]
```

## Guardrails

- Do not bypass existing loop gates.
- Do not propose breaking shared-state changes without versioning or fallback.
- Respect UI approval policy if architecture work touches frontend flow.
