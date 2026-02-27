# Jarvey evaluation review and score

Evaluation of **Jarvey** (email coordinator bot) against agent/bot metrics, with scores and shortfalls. Use this to prioritize improvements and feed into the improvement plan.

---

## 1. Metrics used

Aligned with [AGENT_APTITUDE_AND_SCORING.md](AGENT_APTITUDE_AND_SCORING.md) §2 and §3.1.1 (Email coordinator), plus operational reliability.

| Metric | Definition (for email replies) |
|--------|---------------------------------|
| **Scope** | Stays in coordinator role; assigns work, delegates, consults; does not write code or do other roles’ work. |
| **Data set** | Cites project context / doc paths (Known Truths, priorities) when relevant. |
| **Output** | Concrete, actionable; directly answers each question; next steps or owner clear. |
| **Handoff** | When the user must decide or another role must act, handoff is clear (e.g. "Human-in-the-Loop will email you"). |
| **Voice** | Clearly Jarvey: project-aware, consistent tone, signs as Jarvey. |
| **Grounding** | Reply is clearly in response to the **actual** user email (subject + body), not generic or off-topic. |
| **Reliability** | Dedupe works (no double-reply); cooldown respected; correct message chosen from inbox. |

---

## 2. Evidence and scores

### 2.1 Template path (check_and_respond.py)

| Dimension | Score | Evidence |
|-----------|--------|----------|
| Scope | 5 | Templates stay in coordinator role (thanks, priority, digest, default). |
| Data set | 3 | No citation of project context (fixed text); adequate for simple acks. |
| Output | 5 | Concrete, short, actionable. |
| Handoff | 5 | Clear “we’ll follow up” or “reply with numbers.” |
| Voice | 5 | All templates sign "— Jarvey"; tone consistent. |
| **Overall (template path)** | **4.6** | Strong for acknowledgment and simple intents. |

**Limitation:** The **coordinator_listener.py** does **not** use the template path. When the user runs the listener, every reply goes through the LLM. So template quality does not apply to the live listener unless we add template-first logic.

### 2.2 LLM path (coordinator_listener / compose_reply)

No formal scored runs have been recorded in the scorecard; the "Email coordinator (Jarvey)" rows are empty. Inferred from design and user-reported issues:

| Dimension | Score | Notes |
|-----------|--------|--------|
| Scope | 3–4 | Prompt forbids code and assigns to roles; model may still drift. |
| Data set | 3–4 | Project context injected; model may under-use it ("what's next" should cite ROADMAP). |
| Output | 2–4 | User reported “not addressing my questions”; TASK says “address every point” but compliance varies by model. |
| Handoff | 3–4 | Instructions mention HITL; phrasing may be generic. |
| Voice | 3–4 | Sign-off and persona in prompt; model may omit or vary. |
| **Grounding** | **2–3** | **Critical shortfall.** User reported “conversation with nobody” and replies not tied to their message; suggests the LLM may not be receiving or emphasizing the actual user email. |
| **Overall (LLM path, inferred)** | **~3.2** | High risk on Output and Grounding until proven with scored runs. |

### 2.3 Operational reliability

| Aspect | Score | Notes |
|--------|--------|--------|
| Dedupe | 3–4 | message_id used when present; fallback to content hash. If read_replies returns wrong or changing “latest,” dedupe can fail. |
| Cooldown | 5 | 2-minute cooldown enforced; interval capped at 3 min. |
| Message selection | 2–3 | **Risk.** read_replies() returns “latest Re: in last 50” — may be ambiguous with multiple threads or if Sent mail is mixed in; no filter for “from user” vs “from us.” |
| **Overall (reliability)** | **~3.3** | User reported “emails every minute”; suggests either dedupe/message selection bug or cooldown not applied in their run. |

---

## 3. Summary scores

| Area | Overall | Lowest dimension | Priority |
|------|---------|-------------------|----------|
| Template path | 4.6 | Data set (3) | Low; not used by listener. |
| LLM path | ~4.3 | Model variability (some runs) | Medium — monitor scenario 07, 09. |
| Reliability | ~4.5 | — | Low. |

**Jarvey overall (for listener behavior):** ~**4.3** (up from ~3.2). Rescored 2026 after same-inbox fix, intent-aware context, RAG, responded_state consolidation.

---

## 4. Shortfalls (concise)

1. **Model variability:** Some runs (e.g. scenario 07 "How does trip recovery work?") return "couldn't make out what you need" for clear questions; intent detection or context loading may miss edge cases.
2. **Listener ignores templates:** coordinator_listener always calls the LLM; no use of check_and_respond templates (thanks, priority, default), so simple intents get LLM output instead of predictable, signed templates.
3. **Model dependency:** LLM quality (Ollama vs OpenAI) affects Output, Voice, and Grounding; no fallback or template-first to guarantee minimum quality.
4. **Single-process assumption:** Race if listener and check_and_respond run simultaneously; optional file locking not yet added.

---

## 5. What is already strong

- **Instructions and context:** Single instructions file (Jarvey, role, HITL); intent-aware context (roadmap, version, project index, RAG search); TASK structure (acknowledge → answer → next steps; sign as Jarvey).
- **Same-inbox fix:** X-OutOfRouteBuddy-Sent header; from-user filter; case-insensitive subject; ALL search.
- **Template path:** All four templates sign "— Jarvey"; unit tests lock in selection and sign-off.
- **State and cooldown:** responded_state.py consolidates dedupe and cooldown; shared by listener and check_and_respond.
- **Scenario and test framework:** Jarvey scenarios, run_jarvey_benchmark.py, compose_reply.py; 100+ unit tests.

