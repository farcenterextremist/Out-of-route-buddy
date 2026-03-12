# Improvement Loop — Run Ledger

**Purpose:** Shared, chronological record of every Improvement Loop run. Agents append one block at the end of each run. See [IMPROVEMENT_LOOP_FOR_OTHER_AGENTS.md](./IMPROVEMENT_LOOP_FOR_OTHER_AGENTS.md).

---

## Template (copy and fill for each run)

```markdown
---
## Run YYYY-MM-DD (variant)

- **Focus:** [Security | UI/UX | Shipability | Code Quality | File Structure | Data/Metrics]
- **Variant:** [Quick | Standard | Full]
- **Summary:** [IMPROVEMENT_LOOP_SUMMARY_YYYY-MM-DD.md](./IMPROVEMENT_LOOP_SUMMARY_YYYY-MM-DD.md)
- **Metrics:** Tests [pass/fail], Lint [errors/warnings], Files changed [N], Checkpoint [commit or tag]
- **Next:** [1–2 bullets for next run]
---
```

Replace YYYY-MM-DD and the bracketed values with this run's data. Append the block to the bottom of this file.

---

## Run 2025-03-11 (Full)

- **Focus:** Shipability / Code Quality
- **Variant:** Full (2 hr)
- **Summary:** [120_MINUTE_LOOP_SUMMARY_2025-03-11.md](./120_MINUTE_LOOP_SUMMARY_2025-03-11.md)
- **Metrics:** Tests 1,021 passed; Lint not run (build failed); Files changed 6; Checkpoint not recorded
- **Next:** Add pre-loop checkpoint; resolve pre-existing build issues (AAPT errors)

---
