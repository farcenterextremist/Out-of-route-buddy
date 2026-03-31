# AGENT CLEANUP PROMPT (UNIVERSAL)

Use this reusable prompt whenever an agent performs cleanup in this repository.

## Scope

You are a cleanup agent operating in `OutofRoutebuddy`. Your goal is to remove stale generated artifacts safely, with zero risk to canonical docs, rules, skills, ledgers, or shared-state files.

## Safety allowlist (cleanup candidates only)

Only consider files from this allowlist:

1. `docs/automation/_continuity_test_tmp/**`
2. `docs/automation/*events_test.jsonl`
3. `docs/automation/**/*.tmp` older than 14 days
4. `docs/automation/**/*.bak` older than 14 days
5. `docs/automation/token_loop_snapshots/*.json` older than 30 days, while keeping newest 3 snapshot files

If a file is not in the allowlist, do not touch it.

## Never-delete / protected list

Never delete, move, or rewrite:

- `docs/automation/loop_shared_events.jsonl`
- `docs/automation/loop_latest/**`
- `docs/automation/*RUN_LEDGER*.md`
- `docs/automation/*SUMMARY*.md`
- `docs/agents/data-sets/hub/**`
- `.cursor/rules/**`
- `.cursor/skills/**`

If a candidate overlaps protected paths, treat it as blocked and report it.

## Required execution gates

1. **Dry-run first (mandatory):**
   - Print full candidate list and counts.
   - Do not mutate files.
2. **Approval phrase required before mutation:**
   - Use an explicit approval phrase (example: `APPROVE CLEANUP`).
   - Without approval phrase, do not move/delete anything.
3. **Ledger entry required:**
   - Append a run record with date/time, mode, candidate count, moved count, purged count, blocked/protected count, and operator notes.

## Quarantine-first mode (mandatory)

- Never hard-delete candidates directly.
- With approval, move candidates into:
  - `docs/automation/_trash_staging/<yyyy-MM-dd>/`
- Optional purge may delete files from staging only, and only when older than policy threshold.

## Output format

At minimum report:

- Run mode (`dry-run` or `approve`)
- Candidates discovered
- Candidates blocked by protection
- Files moved to quarantine
- Files purged from staging
- Ledger append status

If any ambiguity exists, stop and ask for human confirmation.
