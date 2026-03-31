# Loop diagnostic sweep

**Purpose:** Add a repeatable problem-hunt step to loops so they do more than build and test. This sweep looks for environment issues, stale risks, debugging signals, and likely regressions.

**Why this exists:** Health checks answer "can the loop run?" A diagnostic sweep answers "what problems are waiting to bite us?"

**Related:** `docs/automation/LOOP_HEALTH_CHECKS.md`, `docs/automation/QUALITY_STANDARDS_AND_PROOF.md`, `docs/qa/FAILING_OR_IGNORED_TESTS.md`, `docs/security/SECURITY_NOTES.md`.

---

## Difference from health checks

| Type | Question | Typical output |
|---|---|---|
| **Liveness** | Is the loop environment alive? | repo/gradle/docs writable status |
| **Readiness** | Is the build/test state good enough to proceed? | test/lint result |
| **Diagnostic sweep** | What problems, regressions, or suspicious weak spots should we investigate? | findings, likely hotspots, next fixes |

---

## Required sweep categories

Each substantial loop run should do at least one check from each category:

1. **Environment**
   - Liveness result
   - Continuity tests when loop contracts or shared state changed

2. **Build/test**
   - Focused failing/ignored test review
   - Build or compile sanity check

3. **Diagnostics**
   - Read lints for touched files
   - Search for TODO/FIXME/error-prone hotspots relevant to the run
   - Review logs or debug hooks if the task is failure-oriented

4. **Behavior risk**
   - Identify one likely regression path
   - Name how it was guarded or why it remains a risk

---

## Sweep checklist

Use this checklist during loop execution:

- [ ] Run liveness check at loop start or phase start
- [ ] Run readiness check at phase end
- [ ] Run continuity tests if loop docs/scripts/shared state changed
- [ ] Read lints for edited files
- [ ] Search for at least one likely failure hotspot related to the loop focus
- [ ] Review failing or ignored tests if touching related areas
- [ ] Name one residual risk and one next diagnostic step

For a repeatable baseline, you can start with:

```powershell
.\scripts\automation\run_loop_diagnostic_baseline.ps1
```

This script does not replace judgment. It standardizes the evidence-gathering step so the loop can paste a cleaner `Diagnostic Sweep` block into its summary.

---

## Recommended problem-hunt sources

Choose the most relevant sources for the current loop:

- `docs/qa/FAILING_OR_IGNORED_TESTS.md`
- `ReadLints` on changed files
- targeted `rg` searches for `TODO|FIXME|error|warning|fallback|deprecated`
- recent loop summaries and ledgers
- `docs/security/SECURITY_NOTES.md`
- app-specific logs or debug docs when investigating failures

Script support:

- `scripts/automation/run_loop_diagnostic_baseline.ps1`
  - Reads the latest liveness state from `docs/automation/loop_health_state.json`
  - Reviews active rows in `docs/qa/FAILING_OR_IGNORED_TESTS.md`
  - Searches focus paths for hotspot markers like `TODO`, `FIXME`, `fallback`, `deprecated`, and `warning`
  - Reports whether unit-test, lint, and detekt reports are present

---

## Output block

When a loop uses this sweep, add:

```markdown
## Diagnostic Sweep

- Environment: [healthy/degraded + evidence]
- Problem search: [what was searched]
- Findings: [1-3 findings or none]
- Residual risk: [one line]
- Next diagnostic step: [one line]
```

---

## Anti-slop rule

Do not mark diagnostics "done" just because tests passed.

A useful sweep must name:

- where problems were searched for,
- what was found or not found,
- and what risk still remains.
