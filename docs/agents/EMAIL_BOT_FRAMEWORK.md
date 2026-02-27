# Email bot framework

This document describes the **coordinator-email** setup so you can reuse it for another project or bot (e.g. a different named coordinator, support bot, or consultation bot).

## What’s in the framework

- **Identity:** A single bot name (e.g. Jarvey) used in instructions, system prompt, and all reply sign-offs (template and LLM).
- **Instructions:** One markdown file that defines role, responsibilities, and team/reference. Injected as the base of the system prompt.
- **Project context:** One markdown file (e.g. coordinator-project-context.md) with condensed project data, injected after instructions so replies are project-aware. Keep it under ~4000 characters.
- **Templates:** Template-based auto-replies for common intents (thanks, priority, default, etc.). Every template signs as the bot (e.g. `— Jarvey`).
- **LLM reply path:** When no template matches, the listener builds a system prompt = instructions + project context + TASK. The TASK tells the model to reply as the bot, structure the reply (acknowledge → answer → next steps), and sign as the bot.
- **Listener:** Optional script that polls the inbox, respects a cooldown, dedupes by message_id, and either picks a template or calls the LLM to compose a reply, then sends via SMTP.
- **Env:** SMTP/IMAP credentials, optional bot name, API key or Ollama URL, paths to instructions and context. See `scripts/coordinator-email/.env.example`.

## Where it lives in this repo

| Piece            | Location |
|------------------|----------|
| Instructions     | `docs/agents/coordinator-instructions.md` |
| Project context  | `docs/agents/coordinator-project-context.md` |
| Templates + send | `scripts/coordinator-email/check_and_respond.py`, `send_email.py` |
| Listener         | `scripts/coordinator-email/coordinator_listener.py` |
| README           | `scripts/coordinator-email/README.md` |

## Building a similar bot

1. Copy or adapt the scripts under `scripts/coordinator-email/` (or create a new folder).
2. Create your own instructions and project-context files; use a clear bot name and “— &lt;BotName&gt;” in every sign-off.
3. Use the checklist: [EMAIL_BOT_FRAMEWORK_CHECKLIST.txt](EMAIL_BOT_FRAMEWORK_CHECKLIST.txt).
