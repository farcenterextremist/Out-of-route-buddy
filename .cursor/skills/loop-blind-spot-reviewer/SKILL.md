---
name: loop-blind-spot-reviewer
description: Reviews the loop system for hidden blind spots such as weak handoffs, proof gaps, doc drift, stale shared state, and missing simulations. Use when the user asks for blind spots, gaps, weak coverage, fortification ideas, or loop risk review.
---

# Loop Blind Spot Reviewer

## Purpose

Find the weak spots that healthy-looking loop runs can still miss.

Primary references:

- `docs/automation/LOOP_BLIND_SPOTS_AND_FORTIFICATION.md`
- `docs/automation/LOOP_HEALTH_CHECKS.md`
- `docs/automation/LOOP_DIAGNOSTIC_SWEEP.md`
- `docs/automation/LOOPMASTER_TAB_AND_SPAWN_MODEL.md`

## Trigger

Use this skill when requests mention:

- "blind spots"
- "gaps in the loop system"
- "what are we missing"
- "fortify the loop system"
- "loop risk review"

## Workflow

1. Check handoff clarity
2. Check proof quality versus health signals
3. Check guide drift and stale shared state
4. Check simulation and operator-behavior coverage
5. Name 1-3 highest-value fortification steps

## Output format

```markdown
## Loop Blind Spots

- Blind spot: [name]
- Why it matters: [short]
- Current coverage: [present/weak/missing]
- Fortification step: [short]
```

## Guardrails

- Prefer specific blind spots over generic "could improve testing"
- Name residual risk even if current checks are green
