---
name: loop-quality-proof
description: Enforces loop quality standards and proof-of-quality evidence for OutOfRouteBuddy runs. Use when the user asks for quality standards, proof of quality, quality gates, audit-quality summaries, or loop quality verification.
---

# Loop Quality Proof

## Purpose

Apply a consistent quality bar across loop runs and require evidence-backed quality claims.

Primary reference: `docs/automation/QUALITY_STANDARDS_AND_PROOF.md`

---

## Trigger

Use this skill when requests mention:

- "quality standards"
- "proof of quality"
- "quality gate"
- "quality audit"
- "show evidence quality is good"
- loop summary quality grading

---

## Workflow

1. **Read baseline**
   - `docs/automation/QUALITY_STANDARDS_AND_PROOF.md`
   - `docs/automation/LOOP_METRICS_TEMPLATE.md`
   - `docs/automation/IMPROVEMENT_LOOP_ROUTINE.md`

2. **Collect mandatory evidence**
   - Liveness: `scripts/automation/loop_health_check.ps1 -Quick`
   - Readiness: tests and lint used for the run
   - Change proof: key files + tests that protect behavior
   - Risk proof: residual risk + next guard/fix
   - Traceability proof: summary, ledger, shared-state updates

3. **Write proof block**
   - Add `## Proof of Quality` to the loop summary using the template in `LOOP_METRICS_TEMPLATE.md`.
   - Use claim-evidence-risk-next-step language.

4. **Grade quality**
   - Apply A/B/C rubric from `QUALITY_STANDARDS_AND_PROOF.md`.
   - If required evidence is missing, do not assign A.

5. **Close loop artifacts**
   - Ensure ledger and shared-state updates are complete for traceability.

---

## Output Template

```markdown
## Proof of Quality

- Standards used: ISO/IEC 25010 + DORA + SRE golden signals (project-adapted)
- Liveness evidence: [command + result]
- Readiness evidence: [tests command/result], [lint command/result]
- Change evidence: [key files/tests proving behavior]
- Residual risks: [known risks and why acceptable this run]
- Traceability: [summary path], [ledger path], [shared-state files updated]
```

---

## Guardrails

- Do not claim quality improvements without concrete evidence.
- Prefer scoped, reproducible checks over vague statements.
- Respect user rule: no unwarranted UI changes without explicit permission.

## Additional Resources

- Standards links and rationale: [reference.md](reference.md)
