# Jarvey Component Inventory, Scores, and Plan-Mode Prompt

**Purpose:** Comprehensive component breakdown, scoring, and a customized prompt for Cursor Plan mode to improve Jarvey.

**Created:** 2026-02-26

---

## 1. Main Components and Sub-Components

### 1.1 Inbox & Message Pipeline

| Component | Path | Sub-Components | Role |
|-----------|------|----------------|------|
| **read_replies** | `read_replies.py` | IMAP connect, folder select, message fetch, from-address filter, X-OutOfRouteBuddy-Sent skip, subject filter (case-insensitive), get_body (multipart), _normalize_email | Read user's latest reply from inbox |
| **_strip_quoted_content** | `coordinator_listener.py` / `check_and_respond.py` | Gmail "On ... wrote:", Outlook "-----Original Message-----", forwarded content stripping | Strip quoted/forwarded text so only user's new text is used |
| **Dedupe** | `responded_state.py` | message_id check, content hash fallback | Prevent double-reply to same message |
| **Cooldown** | `responded_state.py` | last_responded_state.txt, 2-min window | Rate-limit sends |

**Score: 3.8 / 5** — Message selection risk (2–3); same-inbox fix strong (5); dedupe/cooldown adequate (4–5).

---

### 1.2 Response Selection (Template vs LLM)

| Component | Path | Sub-Components | Role |
|-----------|------|----------------|------|
| **Template registry** | `template_registry.py` | Load JSON templates, _match_keywords, _match_keywords_with_confidence, priority order, fetcher resolution (roadmap, version, timeline) | Match user message to fixed templates |
| **choose_response** | `check_and_respond.py` | Template-first logic, confidence threshold (0.7), fallback to LLM | Decide template or LLM path |
| **Templates** | `templates/*.json` | thanks, priority, weekly_digest, default | Predefined reply bodies |
| **coordinator_listener** | `coordinator_listener.py` | Main loop, template-first (if integrated), LLM fallback, load_coordinator_system_prompt | Orchestrate reply flow |

**Score: 4.2 / 5** — Template path strong (4.6); listener template-first integration varies; template quality high.

---

### 1.3 Context & Intent System

| Component | Path | Sub-Components | Role |
|-----------|------|----------------|------|
| **Intent config** | `intents/intents.json` | 25+ intents (roadmap, recovery, version, recent, notes, faq, etc.), keywords, sources | Define intent→context mapping |
| **detect_intents** | `context_loader.py` | Keyword matching on subject+body | Detect which intents apply |
| **load_context_for_user_message** | `context_loader.py` | Base context, intent snippets, special fetchers (version, project_timeline_curated, rag_search, recommend) | Assemble LLM context |
| **get_project_timeline_curated** | `context_loader.py` | project_timeline.json, formatted entries, empty fallback | Curated recent changes |
| **Project index** | `context_loader.py` | File tree, app_structure | Codebase layout for "where is X" |
| **RAG (optional)** | `jarvey_rag.py`, `build_rag_index.py` | Embeddings, vector search, jarvey_embeddings.json | Semantic search over docs |

**Score: 4.0 / 5** — Intent coverage good; project_timeline now populated; RAG optional; context truncation silent.

---

### 1.4 LLM Composition

| Component | Path | Sub-Components | Role |
|-----------|------|----------------|------|
| **compose_reply** | `compose_reply.py` | Load prompt, load context, call LLM (OpenAI/Ollama), structured_output optional | One-off compose for testing |
| **coordinator_listener LLM path** | `coordinator_listener.py` | System prompt, TASK block, user message, context | Compose reply in listener loop |
| **_ensure_jarvey_signoff** | `coordinator_listener.py` | Append "— Jarvey" if missing | Enforce sign-off |
| **Structured output** | `structured_output.py` | Optional JSON schema for reply | Constrain LLM output format |
| **LLM backoff** | `llm_backoff.py` | Retry, timeout handling | Reliability |

**Score: 3.4 / 5** — Grounding (2.5), Hallucination (2.5) critical; Output (3); model variability; sign-off enforced.

---

### 1.5 Sending & State

| Component | Path | Sub-Components | Role |
|-----------|------|----------------|------|
| **send_email** | `send_email.py` | SMTP connect, MIME build, X-OutOfRouteBuddy-Sent header | Send reply |
| **responded_state** | `responded_state.py` | last_responded_state.txt, record_sent, last_sent_within_cooldown, dedupe check | Persist send state |
| **retry_utils** | `retry_utils.py` | Exponential backoff for IMAP/SMTP | Transient failure handling |

**Score: 4.5 / 5** — Sending reliable; state consolidated; retry in place.

---

