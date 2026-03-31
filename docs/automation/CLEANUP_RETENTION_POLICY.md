# Cleanup Retention Policy

## Purpose

Define a safe, repeatable cleanup policy for automation-generated files. This policy minimizes workspace clutter while preserving continuity evidence, loop governance artifacts, and source-of-truth documents.

## Data Classes

### Hot

Short-lived test and scratch artifacts that can be removed quickly after use.

- Examples: continuity temp files, `*events_test.jsonl`, `*.tmp`, `*.bak`

### Warm

Useful operational history that should be kept for a short period and rotated with guardrails.

- Examples: token loop snapshot JSON files

### Cold

Canonical history, ledgers, summaries, shared state, and durable governance records.

- Examples: run ledgers, loop latest state files, hub records, rules/skills metadata

## Retention Windows

- Test temp artifacts: **14 days**
- Token loop snapshots: **30 days**, while **keeping newest 3**
- Quarantine staging hold: **7 days** before purge eligibility

## Execution Gates

1. **Dry-run first**
   - Default execution mode is dry-run.
   - No files are moved/deleted in dry-run mode.
2. **Approval phrase / explicit approval**
   - Mutating actions require explicit approval (`-Approve`).
3. **Ledger append**
   - Every run appends an entry to `docs/automation/CLEANUP_RUN_LEDGER.md`.

## Protected Paths

The cleanup process must not modify or remove files under:

- `docs/automation/loop_shared_events.jsonl`
- `docs/automation/loop_latest/**`
- `docs/automation/*RUN_LEDGER*.md`
- `docs/automation/*SUMMARY*.md`
- `docs/agents/data-sets/hub/**`
- `.cursor/rules/**`
- `.cursor/skills/**`

## Command Examples

Run from repository root.

Dry-run (default):

```powershell
.\scripts\automation\run_data_prune.ps1
```

Approve and move candidates to quarantine staging:

```powershell
.\scripts\automation\run_data_prune.ps1 -Approve
```

Approve and also purge staging files older than 7 days:

```powershell
.\scripts\automation\run_data_prune.ps1 -Approve -PurgeStaging
```
