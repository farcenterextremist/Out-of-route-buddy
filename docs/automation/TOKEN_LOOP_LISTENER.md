# Token Reduction Loop — Listener

**Purpose:** Record token-loop events to structured data (JSONL) for analysis and loop improvement. This listener is the **current event surface for the token-audit lane** inside the broader [LLM_LOOP.md](./LLM_LOOP.md) contract. **The listener starts alongside the loop** when you say "start token loop" — and for now the broader `start llm loop` trigger routes to this same lane. Events are appended to `docs/automation/token_loop_events.jsonl`.

**Script:** `scripts/automation/token_loop_listener.ps1`  
**Tests:** `scripts/automation/test_token_loop_listener.ps1` (listener only); **all token loop tests:** `scripts/automation/run_token_loop_tests.ps1` — compile (parse), wiring (docs + scripts + loop doc keys), listener, snapshot, events analysis.  
**Output:** `docs/automation/token_loop_events.jsonl` (one JSON object per line)

---

## Events

| Event | When | Typical use |
|-------|------|-------------|
| `token_loop_start` | Start of Token Reduction Loop | Agent or run script invokes; also used by current `start llm loop` compatibility wrapper |
| `step_start` | Start of a step (0–7) | Agent invokes with Step and Note |
| `step_end` | End of step (optional) | Agent invokes with metrics (e.g. always_apply_count) |
| `token_loop_end` | End of Token Reduction Loop | Agent or run script invokes |

Step 0 = deep research and analysis; Step 7 = organize results and recommend next tasks (TOKEN_LOOP_NEXT_TASKS.md). Historical event names stay token-specific for compatibility.

---

## Usage

Use the **same RunId** for all events in a single run (e.g. `token-YYYYMMdd-HHmm`). `run_token_loop.ps1` generates RunId and uses it for snapshot and token_loop_start. `run_llm_loop.ps1` currently delegates to the same contract.

**Metrics:** Use **single-quoted** JSON for `-Metrics` in PowerShell so it parses as `metrics` (not `metrics_raw`). Or write JSON to a file and pass `-MetricsPath path\to\file.json` to avoid quoting issues.

**Preferred closeout:** Use `scripts/automation/complete_token_loop_run.ps1` once the summary/ledger is ready. It runs the token shared-state closeout first and only then emits `token_loop_end`, which prevents a false "complete" event when closeout fails.

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

**Run all token loop tests** (compile, wiring, listener, snapshot, events analysis):
```powershell
.\scripts\automation\run_token_loop_tests.ps1
.\scripts\automation\run_token_loop.ps1 -Test   # Same: runs all tests before starting loop
```

---

## Wiring

| Component | Invokes listener |
|-----------|------------------|
| **"Start token loop"** | When user says "start token loop," agent starts listener (`token_loop_start`) and runs steps 0–7 (0=research, 7=organize + recommend next tasks in TOKEN_LOOP_NEXT_TASKS.md), then closes with `complete_token_loop_run.ps1` (preferred) or a manual closeout that ends in `token_loop_end`. No human in the loop. Listener data used to improve the loop. |
| **run_token_loop.ps1** | token_loop_start at start; agent does steps and should prefer `complete_token_loop_run.ps1` for the end-of-run closeout |
| **complete_token_loop_run.ps1** | Preferred end helper: runs `finish_loop_run.ps1` for token shared state, then emits `token_loop_end` only after closeout succeeds |
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
| `changed_rules` | Whether this run changed `.cursor/rules` guidance |
| `changed_skills` | Whether this run changed skill files or skill wiring |
| `changed_settings` | Whether this run changed workspace/editor settings |
| `changed_cache_index_hygiene` | Whether this run changed excludes, `.cursorignore`, or index hygiene |
| `llm_provider` | Provider used for the broader LLM workflow, e.g. `ollama` or `openai` |
| `local_runtime` | Local runtime used for LLM work, e.g. `ollama` |
| `local_model` | Local model checked or used during the run |
| `api_usage_snapshot_written` | Whether a lightweight API usage snapshot was recorded |
| `api_usage_num_model_requests` | Total model requests captured by the API usage snapshot |
| `api_usage_input_tokens` | Total input tokens captured by the API usage snapshot |
| `api_usage_output_tokens` | Total output tokens captured by the API usage snapshot |

---

## Verification

Run the listener test or **all token loop tests** to ensure everything is wired and compiling:

```powershell
.\scripts\automation\test_token_loop_listener.ps1
# Or run full suite (compile parse, wiring, listener, snapshot, events analysis):
.\scripts\automation\run_token_loop_tests.ps1
```

Exit 0 = all tests passed.

---

## Analysis

We use `token_loop_events.jsonl` to improve the loop: run count, steps completed, always-apply trends, and lightweight local-provider notes. See [TOKEN_REDUCTION_LOOP.md](./TOKEN_REDUCTION_LOOP.md) §8 (Research & analysis). For known historical irregularities that should not be "fixed" blindly, see [TOKEN_LOOP_EVENT_ANOMALIES.md](./TOKEN_LOOP_EVENT_ANOMALIES.md).

---

*Integrates with TOKEN_REDUCTION_LOOP. Listener starts with the loop when you say "start token loop," and currently backs the broader `start llm loop` compatibility path too. Use token_loop_events.jsonl to improve the loop (save token spend, manage context squish, and keep local-first LLM workflow lightweight).*
