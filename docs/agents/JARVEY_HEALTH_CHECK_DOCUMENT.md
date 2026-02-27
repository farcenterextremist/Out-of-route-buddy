# Jarvey Health Check Document

**Purpose:** Comprehensive reference for all Jarvey health checks, diagnostics, and troubleshooting. Use for operations, CI, and future training.

---

## 1. Health Check Script

**Script:** `scripts/coordinator-email/health_check.py`

**Usage:**
```bash
cd scripts/coordinator-email
python health_check.py           # Run all checks, print JSON
python health_check.py --quiet   # Exit 0/1 only, no output
python health_check.py --listener  # Require at least one LLM backend for listener mode
```

### Checks Performed

| Check | What it tests | Pass condition | Fail condition |
|-------|---------------|----------------|----------------|
| **config** | `.env` and config_schema validation | Config valid | Missing SMTP/IMAP/email vars |
| **imap** | IMAP connect + auth to inbox | IMAP OK | Connection/auth failed |
| **smtp** | SMTP connect + auth | SMTP OK | Connection/auth failed |
| **ollama** | Ollama API reachability (GET /api/tags) | Ollama reachable | Unreachable or not configured |
| **openai** | OpenAI API key present | Key configured | Not configured |
| **anthropic** | Anthropic API key present | Key configured | Not configured |

### Example Output (Healthy)

```json
{
  "healthy": true,
  "checks": {
    "config": { "ok": true, "message": "Config valid" },
    "imap": { "ok": true, "message": "IMAP OK" },
    "smtp": { "ok": true, "message": "SMTP OK" },
    "ollama": { "ok": true, "message": "Ollama reachable" },
    "openai": { "ok": false, "message": "Not configured" },
    "anthropic": { "ok": false, "message": "Not configured" }
  }
}
```

### Example Output (Unhealthy)

```json
{
  "healthy": false,
  "checks": {
    "config": { "ok": true, "message": "Config valid" },
    "imap": { "ok": false, "message": "IMAP failed: [Errno 111] Connection refused" },
    "smtp": { "ok": true, "message": "SMTP OK" },
    "ollama": { "ok": false, "message": "Ollama unreachable: Connection refused" },
    "openai": { "ok": false, "message": "Not configured" },
    "anthropic": { "ok": false, "message": "Not configured" }
  }
}
```

### Exit Codes

- **0** — All checks pass (or `--listener` and at least one LLM configured)
- **1** — One or more checks failed

---

## 2. Diagnose Jarvey Script

**Script:** `scripts/coordinator-email/diagnose_jarvey.py`

**Usage:**
```bash
python diagnose_jarvey.py              # Run all diagnostics
python diagnose_jarvey.py --fix-dedupe  # Clear last_responded_state.txt to unblock
python diagnose_jarvey.py --reset-circuit  # Reset LLM circuit breaker
```

### Steps Performed

| Step | What it checks | Output |
|------|----------------|--------|
| **Step 1** | Inbox read | Found message (subject, body snippet, message_id) or "No message found" |
| **Step 2** | Dedupe state | last_responded_state.txt contents or "not present" |
| **Step 3** | Cooldown | Last send time, in/out of cooldown |
| **Step 4** | Dedupe check | Would we skip this message? (message_id vs last_id) |

### Example Output (Pipeline Blocked by Dedupe)

```
============================================================
Jarvey Diagnosis
============================================================

--- Step 1: Inbox read (read_replies.py) ---
Result: Found message
  subject: 'Re: Hello'...
  body snippet: 'Tell me about recent changes...'...
  message_id: <CACx0j=...@mail.gmail.com>

--- Step 2: Dedupe state (last_responded_state.txt) ---
State file contains: <CACx0j=...@mail.gm...

--- Step 3: Cooldown (last_sent_timestamp.txt) ---
Last send: 305s ago
  -> Not in cooldown

--- Step 4: Dedupe check ---
WOULD SKIP: message_id matches last_responded_state

--- Summary ---
Dedupe would block: we think we already responded to this message.
  -> To force a reply: python diagnose_jarvey.py --fix-dedupe
  -> WARNING: May cause duplicate reply if we did send.
============================================================
```

### Example Output (Pipeline Should Proceed)

```
--- Summary ---
Pipeline should proceed. Run: python trace_jarvey_workflow.py
  Or ensure coordinator_listener.py is running.
```

---

## 3. Diagnose Inbox Script

**Script:** `scripts/coordinator-email/diagnose_inbox.py`

