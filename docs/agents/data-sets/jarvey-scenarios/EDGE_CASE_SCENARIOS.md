# Jarvey Edge Case Scenarios

Short check-in phrases that could be misinterpreted as unclear but are valid requests. Jarvey must reply with a brief project update or relevant info, not ask for clarification.

## Problem Pattern

User sends a short phrase like "Tell me something." Jarvey incorrectly replies with "couldn't make out what you need" or "short question" instead of sharing a project update.

**Root cause:** These phrases bypass the unclear template (short_body max 15 chars) and go to the LLM. Without explicit prompt guidance, the model treats them as vague.

---

## Edge Case Scenarios

| Body | Expected | Must NOT contain |
|------|----------|------------------|
| Tell me something | Brief project update (roadmap, recent, next steps) | "couldn't make out", "short question" |
| Tell me anything | Same | Same |
| Share something | Same | Same |
| Give me an update | Same | Same |
| Anything new? | Recent changes or timeline | Same |
| Catch me up | Brief status | Same |
| Fill me in | Brief update | Same |
| What's happening? | Status | Same |
| What's going on? | Status | Same |
| Quick summary | Brief summary | Same |
| What's the latest? | Recent changes | Same |
| Update me | Brief update | Same |
| News? | Brief news | Same |
| Status? | Brief status | Same |

---

## Implementation

- **Prompt:** [coordinator_listener.py](scripts/coordinator-email/coordinator_listener.py) `load_coordinator_system_prompt()` — clear-request list and check-in instruction
- **Whitelist:** [templates/unclear.json](scripts/coordinator-email/templates/unclear.json) — short_body_whitelist for shorter variants
- **Mock:** [mock_llm.py](scripts/coordinator-email/mock_llm.py) — `_MOCK_BENCHMARK_REPLIES` for deterministic test replies
- **Tests:** [test_scenario_regression.py](scripts/coordinator-email/test_scenario_regression.py) — `test_tell_me_something_no_clarification`; [test_edge_case_scenarios.py](scripts/coordinator-email/test_edge_case_scenarios.py) — mock-based edge case tests

---

## Run Tests

```bash
# Mock-based (no LLM)
python scripts/coordinator-email/test_edge_case_scenarios.py

# Real LLM (requires Ollama/OpenAI)
python scripts/coordinator-email/test_scenario_regression.py
```
