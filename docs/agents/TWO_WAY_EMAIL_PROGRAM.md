# Two-way email program (user ↔ agents)

**Owner:** DevOps Engineer + Human-in-the-Loop Manager  
**User ask:** "Work on a program that will let me send emails back and forth with them at any point."

---

## Goal

- **User can email the team at any time** and get a response (agents read the reply and act on it).
- **Agents send the user more emails proactively** so the user doesn’t have to always ask for them.
- End state: true two-way communication—either side can initiate and the other can read and respond.

---

## Current state

- **Sending (agents → user):** Working. Scripts: `send_email.py`, `agent_email.py send`, `send_phase_completion_email.py`. Human-in-the-Loop and Coordinator use these.
- **Reading (user → agents):** Working when agents are running. Scripts: `agent_email.py read`, `read_replies.py`; output in `last_reply.txt` or JSON. User must say “I replied” or “check email” for the agent to run read.
- **Gap (partially addressed):** User has to **ask** for emails unless automatic responses are enabled. **Automatic responses:** `scripts/coordinator-email/check_and_respond.py` reads the inbox and sends a short auto-reply to new replies (template-based). Schedule it (e.g. every 15–30 min) so the user gets a reply without starting a session. See that folder's README. Otherwise user has to **ask** for emails (agents don’t send enough proactively). User can’t yet “just email and have the team pick it up” without starting a session and asking to check email.

---

## For now: send more emails (no user ask required)

**Mandate:** Human-in-the-Loop (and Coordinator) must **send the user more emails proactively**. Do not wait for the user to ask.

- After **meaningful work** (not just tiny edits): send a short summary and optional ask.
- After **sessions** where something was decided or built: send a 2–3 sentence update.
- When there’s a **question or recommendation**: send it; don’t hold back until the user asks.
- **Big changes:** Already required per `docs/agents/EMAIL_AT_END_OF_BIG_CHANGES.md` — send without asking. Expand the same idea: when in doubt, send a short update.

Coordinator and Human-in-the-Loop instructions have been updated so proactive sending is the default. No new program needed for this part—just behavior.

---

## Future program (two-way at any time)

**DevOps + Human-in-the-Loop** should design and implement (or document) a **program** so that:

1. **User can send an email anytime** (e.g. to the same inbox the agents use).
2. **Agents pick it up** even if the user didn’t start a Cursor session—e.g.:
   - A **scheduled or on-demand read** (cron, scheduled job, or “when user runs this script” that runs `agent_email.py read` and writes result somewhere the next session can see), or
   - A **small script or flow** the user runs (e.g. “check my reply” or “sync with team”) that reads inbox and saves the latest reply for the next agent run, or
   - Another lightweight mechanism so “user sent email” is visible to the next agent session.
3. **Clear instructions for the user:** how to reply, what subject/format to use, and how to trigger a read if they want a response in the next session.

**Deliverables (to be scoped by DevOps + Human-in-the-Loop):**

- Short design or runbook: how user initiates, how agents read, and how often (e.g. “run read before each session” or “user runs sync script”).
- Any new script or automation (e.g. wrapper that runs read and writes to `last_reply.txt` with a timestamp; or a one-liner the user can run to “send my reply to the team”).
- One-paragraph user-facing instructions (e.g. in README or `docs/agents/OPEN_LINE_OF_COMMUNICATION.md`): “To email the team, reply to [address]. To have the team see your reply, [run X / start a session and say ‘check email’].”

---

## Where this is written

- **Proactive sending:** `docs/agents/OPEN_LINE_OF_COMMUNICATION.md`, `docs/agents/roles/human-in-the-loop-manager.md`, `docs/agents/coordinator-instructions.md`, `docs/agents/EMAIL_AT_END_OF_BIG_CHANGES.md`.
- **Two-way program (task):** This doc; referenced in `docs/agents/data-sets/devops.md` and `docs/agents/data-sets/human-in-the-loop.md`, and in the role cards for DevOps and Human-in-the-Loop.

---

*DevOps and Human-in-the-Loop: treat proactive sending as effective immediately; design and implement the “user can email anytime and agents pick it up” flow as the next step.*
