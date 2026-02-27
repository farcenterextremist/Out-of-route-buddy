# Jarvey No-Response Diagnostic

## I didn’t receive any emails (opener or replies)

Jarvey sends all mail **to the address in `COORDINATOR_EMAIL_TO`**. With your current `.env` that is **mybrandonhelperbot@gmail.com** (the bot’s own inbox).

| If you’re checking… | What to do |
|--------------------|------------|
| **mybrandonhelperbot@gmail.com** | Open that inbox and check **Spam**, **Promotions**, and **Social**. Gmail sometimes filters “send to self” or automated mail there. |
| **A different inbox** (e.g. personal Gmail) | Set `COORDINATOR_EMAIL_TO=yourpersonal@gmail.com` in `scripts/coordinator-email/.env`. Restart the listener. The opener and all replies will then go to that address. (Replies from that address will be accepted if it’s in the allowed list.) |

**Quick test:** From `scripts/coordinator-email` run:
```bash
python send_email.py "Jarvey test" "If you see this, SMTP delivery works."
```
The message goes to whatever `COORDINATOR_EMAIL_TO` is in `.env`. Check that inbox (and Spam) for it.

---

## Error reading inbox (IMAP / credentials)

If the terminal shows **"Error reading inbox: ..."** every poll cycle, the failure is almost always **IMAP login**, not a code bug. Common causes:

| Cause | Fix |
|-------|-----|
| **Invalid credentials** | Set `COORDINATOR_IMAP_USER` and `COORDINATOR_IMAP_PASSWORD` (or `COORDINATOR_SMTP_USER` / `COORDINATOR_SMTP_PASSWORD`) in `scripts/coordinator-email/.env` to the **bot account** (Jarvey’s inbox). Use an **app password** for Gmail; do not use your normal password if 2FA is on. |
| **IMAP disabled** | Gmail: Settings → See all settings → Forwarding and POP/IMAP → Enable IMAP. Save. |
| **Placeholder .env** | If `.env` still has `your-email@example.com` / `your-app-password`, replace with real values. The listener and `read_replies` both load from `scripts/coordinator-email/.env`. |
| **Wrong account** | IMAP user/password must be for the **same** address as `COORDINATOR_EMAIL_FROM` (Jarvey’s inbox). |

After fixing `.env`, restart the listener. No code change is required. If the error persists, run `python scripts/coordinator-email/diagnose_from_to.py` to confirm FROM/TO and that the same `.env` is used.

---

## What We Found

**Jarvey is running** and **is reading your emails**. The terminal shows:
- "Tell me the coolest feature in out project" (Re: Outofroutebuddy)
- "Hello Jarvey! Tell me about the most complex tool in our app right now" (Outofroutebuddy)

**But no "SENT" or "Coordinator reply sent"** appears after those messages. So something stops the flow before sending.

---

## Likely Causes (in order)

### 1. Dedupe — already responded

Jarvey stores the last message it responded to in `last_responded_state.txt`. If it thinks it already replied to this message, it skips without printing.

**Check:** `scripts/coordinator-email/last_responded_state.txt` contains a Message-ID. If your latest email has that same ID, Jarvey will skip.

**Fix:** Send a **new** email (new subject or body) so it gets a new Message-ID. Or temporarily clear `last_responded_state.txt` to force a fresh response (you may get a duplicate reply).

### 2. Ollama slow or not running

"Coolest feature" and "most complex tool" do not match templates, so they use the LLM path. If `COORDINATOR_LISTENER_OLLAMA_URL` is set and OpenAI is not, Jarvey uses Ollama.

- **Ollama not running:** You get "Ollama request failed" (would appear in terminal).
- **Ollama very slow:** Can take 5–10+ minutes. Jarvey may still be waiting.

**Check:** Is Ollama running? `ollama list` in a terminal. If using Ollama, pre-warm with `ollama run llama3.2`.

**Fix:** Use OpenAI for faster replies: set `COORDINATOR_LISTENER_OPENAI_API_KEY` in `.env`.

### 3. Cooldown (2 min)

Jarvey will not send more than once per 2 minutes. If it sent recently, it skips.

**Check:** `last_sent_timestamp.txt` — if the timestamp is within the last 2 minutes, cooldown is active.

### 4. Circuit breaker

After 5+ LLM failures, Jarvey stops calling the LLM for 1 hour.

**Check:** `llm_backoff_state.json` — if `failures >= 5`, the circuit is open.

---

## Quick Diagnostics

### Run with full logging

```bash
cd scripts/coordinator-email
python coordinator_listener.py --log
```

This logs each step (dedupe, cooldown, template, LLM, sent) to `jarvey_workflow.log` and stderr.

### Run a one-shot trace (dry run)

```bash
python trace_jarvey_workflow.py --dry-run
```

This runs one full cycle (read → choose → compose) without sending. Check the output to see:
- Which message was found
- Which template (or default) was chosen
- Whether compose succeeded or failed

### Force a fresh response (use with care)

1. Note the Message-ID of the email you want a reply to (from your email client).
2. Clear state: delete or empty `last_responded_state.txt`.
3. Wait for the next poll (up to 3 min) or restart the listener.

---

## Same-inbox mode

You're in same-inbox mode (FROM and TO are the same). That is supported. Jarvey:
- Skips its own sent messages (via `X-OutOfRouteBuddy-Sent`)
- Only processes messages FROM your address
- Sends replies to your address

---

## Reply with a number (1–10) not understood

If the user replied with just a number (e.g. **6**) to the capability menu and Jarvey responded with "couldn't make out what you need", the cause was:

1. **Short-body clarification:** Messages under 15 characters are matched by the "unclear" template unless whitelisted. The digits **1–10** were not whitelisted, so "6" was treated as unclear and the clarification template was used without calling the LLM.
2. **LLM not instructed:** The system prompt did not say that a single number 1–10 means "user chose that capability menu option."

**Fix applied:** (1) `templates/unclear.json` — added "1" through "10" to `short_body_whitelist` so number-only replies go to the LLM. (2) `coordinator_listener.py` — added an instruction: when the user's message is only a number 1–10, treat it as the capability menu choice (1=Roadmap…, 6=App version/build info, etc.) and answer accordingly; do not reply with "couldn't make out". After restarting the listener, replying with **6** should produce app version/build info.

---

## Emails never arrive — checklist

When replies never show up:

1. **Pre-warm:** Set `JARVEY_PREWARM_OLLAMA=1` so the model loads at startup (avoids 2–5 min cold start on first message).
2. **Fallback:** Set `JARVEY_OLLAMA_FALLBACK_TO_OPENAI=1` and `COORDINATOR_LISTENER_OPENAI_API_KEY` so timeouts retry with OpenAI.
3. **Circuit state:** Check `llm_backoff_state.json` — if `failures >= 5`, circuit is open. Clear the file or run `diagnose_jarvey.py --reset-circuit` to reset.
4. **Timeout:** Set `COORDINATOR_LISTENER_OLLAMA_TIMEOUT=600` (10 min) for slow systems.

---

## Recommended next steps

1. **Run with `--log`** to see whether dedupe or cooldown is triggering.
2. **Confirm LLM config** — OpenAI key or Ollama URL in `.env`.
3. **Send a new, distinct email** (e.g. "What's next?") to test the template path (instant).
4. If using Ollama, **pre-warm** with `ollama run llama3.2` or set `JARVEY_PREWARM_OLLAMA=1` before starting the listener.

---

## See also

- [JARVEY_HEALTH_CHECK_DOCUMENT.md](JARVEY_HEALTH_CHECK_DOCUMENT.md) — All health checks, diagnostics, state files, and example outputs for training.
