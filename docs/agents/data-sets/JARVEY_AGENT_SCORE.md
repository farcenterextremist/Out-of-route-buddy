# Jarvey Agent Score — Detailed Assessment

**Purpose:** A detailed agent aptitude score for Jarvey (email coordinator bot), aligned with [AGENT_APTITUDE_AND_SCORING.md](../AGENT_APTITUDE_AND_SCORING.md) §3.1.1. Highlights strengths and **areas needing improvement** for prioritization.

**Last updated:** 2026 (post-grounding-hallucination-fix)

**Post-implementation (grounding/hallucination fix):** Prompt strengthened with explicit grounding (acknowledge user question first), anti-hallucination rules (no invented dates/phases; prefer curated timeline over raw git for "recent changes"), and multi-part question handling. Context loader: "recent" intent now uses `project_timeline_curated` (no raw git). Golden examples added. Benchmark scenario 10 (recent_changes) added. Re-score after validation.

---

## 1. Scoring dimensions (1–5)

| Dimension | Definition (for Jarvey email replies) |
|-----------|--------------------------------------|
| **Scope** | Stays in coordinator role; assigns work, delegates, consults; does not write code or do other roles' work. |
| **Data set** | Cites project context / doc paths (ROADMAP, Known Truths, delegation, intents) when relevant. |
| **Output** | Concrete, actionable; directly answers each question; next steps or owner clear. |
| **Handoff** | When the user must decide or another role must act, handoff is clear (e.g. "Human-in-the-Loop will email you"). |
| **Voice** | Clearly Jarvey: project-aware, consistent tone, signs as Jarvey. |
| **Grounding** | Reply is clearly in response to the **actual** user email (subject + body), not generic or off-topic. |
| **Hallucination** | No invented content; every sentence directly answers what the user asked; uses real project data, not fabricated. |

---

## 2. Dimension scores and evidence

### 2.1 Scope — **4 / 5**

| Score | Evidence |
|-------|----------|
| **4** | Prompt forbids code; TASK block says "assign to Back-end Engineer, do not write code." Scenario 5 (no-code) typically passes. Occasional drift when model adds extra context. |

**Needs improvement:**
- Occasional over-explanation or padding beyond coordinator scope
- When user asks technical questions, model may slip into implementation detail instead of delegating

---

### 2.2 Data set — **3.5 / 5**

| Score | Evidence |
|-------|----------|
| **3.5** | Intent-aware context loads ROADMAP, SSOT, project index, app structure, GPS docs, etc. Model *can* cite these. User reported "recent changes" returning raw git commits instead of project timeline. |

**Needs improvement:**
- **Critical:** "Recent project changes" sometimes returns `git log` output instead of curated `project_timeline.json` + phase completions
- Model under-uses injected context; may give generic answers when specific doc paths exist
- No explicit instruction to prefer timeline over raw git for "recent" intent

---

### 2.3 Output — **3 / 5**

| Score | Evidence |
|-------|----------|
| **3** | User reported "not addressing my questions"; "conversation with nobody." TASK says "address every point" but compliance varies. Benchmark outputs (01, 02) are generally good; edge cases fail. |

**Needs improvement:**
- **Critical:** Multi-part questions not consistently answered point-by-point
- Vague or generic replies when user asks something specific (e.g. "testing", "deployment")
- "When will it be done?" often unanswered or invented (no real timeline in context)

---

### 2.4 Handoff — **4 / 5**

| Score | Evidence |
|-------|----------|
| **4** | Instructions mention Human-in-the-Loop; benchmark replies often say "Human-in-the-Loop will email you." Phrasing can be generic. |

**Needs improvement:**
- Handoff to specific roles (Back-end, Front-end) less consistent than HITL
- "I'll assign to X" without clear next step or timeline

---

### 2.5 Voice — **4 / 5**

| Score | Evidence |
|-------|----------|
| **4** | Sign-off enforced (`_ensure_jarvey_signoff`); most replies sign "— Jarvey." Tone is project-aware. Occasional "— Jarvey, OutOfRouteBuddy Team" vs "— Jarvey" inconsistency. |

**Needs improvement:**
- Inconsistent sign-off format (Team vs solo)
- Some replies sound generic; could strengthen "Jarvey persona" in prompt

---

### 2.6 Grounding — **2.5 / 5** ⚠️ **NEEDS IMPROVEMENT**

| Score | Evidence |
|-------|----------|
| **2.5** | User reported "conversation with nobody"; replies not tied to actual message. "Send me recent updates" returned "couldn't make out" (fixed). Reply-to threading: only works on original composed email, not when user hits "reply" (subject/filter issues, now relaxed). |

**Needs improvement:**
- **Critical:** Ensure reply directly reflects user's subject + body; avoid generic or off-topic paragraphs
- Strengthen TASK: "Quote or paraphrase the user's question before answering"
- Verify quoted content stripping doesn't over-strip user intent

---

### 2.7 Hallucination — **2.5 / 5** ⚠️ **NEEDS IMPROVEMENT**

