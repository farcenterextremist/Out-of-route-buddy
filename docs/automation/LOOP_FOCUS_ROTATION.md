# Improvement Loop — Focus Rotation

**Purpose:** Each run emphasizes one of six focus areas. Rotate across runs to ensure balanced improvement. Research: "Rotate focus — Security one run, UI/UX next, shipability next."

**References:** [IMPROVEMENT_LOOP_RESEARCH_2025-03.md](./IMPROVEMENT_LOOP_RESEARCH_2025-03.md), [IMPROVEMENT_LOOP_ROUTINE.md](./IMPROVEMENT_LOOP_ROUTINE.md)

---

## Focus Areas (6)

| Focus | Phase 0 | Phase 1 | Phase 2 | Phase 3 |
|-------|---------|---------|---------|---------|
| **Security** | Security research deeper | Full 1.2; PII grep, dependency check | Lighter | Lighter |
| **UI/UX** | Design research 15 min | Lighter | Lighter | Full 3.2; apply design |
| **Shipability** | Read STORE_CHECKLIST | Lighter | Lighter | Add Phase 4.1b shipability check |
| **Code Quality** | Read REDUNDANT, FAILING | Dead code, test fix | Fix one test (high-change area) | Lighter |
| **File Structure** | Read docs layout | Lighter | File structure verification | File Organizer proposes one move |
| **Data/Metrics** | Read last summary metrics, [USER_METADATA_USAGE_GUIDE](./USER_METADATA_USAGE_GUIDE.md) | Lighter | Ensure metrics captured; add one metadata task (research, collection, or display) | Expand metrics; metadata display/collection |

---

## Rotation Order

1. Security
2. UI/UX
3. Shipability
4. Code Quality
5. File Structure
6. Data/Metrics
7. (repeat) Security

---

## How to Set This Run's Focus

1. Read last `IMPROVEMENT_LOOP_SUMMARY_<date>.md`.
2. If "Next run focus" is suggested, use it.
3. Else, if "Focus" is recorded, use next in rotation order.
4. Else, default to Security.

---

## Recording Focus

In every summary, add:

```markdown
**This run's focus:** [Security | UI/UX | Shipability | Code Quality | File Structure | Data/Metrics]
**Next run focus (suggested):** [from File Organizer or metrics]
```
