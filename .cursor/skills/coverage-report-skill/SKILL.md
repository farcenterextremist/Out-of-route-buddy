---
name: coverage-report-skill
description: >-
  Produces ATT&CK/ATLAS heat map from past exercises. Use when the user asks
  for coverage report, heat map, technique coverage, or security exercise
  summary.
---

# Coverage Report Skill

## Invocation

When user asks for **coverage report**, **heat map**, **technique coverage**, or **exercise summary**:

1. Read exercise logs from `docs/agents/security-team-proof-of-work.md` and `docs/agents/data-sets/security-exercises/*.md`
2. Extract technique IDs from Red and Blue blocks
3. Build heat map
4. Output report

---

## Heat Map Format

```markdown
## ATT&CK/ATLAS Coverage Report

**Date range:** YYYY-MM-DD to YYYY-MM-DD
**Exercises:** [count]

### ATT&CK Matrix (sample)

| Technique | Name | Tested | Detected | Mitigated |
|-----------|------|--------|----------|-----------|
| T1566 | Phishing | ✓ | ✓ | ✓ |
| T1190 | Exploit Public-Facing App | ✓ | ✗ | ✓ |
| ATLAS-T-001 | Prompt Injection | ✓ | ✓ | ✓ |

### ATLAS Matrix (AI/LLM)

| Technique | Name | Tested | Detected | Mitigated |
|-----------|------|--------|----------|-----------|
| ATLAS-T-001 | Prompt Injection | ... | ... | ... |
| ATLAS-T-002 | Context Poisoning | ... | ... | ... |

### Gaps

- [Techniques tested but not detected]
- [Techniques not yet tested]

### Summary

- **Covered:** X techniques
- **Gaps:** Y techniques (tested, not detected)
- **Untested:** Z techniques
```

---

## Data Sources

- `docs/agents/security-team-proof-of-work.md` — Exercise log
- `docs/agents/data-sets/security-exercises/*.md` — Dated exercise files
- Extract: exercise_id, date, Red technique IDs, Blue alarm (yes/no), remediation

---

## Legend

| Status | Meaning |
|--------|---------|
| **Tested** | Red simulated this technique |
| **Detected** | Blue alarm went off |
| **Mitigated** | Fix implemented (whether or not detected) |
| **Gap** | Tested but not detected |
| **Untested** | Not in any exercise yet |

---

## Output Location

- Save report to `docs/agents/data-sets/security-exercises/COVERAGE_REPORT_YYYY-MM-DD.md`
- Or append summary to `docs/agents/security-team-proof-of-work.md` § Recent runs

---

## Additional Resources

- Exercise logs: [docs/agents/data-sets/security-exercises/](../../../docs/agents/data-sets/security-exercises/)
- Technique IDs: [red-team-skill/reference.md](../red-team-skill/reference.md)
