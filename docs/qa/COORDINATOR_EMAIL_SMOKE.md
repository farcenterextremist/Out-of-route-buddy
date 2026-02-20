# Coordinator email — smoke test

**Owner:** QA Engineer  
**Purpose:** Ensure send/read flow for the open line doesn’t break.  
**Related:** 25-point #18, `docs/agents/OPEN_LINE_OF_COMMUNICATION.md`, `scripts/coordinator-email/`.

---

## What to verify

1. **Send (dry-run or real):**
   - Run send script (e.g. `send_phase_completion_email.ps1` or `send_email.ps1`) with a test body or preset.
   - Either use a test recipient or dry-run if the script supports it.
   - Expect: no script errors; if real send, message appears in inbox.

2. **Read replies (when Python/IMAP available):**
   - Run `read_replies.py` from `scripts/coordinator-email/` (requires Python and .env with IMAP credentials).
   - Expect: script runs; `last_reply.txt` is updated or “no new replies” behavior is clear.
   - If Python is not available in CI, document “manual smoke: run read_replies.py locally after sending a test email.”

3. **Secrets not committed:**
   - `.env` and `last_reply.txt` must not be committed (see `docs/security/SECURITY_NOTES.md`).
   - Quick check: `git status` and `.gitignore` list both under coordinator-email.

---

## Suggested frequency

- After any change to `scripts/coordinator-email/` (send or read).
- Before “big change” emails that ask the user for a decision (so we know the open line works).

---

*Add a CI step or manual checklist item: “Coordinator email smoke: send test + read (or document skip if no Python/IMAP).”*
