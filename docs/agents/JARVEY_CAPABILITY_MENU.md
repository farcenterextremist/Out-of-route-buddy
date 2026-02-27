# Jarvey Capability Menu

Jarvey can send a startup email with 5–10 multiple-choice options of what it can help with. In subsequent replies, Jarvey includes multiple-choice options when appropriate. All capability sends and user choices are persisted.

---

## Capability options (default)

The default menu is defined in `scripts/coordinator-email/jarvey_capability_menu.json`:

1. Roadmap status and next steps  
2. Recent project changes  
3. Prioritize a feature or task  
4. Where is X defined? (code/docs)  
5. Trip recovery / persistence  
6. App version / build info  
7. Assign work to the team  
8. Save a note or decision  
9. Send update to coworker/family  
10. Something else (describe in reply)

---

## How to enable

Set in `.env`:

```
JARVEY_SEND_OPENER_ON_STARTUP=1
```

When the coordinator listener starts (`python coordinator_listener.py`), Jarvey will send the capability menu email to `COORDINATOR_EMAIL_TO` before entering the main inbox loop.

---

## How to edit options

Edit `scripts/coordinator-email/jarvey_capability_menu.json`:

```json
{
  "options": [
    {"id": "1", "label": "Roadmap status and next steps"},
    {"id": "2", "label": "Recent project changes"},
    ...
  ]
}
```

Add or remove options as needed. IDs should be unique (e.g. "1", "2", "3").

---

## Where data is stored

- **jarvey_choices.json** (in `scripts/coordinator-email/`):
  - `capability_sends`: list of sends (timestamp, subject, options_sent)
  - `user_choices`: list of user replies (timestamp, user_message, choice_or_reply, thread_id)

Data is pruned to the last 100 entries per list to avoid unbounded growth.

---

## Multiple-choice options in replies

When Jarvey replies, it may include short multiple-choice options at the end (e.g. "1. Roadmap 2. Recent changes 3. Assign work"). This happens when:

- The user's message is vague or could be answered in several ways.
- Jarvey has answered a question and there are natural follow-ups.

See `docs/agents/coordinator-instructions.md` § Multiple-choice options for the full guidance.
