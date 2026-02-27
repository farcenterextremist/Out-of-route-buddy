# Coordinator email listener — runbook

**No OpenAI API key?** You have two options:

1. **Use this runbook (Option A)** — Cursor acts as the Coordinator in a chat loop; you or the agent composes each reply. No API key, no extra install.
2. **Use a local LLM (Option B with Ollama)** — Install [Ollama](https://ollama.com), run `ollama run llama3.2`, set `COORDINATOR_LISTENER_OLLAMA_URL=http://localhost:11434` in `scripts/coordinator-email/.env`, then run `python scripts/coordinator-email/coordinator_listener.py`. No API key.

For **automatic** Coordinator replies with OpenAI, run **`coordinator_listener.py`** with `COORDINATOR_LISTENER_OPENAI_API_KEY` in `.env`.

---

## Option A: Cursor chat loop (no API key, no Ollama)

Start a dedicated chat (or background task, if Cursor supports it) and act as the Master Branch Coordinator with this loop:

1. **Read inbox:** Run `python scripts/coordinator-email/agent_email.py read` and note the JSON output (especially `message_id` and `body`).
2. **Check state:** If `message_id` is already in `scripts/coordinator-email/last_responded_state.txt`, skip to step 4.
3. **As Coordinator:** Using [coordinator-instructions.md](./coordinator-instructions.md) and the user's email (subject + body), compose a short reply (assign work, delegate, or say Human-in-the-Loop will follow up). Then run `python scripts/coordinator-email/agent_email.py send "Re: <subject>" "<body>"` and append the `message_id` to `last_responded_state.txt` (or overwrite that file with the single message_id).
4. **Wait:** Say "Wait 3 minutes" or set a timer; then go to step 1.

Repeat until you stop the chat or close Cursor. So long as this loop is running in Cursor, you have an open line with the Coordinator responding to new email.

**Limitation:** Cursor may not support a true background loop (e.g. "wait 3 minutes" may require you to send a follow-up message). If so, use **Option B** below.

---

## Option B: Python Coordinator listener (recommended)

Run the script that uses an LLM API to compose Coordinator replies:

```bash
python scripts/coordinator-email/coordinator_listener.py
```

- Requires: `COORDINATOR_LISTENER_OPENAI_API_KEY` (or `OPENAI_API_KEY`) in `scripts/coordinator-email/.env`, and `pip install openai`.
- **Required for read path:** `COORDINATOR_EMAIL_TO` must be set to the user's email. The listener only considers messages **from** that address (to avoid responding to wrong emails).
- **Optional:** `COORDINATOR_LISTENER_MAX_REPLIES_PER_HOUR=6` caps the number of replies per hour. Omit for no cap.
- Runs until you press Ctrl+C or close the terminal. While it runs, every few minutes it checks the inbox and, for each **new** reply from the user, composes and sends a reply as the Master Branch Coordinator.
- See [scripts/coordinator-email/README.md](../../scripts/coordinator-email/README.md) for full setup.

---

## Context expansion (intent-aware loading)

When the listener composes an LLM reply, it uses **intent-aware context**: the base `coordinator-project-context.md` plus on-demand snippets based on what the user asked. For example:

- **"What's next?"** → adds ROADMAP snippet
- **"How does trip recovery work?"** → adds KNOWN_TRUTHS and TRIP_PERSISTENCE_END_CLEAR
- **"What's the latest app version?"** → adds version from `app/build.gradle.kts`
- **"Who owns the emulator?"** → adds DATA_SETS_AND_DELEGATION_PLAN

See `scripts/coordinator-email/context_loader.py` for the full intent→snippet mapping. Total context is capped at 4500 chars. If a snippet fails to load (file missing, git error), it is omitted; the listener never crashes.

---

## Summary

| Method | API key? | Who composes reply | When it runs |
|--------|----------|--------------------|--------------|
| **Runbook loop (Option A)** | No | You (Cursor agent) in chat | While you keep the chat loop going |
| **coordinator_listener.py (Option B)** | OpenAI: yes. Ollama: no | Script via LLM | While the script runs in a terminal |

So long as Cursor is online and either the runbook loop or the script is running, the open line is active and the Coordinator responds to your emails.
