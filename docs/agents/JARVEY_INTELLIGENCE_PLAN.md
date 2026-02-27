# Jarvey intelligence plan

Improve Jarvey's email reply quality over time via tests, scenarios, scoring, and feedback into training data.

**Evaluation and score:** See [JARVEY_EVALUATION_REVIEW.md](data-sets/JARVEY_EVALUATION_REVIEW.md) for an evaluation of Jarvey on agent/bot metrics (Scope, Data set, Output, Handoff, Voice, Grounding, Reliability), current scores, shortfalls, and a **customized prompt** for creating a plan to address shortfalls and improve functionality.

**Data quality:** See [JARVEY_DATA_QUALITY_SCORE.md](data-sets/JARVEY_DATA_QUALITY_SCORE.md) for a data quality score (structure, clarity, consistency, completeness, discoverability) and a **detailed prompt** for a plan to improve Jarvey’s data quality and keep data files well organized.

---

## 1. Purpose

- **Test Jarvey** in a repeatable way: unit tests (templates, state) and scenario-based evaluation (LLM reply quality).
- **Define scenarios** and document expected behavior; record responses and scores.
- **Feed fixes back** into training data (instructions + project context) so Jarvey improves; document what changed and why.

---

## 2. Test pyramid

| Layer | What | Where |
|-------|------|--------|
| **Unit** | Template selection and state; all templates sign "— Jarvey" | [scripts/coordinator-email/test_check_and_respond.py](../scripts/coordinator-email/test_check_and_respond.py) |
| **Scenarios** | Catalog of user-email → reply; score on 5 dimensions | [docs/agents/data-sets/jarvey-scenarios/](data-sets/jarvey-scenarios/) |
| **Scorecard** | Simple/Semi-simple (and optional scenario rows) | [docs/agents/data-sets/agent-aptitude-scorecard.md](data-sets/agent-aptitude-scorecard.md) — "Email coordinator (Jarvey)" table |

---

## 3. Scenario catalog

| # | Scenario | User email body | Look for |
|---|----------|-----------------|----------|
| 1 | [Simple](data-sets/jarvey-scenarios/01_simple_whats_next.md) | "What's next?" | ROADMAP/next three; concrete next steps; signs as Jarvey. |
| 2 | [Semi-simple](data-sets/jarvey-scenarios/02_semi_simple_prioritize_reports.md) | "Can we prioritize the reports screen and when will it be done?" | Priority + timeline/handoff; project context; signs as Jarvey. |
| 3 | [Thanks (template)](data-sets/jarvey-scenarios/03_thanks_template.md) | "Thanks, that works!" | Template reply (thanks); body contains "— Jarvey". |
| 4 | [Multi-question](data-sets/jarvey-scenarios/04_multi_question.md) | "What's next? Also, who owns the emulator?" | Answers both; structure; handoff; signs as Jarvey. |
| 5 | [No code](data-sets/jarvey-scenarios/05_no_code.md) | "Write me a function to export trips to CSV." | Stays in role; does not write code; assigns to Back-end. |
| 6 | [Unclear](data-sets/jarvey-scenarios/06_unclear.md) | "Something is broken." | Asks for clarification or safe default; does not invent details. |
| 7 | [Recovery](data-sets/jarvey-scenarios/07_recovery.md) | "How does trip recovery work?" | Cites recovery (crash, persistence, TripCrashRecoveryManager); signs as Jarvey. |
| 8 | [Version](data-sets/jarvey-scenarios/08_version.md) | "What's the latest app version?" | Cites version (e.g. 1.0.2); signs as Jarvey. |

---

## 4. How to run

### Listener flow (template-first, then LLM)

When you run `coordinator_listener.py`, the reply flow is: (1) Read inbox via `read_replies.py`. When `COORDINATOR_EMAIL_FROM` is set in `.env`, only messages from **other** addresses (the user) are considered; messages from our own address are skipped. (2) Dedupe by message_id (or content hash if missing); cooldown 2 minutes. (3) **Template-first:** If the user's message matches a template (thanks, priority, weekly digest, or default), Jarvey sends that template reply and the LLM is not called. (4) Otherwise the LLM composes the reply from coordinator-instructions and intent-aware project context (coordinator-project-context plus on-demand snippets by user message; see context_loader.py). (5) State and cooldown are updated after every send. To record Jarvey scores: run the two test prompts (Simple and Semi-simple), paste replies into the scenario files, score on the five dimensions, and fill the "Email coordinator (Jarvey)" table in [agent-aptitude-scorecard.md](data-sets/agent-aptitude-scorecard.md). See [scripts/coordinator-email/README.md](../scripts/coordinator-email/README.md) ("Jarvey scorecard") for the exact steps.

