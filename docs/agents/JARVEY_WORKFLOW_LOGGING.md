# Jarvey Workflow Live Documentation

Jarvey's workflow can be documented **live in real time** to a log file. This helps with debugging, auditing, and understanding what Jarvey does across both the coordinator listener and the scheduled check-and-respond script.

---

## Log File

| File | Purpose |
|------|---------|
| **jarvey_workflow.log** | Human-readable workflow log. Appended by both `coordinator_listener.py` and `check_and_respond.py` when logging is enabled. Location: `scripts/coordinator-email/jarvey_workflow.log` |

---

## Enabling Workflow Logging

### Option 1: Environment variable (recommended)

Add to `.env`:

```
JARVEY_LOG=1
```

Both the coordinator listener and check_and_respond will append workflow steps to `jarvey_workflow.log`.

### Option 2: Command-line flags

- **Coordinator listener:** `python coordinator_listener.py --log`
- **Check and respond:** `python check_and_respond.py --log`

---

## What Gets Logged

### Coordinator listener (when running with `JARVEY_LOG=1` or `--log`)

- Found message (subject, body snippet)
- Dedupe skip (already responded)
- Cooldown skip (last send &lt; 2 min ago)
- Circuit open (LLM failures)
- Clarification template used
- LLM path (composing via Ollama/OpenAI/etc.)
- Retries and fallbacks
- SENT (with backend and latency)

### Check and respond (when running with `JARVEY_LOG=1` or `--log`)

- Reading inbox
- No message found
- Found message (subject, body snippet)
- Dedupe skip
- Cooldown skip
- Composing via LLM
- SENT
- Errors (inbox, compose, send)

Entries from check_and_respond are prefixed with `check_and_respond:` so you can distinguish them from listener entries.

---

## Structured JSON Logging (optional)

For machine-readable logs (e.g. for dashboards or analysis), use structured logging:

```
JARVEY_STRUCTURED_LOG=1
```

or

```
JARVEY_LOG=structured
```

This writes JSON lines to the same `jarvey_workflow.log` with fields such as `ts`, `event`, `trace_id`, `latency_ms`, `message_id`, etc. See [jarvey_log.py](../scripts/coordinator-email/jarvey_log.py) for the schema.

---

## Listeners in Place

| Script | Logging | When |
|--------|---------|------|
| **coordinator_listener.py** | `--log` or `JARVEY_LOG=1` | Every poll cycle (read, decide, compose, send) |
| **check_and_respond.py** | `--log` or `JARVEY_LOG=1` | Each scheduled run (cron, Task Scheduler) |
| **trace_jarvey_workflow.py** | Always | One-off trace to jarvey_workflow.log |

---

## Recommended Setup

To document Jarvey's workflow live in real time:

1. Add `JARVEY_LOG=1` to `scripts/coordinator-email/.env`
2. Run `python coordinator_listener.py` (or with `--log` if you prefer)
3. When using scheduled check_and_respond, ensure the task runs with the same `.env` so `JARVEY_LOG=1` is applied

The log file grows over time. Rotate or archive it periodically if needed.
