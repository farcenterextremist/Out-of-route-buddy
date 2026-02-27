# Jarvey Note Disconnect — Analysis & Verification

**Date:** 2026-02-26  
**Issue:** User sent a note to be "sent to the development team." Jarvey replied with "recent project changes" and "Note Saved:4" — but the note was not saved and was not sent to the team.

---

## 1. What the user sent

> This note will be sent to the rest of the development team. Inform them that we will be having a new employee working with us. Dedicated to taking notes and organizing those notes and distributing those notes. His name is Chad, but in reality, he's just a Separate version of Jarvey. Chad will start work whenever jarvey starts work. Chad will delegate any notes to the proper recipients.

**User intent:** (a) Take this note, (b) Send it to the development team.

---

## 2. What Jarvey replied

- "You asked about recent project changes" — **Wrong.** User did not ask about recent changes; user gave a directive.
- Listed Chad as "recent updates" — **Invented framing.**
- "Note Saved:4" at bottom — **Malformed or hallucinated.** No such format exists in the codebase.

---

## 3. Verification: Was the note saved?

**No.** `docs/agents/EMAIL_NOTES.md` does **not** contain the Chad note. It only contains prior "Test note" entries. The note was **not** recorded.

---

## 4. Root causes

| Cause | Explanation |
|-------|-------------|
| **Wrong intent** | Message may have matched "recent" or similar keywords. Intent routing treated it as "recent project changes" instead of "notes + send to team." |
| **No "send to team" action** | Jarvey has no action to email a note to "the development team." Only `save_note` (appends to file) and `send` to COORDINATOR_EMAIL_TO (or COWORKER/FAMILY when user says "send to coworker/family"). |
| **save_note not executed** | The LLM likely did not return a valid `{"action": "save_note", "params": {"note": "...", "topic": "..."}}` in its reply, or the JSON was malformed. "Note Saved:4" suggests the model invented a status instead of using structured output. |
| **Silent failure** | If `execute_action` fails or `parse_structured_reply` returns None, the error is swallowed (`except Exception: pass`). No feedback to the user that the note was not saved. |

---

## 5. What Jarvey can actually do today

| Action | What it does |
|--------|--------------|
| **save_note** | Appends to `docs/agents/EMAIL_NOTES.md`. Does NOT send any email. |
| **assign_work** | Appends to EMAIL_NOTES.md with topic "Assigned to {role}". Does NOT send email. |
| **send** | Sends email to COORDINATOR_EMAIL_TO (user) or COORDINATOR_EMAIL_COWORKER/FAMILY when user says "send to coworker/family." There is no "team" distribution list. |

---

## 6. Recommended fixes

1. **Prompt clarification:** When user says "send this note to the team" or "send to the development team":
   - Do NOT say "You asked about recent project changes."
   - Do: (a) Save the note via save_note JSON, (b) Reply: "I've added that to the project notes. I can only email configured recipients (you, coworker, or family). To notify the team, set COORDINATOR_EMAIL_COWORKER and say 'send to coworker,' or I can include this in the next email I send you."

2. **Intent detection:** Add keywords for "send to team," "send this note," "inform the team" so the notes intent is triggered, not "recent."

3. **Verification:** After save_note executes, log success. Optionally: if the LLM includes a save_note block, verify it was parsed and executed before sending the reply.

4. **No "Note Saved:N" in prose:** Instruct the model to never output "Note Saved:4" or similar in the email body. Use only the structured JSON block; the coordinator strips it and the prose should say "I've added that to the project notes."

5. **Optional: send_note_to_coworker action:** If user says "send this note to the team" and COORDINATOR_EMAIL_COWORKER is set, send an email to that address with the note content. (Requires new action.)

---

## 7. How to verify notes are saved (going forward)

```bash
# Check EMAIL_NOTES.md for recent entries
cat docs/agents/EMAIL_NOTES.md
```

Or ask Jarvey: "What notes do you have from my emails?" — Jarvey loads EMAIL_NOTES.md and summarizes. If the note was saved, it will appear there.

---

*See [EMAIL_NOTES_DATA.md](EMAIL_NOTES_DATA.md) for the note-taking flow and [JARVEY_IMPROVEMENT_LOG.md](JARVEY_IMPROVEMENT_LOG.md) for fixes applied.*