### Benchmark (all 10 scenarios)

Run all scenarios and get automated pass/fail:

```bash
python scripts/coordinator-email/run_jarvey_benchmark.py
```

Writes replies to `docs/agents/data-sets/jarvey-scenarios/benchmark_output/`. See [scripts/coordinator-email/README.md](../scripts/coordinator-email/README.md) ("Jarvey benchmark") and [SCENARIO_RUN_RESULTS.md](data-sets/jarvey-scenarios/SCENARIO_RUN_RESULTS.md) §4a.

### Full training session

Run the complete training session (benchmark + audit + email report):

```bash
python scripts/coordinator-email/send_jarvey_training_report.py
```

**What it does:** (1) Runs `run_jarvey_benchmark.py --record --remove-failures` — records all scenario results, moves failed outputs to `benchmark_output/removed/`, appends to TRAINING_DATA_REMOVED.md. (2) Runs `audit_jarvey_training_data.py --apply` — scans for out-of-scope content, appends findings to TRAINING_DATA_REMOVED.md. (3) Emails the report (pass/fail table, removed data log, Jarvey boundaries reminder) to COORDINATOR_EMAIL_TO.

**Data recording:** `TRAINING_SESSION_RECORD.json` stores timestamp, scenario results, pass/fail counts. `TRAINING_DATA_REMOVED.md` logs all removed data (failed benchmarks + out-of-scope) with reasons.

**Dry-run:** `python send_jarvey_training_report.py --dry-run` builds the report without sending.

### Unit tests

From repo root:

```bash
python scripts/coordinator-email/test_check_and_respond.py -v
```

Or with pytest:

```bash
python -m pytest scripts/coordinator-email/test_check_and_respond.py -v
```

### One-off compose (scenario reply without sending email)

Requires `.env` with `COORDINATOR_LISTENER_OPENAI_API_KEY` (or `OPENAI_API_KEY`) or `COORDINATOR_LISTENER_OLLAMA_URL` (or `OLLAMA_URL`).

From repo root (or from `scripts/coordinator-email/`):

```bash
python scripts/coordinator-email/compose_reply.py "Re: OutOfRouteBuddy" "What's next?"
```

Write reply to a file:

```bash
python scripts/coordinator-email/compose_reply.py "Re: OutOfRouteBuddy" "What's next?" --out docs/agents/data-sets/jarvey-scenarios/01_simple_response.txt
```

### Jarvey scenario runbook

For each scenario (1, 2, 4, 5, 6, 7, 8 — LLM path; 3 is template-only):

1. **Run one-off compose** with the prompt from the scenario file (subject + body), or send the test email to the coordinator inbox and run the listener once.
2. **Paste or save the reply** into the **Response** section of the scenario file in `docs/agents/data-sets/jarvey-scenarios/`.
3. **Score** using the same 5 dimensions as [AGENT_APTITUDE_AND_SCORING.md](AGENT_APTITUDE_AND_SCORING.md) §3.1.1: Scope, Data set, Output, Handoff, Voice (1–5 each). Fill the **Score** table in the scenario file; optionally add the row to the "Email coordinator (Jarvey)" table in [agent-aptitude-scorecard.md](data-sets/agent-aptitude-scorecard.md).
4. **If low score:** Decide training vs data injection (see §5); update [coordinator-instructions.md](coordinator-instructions.md) or [coordinator-project-context.md](coordinator-project-context.md); document in **Fix notes** in the scenario file; re-run the scenario and re-score to confirm.

---

## 5. Feedback loop

After scoring, interpret and update training data:

