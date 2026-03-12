# Synthetic Data Loop — Run Ledger

**Purpose:** Shared, chronological record of every Synthetic Data Loop run. Agents append one block at the end of each run. See [SYNTHETIC_DATA_LOOP_FOR_OTHER_AGENTS.md](./SYNTHETIC_DATA_LOOP_FOR_OTHER_AGENTS.md).

---

## Template (copy and fill for each run)

```markdown
---
## Run YYYY-MM-DD

- **Focus:** [Create | Prune | Mesh | Quality report | Full]
- **Outputs:** [links to synthetic data, pruning proposal, quality report]
- **Tier changes applied:** [None | list after user approval]
- **Metrics:** Checkpoint [commit or tag], Files/outputs [brief]
- **Next:** [1–2 bullets for next run]
---
```

Replace YYYY-MM-DD and the bracketed values with this run's data. Append the block to the bottom of this file.

---

*Append new run blocks below.*

---
## Run 2025-03-11

- **Focus:** Full (gather + prune proposal + quality report)
- **Outputs:** [gather_manifest_2025-03-11.md](./synthetic_data/gather_manifest_2025-03-11.md), [pruning_proposal_2025-03-11.md](./synthetic_data/pruning_proposal_2025-03-11.md), [quality_report_2025-03-11.md](./synthetic_data/quality_report_2025-03-11.md)
- **Tier changes applied:** None (no live DB; proposal N/A)
- **Metrics:** Checkpoint `synthetic-data-loop-pre-20250311`, Files: 3 outputs in `docs/automation/synthetic_data/`
- **Next:** (1) Next run: read this ledger and quality report first; consider adding a small PLATINUM/SILVER trip generator or export script to populate `synthetic_data/` for prune/quality. (2) If running with device/emulator DB: run getTripsByTier(SILVER/PLATINUM) in Phase 1 and propose real prune candidates in Phase 2. (3) Use "Next" from this block to improve next time — record what worked and what to try next.

---
