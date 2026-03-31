# Token Loop — Run Ledger

**Purpose:** Chronological record of every Token Loop run. At Step 7, append one block below. Keeps runs easy to find and compare.

**Where else it's documented:**

- **Snapshot (rollback):** `docs/automation/token_loop_snapshots/<run_id>.json`
- **Tasks & details:** [TOKEN_LOOP_NEXT_TASKS.md](./TOKEN_LOOP_NEXT_TASKS.md) — one section per run with Rule output vs baseline, what worked, recommended tasks
- **Rule output trend:** [TOKEN_LOOP_IMPROVEMENT_PLAN.md](./TOKEN_LOOP_IMPROVEMENT_PLAN.md) §4 — optional "Rule output this run" per run
- **Events:** `docs/automation/token_loop_events.jsonl` — step_start/step_end, metrics
- **Consistency snippet:** [LOOP_CONSISTENCY_LEDGER_SNIPPET.md](./LOOP_CONSISTENCY_LEDGER_SNIPPET.md) — copy into each run block

---

## Template (append one block per run)

```markdown
---
## Run token-YYYYMMdd-HHmm (YYYY-MM-DD)

- **Summary:** [One line: what this run achieved or focused on.]
- **Rule output:** always_apply_count **N**, always_apply_lines **N** (target &lt;50).
- **Snapshot:** [token_loop_snapshots/<run_id>.json](./token_loop_snapshots/<run_id>.json) | **Details:** [TOKEN_LOOP_NEXT_TASKS.md#run-<run_id>](./TOKEN_LOOP_NEXT_TASKS.md)
- **Steps:** 0–7 completed (Y/N) | token_loop_end (Y/N)
- **Proof of quality:** Liveness [pass/fail], readiness evidence [scoped test/lint or rationale], residual risk [one line]
- **Loop consistency check:** Copy from [LOOP_CONSISTENCY_LEDGER_SNIPPET.md](./LOOP_CONSISTENCY_LEDGER_SNIPPET.md) and record `Consistency score: X/10`.
---
```

---

## Run token-20260316-0147 (2026-03-16)

- **Summary:** Loop #8; full token loop with preflight, rules/settings tightening, and Step 0 research refresh; data-separation moved off always-apply.
- **Rule output:** always_apply_count **1**, always_apply_lines **45** (target &lt;50).
- **Snapshot:** [token_loop_snapshots/token-20260316-0147.json](./token_loop_snapshots/token-20260316-0147.json) | **Details:** [TOKEN_LOOP_NEXT_TASKS.md#run-token-20260316-0147-2026-03-16](./TOKEN_LOOP_NEXT_TASKS.md#run-token-20260316-0147-2026-03-16)
- **Steps:** 0–7 completed Y | token_loop_end Y
- **Proof of quality:** Liveness pass via `scripts/automation/loop_health_check.ps1 -Quick`; readiness pass via `scripts/automation/run_token_loop_tests.ps1`; residual risk: `.cursorignore` could not be created because of permissions, and one manual Step 0 `step_start` listener event used an auto-generated RunId instead of the active run ID.

### Loop Consistency Check
- Trigger + owner: pass
- Start gates complete: pass
- Checkpoint recorded: pass
- Plan/phase scope explicit: pass
- Validation evidence (liveness/readiness): pass
- Proof of quality present: pass
- Ledger updated: pass
- Shared state updated: pass
- Hub handoff/index handled: not-applicable
- Next-step continuity: pass
- Consistency score: 10/10

---

## Run token-20260313-1843 (2026-03-13)

- **Summary:** Loop #7; full steps 0–7 + token_loop_end; Hub+NEXT_TASKS; 2 always-apply, 53 lines.
- **Rule output:** always_apply_count **2**, always_apply_lines **53** (target 1 rule, &lt;50 lines).
- **Snapshot:** [token_loop_snapshots/token-20260313-1843.json](./token_loop_snapshots/token-20260313-1843.json) | **Details:** [TOKEN_LOOP_NEXT_TASKS.md#run-token-20260313-1843](./TOKEN_LOOP_NEXT_TASKS.md)
- **Steps:** 0–7 completed Y | token_loop_end Y

---

## Run token-20260313-1756 (2026-03-13)

- **Summary:** Loop #6; full steps 0–7 + token_loop_end; Hub consulted; context compression/embedding/LLM opt in scope; 8 rules, 2 always-apply, 53 lines.
- **Rule output:** always_apply_count **2**, always_apply_lines **53** (target 1 rule, &lt;50 lines).
- **Snapshot:** [token_loop_snapshots/token-20260313-1756.json](./token_loop_snapshots/token-20260313-1756.json) | **Details:** [TOKEN_LOOP_NEXT_TASKS.md#run-token-20260313-1756-2026-03-13](./TOKEN_LOOP_NEXT_TASKS.md)
- **Steps:** 0–7 completed Y | token_loop_end Y

---

## Run token-20260311-2305 (2026-03-11)

- **Summary:** Loop #5; progress report updated to require Loop #, proof of work, and benefits every time; full steps 0–7 + token_loop_end.
- **Rule output:** always_apply_count **2**, always_apply_lines **53** (target 1 rule, &lt;50 lines).
- **Snapshot:** [token_loop_snapshots/token-20260311-2305.json](./token_loop_snapshots/token-20260311-2305.json) | **Details:** [TOKEN_LOOP_NEXT_TASKS.md#run-token-20260311-2305-2026-03-11](./TOKEN_LOOP_NEXT_TASKS.md)
- **Steps:** 0–7 completed Y | token_loop_end Y

---

## Run token-20260311-2227 (2026-03-11)

- **Summary:** Launch 3 token loop; Step 0 research (2025 token practices); progress report §4.4 added; two always-apply rules (data-separation + self-improvement) — target one.
- **Rule output:** always_apply_count **2**, always_apply_lines **53** (target 1 rule, &lt;50 lines).
- **Snapshot:** [token_loop_snapshots/token-20260311-2227.json](./token_loop_snapshots/token-20260311-2227.json) | **Details:** [TOKEN_LOOP_NEXT_TASKS.md#run-token-20260311-2227-2026-03-11](./TOKEN_LOOP_NEXT_TASKS.md)
- **Steps:** 0–7 completed Y | token_loop_end Y

---

## Run token-20260311-2115 (2026-03-11)

- **Summary:** Improvement plan implemented; snapshot fix (always_apply_count from rules array); Step 0/7 wired to improvement plan; full loop run.
- **Rule output:** always_apply_count **1**, always_apply_lines **43** (target &lt;50).
- **Snapshot:** [token_loop_snapshots/token-20260311-2115.json](./token_loop_snapshots/token-20260311-2115.json) | **Details:** [TOKEN_LOOP_NEXT_TASKS.md](./TOKEN_LOOP_NEXT_TASKS.md#run-token-20260311-2115-2026-03-11)
- **Steps:** 0–7 completed Y | token_loop_end Y

---

## Run token-20260311-2031 (2026-03-11)

- **Summary:** First full token loop; snapshot/listener wired; discovered always_apply_count bug (reported 3, correct 1).
- **Rule output:** always_apply_count **1** (intended; snapshot bug showed 3), always_apply_lines **42**.
- **Snapshot:** [token_loop_snapshots/token-20260311-2031.json](./token_loop_snapshots/token-20260311-2031.json) | **Details:** [TOKEN_LOOP_NEXT_TASKS.md](./TOKEN_LOOP_NEXT_TASKS.md#run-token-20260311-2031-2026-03-11)
- **Steps:** 0–7 completed Y | token_loop_end Y

---

*Append a new block at Step 7 of each token loop run.*
