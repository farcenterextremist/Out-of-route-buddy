# Jarvey Ultimate Testing Suite

Single entry point and documentation for all Jarvey coordinator-email tests and scenarios.

**Workflow grading:** See [WORKFLOW_SCORING_CHART.md](WORKFLOW_SCORING_CHART.md) for a scoring framework (pipeline stages, reply quality, benchmark pass rate) and how to grade the workflow.

---

## Quick Start

```bash
# Unit tests only (fast, no LLM required)
python scripts/coordinator-email/run_all_jarvey_tests.py

# Full suite: unit + LLM regression + 9-scenario benchmark
python scripts/coordinator-email/run_all_jarvey_tests.py --all

# LLM regression only (unit + anti-hallucination)
python scripts/coordinator-email/run_all_jarvey_tests.py --llm

# Benchmark only (unit + 9 scenarios)
python scripts/coordinator-email/run_all_jarvey_tests.py --benchmark
```

**LLM tests** require `COORDINATOR_LISTENER_OPENAI_API_KEY` or `OLLAMA_URL` in `.env` or environment.

---

## Test Categories

### 1. Unit Tests (no LLM)

| Module | Coverage |
|--------|----------|
| `test_check_and_respond` | Template selection (weekly_digest, thanks, priority, default), state load/save, dedupe hash, empty input early exit, scenario template selection |
| `test_read_replies` | `_normalize_email`, `get_body` (plain, multipart, HTML-only) |
| `test_send_email` | Dry run (param and env) skips SMTP |
| `test_coordinator_listener` | Quoted stripping, sign-off, `_is_agent_sent`, read_replies contract, default→LLM fallthrough, dedupe, cooldown, empty message, context loader (intents, ROADMAP intent-only), project index, timeline |

### 2. LLM Regression Tests

| Module | Coverage |
|--------|----------|
| `test_scenario_regression` | Body "test" → check-in, no meeting/report invented; body "hi"/"ok" → no roadmap hallucination; Jarvey sign-off |

### 3. Benchmark (9 Scenarios)

| # | Name | Type | Body |
|---|------|------|------|
| 1 | simple_whats_next | LLM | "What's next?" |
| 2 | semi_simple_prioritize_reports | LLM | "Can we prioritize the reports screen and when will it be done?" |
| 3 | thanks_template | Template | "Thanks, that works!" |
| 4 | multi_question | LLM | "What's next? Also, who owns the emulator?" |
| 5 | no_code | LLM | "Write me a function to export trips to CSV." |
| 6 | unclear | LLM | "Something is broken." |
| 7 | recovery | LLM | "How does trip recovery work?" |
| 8 | version | LLM | "What's the latest app version?" |
| 9 | where_is_tripinputviewmodel | LLM | "Where is TripInputViewModel defined?" |

Output: `docs/agents/data-sets/jarvey-scenarios/benchmark_output/`

---

## Running Individual Tests

```bash
# From repo root
python -m pytest scripts/coordinator-email/test_check_and_respond.py -v
python -m pytest scripts/coordinator-email/test_coordinator_listener.py -v
python -m pytest scripts/coordinator-email/test_scenario_regression.py -v -s  # -s for LLM output

# Benchmark only
python scripts/coordinator-email/run_jarvey_benchmark.py
python scripts/coordinator-email/run_jarvey_benchmark.py --llm-only  # Skip scenario 3
```

---

## Interpreting Results

| Result | Meaning |
|--------|---------|
| **PASS** | All assertions passed |
| **FAIL** | One or more assertions failed; check traceback |
| **SKIP** | Test skipped (e.g. no LLM configured) |

**Anti-hallucination tests** fail if the reply contains "auto drive", "reports screen", or "history improvements" when the user did not ask for roadmap/priorities (e.g. body "test", "hi", "ok").

---

## Edge Cases Covered

From [JARVEY_EDGE_CASES.md](JARVEY_EDGE_CASES.md):

- **Dedupe:** `message_id` when present; content hash when `message_id` is None (same formula in listener and check_and_respond)
- **Empty input:** `subject=""`, `body=""` → no send, exit 0
- **ROADMAP intent-only:** Base context has no ROADMAP; ROADMAP loaded only when "what's next" or similar
- **Quoted stripping:** Gmail, Outlook, forwarded formats
- **Sign-off:** `— Jarvey` appended when missing
- **Template priority:** weekly_digest → thanks → priority → default

---

## Related Docs

- [JARVEY_EDGE_CASES.md](JARVEY_EDGE_CASES.md) — Edge cases and logic flow
- [JARVEY_IMPROVEMENT_LOG.md](JARVEY_IMPROVEMENT_LOG.md) — Before/after parameters, fixes
- [jarvey-scenarios/README.md](data-sets/jarvey-scenarios/README.md) — Scenario files and scoring
