# Future checklist: Receive and handle your email replies

This checklist describes what’s needed so you can **reply to coordinator emails** and the system can **read and use** those replies (e.g. workdays, confirmations, decisions).

---

## Current state

- **Outgoing only:** We send email to you via `scripts/coordinator-email/send_email.py` (SMTP).
- **No inbox access:** We do not read your mailbox, so we cannot see your replies.

---

## Future checklist (to enable replies)

### 1. Inbox access

- [ ] **Choose which mailbox to read**
  - Option A: Read the same Gmail inbox we send from (`brandonfrey2work@gmail.com`).
  - Option B: Use a dedicated “coordinator” address that forwards to a mailbox we can poll (keeps personal inbox separate).
- [ ] **Enable IMAP** on that Gmail (or provider) account (Gmail: Settings → Forwarding and POP/IMAP → Enable IMAP).
- [ ] **Add IMAP credentials** to `.env` (e.g. same app password if Gmail; or separate IMAP password). Do not commit `.env`.

### 2. Reply detection and threading

- [ ] **Define how we identify “replies”**
  - e.g. Emails in inbox where subject starts with `Re: OutOfRouteBuddy` or contains a known thread-id.
- [ ] **Optionally use a stable “Reply-To” or subject tag** on outgoing emails so we can filter (e.g. `[OutOfRouteBuddy]` in subject).

### 3. Script or service to read replies

- [x] **Add a script** `scripts/coordinator-email/read_replies.py` that:
  - Connects via IMAP to the chosen mailbox (Gmail: same app password as SMTP).
  - Logs in with credentials from `.env` (IMAP_* or SMTP_* fallback).
  - Finds recent emails that match our “reply” criteria (Re: + OutOfRouteBuddy in subject).
  - Reads subject and body (plain text or first text part).
- [x] **Output format:** Writes to `scripts/coordinator-email/last_reply.txt` so the coordinator/Human-in-the-Loop Manager can use the content.
- [x] **Mark as read** so we don’t reprocess the same reply.

### 4. Security and safety

- [ ] **Document** that `.env` holds IMAP credentials and must stay local and gitignored.
- [ ] **Limit scope:** Script only reads mail (no delete/send from this script) unless we explicitly add and document that later.
- [ ] **Rate limit:** Don’t poll inbox too often (e.g. run on demand or at most every N minutes if we automate later).

### 5. How the coordinator uses replies

- [ ] **Define where reply content goes:** e.g. “Human-in-the-Loop Manager (or coordinator) reads `last_reply.txt` when user says they replied,” or a simple dashboard/file the user can open.
- [ ] **Optional:** Parse reply for structured data (e.g. workdays, time zone) and write to `docs/agents/team-parameters.md` or similar so the coordinator “monitors parameters” from your replies.
- [ ] **Optional:** Send an automatic “We received your reply and noted: …” confirmation email after processing.

### 6. Documentation and rollout

- [ ] **Update** `scripts/coordinator-email/README.md` with:
  - How to enable IMAP.
  - How to run `read_replies.py` (or equivalent) and where output goes.
- [ ] **Update** Human-in-the-Loop Manager / coordinator instructions to say: “When the user says they replied, run the read_replies script and use the output.”
- [ ] **Test:** Send a test email, you reply, run the script, confirm the reply content is captured correctly.

---

## After this is done

- You reply to any coordinator email → we run the read-replies script (or a scheduled job) → coordinator/Human-in-the-Loop Manager sees your reply and can confirm, update workdays/parameters, and continue the conversation.

---

## Notes

- This checklist is **future work**; nothing here is implemented yet.
- When you’re ready to implement, we can do it step by step (e.g. IMAP script first, then wiring into coordinator docs).
- You mentioned you’ll show the email you meant to send; that can be used to tune subject/body parsing and what we store in team parameters.
