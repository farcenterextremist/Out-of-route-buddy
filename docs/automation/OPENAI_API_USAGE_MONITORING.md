# OpenAI API Usage Monitoring

**Purpose:** Provide a lightweight, optional API-usage snapshot for loop runs that use OpenAI-backed scripts. Inside the broader [LLM_LOOP.md](./LLM_LOOP.md) contract, this is **provider-specific monitoring**, not the default identity of the loop.

**Script:** `scripts/automation/monitor_openai_api_usage.ps1`

---

## What it captures

- usage category (default: `completions`)
- time window
- total model requests
- input tokens
- output tokens
- cached input tokens
- audio token fields when present
- reasoning tokens when present

The script writes a compact JSON summary to `docs/automation/api_usage_snapshots/` by default.

**Important default:** The current LLM-loop direction is local-first with `Ollama` on the desktop side in Cursor. That means many LLM-loop runs will not need this monitor at all.

---

## Live usage mode

Requires:

- `OPENAI_API_KEY`
- optional `OPENAI_ORG_ID`
- optional `OPENAI_PROJECT_ID`

Example:

```powershell
.\scripts\automation\monitor_openai_api_usage.ps1 -Category completions -WindowHours 24
```

This uses the official OpenAI organization usage endpoint family, for example `GET /organization/usage/completions`.

---

## Fixture/test mode

For lightweight validation without hitting the API:

```powershell
.\scripts\automation\test_monitor_openai_api_usage.ps1
```

Or run the full token suite:

```powershell
.\scripts\automation\run_token_loop_tests.ps1
```

---

## Loop usage

Use this when:

- an LLM-loop run touched OpenAI-backed scripts
- you want a recent usage snapshot for proof-of-quality or cost awareness
- you want a lightweight API check without opening dashboards manually

Do **not** use this as a required step for local `Ollama`-first runs unless the run also touched OpenAI-backed tooling.

Recommended listener metrics when this monitor is used:

- `api_usage_snapshot_written`
- `api_usage_num_model_requests`
- `api_usage_input_tokens`
- `api_usage_output_tokens`

---

## Guardrail

If no API key is available, skip the live snapshot and note that the loop fell back to token proxies, local-runtime notes, or fixture-based validation. Keep the loop lightweight; do not block the whole run on provider monitoring unless API usage is the main subject of the run.