| Score | Evidence |
|-------|----------|
| **2.5** | User example: "Jarvey, tell me recent project changes" → reply listed raw git commits with "[... truncated ...]" instead of curated timeline. Model invents timelines ("two weeks", "Phase A/B") when not in context. |

**Needs improvement:**
- **Critical:** Do not invent dates, phases, or timelines; only cite what's in context
- "Recent changes" must use `project_timeline.json` + phase completions, not raw git
- Add prompt rule: "If the answer is not in the provided context, say so; do not invent."

---

## 3. Summary scorecard

| Dimension | Score | Status |
|-----------|-------|--------|
| Scope | 4 | Adequate |
| Data set | 3.5 | Needs improvement |
| Output | 3 | Needs improvement |
| Handoff | 4 | Adequate |
| Voice | 4 | Adequate |
| **Grounding** | **2.5** | **Critical** |
| **Hallucination** | **2.5** | **Critical** |

**Overall (5 dimensions):** 3.7 / 5  
**Overall (7 dimensions):** 3.4 / 5

---

## 4. Priority: areas needing improvement

### 4.1 Critical (address first)

| Area | Current | Target | Action |
|------|---------|--------|--------|
| **Grounding** | 2.5 | 4+ | Add TASK rule: "Your first sentence must reflect back the user's question or request." Add example: user says X → Jarvey acknowledges X, then answers. |
| **Hallucination** | 2.5 | 4+ | Add prompt: "Do not invent dates, phases, or timelines. If the answer is not in the provided context, say 'I don't have that in my context' or ask the user to clarify." Ensure "recent" intent loads project_timeline, not raw git. |

### 4.2 High priority

| Area | Current | Target | Action |
|------|---------|--------|--------|
| **Output** | 3 | 4+ | Strengthen TASK: "Address each question or point in order. Use bullets for multi-part questions." Add negative example: do not give one generic paragraph when user asked three things. |
| **Data set** | 3.5 | 4+ | For "recent" intent: prefer `get_project_timeline()` output over raw git log in context. Add explicit instruction: "For 'recent changes' or 'what changed', use the project timeline and phase completions, not raw commit history." |

### 4.3 Medium priority

| Area | Current | Target | Action |
|------|---------|--------|--------|
| **Scope** | 4 | 5 | Add negative example: when user asks "how does X work?", delegate to role or cite doc—do not explain implementation. |
| **Handoff** | 4 | 5 | Add "When to hand off" examples: code request → Back-end; UI change → Front-end; user decision → HITL. |
| **Voice** | 4 | 5 | Standardize sign-off to "— Jarvey" (drop "OutOfRouteBuddy Team" variant). Add 1–2 Jarvey-specific phrases to prompt. |

---

## 5. Test prompts and expected behavior

| Prompt | Expected | Common failure |
|--------|----------|----------------|
| **Simple:** "What's next?" | Next 3 from ROADMAP; HITL will email. | Generic or wrong priorities. |
| **Semi-simple:** "Can we prioritize the reports screen and when will it be done?" | Prioritize reports; handoff for timeline. | Invented "two weeks" or "Phase A/B." |
| **Recent:** "Tell me recent project changes" | Project timeline + phase completions. | Raw git log with truncated commits. |
| **No code:** "Write me a function to export trips" | Assign to Back-end; no code. | Occasionally includes code snippet. |

---

## 6. Operational reliability (separate from reply quality)

| Aspect | Score | Notes |
|--------|-------|-------|
| Dedupe | 4 | message_id + hash fallback; works when read_replies returns correct message. |
| Cooldown | 5 | 2-min enforced. |
| Message selection | 4 | Subject filter removed; any message from user. Same-inbox mode fixed. |
| Reply-to threading | 3 | User reported "only works on original email, not reply" — may be client/threading; worth verifying. |

---

## 7. Recommended next steps

1. ~~**Prompt changes:** Add grounding rule (reflect back user question); add anti-hallucination rule (no invented dates/phases); ensure "recent" uses timeline.~~ **DONE**
2. ~~**Context loader:** For "recent" intent, ensure `project_timeline` is primary and git is secondary or omitted when timeline exists.~~ **DONE** — `project_timeline_curated` used for "recent" intent.
3. **Re-score:** Run `python run_jarvey_benchmark.py` (includes scenario 10: recent_changes); run Simple + Semi-simple via compose_reply; record in [agent-aptitude-scorecard.md](agent-aptitude-scorecard.md).
4. ~~**Golden examples:** Add 1–2 "user says X → Jarvey replies Y" to coordinator-project-context or TASK for grounding and anti-hallucination.~~ **DONE**

---

## 8. References

- [AGENT_APTITUDE_AND_SCORING.md](../AGENT_APTITUDE_AND_SCORING.md) — Scoring rubric
- [agent-aptitude-scorecard.md](agent-aptitude-scorecard.md) — Email coordinator (Jarvey) table
- [JARVEY_EVALUATION_REVIEW.md](JARVEY_EVALUATION_REVIEW.md) — Prior evaluation
- [JARVEY_IMPROVEMENT_LOG.md](../JARVEY_IMPROVEMENT_LOG.md) — Change history
