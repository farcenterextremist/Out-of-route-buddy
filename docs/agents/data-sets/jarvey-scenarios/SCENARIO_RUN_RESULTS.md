# Jarvey scenario run results and why it works

This document records what was run, the results, and why the Jarvey system works.

---

## 1. What was run

### Unit tests (all scenarios that use the template path)

**Command:** `python scripts/coordinator-email/test_check_and_respond.py -v`

**Result:** **15 tests passed.**

| Test / scenario coverage | Result |
|--------------------------|--------|
| Template selection: weekly_digest, thanks, priority, default | PASS |
| All template bodies contain "— Jarvey" (regression test) | PASS |
| Thanks template for "Thanks, that works!" (Scenario 3) | PASS (`test_thanks`, `test_all_templates_sign_as_jarvey`) |
| State: load/save last_responded_id | PASS |
| read_replies contract (4 values) | PASS |

**Conclusion:** Scenario **3 (Thanks)** is fully verified. The template path returns the correct "thanks" body with sign-off "— Jarvey" for inputs like "Thanks, that works!".

### LLM scenarios (1, 2, 4, 5, 6, 7, 8, 9)

These require an LLM (OpenAI API key or Ollama running). To run them yourself:

```bash
# From repo root; ensure .env has COORDINATOR_LISTENER_OPENAI_API_KEY or COORDINATOR_LISTENER_OLLAMA_URL

# Scenario 1 — Simple
python scripts/coordinator-email/compose_reply.py "Re: OutOfRouteBuddy" "What's next?"

# Scenario 2 — Semi-simple
python scripts/coordinator-email/compose_reply.py "Re: OutOfRouteBuddy" "Can we prioritize the reports screen and when will it be done?"

# Scenario 4 — Multi-question
python scripts/coordinator-email/compose_reply.py "Re: OutOfRouteBuddy" "What's next? Also, who owns the emulator?"

# Scenario 5 — No code
python scripts/coordinator-email/compose_reply.py "Re: OutOfRouteBuddy" "Write me a function to export trips to CSV."

# Scenario 6 — Unclear
python scripts/coordinator-email/compose_reply.py "Re: OutOfRouteBuddy" "Something is broken."

# Scenario 7 — Recovery (intent-aware: KNOWN_TRUTHS snippet)
python scripts/coordinator-email/compose_reply.py "Re: OutOfRouteBuddy" "How does trip recovery work?"

# Scenario 8 — Version (intent-aware: app version from build.gradle.kts)
python scripts/coordinator-email/compose_reply.py "Re: OutOfRouteBuddy" "What's the latest app version?"

# Scenario 9 — Where is X defined (project index)
python scripts/coordinator-email/compose_reply.py "Re: OutOfRouteBuddy" "Where is TripInputViewModel defined?"
```

Paste each output into the **Response** section of the corresponding file in `docs/agents/data-sets/jarvey-scenarios/` (01–09), then score on the five dimensions (Scope, Data set, Output, Handoff, Voice) and add any Fix notes.

---

## 2. Scenario 3 — Verified response (template path)

For **"Thanks, that works!"** the system uses the **template** path (no LLM). The returned body is:

```
Hi,

Thanks for getting back to us. We'll continue from here in the next session or by email.

— Jarvey
```

**Why this works:** `check_and_respond.choose_response()` matches the combined subject+body against regex patterns. The pattern `thank|thanks|got it|sounds good|...` matches "thanks" in the body, so the "thanks" template is selected. All templates are required by tests to include "— Jarvey", so the sign-off is correct.

---

## 3. Why the Jarvey system works

### Two reply paths

1. **Template path** (used by `check_and_respond.py` and by the listener when it uses templates):  
   User email is matched against keywords (e.g. thanks, weekly board digest, priority, what next). If a match is found, a fixed reply body is sent (all signed "— Jarvey"). No LLM call. Fast and deterministic.

2. **LLM path** (used by `coordinator_listener.py` when no template matches):  
   The listener builds a system prompt from:
   - `coordinator-instructions.md` (Jarvey’s role, handoffs, team)
   - **Intent-aware context** (`context_loader.py`): base `coordinator-project-context.md` plus on-demand snippets by user message (e.g. "What's next?" → ROADMAP; "How does recovery work?" → KNOWN_TRUTHS; "What version?" → app version from build.gradle.kts)
   and a TASK block that tells the model to: acknowledge the user, answer each point, give next steps, and sign as Jarvey. The user message is the email subject + body. The LLM returns the reply body, which is sent via the same `send()` function.

### Single state and cooldown

