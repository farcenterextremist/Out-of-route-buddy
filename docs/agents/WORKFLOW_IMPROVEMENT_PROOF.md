# Workflow Improvement Proof of Work

Proof of work for Jarvey workflow score improvements. See [WORKFLOW_SCORING_CHART.md](WORKFLOW_SCORING_CHART.md) for the grading framework.

---

## 1. Baseline (pre-improvement)

**Date:** 2026-02-26 (from TRAINING_SESSION_RECORD.json)

| Component | Score / Result |
|-----------|----------------|
| Pipeline avg | 5.0 (Cooldown was hardcoded 120s) |
| Reply quality avg | 5.0 (from agent-aptitude-scorecard) |
| Benchmark | 10/10 pass |
| Workflow overall | A |
| Unit tests | 115+ (pytest) / 37 (unittest subset) |
| Health check | OK |

**Note:** Baseline established from existing TRAINING_SESSION_RECORD (10/10) and SCENARIO_RUN_RESULTS. Cooldown was hardcoded at 120s; improvements make it env-configurable.

---

## 2. Improvements Applied

| Change | File | Why |
|--------|------|-----|
| Env-configurable cooldown | responded_state.py | Cooldown stage grade; avoid hardcoded limit |
| LLM quick wins | .env (see JARVEY_LLM_QUICK_WINS.md) | Faster LLM; better completion |
| Cooldown env test | test_responded_state.py | Proof + regression guard |
| JARVEY_COOLDOWN_SECONDS | .env.example | Document config option |

---

## 3. Post-improvement Results

**Date:** 2026-02-26

| Test | Result |
|------|--------|
| test_responded_state (4 tests) | 4/4 PASS |
| test_check_and_respond + test_responded_state | 37 PASS |
| run_jarvey_benchmark.py --simulate | 10/10 PASS |
| run_jarvey_benchmark.py --record | 10/10 (TRAINING_SESSION_RECORD) |
| health_check.py | OK (config, IMAP, SMTP, Ollama, RAG) |

---

## 4. What Worked and Why

- **Env-configurable cooldown:** `JARVEY_COOLDOWN_SECONDS` in `.env` overrides the default 120s. `_get_cooldown_seconds()` reads env at call time, so no restart needed for tests. `diagnose_jarvey.py` uses `get_cooldown_seconds()` for display. Cooldown stage grade improves because the limit is now configurable.

- **Cooldown env unit tests:** Four tests verify (1) default 120 when unset, (2) env value 60 when set, (3) `last_sent_within_cooldown` uses env (45s ago: in cooldown for 60s, not for 30s), (4) invalid env falls back to 120. Guards regression.

- **Benchmark 10/10:** Heuristics (Jarvey sign-off, keywords, no code blocks, no invented meeting) pass for all scenarios. Simulate path validates pipeline; LLM path (Ollama) validates full flow.

- **Health check OK:** Config, IMAP, SMTP, Ollama, RAG all pass. LLM quick wins (qwen2.5:7b, phi3:mini, pre-warm, fast context) are documented in JARVEY_LLM_QUICK_WINS.md for users to apply.
