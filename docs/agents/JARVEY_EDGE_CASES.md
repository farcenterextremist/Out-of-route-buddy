# Jarvey Edge Cases and Logic Flow

This document records edge cases, expected behavior, and configuration requirements for the Jarvey email coordinator system.

---

## 1. Architecture Overview

- **coordinator_listener.py** — Main loop; template-first, LLM fallback; uses `last_responded_state.txt` and `last_sent_timestamp.txt`
- **check_and_respond.py** — Cron/scheduled; template-only; shares same state files
- **read_replies.py** — IMAP read; returns (subject, body, date, message_id)
- **send_email.py** — SMTP send; supports COORDINATOR_DRY_RUN

---

## 2. Dedupe Semantics

Both scripts use `last_responded_state.txt` to avoid double-replying.

| Scenario | dedupe_id | Behavior |
|----------|-----------|----------|
| message_id present | `message_id` | Save and compare by message_id |
| message_id None | `hash:sha256(subject + body[:500])` | Save and compare by content hash |

**Important:** `check_and_respond` and `coordinator_listener` must use the same dedupe logic. When `message_id` is None (e.g. malformed email, missing Message-ID header), both use a content hash of `(subject + "\n" + stripped_body[:500])` so they recognize the same message.

---

## 3. Empty / Whitespace Input

| Input | coordinator_listener | check_and_respond |
|-------|----------------------|-------------------|
| subject="", body="" | Returns early (no send) | Exits 0, no send |
| subject="Re: OutOfRouteBuddy", body="" | Proceeds to LLM | Proceeds, default template |
| subject="", body="   " | body_trim="", returns early | body from get_body is ""; exits 0 |

---

## 4. Quoted Content Stripping

`_strip_quoted_content` removes quoted/forwarded blocks so we respond only to the user's new text. **Jarvey replies to both Re: replies and forwarded emails**—we extract the user's new content and respond to that.

**Stops at (quote/forward markers):**
- Gmail: `On ... wrote:`, `On Mon, Jan 1, 2024 at 10:00 AM ...`
- Outlook: `-----Original Message-----`
- Forwarded: `----- forwarded message -----`, `---------- Forwarded message ---------`, `Begin forwarded message:`
- `From: ... Sent: ...`

**Does NOT strip:**
- "On second thought, I agree" (no "wrote")
- User content before any quote marker

---

## 5. Sign-off Enforcement

`_ensure_jarvey_signoff` appends `— Jarvey` when the reply lacks it.

- Checks last 50 chars for `— Jarvey`, `—Jarvey`, `– Jarvey`, `- Jarvey` (em dash, en dash, hyphen)
- Empty/whitespace reply: returns unchanged

---

## 6. Template Selection Priority

Order: weekly_digest → thanks → priority → default.

- "weekly thanks for the digest" → weekly_digest (checked first)
- "Thanks, prioritize reports" → thanks (thanks before priority)
- "thankful" → default (word boundary excludes it)

---

## 7. read_replies Edge Cases

| Scenario | Behavior |
|----------|----------|
| COORDINATOR_EMAIL_TO unset | Accepts any From (no user filter). **Config requirement:** Set COORDINATOR_EMAIL_TO to restrict to user's address. |
| our_from == user_email | Same-inbox mode; continues with warning. **Remember: sending an email to yourself triggers Jarvey** — the message is FROM you and TO you; Jarvey reads that inbox for messages FROM COORDINATOR_EMAIL_TO. In same-inbox mode we must NOT skip by our_from (would skip user's messages); we rely on X-OutOfRouteBuddy-Sent only to skip agent-sent. |
| Search ALL (not UNSEEN) | We search ALL so we find user messages even if already marked read (e.g. by email client). Dedupe prevents double-reply. |
| Multipart with HTML-only | get_body returns "" (no text/plain part) |
| Malformed MIME | get_body may raise or return partial |

---

## 8. LLM Path Edge Cases

| Scenario | Behavior |
|----------|----------|
| OpenAI returns empty | Raises RuntimeError; run_once returns; no send, no save |
| Ollama timeout | Raises; run_once returns; no send, no save |
| Compose succeeds, send fails | No save_responded_id; retry on next loop (re-compose) |
| Project context file missing | load_project_context returns "" |
| Project context > 4500 chars | Truncated with note |

---

## 9. Rate Limiting

- **Cooldown:** 2 min shared via `last_sent_timestamp.txt`; never send more than once per COOLDOWN_SECONDS
- **Per-hour cap:** Optional `COORDINATOR_LISTENER_MAX_REPLIES_PER_HOUR`; when set, limits replies in rolling 3600s window
- **Race:** Two processes (listener + cron) could both pass cooldown before either writes. Low risk with 3-min interval.

---

## 10. Configuration Requirements

| Variable | Purpose |
|----------|---------|
| COORDINATOR_EMAIL_TO | Recipient; when set, read_replies filters to messages FROM this address |
| COORDINATOR_EMAIL_FROM | Sender; used to skip our own messages in same-inbox mode |
| COORDINATOR_DRY_RUN | 1/true/yes to skip actual SMTP send (for tests) |
| COORDINATOR_LISTENER_OLLAMA_URL or OLLAMA_URL | For local LLM |
| COORDINATOR_LISTENER_OPENAI_API_KEY or OPENAI_API_KEY | For OpenAI |

---

## 11. Response patterns (user-facing)

For how Jarvey should respond to specific user prompts (out-of-scope, emotional, meta, can't do, undo, timeline unknown, policy conflict), see **[JARVEY_CONTEXT_PLAN.md](JARVEY_CONTEXT_PLAN.md)**. Summary:

| Scenario | Response pattern |
|----------|------------------|
| Out of scope (weather, jokes, politics) | Politely decline and redirect to project |
| Emotional / frustrated user | Lead with empathy, then ask for specifics |
| Meta (Are you AI?) | Full disclosure |
| Can't do — config gap | Explain .env; add recipient to COORDINATOR_EMAIL_* |
| Can't do — action (run build, deploy) | Assign to role (e.g. DevOps) |
| 5+ questions | Answer all in order |
| Undo / ignore request | Acknowledge, disregard, move on |
| Timeline unknown | Offer to ask team and email estimate |
| Policy conflict | Flag for user; draft tradeoffs |
