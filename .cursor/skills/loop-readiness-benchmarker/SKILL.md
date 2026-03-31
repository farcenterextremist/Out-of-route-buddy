---
name: loop-readiness-benchmarker
description: Tracks and benchmarks loop readiness/liveness performance trends (tests, lint, pulse) across runs. Use when analyzing loop health metrics, identifying regressions, or improving loop reliability targets.
---

# Loop Readiness Benchmarker

## Purpose

Make loop health measurable over time and detect reliability regressions early.

Primary references:

- `docs/automation/LOOP_HEALTH_CHECKS.md`
- `docs/automation/QUALITY_STANDARDS_AND_PROOF.md`
- `docs/automation/LOOP_METRICS_TEMPLATE.md`
- `docs/automation/pulse_log.txt`
- `docs/automation/loop_health_state.json`

---

## Trigger

Use this skill when requests mention:

- "loop performance"
- "readiness trend"
- "health metrics"
- "test/lint duration"
- "reliability regression"

---

## Workflow

1. **Collect evidence**
   - Pull recent liveness/readiness signals from summaries, pulse log, and health state.

2. **Build trend snapshot**
   - Capture pass/fail trend, rough durations, and failure hotspots.

3. **Compare against baseline**
   - Identify improving, stable, and degrading signals.

4. **Recommend actions**
   - Propose 1-3 high-impact fixes (e.g. flaky test isolation, narrower lint scope for fast checks, missing health gate).

5. **Write concise report**
   - Add benchmark notes to summary or ledger and include residual risk.

---

## Output format

```markdown
## Readiness Benchmark
- Liveness trend: [improving/stable/degrading]
- Readiness trend (tests/lint): [improving/stable/degrading]
- Median-ish duration trend: [up/down/stable]
- Regressions detected: [list or none]
- Recommended next actions: [1-3 bullets]
```

---

## Guardrails

- Prefer reproducible evidence over intuition.
- If data is incomplete, mark assumptions explicitly.
- Keep recommendations scoped and executable next run.
