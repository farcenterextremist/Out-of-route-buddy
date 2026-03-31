---
name: loop-health-manager
description: Manages loop health using liveness, readiness, continuity checks, and diagnostic sweeps. Use when the user mentions loop health, loop health manager, reliability checks, readiness, diagnostics, or system health during loops.
---

# Loop Health Manager

## Purpose

Act as the operational owner for loop health so every loop checks environment health, readiness, continuity, and residual risk in a consistent way.

Primary references:

- `docs/automation/LOOP_HEALTH_CHECKS.md`
- `docs/automation/LOOP_DIAGNOSTIC_SWEEP.md`
- `docs/automation/QUALITY_STANDARDS_AND_PROOF.md`
- `.cursor/skills/loop-readiness-benchmarker/SKILL.md`
- `.cursor/skills/loop-diagnostic-sweeper/SKILL.md`
- `.cursor/skills/loop-continuity-test-engineer/SKILL.md`

## Trigger

Use this skill when requests mention:

- "loop health manager"
- "loop health"
- "system health during loops"
- "readiness"
- "liveness"
- "diagnostic sweep"
- "health check all loops"

## Workflow

1. **Run or confirm liveness**
   - Environment, repo, gradle, writable automation paths.

2. **Run or confirm readiness**
   - Tests, lint, or scoped proof used for the run.

3. **Run continuity when relevant**
   - Required after loop docs, scripts, gates, or shared-state changes.

4. **Run a diagnostic sweep**
   - Lints, ignored tests, hotspot search, residual risk.

5. **Summarize health clearly**
   - Healthy, degraded, blocked, or healthy-with-risk.

## Output format

```markdown
## Loop Health Report

- Liveness: [pass/fail + evidence]
- Readiness: [pass/fail + evidence]
- Continuity: [pass/fail/not-needed]
- Diagnostic sweep: [done + key finding]
- Residual risk: [one line]
- Next health action: [one line]
```

## Guardrails

- Do not mark a loop healthy just because one script passed.
- Include residual risk even when checks are green.
- Prefer reproducible evidence over intuition.
