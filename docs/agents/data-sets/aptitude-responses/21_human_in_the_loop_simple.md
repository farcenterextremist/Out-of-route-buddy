# Human-in-the-Loop Manager — Simple

- **Role card:** `docs/agents/roles/human-in-the-loop-manager.md`
- **Prompt type:** Simple

## Prompt (copy-paste)

```
What script do you use to send email to the user, and where is it?
```

## Look for (scoring)

scripts/coordinator-email/, send_email.py or README; draft only; no product decisions.

---

## Response

I use the email script in **`scripts/coordinator-email/`** — e.g. **`agent_email.py`** (or `send_email.py` / script described in that folder’s README) to send to the user. Path: **`scripts/coordinator-email/`** from repo root. I draft only; I don’t make product decisions. See `docs/agents/OPEN_LINE_OF_COMMUNICATION.md` for when to send.

