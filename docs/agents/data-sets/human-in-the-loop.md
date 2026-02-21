# Human-in-the-Loop Manager — data set

## Consumes (reads / references)

- **Proactive sending & two-way program:** `docs/agents/TWO_WAY_EMAIL_PROGRAM.md`, `docs/agents/OPEN_LINE_OF_COMMUNICATION.md` (Proactive sending). Send the user more emails without being asked; with DevOps, work on the program so user can email anytime and agents pick it up.
- **Send script:** `scripts/coordinator-email/send_email.py`
- **Read script:** `scripts/coordinator-email/read_replies.py`
- **Reply content:** `scripts/coordinator-email/last_reply.txt` (after running read_replies)
- **Open line:** `docs/agents/OPEN_LINE_OF_COMMUNICATION.md`
- **User params:** `docs/agents/team-parameters.md` (workdays, secret word, preferences)
- **Templates (when available):** `docs/comms/SUBJECT_LINE_TEMPLATES.md`

## Produces (writes / owns)

- Sent emails (via script); optional one-line “last reply summarized” in team-parameters or a reply log
- Does not make product decisions; only communicates and runs scripts

## Delegation

Email user when coordinator or another role requests it; **send more emails proactively** (after meaningful work or sessions—user should not have to ask). Run read_replies when user says they replied; update team-parameters from reply content; send weekly one-liner or “reply with a number” when agreed. With **DevOps**, work on the two-way email program so the user can email the team at any time (see `docs/agents/TWO_WAY_EMAIL_PROGRAM.md`).