| Signal | Likely cause | Action |
|--------|----------------|--------|
| **Low Scope (1–2)** | Does code or other roles' work | **Training:** Tighten coordinator-instructions (out-of-scope, no code). |
| **Low Data set (1–2)** | Doesn't cite project context | **Data injection:** Add paths/examples to coordinator-project-context.md. |
| **Low Output (1–2)** | Vague or wrong | **Training + data:** Clarify structure in instructions; optional example snippet in context. |
| **Low Handoff (1–2)** | Unclear who does what | **Training:** In instructions, add "When to hand off to Human-in-the-Loop" and example phrase. |
| **Low Voice (1–2)** | Doesn't sign as Jarvey or wrong tone | **Training:** Reinforce "sign as — Jarvey" and persona in instructions. |

After any change to instructions or context, re-run the affected scenarios and re-score. Document the fix in the scenario file's **Fix notes** section.

---

## 6. Training data locations

| File | Purpose |
|------|---------|
| [coordinator-instructions.md](coordinator-instructions.md) | Role, responsibilities, handoffs, persona (Jarvey). |
| [coordinator-project-context.md](coordinator-project-context.md) | Project facts, ROADMAP, conventions; optional example reply snippets. |
| [scripts/coordinator-email/context_loader.py](../../scripts/coordinator-email/context_loader.py) | Intent-aware context: loads on-demand snippets (KNOWN_TRUTHS, ROADMAP, version, git, etc.) by user message. |

Optional: a separate [jarvey-lessons-learned.md](data-sets/jarvey-lessons-learned.md) as a changelog of fixes if the list grows; reference it from this plan. Prefer updating instructions/context so the model sees fixes directly.

---

## 7. Jarvey data and scripts (index)

Every file that affects Jarvey, with one-line purpose:

| File | Purpose |
|------|---------|
| `docs/agents/coordinator-instructions.md` | Role, handoffs, team; loaded by listener/compose_reply. |
| `docs/agents/coordinator-project-context.md` | Injected project context (~4000 char); loaded by listener/compose_reply. |
| `docs/agents/data-sets/jarvey.md` | Jarvey data set (consumes/produces, canonical lists); index for humans. |
| `docs/agents/data-sets/jarvey-scenarios/` | Scenario catalog (01–08), README, SCENARIO_RUN_RESULTS. |
| `docs/agents/data-sets/agent-aptitude-scorecard.md` | Email coordinator (Jarvey) scorecard rows. |
| `docs/agents/data-sets/JARVEY_EVALUATION_REVIEW.md` | Evaluation, scores, shortfalls, improvement prompt. |
| `docs/agents/data-sets/JARVEY_DATA_QUALITY_SCORE.md` | Data quality dimensions and re-score prompt. |
| `scripts/coordinator-email/coordinator_listener.py` | Listener loop; loads instructions + intent-aware context. |
| `scripts/coordinator-email/context_loader.py` | Intent detection and on-demand snippet loading (KNOWN_TRUTHS, ROADMAP, version, git). |
| `scripts/coordinator-email/compose_reply.py` | One-off compose; uses same prompt as listener. |
| `scripts/coordinator-email/run_jarvey_benchmark.py` | Benchmark runner; runs all 10 scenarios, heuristic pass/fail; --record, --remove-failures. |
| `scripts/coordinator-email/audit_jarvey_training_data.py` | Audit for out-of-scope content; --apply appends to TRAINING_DATA_REMOVED.md. |
| `scripts/coordinator-email/send_jarvey_training_report.py` | Full training session: benchmark + audit + email report. |
| `scripts/coordinator-email/check_and_respond.py` | Template selection and state. |

---

## 8. When to re-score data quality

Re-run the five dimensions in [JARVEY_DATA_QUALITY_SCORE.md](data-sets/JARVEY_DATA_QUALITY_SCORE.md) §2 and update the overall score in §3: **after** adding or removing Jarvey data files, **after** a reorg of data-sets, or **at least once per quarter**. This keeps the data quality score current and measurable.

---

## 9. Optional integration with agent-aptitude

- **Current (Option A):** Jarvey scenarios and scoring are separate from the 26-prompt agent aptitude runbook. This plan and the scorecard "Email coordinator (Jarvey)" table are the single place for Jarvey.
- **Option B (future):** Add a `jarvey_email` entry to [scripts/agent-aptitude/prompt_manifest.json](../scripts/agent-aptitude/prompt_manifest.json) so response files and runbook can be generated for Jarvey alongside the other agents; extend the training priority report to include Jarvey. Start with Option A; add B if you want one runbook for all agents including email.
