# Markdownlint Remediation Plan

**Purpose:** Reduce legacy markdownlint noise while we preserve existing docs and improve incrementally.

---

## What was addressed now

1. Added lint baseline config:
   - `.markdownlint.jsonc`
2. Added a Heavy sandboxed future feature for full automation:
   - `Documentation lint remediation automation` (`FUTURE_IDEAS.md` § 14.29)
3. Continued strict rule:
   - No new loop governance docs should add avoidable lint debt.

---

## Phased cleanup

| Phase | Goal | Scope |
| --- | --- | --- |
| 1 | Baseline and stop growth | Keep new docs clean; avoid introducing new style debt |
| 2 | High-value docs first | Loop governance docs (`LOOP_*`, `IMPROVEMENT_*`, run ledgers) |
| 3 | Full normalization | Remaining legacy docs in `docs/automation` and `docs/product` |
| 4 | Optional automation | Implement feature §14.29 to normalize markdown style automatically |

---

## Success criteria

- Warnings trend down each loop run
- No critical markdown quality regressions in newly edited files
- Governance docs remain readable and enforceable
