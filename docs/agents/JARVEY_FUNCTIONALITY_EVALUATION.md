# Jarvey Functionality Evaluation

**Purpose:** Evaluate Jarvey's current functionality, identify areas needing the most improvement, and recommend tests to add.

---

## 1. Agent Flow Overview

```
read_replies() → _strip_quoted_content() → choose_response() → [template match?] → compose_reply_*() → _ensure_jarvey_signoff() → send()
                     ↓                           ↓                      ↓
              body_trim used              template path          LLM path (OpenAI/Ollama)
              for all downstream          (thanks, priority,     + sign-off enforcement
                                           weekly_digest)
```

**Key modules:**
- `coordinator_listener.py` — Main loop; strips quoted content, dedupes, rate-limits, template-first, LLM fallback
- `read_replies.py` — IMAP inbox reader; filters by subject (OutOfRouteBuddy), from-address, UNSEEN/ALL
- `check_and_respond.py` — Template selection (`choose_response`)
- `send_email.py` — SMTP sender

---

## 2. Areas Needing the Most Improvement

### 2.1 Critical (High Priority)

| Area | Current state | Issue | Recommendation |
|------|---------------|-------|----------------|
| **Grounding** | Partially fixed | Quoted content stripped; prompt strengthened. LLM may still drift or under-weight user message. | Add scenario tests; enforce "user message must be quoted or paraphrased" in prompt; add optional example reply. |
| **Message selection** | `read_replies` returns "latest in last 50" | Ambiguous with multiple threads; no explicit thread-id; relies on `COORDINATOR_EMAIL_TO` filter. | Add unit tests for `_normalize_email`, `get_body`; document filter behavior; consider thread-id or explicit "latest from user" logic. |
| **Deduplication** | `message_id` + content hash fallback | Hash now uses `body_trim` (stripped body) for consistency. | Done. |
| **LLM path reliability** | No tests | Empty LLM response, timeout, model drift not covered. | Add mocked tests for empty LLM response handling; add timeout handling. |

### 2.2 Important (Medium Priority)

| Area | Current state | Issue | Recommendation |
|------|---------------|-------|----------------|
| **Quoted content stripping** | `_strip_quoted_content` implemented | No unit tests; edge cases (nested quotes, non-English formats) may slip. | Add unit tests for Gmail, Outlook, forwarded formats. |
| **Sign-off enforcement** | `_ensure_jarvey_signoff` appends if missing | No unit tests; edge cases (e.g. "— OutOfRouteBuddy Team" alone) not handled. | Add unit tests; optionally replace wrong sign-off. |
| **Template matching** | Regex-based | Priority order when multiple match; case sensitivity; partial matches (e.g. "thankful"). | Add edge-case tests; document template priority. |
| **Cooldown / state** | Shared files | Race if listener and check_and_respond run simultaneously. | Document single-process assumption; add optional file locking. |

### 2.3 Lower Priority

| Area | Current state | Issue | Recommendation |
|------|---------------|-------|----------------|
| **Project context** | 4500 chars, silent truncation | Important context may be lost. | Log truncation; consider summarization. |
| **MIME / email parsing** | `get_body` handles multipart | Malformed emails may crash. | Add defensive parsing; try/except in `get_body`. |

---

## 3. Recent Fixes (Post-“Random Responses” Bug)

| Fix | Status |
|-----|--------|
| Strip quoted content before LLM | ✅ Implemented |
| Strengthen prompt: "Respond ONLY to user's message" | ✅ Implemented |
| Sign-off enforcement: append "— Jarvey" if missing | ✅ Implemented |
| Debug logging: subject + body snippet | ✅ Implemented |
| JARVEY_INTENT_AND_GOALS.md | ✅ Created |

---

## 4. Tests to Add

### 4.1 Priority 1: Critical Path Tests
- [x] `_strip_quoted_content` — Gmail "On ... wrote:", Outlook "-----Original Message-----", forwarded
- [x] `_ensure_jarvey_signoff` — has sign-off, missing sign-off, wrong sign-off
- [x] Dedupe: `message_id` prevents second reply; same message_id = no send
- [x] Cooldown: `last_sent_within_cooldown` returns True within 2 min

### 4.2 Priority 2: Message Selection Tests
- [ ] `_normalize_email` — various formats
- [ ] `get_body` — empty, multipart, charset handling
- [ ] read_replies contract: 4-value return when no match

### 4.3 Priority 3: Template Matching Edge Cases
- [x] Case sensitivity: "THANKS" vs "thanks"
- [x] Template priority: weekly_digest vs thanks when both match
- [x] "thankful" does not match "thanks" (word-boundary regex)

### 4.4 Priority 4: LLM Path Tests
- [ ] Empty LLM response → RuntimeError, no send
- [ ] LLM reply without "— Jarvey" → sign-off appended

### 4.5 Priority 5: Scenario / Integration Tests
- [ ] `compose_reply.py` with "test" → reply contains "here" or "ready" (no meetings/reports)
- [ ] Scenario 01, 05, 06 from jarvey-scenarios

---

## 5. Existing Test Coverage

| File | Tests | Coverage |
|------|-------|----------|
| `test_check_and_respond.py` | 18 | Template selection, state, read_replies contract, case sensitivity, thankful, template priority |
| `test_coordinator_listener.py` | 20 | Default→LLM, empty no-send, read_replies shape, `_strip_quoted_content`, `_ensure_jarvey_signoff`, dedupe, cooldown, `_is_agent_sent` (same-inbox) |

**Total:** 39 tests. **Remaining gaps:** read_replies internals (IMAP), LLM path (empty response).

---

## 6. Same-Inbox Email (Resolved)

**Previously:** When `COORDINATOR_EMAIL_FROM` and `COORDINATOR_EMAIL_TO` were the same address, the agent skipped all messages from that address (including the user's return emails) and never read them.

**Fix implemented (Option B):** Agent-sent emails include `X-OutOfRouteBuddy-Sent: true`. `read_replies` skips only messages with that header, so the agent can read the user's replies while ignoring its own sends. See [scripts/coordinator-email/README.md](../scripts/coordinator-email/README.md) § Same-inbox setup.

---

## 7. Related Docs

- [JARVEY_IMPROVEMENT_LOG.md](JARVEY_IMPROVEMENT_LOG.md) — Same-inbox fix and other improvements; [JARVEY_INTELLIGENCE_PLAN.md](JARVEY_INTELLIGENCE_PLAN.md) for future improvement planning
- [JARVEY_INTENT_AND_GOALS.md](JARVEY_INTENT_AND_GOALS.md) — Intent, goals, non-goals
- [JARVEY_EVALUATION_REVIEW.md](data-sets/JARVEY_EVALUATION_REVIEW.md) — Metrics, scores, shortfalls
- [coordinator-instructions.md](coordinator-instructions.md) — Role and handoffs
- [coordinator-project-context.md](coordinator-project-context.md) — Injected context
