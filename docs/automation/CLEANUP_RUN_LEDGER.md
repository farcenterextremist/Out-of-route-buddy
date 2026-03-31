# Cleanup Run Ledger

Append one block per cleanup run.

---

## Cleanup Run Template

```text
## Cleanup Run: <run_id>
- Timestamp (UTC): <yyyy-MM-ddTHH:mm:ssZ>
- Operator: <agent/user>
- Mode: <dry-run|approve>
- Purge staging: <true|false>

### Discovery
- Candidate count: <n>
- Protected-blocked count: <n>
- Candidate roots:
  - docs/automation/_continuity_test_tmp/**
  - docs/automation/*events_test.jsonl
  - docs/automation/**/*.tmp (older than 14d)
  - docs/automation/**/*.bak (older than 14d)
  - docs/automation/token_loop_snapshots/*.json (older than 30d, keep newest 3)

### Actions
- Moved to quarantine: <n>
- Quarantine path: docs/automation/_trash_staging/<yyyy-MM-dd>/
- Purged from staging (older than 7d): <n>

### Notes
- Warnings: <none|details>
- Exceptions: <none|details>
```