### 1.6 Configuration & Ops

| Component | Path | Sub-Components | Role |
|-----------|------|----------------|------|
| **config_schema** | `config_schema.py` | Validate .env at startup | Fail fast on missing vars |
| **health_check** | `health_check.py` | IMAP, SMTP, LLM connectivity | Diagnostic |
| **check_and_respond** | `check_and_respond.py` | Standalone scheduled script | Cron/Task Scheduler entry point |
| **agent_email** | `agent_email.py` | read/send subcommands | Cursor agent interface |

**Score: 4.5 / 5** — Config validation; health check; clear entry points.

---

### 1.7 Training & Evaluation

| Component | Path | Sub-Components | Role |
|-----------|------|----------------|------|
| **run_jarvey_benchmark** | `run_jarvey_benchmark.py` | 35+ scenarios, heuristic pass/fail, --record, --remove-failures | Automated scenario testing |
| **send_jarvey_training_report** | `send_jarvey_training_report.py` | Benchmark + audit + email report | Full training session |
| **audit_jarvey_training_data** | `audit_jarvey_training_data.py` | Out-of-scope scan, TRAINING_DATA_REMOVED | Data hygiene |
| **jarvey-scenarios** | `docs/agents/data-sets/jarvey-scenarios/` | 01–35 scenario files, benchmark_output | Scenario catalog |
| **agent-aptitude-scorecard** | `agent-aptitude-scorecard.md` | Email coordinator (Jarvey) table | Score tracking |

**Score: 4.5 / 5** — Strong benchmark framework; scorecard underused; re-score after changes.

---

### 1.8 Instructions & Data

| Component | Path | Sub-Components | Role |
|-----------|------|----------------|------|
| **coordinator-instructions** | `coordinator-instructions.md` | Role, responsibilities, team roster, delegation, do not implement | System prompt base |
| **coordinator-project-context** | `coordinator-project-context.md` | Boundaries, what OOR is, recent changes flow, golden examples | Injected context |
| **JARVEY_FAQ** | `JARVEY_FAQ.md` | Q&A for common questions | Intent fallback |
| **project_timeline.json** | `project_timeline.json` | Curated entries (date, type, title, detail) | Recent changes source |
| **RECENT_CHANGES_DATA** | `RECENT_CHANGES_DATA.md` | Data flow, current summary | Meta + fallback |

**Score: 4.2 / 5** — Instructions clear; project_timeline now populated; golden examples added.

---

### 1.9 Supporting Scripts

| Component | Path | Role |
|-----------|------|------|
| **diagnose_jarvey** | `diagnose_jarvey.py` | Debug Jarvey flow |
| **trace_jarvey_workflow** | `trace_jarvey_workflow.py` | Trace intent + context |
| **send_opener** | `send_opener.py` | Send opener email |
| **send_jarvey_updates** | `send_jarvey_updates.py` | Send updates digest |
| **send_phase_completion_email** | `send_phase_completion_email.py` | Phase completion + append_to_timeline |
| **jarvey_log** | `jarvey_log.py` | Structured logging |
| **conversation_memory** | `conversation_memory.py` | Optional conversation history |
| **jarvey_fetchers** | `jarvey_fetchers.py` | YouTube, web search (recommend intent) |
| **handlers** | `handlers.py` | Pluggable Handler protocol |

**Score: 4.0 / 5** — Good tooling; some scripts optional/underused.

---

## 2. Summary Scores by Area

| Area | Score | Lowest Sub-Score | Priority |
|------|-------|------------------|----------|
| Inbox & Message Pipeline | 3.8 | Message selection (2–3) | High |
| Response Selection (Template vs LLM) | 4.2 | Listener template-first | Medium |
| Context & Intent System | 4.0 | Truncation silent | Medium |
| **LLM Composition** | **3.4** | **Grounding (2.5), Hallucination (2.5)** | **Critical** |
| Sending & State | 4.5 | — | Low |
| Configuration & Ops | 4.5 | — | Low |
| Training & Evaluation | 4.5 | Scorecard underused | Medium |
| Instructions & Data | 4.2 | — | Low |
| Supporting Scripts | 4.0 | — | Low |

**Jarvey Overall: 4.0 / 5** (weighted by critical path)

---

## 3. Dimension Scores (from JARVEY_AGENT_SCORE)

| Dimension | Score | Status |
|----------|-------|--------|
| Scope | 4 | Adequate |
| Data set | 3.5 | Needs improvement |
| Output | 3 | Needs improvement |
| Handoff | 4 | Adequate |
| Voice | 4 | Adequate |
| **Grounding** | **2.5** | **Critical** |
| **Hallucination** | **2.5** | **Critical** |

