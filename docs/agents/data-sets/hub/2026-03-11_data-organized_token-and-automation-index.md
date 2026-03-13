# Organized Data — Token Loop & Automation Index

**Date:** 2026-03-11  
**Purpose:** Single organized view of where key data lives, current state, and how to use it. For agents and next token loop.

---

## 1. Data locations (where things live)

### Token loop (docs/automation/)

| Asset | Path | Purpose |
|-------|------|---------|
| **Run ledger** | [TOKEN_LOOP_RUN_LEDGER.md](../../automation/TOKEN_LOOP_RUN_LEDGER.md) | Chronological runs; summary + snapshot link + steps per run |
| **Next tasks** | [TOKEN_LOOP_NEXT_TASKS.md](../../automation/TOKEN_LOOP_NEXT_TASKS.md) | Recommended TODOs per run; read at Step 0, updated at Step 7 |
| **Practices** | [TOKEN_SAVING_PRACTICES.md](../../automation/TOKEN_SAVING_PRACTICES.md) | Standard practices §1; what worked/didn't §3 per run |
| **Improvement plan** | [TOKEN_LOOP_IMPROVEMENT_PLAN.md](../../automation/TOKEN_LOOP_IMPROVEMENT_PLAN.md) | Blindspots, baseline, context research; read Step 0 |
| **Master plan** | [TOKEN_LOOP_MASTER_PLAN.md](../../automation/TOKEN_LOOP_MASTER_PLAN.md) | Phases, completion %, visual; update at Step 7 when items change |
| **Loop doc** | [TOKEN_REDUCTION_LOOP.md](../../automation/TOKEN_REDUCTION_LOOP.md) | Steps 0–7, §4.4 progress report (Loop #, proof of work, benefits) |
| **Events (JSONL)** | `docs/automation/token_loop_events.jsonl` | token_loop_start/end, step_start/step_end, metrics per run |
| **Snapshots** | `docs/automation/token_loop_snapshots/<run_id>.json` | Per-run state: rules, always_apply_count/lines, git_head, settings_snippet |

### Scripts (scripts/automation/)

| Script | Purpose |
|--------|---------|
| `run_token_loop.ps1` | Start loop: snapshot + token_loop_start; prints RunId |
| `token_loop_listener.ps1` | Record events (start, step_start/end, token_loop_end) |
| `token_loop_state_snapshot.ps1` | Write snapshot JSON for rollback + progress |
| `run_token_loop_tests.ps1` | Full suite: compile, wiring, listener, snapshot, events analysis |

### Hub (this folder)

| File | Role / topic | Description |
|------|--------------|-------------|
| [2026-03-11_token-loop_loop5-report-proof-of-work-and-benefits.md](./2026-03-11_token-loop_loop5-report-proof-of-work-and-benefits.md) | token-loop | Loop #5 report: proof of work, benefits, next TODOs |
| [README.md](./README.md) | hub | Hub index and how to use |

### Other automation (docs/automation/)

| Doc | Purpose |
|-----|---------|
| [CURSOR_SELF_IMPROVEMENT.md](../../automation/CURSOR_SELF_IMPROVEMENT.md) | Self-improvement rules; references token loop as Phase 0.6 |
| [120_MINUTE_IMPROVEMENT_LOOP.md](../../automation/120_MINUTE_IMPROVEMENT_LOOP.md) | 2-hour improvement loop |
| [LOOP_MASTER_ALLOWLIST.md](../../automation/LOOP_MASTER_ALLOWLIST.md) | Allowlist for loop commands |

---

## 2. Current state (as of 2026-03-11)

- **Loop count:** 5 runs (`token_loop_start` in events).
- **Latest run:** token-20260311-2305 (Loop #5).
- **Rule output:** always_apply_count **2**, always_apply_lines **53** (target: 1 rule, &lt;50 lines). Rules: self-improvement.mdc, data-separation.mdc.
- **Master plan:** 21/26 = 81% (Phase 6 testing 6/6 done).
- **Progress report:** Every run must include Loop #, proof of work, and how we benefit (§4.4).

---

## 3. How to use this data

- **Next token loop (Loop #6):** Run `.\scripts\automation\run_token_loop.ps1`; use same RunId for all listener calls; at Step 0 read TOKEN_LOOP_NEXT_TASKS (latest section), TOKEN_SAVING_PRACTICES, TOKEN_LOOP_IMPROVEMENT_PLAN; at Step 7 append ledger, NEXT_TASKS, TOKEN_SAVING_PRACTICES §3; report Loop #, proof of work, benefits.
- **Other agents:** Use hub index (README.md) for completed reports; use this file for a map of automation + token data and current state.
- **Rollback / comparison:** Snapshot JSONs in `token_loop_snapshots/`; ledger for run list and links.

---

*Organized and sent to hub on user request: "organize data and send to hub".*
