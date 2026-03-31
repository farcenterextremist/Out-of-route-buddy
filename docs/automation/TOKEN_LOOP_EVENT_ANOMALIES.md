# Token Loop Event Anomalies

**Purpose:** Durable record of known historical token-loop event anomalies so future runs do not need to rediscover them from raw `token_loop_events.jsonl` output.

**Source of truth for raw history:** `docs/automation/token_loop_events.jsonl`
**Detection path:** `scripts/automation/test_token_loop_events_analysis.ps1`

---

## Current known anomalies

### 1. Incomplete historical start

- `run_id`: `token-20260311-1944`
- Shape: `token_loop_start` exists without a matching `token_loop_end`
- Interpretation: treat as an older aborted or partial run, not as a completed token loop
- Action taken: documented only; no synthetic `token_loop_end` was appended
- Why: backfilling a fake completion would make the historical trace less trustworthy

### 2. Orphan stray run id from manual Step 0 misfire

- `run_id`: `token-20260316-0152`
- Shape: orphan `step_start` for Step `0` without matching start/end under that run id
- Related completed run: `token-20260316-0147`
- Interpretation: manual listener call used an auto-generated run id instead of the active token run id
- Action taken: documented only; latest-start/latest-complete analysis now reports the real completed run separately

---

## What is considered healthy now

- Latest started token run: `token-20260316-0147`
- Latest completed token run: `token-20260316-0147`
- Shared-state latest file: `docs/automation/loop_latest/token.json`
- Preferred closeout path: `scripts/automation/complete_token_loop_run.ps1`

---

## Maintenance rule

When a new anomaly is discovered:

1. Confirm it from `token_loop_events.jsonl`.
2. Decide whether it is a real incomplete run, an orphan event, or a reporting bug.
3. Record it here before changing history.
4. Only backfill history when the evidence is strong and the repair is explicitly justified.
