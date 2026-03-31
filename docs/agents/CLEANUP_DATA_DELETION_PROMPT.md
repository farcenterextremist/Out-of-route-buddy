# Cleanup Data Deletion — Prompt for Neighbor Agents

**Purpose:** Give this prompt to any agent (including neighbor agents) when running a **cleanup-only pass** for OutOfRouteBuddy loop data. Ensures only low-value, reproducible, or temporary artifacts are removed.

---

## Copy/paste prompt

```text
You are running a cleanup-only pass for OutOfRouteBuddy loop data.

GOAL
Delete only low-value, reproducible, or temporary artifacts.
Do NOT delete canonical loop state, ledgers, summaries, hub artifacts, source code, tests, rules, or skills.

DELETE ALLOWLIST (safe targets)
1) Test-only temp artifacts:
- docs/automation/_continuity_test_tmp/**
- docs/automation/*events_test.jsonl
- docs/automation/token_loop_events_test.jsonl
- docs/automation/loop_events_test.jsonl

2) Disposable generated scratch files older than 14 days:
- docs/automation/**/*.tmp
- docs/automation/**/*.bak

3) Optional age-based cleanup (only if older than 30 days, keep newest 3):
- docs/automation/token_loop_snapshots/*.json

NEVER DELETE
- docs/automation/loop_shared_events.jsonl
- docs/automation/loop_latest/*.json
- docs/automation/*RUN_LEDGER*.md
- docs/automation/*SUMMARY*.md
- docs/agents/data-sets/hub/**
- .cursor/rules/**, .cursor/skills/**
- app/**, src/**, tests/**, gradle files, workflow files

EXECUTION GATE
1) Show dry-run list first (exact paths + reason).
2) Wait for explicit approval phrase: "approve cleanup batch".
3) Delete only approved paths.
4) Report:
   - deleted_count
   - deleted_paths
   - skipped_protected_paths
   - recommended next cleanup
```

---

## Related

- **Policy and better ideas:** [docs/automation/CLEANUP_RETENTION_POLICY.md](../automation/CLEANUP_RETENTION_POLICY.md)
- **Automated prune script:** `scripts/automation/run_data_prune.ps1` (use `-DryRun` then `-Approve`)
- **Safety test:** `scripts/automation/test_cleanup_safety.ps1` (fails if protected paths appear in candidates)
