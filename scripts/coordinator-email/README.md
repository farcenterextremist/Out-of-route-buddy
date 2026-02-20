# Coordinator Email – Human-in-the-Loop

This folder contains the script used by the **Human-in-the-Loop Manager** to send you (the user) emails with suggestions, questions, and updates from the team.

**Mailing list / consultation:** The recipient you set in `.env` (`COORDINATOR_EMAIL_TO`) is the project owner on the consultation mailing list. The coordinator and Human-in-the-Loop Manager will use this to recommend changes, consult with you on decisions, and send updates. Add your email below to join.

## Setup

1. **Copy the example env file**
   ```bash
   cp .env.example .env
   ```

2. **Edit `.env`** with your settings:
   - `COORDINATOR_EMAIL_TO` – Your email address (recipient).
   - `COORDINATOR_EMAIL_FROM` – Sender address (often same as SMTP user).
   - `COORDINATOR_SMTP_HOST` – e.g. `smtp.gmail.com`, `smtp.office365.com`.
   - `COORDINATOR_SMTP_PORT` – Usually `587` (TLS) or `465` (SSL).
   - `COORDINATOR_SMTP_USER` – Your SMTP username / email.
   - `COORDINATOR_SMTP_PASSWORD` – App password or account password (prefer app-specific password).

3. **Do not commit `.env`** – It is listed in `.gitignore` for `scripts/coordinator-email/.env`.

If `python` is not found when you run the script, see **`docs/Make_Python_Available.md`** (add Python to PATH or run `scripts\ensure_python_on_path.ps1` once).

## Usage

The Human-in-the-Loop Manager (or you, when testing) runs:

```bash
# From repo root
python scripts/coordinator-email/send_email.py "Subject line" "Body text in plain text."

# Or from this folder
python send_email.py "Subject line" "Body text."
```

- **First argument:** Subject line.
- **Second argument:** Body (plain text). Use quotes so the whole message is one argument.

For multi-line bodies, use a body file: `python send_email.py "Subject" @path/to/body.txt`

**After Phase A/B/C execution (automated):** Run as the last step so the user gets the summary without manual action: `python scripts/coordinator-email/send_phase_completion_email.py phase_abc` or `scripts\coordinator-email\send_phase_completion_email.bat phase_abc`. Presets are in `send_phase_completion_email.py`; add more for other milestones.

---

## Agent entrypoint (read + reply)

For the agent (Coordinator, Human-in-the-Loop Manager), a single script provides read and send:

```bash
# Read latest reply → JSON on stdout: {"found": true, "subject": "...", "body": "...", "date": "..."}
python scripts/coordinator-email/agent_email.py read

# Send email (body can be @path/to/body.txt)
python scripts/coordinator-email/agent_email.py send "Subject" "Body"
```

The agent can use the JSON from `agent_email.py read` in context without opening a file. See **`docs/agents/OPEN_LINE_OF_COMMUNICATION.md`**.

**Alternative:** `read_replies.py --json` also prints JSON; `read_replies.py` without flags writes `last_reply.txt` as before.

---

## Reading replies (IMAP)

When the user replies to a coordinator email, you can fetch the latest reply with:

```bash
python read_replies.py
```

- **Requires:** IMAP enabled on the account (Gmail: Settings → Forwarding and POP/IMAP → Enable IMAP).
- **Credentials:** Uses `COORDINATOR_IMAP_USER` / `COORDINATOR_IMAP_PASSWORD` if set in `.env`; otherwise falls back to `COORDINATOR_SMTP_USER` / `COORDINATOR_SMTP_PASSWORD` (same Gmail app password usually works).
- **Output:** Writes the latest matching reply to `last_reply.txt` in this folder (subject + body). Looks for messages with "Re:" and "OutOfRouteBuddy" in the subject.
- **Run on demand:** When the user says "I replied," run this script, then read `last_reply.txt` to respond or update team parameters.

## Requirements

- Python 3.6+
- No extra packages required (uses standard library `smtplib` and `email`).

## Security

- Keep `.env` out of version control and off shared machines.
- Use an app-specific password for Gmail/Outlook rather than your main account password.
- Restrict who can run the script if it runs in a shared or automated environment.
