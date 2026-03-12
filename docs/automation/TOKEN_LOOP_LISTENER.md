# Token Reduction Loop — Listener

**Purpose:** Record token-loop events to structured data (JSONL) for analysis and loop improvement. **The listener starts alongside the loop** when you say "start token loop" — we use this data to improve the loop (save token spend, manage context squish). Events are appended to `docs/automation/token_loop_events.jsonl`.

**Script:** `scripts/automation/token_loop_listener.ps1`  
**Tests:** `scripts/automation/test_token_loop_listener.ps1` (listener only); **all token loop tests:** `scripts/automation/run_token_loop_tests.ps1` (listener + snapshot + events analysis).  
**Output:** `docs/automation/token_loop_events.jsonl` (one JSON object per line)

---

## Events

| Event | When | Typical use |
|-------|------|-------------|
| `token_loop_start` | Start of Token Reduction Loop | Agent or run script invokes |
| `step_start` | Start of a step (0–7) | Agent invokes with Step and Note |
| `step_end` | End of step (optional) | Agent invokes with metrics (e.g. always_apply_count) |
| `token_loop_end` | End of Token Reduction Loop | Agent or run script invokes |

Step 0 = deep research and analysis; Step 7 = organize results and recommend next tasks (TOKEN_LOOP_NEXT_TASKS.md).

---

## Usage

Use the **same RunId** for all events in a single run (e.g. `token-YYYYMMdd-HHmm`). `run_token_loop.ps1` generates RunId and uses it for snapshot and token_loop_start.

**Metrics:** Use **single-quoted** JSON for `-Metrics` in PowerShell so it parses as `metrics` (not `metrics_raw`). Or write JSON to a file and pass `-MetricsPath path\to\file.json` to avoid quoting issues.

```powershell
# From repo root
.\scripts\automation\token_loop_listener.ps1 -Event token_loop_start -Note "Full token audit"
.\scripts\automation\token_loop_listener.ps1 -Event step_start -Step "1" -Note "audit rules"
.\scripts\automation\token_loop_listener.ps1 -Event step_end -Step "1" -Metrics '{"always_apply_count":1,"always_apply_lines":57}'
.\scripts\automation\token_loop_listener.ps1 -Event token_loop_end -Note "steps=6" -Metrics '{"steps_completed":6}'
# Alternative: metrics from file (avoids quoting)
# '{"always_apply_count":1,"always_apply_lines":57}' | Set-Content -Path $env:TEMP\token_metrics.json
# .\scripts\automation\token_loop_listener.ps1 -Event step_end -Step "1" -MetricsPath $env:TEMP\token_metrics.json
```

**Test (simulation):**
```powershell
.\scripts\automation\test_token_loop_listener.ps1
.\scripts\automation\test_token_loop_listener.ps1 -KeepOutput  # Keep test output file
```

**Run all token loop tests** (listener + snapshot + events analysis):
```powershell
.\scripts\automation\run_token_loop_tests.ps1
.\scripts\automation\run_token_loop.ps1 -Test   # Same: runs all tests before starting loop
```

---

## Wiring

| Component | Invokes listener |
|-----------|------------------|
| **"Start token loop"** | When user says "start token loop," agent starts listener (`token_loop_start`) and runs steps 0–7 (0=research, 7=organize + recommend next tasks in TOKEN_LOOP_NEXT_TASKS.md), then `token_loop_end`. No human in the loop. Listener data used to improve the loop. |
| **run_token_loop.ps1** | token_loop_start at start; agent does steps and token_loop_end at end |
| **Agent (Token Reduction Loop)** | step_start / step_end at each step when following TOKEN_REDUCTION_LOOP.md |
| **Phase 0.6 (Improvement Loop)** | Optional: token_loop_start + step 1–2 + token_loop_end |

---

## JSONL Format

Each line is a JSON object:

```json
{"ts":"2025-03-11T19:00:00Z","event":"step_end","run_id":"token-20250311-1900","step":"1","metrics":{"always_apply_count":1,"always_apply_lines":57}}
```

---

## Metrics to Record (optional on step_end / token_loop_end)

| Metric | Meaning |
|--------|---------|
| `always_apply_count` | Number of rules with alwaysApply: true |
| `always_apply_lines` | Total lines of always-apply rules (proxy for token cost) |
| `steps_completed` | Steps finished this run |
| `conversions_done` | Rules converted from always-apply to glob/description (if any) |

---

## Verification

Run the listener test or all token loop tests to ensure everything is wired:

```powershell
.\scripts\automation\test_token_loop_listener.ps1
# Or run all token loop tests (listener + snapshot + events analysis):
.\scripts\automation\run_token_loop_tests.ps1
```

Exit 0 = all tests passed.

---

## Analysis

We use `token_loop_events.jsonl` to improve the loop: run count, steps completed, always-apply trends. See [TOKEN_REDUCTION_LOOP.md](./TOKEN_REDUCTION_LOOP.md) §8 (Research & analysis).

---

*Integrates with TOKEN_REDUCTION_LOOP. Listener starts with the loop when you say "start token loop." Use token_loop_events.jsonl to improve the loop (save token spend, manage context squish).*
