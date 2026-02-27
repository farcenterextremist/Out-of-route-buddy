# Two-way email (user-facing)

**Purpose:** Quick instructions for how you (the user) reply and how Jarvey reads your replies.

---

## Current setup: Jarvey has his own email

Jarvey uses a **dedicated email address** (e.g. `mybrandonhelperbot@gmail.com`). You receive emails from Jarvey at your personal address. When you reply, your reply goes to **Jarvey's inbox**—not yours. Jarvey reads his inbox and only processes messages from you.

---

## How you reply

1. Reply to any email from Jarvey from your normal inbox (Gmail, Outlook, etc.).
2. Your reply is sent **to Jarvey's address**—the same address Jarvey sends from.
3. Subject can be **Re:** or **Fwd:** anything—Jarvey reads all messages from you. Keeping "OutOfRouteBuddy" in the subject helps with threading.

---

## How Jarvey reads your reply

**On demand:** When you say "I replied" or "check email" in a Cursor session, the agent runs `python scripts/coordinator-email/agent_email.py read` (or `read_replies.py`) and reads **Jarvey's inbox** for your latest reply. The agent then uses that to continue work, update plans, or send a follow-up.

**Automatic responses (optional):** You can schedule `check_and_respond.py` to run every 15–30 minutes (Windows Task Scheduler or cron). When you reply, the script reads Jarvey's inbox and Jarvey sends a reply. No need to open Cursor for a basic acknowledgment. See the coordinator-email README for how to set up the schedule.

---

## One-line summary

**Reply to Jarvey's email.** Your reply goes to Jarvey's inbox. We read when you tell us you replied, when we check in a session, or (if you've set it up) automatically on a schedule.

---

## For agents

See `docs/agents/OPEN_LINE_OF_COMMUNICATION.md` for full send/read flow. Scripts: `send_email.py`, `agent_email.py send`, `agent_email.py read`, `read_replies.py`. Output: `last_reply.txt` or JSON on stdout.
