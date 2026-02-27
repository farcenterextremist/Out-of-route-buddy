# Email Notes — Data Flow for Jarvey

**Purpose:** Document how Jarvey takes notes from your email replies, stores them in the project, and references them when you ask.

---

## Storage

- **File:** `docs/agents/EMAIL_NOTES.md`
- **Format:** Markdown with dated sections: `## YYYY-MM-DD — [topic]\n\n{note}\n\n`
- **Append-only:** Notes are appended; never overwritten.

---

## Taking Notes (from your email)

When your email contains information worth saving (decisions, priorities, feedback), Jarvey adds a structured action to the reply:

```json
{"action": "save_note", "params": {"note": "<text to save>", "topic": "<optional topic>"}}
```

**Triggers:** "Add to notes: ...", "Save this", "Remember this", "Prioritize X over Y", or any decision/feedback you share.

**Flow:**
1. LLM composes reply and includes save_note JSON block
2. `structured_output.execute_action()` calls `append_to_notes(note, topic)`
3. Note is appended to EMAIL_NOTES.md
4. JSON block is stripped from the email body before sending

**Entry points:** coordinator_listener, check_and_respond, trace_jarvey_workflow (all execute structured output).

---

## Referencing Notes (when you ask)

**Intent:** `notes` — keywords: "notes", "my notes", "what notes", "saved notes", "add to notes", "save this", "take a note", "remember this", "add to project", "record this", "jot down"

**Source:** `email_notes` → `get_email_notes()` loads EMAIL_NOTES.md (cap 1500 chars)

When you ask "What notes do you have?" or "My notes from emails?", Jarvey loads EMAIL_NOTES.md and summarizes. If empty: "No notes saved yet. Share decisions or feedback and I'll add them."

---

## Code References

- **Append:** `context_loader.append_to_notes(note, topic)`
- **Read:** `context_loader.get_email_notes(cap_chars)`
- **Execute:** `structured_output._execute_save_note(params, env)`
- **Intent:** `intents.json` → `notes` with source `email_notes`

---

## Related Docs

- [coordinator-project-context.md](coordinator-project-context.md) — Golden examples for save/retrieve notes
- [JARVEY_FAQ.md](data-sets/JARVEY_FAQ.md) — "What are my notes? Email notes?"
