# Custom Prompt: Jarvey Improvement Plan

**Purpose:** Use this prompt (e.g. in Cursor or a planning session) to generate a concrete plan to address Jarvey's shortfalls. The prompt includes quality context and a critical finding about same-inbox email setups.

---

## START OF PROMPT

Create a detailed, actionable plan to improve **Jarvey** (the email coordinator bot) and address the areas needing the most improvement. Reference the evaluation in `docs/agents/JARVEY_FUNCTIONALITY_EVALUATION.md` and `docs/agents/data-sets/JARVEY_EVALUATION_REVIEW.md`.

---

### Context: Same-Inbox Email Setup (Critical)

**The user communicates with Jarvey using their own email.** When the agent sends reply emails, those replies show up in the user's inbox. The user then replies from the same inbox.

**This can prevent the agent from reading the user's return emails.**

**Why:** In `read_replies.py`, the logic is:

1. **Skip messages FROM ourselves** (`COORDINATOR_EMAIL_FROM`) — so we don't reply to our own sent messages.
2. **Only consider messages FROM the user** (`COORDINATOR_EMAIL_TO`) — so we only process the user's replies.

When `COORDINATOR_EMAIL_FROM` and `COORDINATOR_EMAIL_TO` are the **same address** (user's email):

- The agent sends FROM that address.
- The user replies FROM that same address.
- `read_replies` skips all messages FROM that address (to avoid replying to the agent's own sends).
- **Result:** The user's return emails are skipped. The agent never reads them.

**Implications for the plan:**

1. **Same-inbox detection and handling** — Propose a way to detect when FROM and TO are the same, and adjust the filter so the agent can read the user's replies. Options to consider:
   - **Option A:** When `COORDINATOR_EMAIL_FROM == COORDINATOR_EMAIL_TO`, do NOT skip messages from that address (because in INBOX we only have *received* messages; our *sent* messages are in Sent folder). Risk: if the agent's sent replies somehow appear in INBOX (e.g. via rules or BCC), we might reply to ourselves in a loop.
   - **Option B:** Use a custom header (e.g. `X-OutOfRouteBuddy-Sent: true`) on agent-sent emails, and skip only messages with that header. This distinguishes "agent sent" from "user replied" even when both are from the same address.
   - **Option C:** Record Message-IDs of emails we send; skip those IDs when reading. Requires persisting sent Message-IDs.
   - **Option D:** Recommend using two different addresses (agent@domain.com vs user@gmail.com) as the preferred setup, and document the same-inbox limitation clearly.

2. **Documentation** — Add a "Same-inbox setup" section to the coordinator-email README explaining:
   - When same address is used, the agent may not read the user's replies (current behavior).
   - Recommended: use a dedicated agent address (e.g. jarvey@outofroutebuddy.com) for sending; user replies from their personal address.
   - If same address is required, document which option (A–C) was implemented and any caveats.

---

### Context: Areas Needing the Most Improvement

| Priority | Area | Issue |
|----------|------|-------|
| **Critical** | Same-inbox filter | When FROM=TO, user's return emails are skipped. Agent never reads them. |
| **Critical** | Grounding | LLM may drift or under-weight user message; replies not tied to what user wrote. |
| **Critical** | Message selection | "Latest in last 50" ambiguous with multiple threads; no thread-id. |
| **Critical** | Deduplication | Hash uses raw body, not stripped; possible double-reply. |
| **Important** | LLM path reliability | No tests for empty response, timeout; model drift. |
| **Important** | Template matching | Edge cases (case, priority, partial matches) not tested. |
| **Lower** | Project context | 4500-char truncation; important context may be lost. |

---

### Requirements for the Plan

1. **Same-inbox fix (highest priority)**  
   Propose a concrete fix so the agent can read the user's return emails when using the same address for both agent and user. Compare options A–D (or variants), recommend one, and list implementation steps (files, functions, env vars).

2. **Grounding and message selection**  
   - Ensure the reply is always based on the actual user email (subject + body).  
   - Ensure `read_replies` returns the user's latest reply correctly (including after the same-inbox fix).  
   - Consider thread-id or "latest from user" logic for multi-thread clarity.

3. **Deduplication**  
   - Use hash of `body_trim` (stripped body) instead of raw `body` for content-hash fallback.  
   - Add unit tests for dedupe logic.

4. **LLM path reliability**  
   - Add mocked tests for empty LLM response handling.  
   - Add timeout handling for OpenAI/Ollama calls.  
   - Document model fallback or template-first guarantees.

5. **Template and scenario tests**  
   - Add edge-case tests: case sensitivity, template priority, partial matches.  
   - Add scenario test: "test" → reply contains "here" or "ready" (no invented meetings/reports).

6. **Documentation**  
   - Update `scripts/coordinator-email/README.md` with: same-inbox setup, recommended two-address setup, message selection behavior, dedupe and cooldown.  
   - Update `docs/agents/JARVEY_FUNCTIONALITY_EVALUATION.md` after implementing the same-inbox fix.

---

### Deliverables

The plan should:

- List concrete tasks in implementation order.
- Cite file paths and function names.
- Include a "Same-inbox fix" section with the chosen option and rationale.
- Be reviewable before implementation (do not implement in this step—only produce the plan).

---

### Reference Files

- `scripts/coordinator-email/read_replies.py` — IMAP reader; FROM/TO filter logic (lines 96–146)
- `scripts/coordinator-email/coordinator_listener.py` — Main loop; dedupe hash (lines 290–296)
- `docs/agents/JARVEY_FUNCTIONALITY_EVALUATION.md` — Improvement areas, test coverage
- `docs/agents/JARVEY_INTENT_AND_GOALS.md` — Intent, goals, non-goals
- `docs/agents/data-sets/JARVEY_EVALUATION_REVIEW.md` — Metrics, shortfalls, improvement prompt

---

## END OF PROMPT