- **last_responded_state.txt** stores the last message_id (or content hash) we replied to. The listener and `check_and_respond.py` both use this file, so we never reply twice to the same message.
- **Cooldown** (e.g. 2 minutes): we do not send more than one email per cooldown period, so we avoid spamming even if something misbehaves.

### Training data in one place

- **Instructions:** `coordinator-instructions.md` — who Jarvey is, when to hand off to Human-in-the-Loop, no code.
- **Context:** `coordinator-project-context.md` — what the project is, what’s next (Auto drive, Reports, History), key doc names. Injected into the LLM system prompt so replies are project-aware.

### Structure in the prompt

The TASK in the listener prompt explicitly asks for: (1) Acknowledge — one short sentence reflecting the user’s question; (2) Answer — address each point, use bullets if multiple; (3) Next steps / handoff — who will do what; sign as "— Jarvey". That structure makes replies consistent and scorable on Output and Handoff.

### Tests and scenarios

- **Unit tests** guard template selection and sign-off so a regression (e.g. "OutOfRouteBuddy Team" instead of "Jarvey") is caught.
- **Scenario files** document expected behavior for each prompt type. When you run the LLM scenarios and score them, you can feed low scores back into instructions or context (see JARVEY_INTELLIGENCE_PLAN.md feedback loop).

---

## 4. Summary

| Scenario | Type | Run result | Notes |
|----------|------|------------|--------|
| 1 — Simple | LLM | **PASS** | Ran 2025-02-25; pasted and scored 5.0. |
| 2 — Semi-simple | LLM | **PASS** | Ran 2025-02-25; pasted and scored 5.0. |
| 3 — Thanks | Template | **PASS** | Verified by unit tests; body contains "— Jarvey". |
| 4 — Multi-question | LLM | **PASS** | Ran 2025-02-25; pasted and scored 5.0. |
| 5 — No code | LLM | **PASS** | Fixed: TASK block + golden example; now delegates instead of writing code. |
| 6 — Unclear | LLM | **PASS** | Ran 2025-02-25; pasted and scored 4.8. |

---

## 4a. Benchmark run (automated)

**Command:** `python scripts/coordinator-email/run_jarvey_benchmark.py`

**Latest run:** All 9 scenarios (7–9 added for recovery, version, where-is-X).

| Scenario | Status | Notes |
|----------|--------|-------|
| 1 — Simple | PASS | Heuristic: Jarvey + project context. |
| 2 — Semi-simple | PASS | Heuristic: Jarvey + report/priority; no code block. |
| 3 — Thanks | PASS | Template path; "Thanks for getting back" + "— Jarvey". |
| 4 — Multi-question | PASS | Heuristic: Jarvey + emulator/next. |
| 5 — No code | PASS | Heuristic: no code indicators; TASK block fix applied. |
| 6 — Unclear | PASS | Heuristic: no invented meeting/report. |
| 7 — Recovery | PASS | Heuristic: Jarvey + recovery context. |
| 8 — Version | PASS | Heuristic: Jarvey + version info. |
| 9 — Where is TripInputViewModel | PASS | Heuristic: Jarvey + file path from project index. |

**Fix notes (2025):** Strengthened TASK block in `coordinator_listener.py` with explicit "If the user asks for code... assign to Back-end Engineer. Do not write any code." Added golden example in `coordinator-project-context.md` for no-code scenario. Refined heuristic for Scenario 5 to avoid false positives (e.g. "TripTrackingService class" in delegation).

---

## 4b. Training Run Results

| Date | Pass | Mode | Record |
|------|------|------|--------|
| 2026-02-26 | 10/10 | simulate | [TRAINING_SESSION_RECORD.json](TRAINING_SESSION_RECORD.json) |
| 2026-02-26 | 10/10 | simulate | [TRAINING_SESSION_RECORD.json](TRAINING_SESSION_RECORD.json) (post-cleanup run) |

**Final verification:** `run_all_jarvey_tests.py` — 123 unit tests passed.

---

## 5. Data quality plan — completeness steps (manual)

As part of the [Jarvey data quality improvement plan](JARVEY_DATA_QUALITY_SCORE.md): run the five LLM scenarios (1, 2, 4, 5, 6) with the commands in §1 above when API key or Ollama is available. For each run: paste the reply into the **Response** section of the corresponding scenario file (01, 02, 04, 05, 06), score on Scope / Data set / Output / Handoff / Voice (1–5), fill the **Score** table in that file, then add the Simple and Semi-simple rows to the "Email coordinator (Jarvey)" table in [agent-aptitude-scorecard.md](../agent-aptitude-scorecard.md). After doing that, add the date and a one-line summary here (e.g. "2025-XX-XX: Ran scenarios 1,2,4,5,6; pasted and scored; scorecard updated.").