---

## 4. Customized Prompt for Plan Mode

**Use this prompt in Cursor Plan mode to generate an actionable improvement plan for Jarvey.**

---

**START OF PLAN-MODE PROMPT**

---

You are in **Plan mode**. Create a detailed, actionable plan to improve **Jarvey** (the email coordinator bot for OutOfRouteBuddy).

**Input context:**
- Component inventory and scores: `docs/agents/JARVEY_COMPONENT_SCORE_AND_PLAN_PROMPT.md`
- Evaluation: `docs/agents/data-sets/JARVEY_EVALUATION_REVIEW.md`
- Agent score: `docs/agents/data-sets/JARVEY_AGENT_SCORE.md`
- Architecture: `docs/agents/JARVEY_ARCHITECTURE.md`
- Functionality gaps: `docs/agents/JARVEY_FUNCTIONALITY_EVALUATION.md`

**Critical shortfalls (address first):**
1. **Grounding (2.5/5):** Replies not clearly tied to user's actual message; "conversation with nobody" reported.
2. **Hallucination (2.5/5):** Model invents dates, phases, timelines; "recent changes" sometimes returned raw git instead of curated timeline (partially fixed).
3. **Output (3/5):** Multi-part questions not answered point-by-point; generic replies.
4. **Message selection (2–3/5):** read_replies "latest in 50" may be ambiguous; filter behavior needs hardening.

**Requirements for the plan:**

1. **Grounding**
   - Propose concrete changes so every reply reflects the user's subject + body.
   - Add TASK rule: "Your first sentence must acknowledge or paraphrase the user's question."
   - Add 1–2 golden examples (user says X → Jarvey acknowledges X, then answers).
   - Cite files: `coordinator_listener.py`, `coordinator-instructions.md`, `coordinator-project-context.md`.

2. **Anti-hallucination**
   - Ensure "recent" intent uses only `project_timeline_curated` (no raw git).
   - Add prompt rule: "If the answer is not in the provided context, say 'I don't have that in my context' or ask the user to clarify."
   - Do not invent dates, phases, or timelines.
   - Verify `context_loader.py` source for "recent" is `project_timeline_curated`.

3. **Output quality**
   - Strengthen TASK: "Address each question or point in order. Use bullets for multi-part questions."
   - Add negative example: do not give one generic paragraph when user asked three things.
   - Consider structured_output for multi-part replies.

4. **Message selection**
   - Document read_replies filter behavior (from-address, X-OutOfRouteBuddy-Sent, subject).
   - Propose unit tests for `_normalize_email`, `get_body`, edge cases.
   - Consider thread-id or "latest from user" logic if ambiguous.

5. **Template-first verification**
   - Confirm coordinator_listener uses `choose_response()` before LLM.
   - If not, add integration: when template matches (thanks, priority, weekly_digest, default), send template and skip LLM.
   - Ensure state and cooldown updated on template path.

6. **Scoring and regression**
   - Run `run_jarvey_benchmark.py` (scenarios 01, 02, 05, 06, 07, 08, 10).
   - Run compose_reply for Simple ("What's next?") and Semi-simple ("Can we prioritize reports and when will it be done?").
   - Record scores in `agent-aptitude-scorecard.md` "Email coordinator (Jarvey)" table.
   - Re-run after plan implementation to measure improvement.

7. **Documentation**
   - Update `scripts/coordinator-email/README.md` with flow (template-first → LLM), message selection, dedupe.
   - Update `JARVEY_INTELLIGENCE_PLAN.md` if flow changes.

**Deliverables:**
- Ordered list of concrete tasks with file paths and function names.
- Do not implement—produce the plan document only for review and execution.
- Each task should be independently verifiable (test or manual check).

**END OF PLAN-MODE PROMPT**

---

## 5. Quick Reference: Component → File Map

| Main Component | Primary Files |
|----------------|---------------|
| Inbox & Message | `read_replies.py`, `responded_state.py` |
| Response Selection | `template_registry.py`, `check_and_respond.py`, `coordinator_listener.py` |
| Context & Intent | `context_loader.py`, `intents/intents.json`, `jarvey_rag.py` |
| LLM Composition | `compose_reply.py`, `coordinator_listener.py`, `structured_output.py` |
| Sending & State | `send_email.py`, `responded_state.py`, `retry_utils.py` |
| Config & Ops | `config_schema.py`, `health_check.py`, `agent_email.py` |
| Training | `run_jarvey_benchmark.py`, `send_jarvey_training_report.py`, `audit_jarvey_training_data.py` |
| Instructions | `coordinator-instructions.md`, `coordinator-project-context.md`, `JARVEY_FAQ.md` |
