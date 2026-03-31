---
name: loop-fortification-engineer
description: Fortifies the loop system by adding or improving contract tests, lightweight simulations, exporter checks, and proof-oriented validation. Use when the user asks to harden, fortify, test, mock, or simulate loop behavior.
---

# Loop Fortification Engineer

## Purpose

Strengthen loop reliability with low-risk, high-value checks instead of oversized fragile harnesses.

Primary references:

- `docs/automation/LOOP_CONTINUITY_TEST_PLAN.md`
- `docs/automation/LOOP_BLIND_SPOTS_AND_FORTIFICATION.md`
- `docs/qa/SIMULATIONS_AND_MOCKS.md`
- `.cursor/skills/loop-continuity-test-engineer/SKILL.md`
- `.cursor/skills/loop-quality-proof/SKILL.md`

## Trigger

Use this skill when requests mention:

- "fortify the loop system"
- "loop tests"
- "mocks"
- "simulations"
- "hardening"
- "quality check"

## Workflow

1. Choose the weakest current contract or operator path
2. Prefer deterministic fast checks first
3. Add a lightweight simulation when a real operator flow is not protected
4. Update the continuity plan if obligations changed
5. Run the relevant suite and report residual risk

## Guardrails

- Prefer additive tests over rewrites
- Do not build a huge brittle harness without a clear target
- Make failures actionable and human-readable
