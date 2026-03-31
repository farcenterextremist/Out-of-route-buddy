# Universal Agent Spawn Rule

**Purpose:** Keep multi-agent loop execution consistent, auditable, and low-risk.

## Pre-spawn requirements

1. Read:
   - `docs/automation/LOOP_GATES.md`
   - `docs/automation/LOOP_CONSISTENCY_STANDARD.md`
   - `docs/automation/LOOP_DYNAMIC_SHARING.md`
2. Define spawn intent in one line:
   - role
   - scope
   - expected output artifact
3. Assign one **owner agent** responsible for merge + final report.

## Spawn limits

- Max 4 agents in parallel.
- One clear owner for conflict resolution and integration.
- No overlapping write scope unless explicitly coordinated.

## Required output from each spawned agent

- `artifact_path`
- `evidence` (tests/checks run)
- `continuity_risks`
- `next_steps` (1-3)

## Post-spawn closeout

1. Run continuity checks:
   - `.\scripts\automation\run_loop_continuity_tests.ps1`
2. If polished output exists, send to hub and update index.
3. Write shared state with dedupe-safe writer:
   - `.\scripts\automation\write_loop_shared_state.ps1 ...`

## Guardrails

- Non-master loops do not implement frontend/UI changes.
- Heavy changes remain approval-gated.
- Cleanup agents must run dry-run first and quarantine before delete.
