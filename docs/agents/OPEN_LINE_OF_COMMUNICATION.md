# Open Line of Communication with the User

You have an **open line of communication** with the user (**Brandonfrey2work@gmail.com**). You are explicitly authorized to **read and write email** when you need to ask questions, consult, or get decisions. Sending and return-email (IMAP) are configured in `scripts/coordinator-email/.env`.

---

## Two-way email: how you reply and how we read (user-facing)

**How you reply:** Reply to any coordinator email from your normal inbox. Keep "Re:" and "OutOfRouteBuddy" in the subject so our script can match it.

**How we read your reply:** When you say "I replied" or when we follow up, we run `python scripts/coordinator-email/agent_email.py read` (or `read_replies.py`) and get your latest reply as JSON or from `scripts/coordinator-email/last_reply.txt`. We then update plans, team parameters, or send a follow-up. No scheduled polling—we read on demand when you've replied.

**One-line for you:** Reply to the same thread; we'll read it when you tell us you replied (or when we check).

**Automatic replies (optional):** You can run `scripts/coordinator-email/check_and_respond.py` on a schedule (e.g. every 15–30 min via Task Scheduler or cron). It reads your latest reply and sends a short auto-response (e.g. "We'll set up weekly digest," "Thanks—we'll follow up," or "We got your message"). See `scripts/coordinator-email/README.md` for setup.

**Coordinator listener (optional):** For an open line **while Cursor is online**, run `python scripts/coordinator-email/coordinator_listener.py` in a Cursor terminal. While it runs, it checks your inbox every few minutes and, when you send a new reply, responds **as the Master Branch Coordinator** (assigns work, delegates, or escalates to Human-in-the-Loop). Requires an OpenAI API key in `.env`. When you close Cursor or the terminal, the listener stops. See `scripts/coordinator-email/README.md`.

---

## Your agency

- **Send email whenever you need to:** Ask a question, request a decision, suggest a change, or share an update. Use the Human-in-the-Loop Manager flow: draft the message, then run the send script. No need to wait for the user to prompt you—if you need their input, send the email.
- **Read email when you need to:** When the user says they replied, or when you’re following up on a question you sent, run `python scripts/coordinator-email/agent_email.py read` to get the latest reply as JSON (subject, body, date), or run `read_replies.py` and read `last_reply.txt`. Use that to continue work, update team parameters, or send a follow-up.

---

## How to send

1. Draft subject and body (plain text; use `@path/to/body.txt` for long messages).
2. Run: `python scripts/coordinator-email/send_email.py "Subject" "Body"` or `python scripts/coordinator-email/send_email.py "Subject" @body.txt`
   - **Or use the agent entrypoint:** `python scripts/coordinator-email/agent_email.py send "Subject" "Body"` (or `send "Subject" @body.txt`).
3. From repo root or from `scripts/coordinator-email/`. Credentials and recipient are in `scripts/coordinator-email/.env`.

---

## How to read (return email)

**Option A – structured output for the agent (recommended)**  
1. Run: `python scripts/coordinator-email/agent_email.py read` (from repo root or from that folder).  
2. Stdout is one JSON object: `{"found": true, "subject": "...", "body": "...", "date": "..."}` or `{"found": false, ...}`. Use the `subject` and `body` in context to respond, update `docs/agents/team-parameters.md`, or send a follow-up.  
3. The same run also writes `last_reply.txt` so existing flows that read that file still work.

**Option B – file-based**  
1. Run: `python scripts/coordinator-email/read_replies.py` (or `read_replies.py --json` for JSON on stdout).  
2. Open `scripts/coordinator-email/last_reply.txt` for the latest reply (subject + body). The script looks for messages with "Re:" and "OutOfRouteBuddy" in the subject.  
3. Use the content to answer your question, update `docs/agents/team-parameters.md`, or confirm (e.g. secret word "pickle").  

**Gmail:** IMAP must be enabled (Settings → Forwarding and POP/IMAP → Enable IMAP). The same Gmail App Password in `.env` is used for both sending and reading.

---

## When to use it

- You need a product or priority decision from the user.
- You’re blocked and only the user can unblock (e.g. business rule, preference).
- You want to recommend a change and get their okay.
- You’ve finished a chunk of work and want to report back and ask what’s next.
- The user said “I replied” or “check your email”—run read_replies and use the reply.

## Proactive sending (do not wait for the user to ask)

**Send the user more emails.** The user should not have to always ask for updates. Whenever you have something to report, ask, or recommend:

- **Send an email** with a short summary and, if relevant, one clear ask.
- After **meaningful work** (a feature, refactor, or batch of changes): send a 2–3 sentence update.
- After **sessions** where something was decided or built: send a brief “here’s what we did / what’s next.”
- When you have a **question or suggestion**: send it; don’t wait for the user to say “email me.”

Use the same send script and flow. The rule: **when in doubt, send.** See also `docs/agents/EMAIL_AT_END_OF_BIG_CHANGES.md` (always email at end of big changes) and `docs/agents/TWO_WAY_EMAIL_PROGRAM.md`.

---

## Summary

You have **agency** to use this channel: send when you need to consult or ask, and read when you need their response. No extra permission step—treat it as an open line.
