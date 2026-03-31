---
name: loop-continuity-test-engineer
description: Designs and enforces continuity tests for loop gates, shared state, and run traceability. Use when loop docs/scripts change or when validating cross-loop reliability.
---

# Loop Continuity Test Engineer

## Purpose

Keep loop operations stable by turning gate and state assumptions into executable checks.

Primary references:
- `docs/automation/LOOP_CONTINUITY_TEST_PLAN.md`
- `scripts/automation/run_loop_continuity_tests.ps1`
- `scripts/automation/test_loop_gate_contract.ps1`
- `scripts/automation/test_shared_state_contract.ps1`

## Trigger

Use this skill when requests mention:
- "continuity tests"
- "gate regression"
- "shared state reliability"
- "verify loop wiring"
- "stability checks"

## Workflow

1. **Map assumptions to checks**
   - Gate obligations -> text/contract checks
   - Shared-state assumptions -> JSON and key presence checks
   - Traceability assumptions -> summary/ledger path checks

2. **Implement or update tests**
   - Keep tests deterministic and fast
   - Fail with actionable messages

3. **Run aggregated continuity suite**
   - Execute continuity runner script
   - Record pass/fail in loop summary when relevant

4. **Harden weak spots**
   - Add missing checks for newly introduced obligations
   - Remove flaky assertions and replace with structural assertions

## Output format

```markdown
## Continuity Test Result
- Suite: [name]
- Status: [pass/fail]
- Risk if failing: [short]
- Immediate fix: [short]
```

## Guardrails

- Do not rely on brittle timing-based assertions.
- Keep checks backward compatible unless explicit migration is approved.
- Prefer additive test coverage over rewrites.
