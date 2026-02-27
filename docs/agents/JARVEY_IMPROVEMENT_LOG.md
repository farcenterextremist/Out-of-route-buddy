# Jarvey Improvement Log (Jarvey's Memory)

**Purpose:** Record changes, before/after parameters, and fixes so we can track improvement and avoid regressions. Update this file when making changes that affect Jarvey's behavior.

---

## Parameters (1–5 scale)

| Parameter | Definition |
|-----------|------------|
| **Scope** | Stays in coordinator role; assigns work, delegates, consults; does not write code or do other roles' work. |
| **Data set** | Cites project context / doc paths (Known Truths, priorities) when relevant. |
| **Output** | Concrete, actionable; directly answers each question; next steps or owner clear. |
| **Handoff** | When the user must decide or another role must act, handoff is clear. |
| **Voice** | Clearly Jarvey: project-aware, consistent tone, signs as Jarvey. |
| **Grounding** | Reply is clearly in response to the **actual** user email (subject + body), not generic or off-topic. |
| **Hallucination** | No invented paragraphs; every sentence directly answers what the user asked. |

---

## Entry format

| Date | Change | Before (params) | After (params) | Notes |
|------|--------|-----------------|----------------|-------|
| YYYY-MM-DD | Short description | Scope X, Output Y, … | Scope X, Output Y, … | Optional details |

---

## Entries

### 2025-03-XX: Anti-hallucination and HITL distillation

| Date | Change | Before (params) | After (params) | Notes |
|------|--------|-----------------|----------------|-------|
| 2025-03-XX | Anti-hallucination and HITL distillation | Grounding 2–3, Output 2–4, Hallucination N/A | Grounding 4+, Output 4+, Hallucination 5 (target) | See plan: jarvey_anti-hallucination_and_hitl_distillation |

**Changes made:**
1. **Context:** Removed ROADMAP from base context; ROADMAP now loaded on-demand only when user asks "what's next" or similar (intent: roadmap).
2. **Prompt:** Added HITL persona block: "You ask questions, summarize, and relay—you do not decide or invent. Only write paragraphs that directly answer what the user asked."
3. **Prompt:** Strengthened TASK with anti-hallucination rules: "Do not add a second (or later) paragraph about roadmap, priorities, recent work, or next steps unless the user explicitly asked for that." and "If the user asked one thing, answer that one thing. Do not pad the reply with unrelated context."
4. **Prompt:** Added JARVEY_INTENT_AND_GOALS reference: "Respond only to what the user wrote; do not invent or hallucinate."

**Baseline (inferred from JARVEY_EVALUATION_REVIEW):**
- Scope: 3–4
- Data set: 3–4
- Output: 2–4
- Handoff: 3–4
- Voice: 3–4
- Grounding: 2–3 (critical shortfall)
- Hallucination: not previously tracked

---

### 2026-02-25: Same-inbox mode fix — user messages no longer skipped

| Date | Change | Before (params) | After (params) | Notes |
|------|--------|-----------------|----------------|-------|
| 2026-02-25 | read_replies same-inbox skip bug | User messages skipped (no response) | User messages processed | Critical fix |

**Problem:** In same-inbox mode (COORDINATOR_EMAIL_FROM == COORDINATOR_EMAIL_TO), the "skip messages from ourselves" check was skipping the user's messages too, because they're FROM the same address. Jarvey never responded.

**Fix:** Only skip by our_from when we have a dedicated bot address (`our_from != user_email`). In same-inbox mode, rely on X-OutOfRouteBuddy-Sent header only to skip agent-sent messages.

**File:** `scripts/coordinator-email/read_replies.py`

---

### 2026-02-25: read_replies UNSEEN-only and case-sensitive subject

| Date | Change | Before (params) | After (params) | Notes |
|------|--------|-----------------|----------------|-------|
| 2026-02-25 | read_replies search + subject | User messages skipped | User messages found | Two fixes |

**Problems:**
1. **UNSEEN-only:** We searched UNSEEN first; if any UNSEEN existed (e.g. newsletters), we only checked those. User messages already marked read (by client or prior run) were never seen.
2. **Case-sensitive subject:** Subject "Outofroutebuddy" (lowercase) failed the check; only "OutOfRouteBuddy" matched.

**Fixes:**
1. Search ALL instead of UNSEEN first. Dedupe (last_responded_state) prevents double-reply.
2. Subject check now case-insensitive.

**File:** `scripts/coordinator-email/read_replies.py`

---

### 2026-02-25: Prompt fix — don't treat clear requests as "unclear"

| Date | Change | Before (params) | After (params) | Notes |
|------|--------|-----------------|----------------|-------|
| 2026-02-25 | Unclear fallback too broad | "Send me recent updates" → "couldn't make out" | Clear requests answered | Prompt tightening |

**Problem:** User sent "Send me a new email with a new subject about recent updates to outofroutebuddy" and got "I got your message but couldn't make out what you need."

**Fix:** Narrow "unclear" to truly empty/illegible. Add: "Do NOT use that phrase when the user has given a clear request." Add: "If the user asks for 'a new email' or 'send me' something about a topic, your reply IS that email—compose it with that topic and a fitting subject."

**File:** `scripts/coordinator-email/coordinator_listener.py`

---

## Related docs

- [JARVEY_EVALUATION_REVIEW.md](data-sets/JARVEY_EVALUATION_REVIEW.md) — Metrics and shortfalls
- [JARVEY_INTENT_AND_GOALS.md](JARVEY_INTENT_AND_GOALS.md) — Core principle: respond only to what the user wrote
- [agent-aptitude-scorecard.md](data-sets/agent-aptitude-scorecard.md) — Email coordinator (Jarvey) scoring table