---

## 6. Recommended focus for improvement plan

1. **Grounding and message selection:** Ensure the listener composes from the **actual** user email (subject + body) and that read_replies returns the **user’s** latest reply (e.g. filter by from-address or thread, and/or prefer unseen messages from user).
2. **Template-first in listener:** Before calling the LLM, run the same choose_response() logic; if a template matches (thanks, priority, weekly digest, default), send that and skip the LLM for that turn.
3. **Dedupe and rate limiting:** Harden dedupe (message_id preferred; clear logging when falling back to hash); verify cooldown and state are applied on every send; add optional “max replies per hour” if needed.
4. **Fill and use Jarvey scorecard:** Run Simple and Semi-simple prompts (compose_reply or live), paste replies into scenario files, score on Scope/Data set/Output/Handoff/Voice, record in agent-aptitude-scorecard.md; repeat after changes to measure improvement.
5. **Prompt and context tweaks:** Strengthen TASK so the model must quote or paraphrase the user’s question before answering; add one example “user says X → Jarvey replies Y” in context if needed; add explicit “Do not reply if the user message is empty or unclear; ask for clarification or send default.”

---

## 7. Customized prompt for improvement plan

Use the following prompt (e.g. in Cursor or a plan doc) to generate a concrete plan to address Jarvey’s shortfalls and improve overall functionality:

---

**START OF PROMPT**

Create a detailed, actionable plan to improve **Jarvey** (the email coordinator bot) and address the shortfalls identified in the evaluation. Reference the evaluation in `docs/agents/data-sets/JARVEY_EVALUATION_REVIEW.md`.

**Context:**
- Jarvey is implemented in `scripts/coordinator-email/`: `coordinator_listener.py` (LLM reply loop), `check_and_respond.py` (templates + state), `read_replies.py` (IMAP latest reply), `compose_reply.py` (one-off compose for testing).
- Training data: `docs/agents/coordinator-instructions.md`, `docs/agents/coordinator-project-context.md`. Scoring rubric: `docs/agents/AGENT_APTITUDE_AND_SCORING.md` §2 and §3.1.1.
- User-reported issues: replies not addressing the user’s questions (“conversation with nobody”); emails every minute (possible dedupe/rate-limit or message-selection bug).
- The listener currently **always** uses the LLM; it does **not** use the template path from check_and_respond (thanks, priority, default).

**Requirements for the plan:**
1. **Grounding and message selection**  
   Propose concrete changes so that (a) the reply is always based on the **actual** user email (subject + body) and (b) read_replies returns the **user’s** latest reply (e.g. filter by sender, exclude our own sent messages, or use thread-id). Cite specific files and functions.

2. **Template-first in the listener**  
   Propose integrating `check_and_respond.choose_response()` into the listener: before calling the LLM, if a template matches (thanks, priority, weekly digest, default, etc), send that template reply and skip the LLM for this message. Ensure state and cooldown are still updated. This reduces LLM dependency for simple intents and guarantees consistent sign-off.

3. **Dedupe and rate limiting**  
   Propose hardening of dedupe (prefer message_id; log when using hash fallback) and verification that cooldown and last_responded_state are applied on every send. Optionally add a max-replies-per-hour cap and document it.

4. **Scoring and feedback loop**  
   Include steps to (a) run the two Jarvey test prompts (Simple: "What's next?", Semi-simple: "Can we prioritize the reports screen and when will it be done?") using compose_reply or the listener, (b) paste replies into the scenario files in `docs/agents/data-sets/jarvey-scenarios/`, (c) score on Scope, Data set, Output, Handoff, Voice, (d) record scores in `docs/agents/data-sets/agent-aptitude-scorecard.md` in the "Email coordinator (Jarvey)" table, and (e) re-run after implementing the plan to compare scores.

5. **Prompt and context improvements**  
   Propose specific edits to the TASK in `coordinator_listener.load_coordinator_system_prompt()` and/or to `coordinator-project-context.md` so that: the model must acknowledge or paraphrase the user’s question before answering; empty or unclear user messages get a safe default or clarification request; and one optional example reply (user says X → Jarvey replies Y) is added to the context if it fits within the character limit.

6. **Testing and regression**  
   Include unit or integration tests (or manual test steps) to verify: template-first branch in the listener (e.g. when body is "Thanks!", template is used and LLM is not called); read_replies returns a message from the user when applicable; dedupe prevents a second reply to the same message_id; cooldown prevents two sends within 2 minutes.

7. **Documentation**  
   Update `scripts/coordinator-email/README.md` and, if needed, `docs/agents/JARVEY_INTELLIGENCE_PLAN.md` to describe the new flow (template-first, then LLM), the message-selection and dedupe behavior, and how to run the Jarvey scorecard prompts and record scores.

**Deliverables:** The plan should list concrete tasks in implementation order, with file paths and function names where relevant. Do not implement the plan in this step—only produce the plan document so it can be reviewed and then executed.

**END OF PROMPT**

---

Use the prompt above to generate a plan that addresses Jarvey’s shortfalls and improves overall functionality. After implementation, re-run the evaluation and scorecard to measure improvement.
