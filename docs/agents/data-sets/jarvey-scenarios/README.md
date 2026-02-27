# Jarvey scenario files

One file per scenario for evaluating Jarvey's email replies. See [docs/agents/JARVEY_INTELLIGENCE_PLAN.md](../../JARVEY_INTELLIGENCE_PLAN.md) for the test pyramid, runbook, and feedback loop.

**Run results and explanation:** [SCENARIO_RUN_RESULTS.md](SCENARIO_RUN_RESULTS.md) — what was run, scenario 3 (template) verification, commands for LLM scenarios, and why the system works.

**Edge case scenarios:** [EDGE_CASE_SCENARIOS.md](EDGE_CASE_SCENARIOS.md) — short check-in phrases (e.g. "Tell me something", "Update me") that must get project updates, not clarification.

**Run:** For each scenario (1, 2, 4, 5, 6), run `python scripts/coordinator-email/compose_reply.py "Re: OutOfRouteBuddy" "<body>"` (or use `--out <path>`), then paste the reply into the **Response** section of the corresponding `.md` file. Score on the five dimensions and document any fixes in **Fix notes**.

Scenario 3 (thanks) is template-only; use unit tests in `test_check_and_respond.py` to verify.

---

## Training session data

| File / folder | Purpose |
|---------------|---------|
| **TRAINING_SESSION_RECORD.json** | Per-session record: timestamp, scenario results (pass/fail, reason), output paths. Written by `run_jarvey_benchmark.py --record`. |
| **TRAINING_DATA_REMOVED.md** | Log of removed data: failed benchmark outputs and out-of-scope content, with reasons. Appended by benchmark `--remove-failures` and `audit_jarvey_training_data.py --apply`. |
| **benchmark_output/removed/** | Failed benchmark outputs moved here (suffix `_FAIL_<reason>.txt`). |
| **benchmark_logs/** | Live run logs: `benchmark_run_YYYY-MM-DD_HH-MM-SS_simulate|llm.log`. Use `--live` for streaming output. |

---

## Conventions / Adding a scenario

- **Naming:** Use `NN_description.md` with zero-padded number (e.g. `07_`, `08_` for future scenarios). Current: 01–06.
- **Section layout:** Keep the same structure in each scenario file: Prompt type, Instructions/context, Prompt (user email), Look for (scoring), Response, Score (optional), Fix notes (optional). Align dimension names with [AGENT_APTITUDE_AND_SCORING.md](../../AGENT_APTITUDE_AND_SCORING.md) §3.1.1 (Scope, Data set, Output, Handoff, Voice).
