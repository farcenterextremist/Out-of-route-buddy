# Email the user at the end of big changes

**Rule:** When a **big change** is completed, the team must **email the user** with a short summary. **Send the email without asking the user first** — do not ask "should I send you an email?"; just send it. It is okay to pause the project to send that email; **always email at the end of big changes**.

---

## What counts as a "big change"

- Completion of a **major phase** of work (e.g. emulator editing Phase 2b “Figma-inspired” done, or all of Phase 0–8 emulator plan).
- **Feature shipped** (e.g. statistics monthly-only, Auto drive UI, new Reports screen).
- **Major refactor or milestone** (e.g. Gradle 9 readiness, security review done, ROADMAP + FEATURE_BRIEFs created).
- **User asked for an email** (e.g. “send me a summary when this is done”).

Not required for: single small edits, one-off bug fixes, or internal doc tweaks that don’t change behavior.

**Broader rule:** Send the user more emails in general—after meaningful work or sessions, not only at “big changes.” The user should not have to ask for updates. See `docs/agents/TWO_WAY_EMAIL_PROGRAM.md` and the “Proactive sending” section in `docs/agents/OPEN_LINE_OF_COMMUNICATION.md`.

---

## Who sends the email

- **Human-in-the-Loop Manager** sends the email, using the project script.
- **Coordinator** (or any role) hands off: “Big change done: [X]. Please email the user with [subject + 2–3 sentence summary + optional ask].”
- **Do not ask the user for permission to send.** Send the summary email automatically when the big change is done. For Phase A/B/C, **run the automated script** as the final step: `python scripts/coordinator-email/send_phase_completion_email.py phase_abc` (or the .bat). For other milestones, use `send_email.py` or add a preset to `send_phase_completion_email.py`.

---

## How to send (automated)

**Preferred — use the automated preset script** so big-change emails are sent without manual steps:

- **After Phase A/B/C completion:** Run (from repo root or from `scripts/coordinator-email/`):
  ```bash
  python scripts/coordinator-email/send_phase_completion_email.py phase_abc
  ```
  Or double‑click / run: `scripts\coordinator-email\send_phase_completion_email.bat phase_abc`
- The script uses the preset subject and body file (e.g. `phase_abc_completion_body.txt`). No drafting needed; **run it as the last step** when Phase A/B/C (or the listed presets) are done.

**Other big changes** (no preset yet): Draft subject and body, then run:
  ```bash
  python scripts/coordinator-email/send_email.py "Subject here" "Body text."
  ```
  Or use a body file: `python scripts/coordinator-email/send_email.py "Subject" @path/to/body.txt`

**Config:** Recipient and SMTP are in `scripts/coordinator-email/.env` (see that folder’s README).

---

## What to include in the body

- **What was completed** (1–3 sentences).
- **What the user might want to do next** (e.g. “Run the emulator and try click-to-select,” “Review ROADMAP when you have time”).
- **Optional ask** (e.g. “Reply with any changes you’d like,” “Let us know if you want to prioritize X next”).

Keep it short; the user can reply for details.

---

## Where this is written into the process

- **Coordinator:** `docs/agents/coordinator-instructions.md` — when a big change is done, hand off to Human-in-the-Loop to email the user.
- **Human-in-the-Loop Manager:** `docs/agents/roles/human-in-the-loop-manager.md` — after big changes, send a summary email; use this doc as the rule.
- **Execution plans:** e.g. `docs/agents/EMULATOR_EDITING_EXTENSIVE_AGENT_TODOS.md` — “After completing a big change, email the user per docs/agents/EMAIL_AT_END_OF_BIG_CHANGES.md.”

---

*Stopping to send this email is expected and correct. Always email at the end of big changes. Send without asking the user first.*
