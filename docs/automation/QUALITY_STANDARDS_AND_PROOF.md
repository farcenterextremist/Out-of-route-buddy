# Quality Standards and Proof of Quality

**Purpose:** Define a practical, repeatable quality bar for loop runs and require verifiable proof in every summary.

**Why this exists:** "Quality" is too vague unless each run proves what was checked, what passed, and what risk remains.

---

## Research-backed standards used

## 1) Product quality model (ISO/IEC 25010)

Use the ISO quality model as the structure for software quality decisions:

- Functional suitability
- Performance efficiency
- Compatibility
- Usability
- Reliability
- Security
- Maintainability
- Portability

For loop runs, this project emphasizes: **functional suitability, reliability, security, maintainability, and usability**.

## 2) Delivery quality (DORA metrics)

Use delivery-performance evidence to avoid "it feels better" claims:

- Deployment frequency (tracked over release cycles)
- Lead time for changes
- Change fail rate
- Failed deployment recovery time
- Deployment rework rate

For loop summaries, at minimum capture local proxies (test/lint pass, regressions, rollback need, rework needed).

## 3) Operational quality (Google SRE golden signals)

Monitor and reason using:

- Latency
- Traffic
- Errors
- Saturation

For this repo, map to practical checks: test runtime trends, error counts, failing-test rate, and warning/debt pressure.

---

## Proof-of-quality requirement (mandatory per loop summary)

Every loop summary must include a **Proof of Quality** section with evidence links/paths.

Minimum evidence:

1. **Environment/liveness proof**
   - `scripts/automation/loop_health_check.ps1 -Quick` result
2. **Readiness proof**
   - Unit test result (`:app:testDebugUnitTest` or scoped test command used)
   - Lint result (`:app:lintDebug` or scoped lint evidence)
3. **Change proof**
   - Files changed list (or diff summary)
   - What behavior was protected (new/updated tests)
4. **Risk proof**
   - Known residual risk(s) and why acceptable for this run
   - Next guard/fix planned
5. **Traceability proof**
   - Summary path
   - Ledger entry path
   - Shared-state update (`loop_shared_events.jsonl`, `loop_latest/<loop>.json`)

If any mandatory proof is missing, quality grade cannot be A.

---

## Loop quality gate rubric

Use this rubric at loop end:

- **A (ship confidence):** All required proof present; tests/lint green (or scoped with clear rationale); no new critical risk.
- **B (usable, not ideal):** Most proof present; one non-critical gap documented with explicit follow-up.
- **C (incomplete):** Missing key proof, unresolved regressions, or unclear risk ownership.

---

## Quick mapping for OutOfRouteBuddy

| Quality area | Loop evidence |
|---|---|
| Functional suitability | Feature behavior verified by targeted tests or explicit manual validation note |
| Reliability | Regressions guarded; failing tests triaged with owner/next step |
| Security | Security notes/checklist touched when relevant; no sensitive logging introduced |
| Maintainability | Small scoped changes, docs synced, clear follow-up tasks |
| Usability | No unwarranted UI change; accessibility/copy checks when UI touched |

---

## Anti-slop rule for quality claims

Never claim "quality improved" without evidence. Use this sentence pattern:

`Claim -> Evidence -> Residual risk -> Next step`

Example:

- Claim: "Drawer reliability improved."
- Evidence: "Focused MainActivity Robolectric test and lint pass."
- Residual risk: "Menu-item routing not yet regression-tested."
- Next step: "Add routing assertion in next mini loop."

