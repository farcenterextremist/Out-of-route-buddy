# Jarvey (Email coordinator) — data set

What the Email coordinator bot **consumes** and **produces**. Used for delegation and to point at the right paths. Training files (instructions, context) live in `docs/agents/`; this file is the index for humans and for the data-sets README.

---

## Consumes

- **Injected by listener/compose_reply:** `docs/agents/coordinator-instructions.md`, `docs/agents/coordinator-project-context.md`
- **For delegation and current project facts:** `docs/agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md`, `docs/agents/DATA_SETS_AND_DELEGATION_PLAN.md`
- **User reply input:** `scripts/coordinator-email/read_replies.py` output → `last_reply.txt` (subject, body, date)

**Roadmap/priorities:** Use only when the user asks about "what's next" or future ideas. Paths: `docs/product/ROADMAP.md`, `docs/CRUCIAL_IMPROVEMENTS_TODO.md`, and any "What's next" content in coordinator-project-context.

---

## Produces

- Email reply bodies (via listener or compose_reply)
- State: `last_responded_state.txt`, `last_sent_timestamp.txt` (in `scripts/coordinator-email/`)

---

## Points to

| What | Path | Purpose |
|------|------|---------|
| **Project brain** | [../JARVEY_PROJECT_BRAIN.md](../JARVEY_PROJECT_BRAIN.md) | Bridge from user to project: intent map, entity lookup, golden patterns, structure. |
| Improvement log | [../JARVEY_IMPROVEMENT_LOG.md](../JARVEY_IMPROVEMENT_LOG.md) | Before/after params, fixes, memory. |
| LLM options | [../JARVEY_LLM_OPTIONS.md](../JARVEY_LLM_OPTIONS.md) | Open-source model alternatives, substitution, quality. |
| Scenario catalog | [jarvey-scenarios/](jarvey-scenarios/) | One file per scenario (01–10); run compose_reply, paste reply, score. |
| Scorecard | [agent-aptitude-scorecard.md](agent-aptitude-scorecard.md) | "Email coordinator (Jarvey)" table — Simple and Semi-simple rows. |
| Evaluation | [JARVEY_EVALUATION_REVIEW.md](JARVEY_EVALUATION_REVIEW.md) | Metrics, scores, shortfalls, improvement prompt. |
| Functionality | [../JARVEY_FUNCTIONALITY_EVALUATION.md](../JARVEY_FUNCTIONALITY_EVALUATION.md) | Agent flow, improvement areas, test recommendations. |
| Data quality | [JARVEY_DATA_QUALITY_SCORE.md](JARVEY_DATA_QUALITY_SCORE.md) | Data quality dimensions and re-score prompt. |
| Intelligence plan | [../JARVEY_INTELLIGENCE_PLAN.md](../JARVEY_INTELLIGENCE_PLAN.md) | Test pyramid, runbook, feedback loop. |
| Prompt question bank | [../JARVEY_PROMPT_QUESTION_BANK.md](../JARVEY_PROMPT_QUESTION_BANK.md) | Long word list of similar prompts for intent expansion, tests, future planning. |

---

## Canonical lists

**Data Jarvey uses for general conversation:** Current project facts (what the app is, stack, key implemented behavior per Known Truths); **recent changes** (e.g. from team-parameters, completion emails, or a short "recent work" section in context); **open questions or decisions for the user** (things Human-in-the-Loop or the coordinator would ask). Paths: `docs/agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md`, `docs/agents/team-parameters.md`, `docs/agents/DATA_SETS_AND_DELEGATION_PLAN.md`; use delegation and roles when assigning work.

**When the user asks about "what's next" or priorities:** Then (and only then) reference ROADMAP, next three, or future ideas. Paths: `docs/product/ROADMAP.md`, `docs/CRUCIAL_IMPROVEMENTS_TODO.md`, and any "What's next" content in coordinator-project-context.

**Reply conventions:** Sign as "— Jarvey"; structure: acknowledge → answer → next steps/handoff. See coordinator-instructions and the TASK in `coordinator_listener.load_coordinator_system_prompt()`.
