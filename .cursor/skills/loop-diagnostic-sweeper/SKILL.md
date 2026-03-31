---
name: loop-diagnostic-sweeper
description: Adds a structured diagnostic and problem-hunt pass to loop runs using liveness, readiness, continuity, lint, and hotspot searches. Use when auditing loop health, debugging loop drift, or ensuring loops actively search for problems instead of only running tests.
---

# Loop Diagnostic Sweeper

## Purpose

Make loop runs better at finding trouble early by combining health checks with a focused problem-hunt pass.

Primary references:

- `docs/automation/LOOP_DIAGNOSTIC_SWEEP.md`
- `docs/automation/LOOP_HEALTH_CHECKS.md`
- `docs/automation/QUALITY_STANDARDS_AND_PROOF.md`
- `docs/qa/FAILING_OR_IGNORED_TESTS.md`

## Trigger

Use this skill when requests mention:

- "health check all loops"
- "debug the loop"
- "search for problems"
- "diagnostic sweep"
- "loop reliability"
- "find regressions"

## Workflow

1. **Run health baseline**
   - Confirm liveness/readiness evidence exists or run it.

2. **Run continuity when relevant**
   - If loop contracts, shared state, or automation scripts changed, run continuity tests.

3. **Do a focused problem hunt**
   - Read lints for touched files.
   - Search likely hotspots tied to the loop focus.
   - Review ignored/failing tests or recent warnings.

4. **Name the risk**
   - Identify at least one residual risk or weak spot.
   - Suggest one next diagnostic action.

## Output format

```markdown
## Diagnostic Sweep

- Environment: [healthy/degraded + evidence]
- Problem search: [what was searched]
- Findings: [1-3 findings or none]
- Residual risk: [one line]
- Next diagnostic step: [one line]
```

## Guardrails

- Tests passing is not enough; diagnostics must still search for hidden risk.
- Keep searches targeted to the loop focus.
- Prefer reproducible findings over vague suspicion.