**Usage:**
```bash
python diagnose_inbox.py
```

**Purpose:** Shows why `read_replies` might not find messages. Fetches last 20 inbox messages and applies the same filters (subject, from-user, agent-sent, our_from).

### Output Fields Per Message

| Field | Meaning |
|-------|---------|
| Subject | Email subject |
| From | Raw From header |
| Sender (normalized) | Parsed email address |
| Agent-sent | Has X-OutOfRouteBuddy-Sent header |
| Has OutOfRouteBuddy/OutOfRoute | Subject contains required string |
| Skip (agent) | Would skip (we sent it) |
| Skip (our_from) | Would skip (from our bot, when we have dedicated address) |
| From user | Sender matches COORDINATOR_EMAIL_TO |
| WOULD ACCEPT | Passes all filters |

---

## 4. State Files

| File | Purpose | Location |
|------|---------|----------|
| **last_responded_state.txt** | Message-ID (or hash) of last message we replied to | scripts/coordinator-email/ |
| **last_sent_timestamp.txt** | Unix timestamp of last send (cooldown) | scripts/coordinator-email/ |
| **llm_backoff_state.json** | Circuit breaker: failures count, last_failure_ts, last_success_ts | scripts/coordinator-email/ |
| **replies_sent_timestamps.txt** | Per-hour rate limit tracking | scripts/coordinator-email/ |
| **jarvey_workflow.log** | Workflow log when JARVEY_LOG=1 or --log | scripts/coordinator-email/ |

### llm_backoff_state.json

```json
{
  "failures": 1,
  "last_failure_ts": 1772114509.13,
  "last_success_ts": 1772113404.35
}
```

- **failures >= 5** → Circuit open; no LLM calls for 1 hour
- **Reset:** Delete file or run `diagnose_jarvey.py --reset-circuit`

---

## 5. Trace Workflow (Dry Run)

**Script:** `scripts/coordinator-email/trace_jarvey_workflow.py`

**Usage:**
```bash
python trace_jarvey_workflow.py --dry-run   # Compose but don't send
python trace_jarvey_workflow.py             # Full cycle including send
```

**Purpose:** Run one poll cycle (read → dedupe → compose → send) with logging. Use `--dry-run` to see what would be sent without actually sending.

---

## 6. Quick Reference — When Jarvey Doesn't Respond

| Symptom | Check | Fix |
|---------|-------|-----|
| No message found | diagnose_inbox.py | Ensure subject has OutOfRouteBuddy/OutOfRoute; message from COORDINATOR_EMAIL_TO |
| Dedupe block | last_responded_state.txt | Send new email, or `diagnose_jarvey.py --fix-dedupe` |
| Cooldown | last_sent_timestamp.txt | Wait 2 min or clear file |
| Circuit open | llm_backoff_state.json | `diagnose_jarvey.py --reset-circuit` |
| Ollama timeout | Terminal: "timed out" | Increase COORDINATOR_LISTENER_OLLAMA_TIMEOUT; or JARVEY_OLLAMA_FALLBACK_TO_OPENAI=1 |
| Ollama not running | health_check.py | Start Ollama; `ollama run llama3.2` |
| Config invalid | health_check.py | Fix .env (SMTP, IMAP, email vars) |

---

## 7. Training Data — Example Scenarios

### Scenario A: Healthy System

- health_check.py → exit 0, all relevant checks ok
- diagnose_jarvey.py → "Pipeline should proceed" or "No message found" (inbox empty)
- Listener runs; replies sent within poll interval

### Scenario B: Dedupe Block

- diagnose_jarvey.py → "WOULD SKIP: message_id matches last_responded_state"
- Fix: --fix-dedupe or send new email

### Scenario C: Ollama Down

- health_check.py → ollama: "Ollama unreachable"
- Listener: "Ollama request failed" in terminal
- Fix: Start Ollama; or set JARVEY_OLLAMA_FALLBACK_TO_OPENAI=1 + OpenAI key

### Scenario D: Circuit Open

- llm_backoff_state.json: failures >= 5
- Listener skips compose, no "LLM path" in log
- Fix: diagnose_jarvey.py --reset-circuit

---

## 8. Related Docs

- [JARVEY_DIAGNOSTIC.md](JARVEY_DIAGNOSTIC.md) — No-response troubleshooting
- [JARVEY_RESPONSE_TIME.md](JARVEY_RESPONSE_TIME.md) — Speed tips
- [scripts/coordinator-email/README.md](../scripts/coordinator-email/README.md) — Setup and usage
