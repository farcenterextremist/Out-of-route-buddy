# Jarvey Wiring Errors — Log

Append errors encountered during wiring verification or runtime. See [JARVEY_WIRING_PLAN.md](JARVEY_WIRING_PLAN.md) for the full plan.

---

## Test run log

| Date | Command | Result | Notes |
|------|---------|--------|-------|
| 2026-02-26 | `python run_all_jarvey_tests.py` | 170 passed, 1 skipped, exit 0 | Unit tests; test_context_loader, test_structured_output added; truncate tests fixed |

---

## Expected observations (not errors)

These appear in test output but are **expected** — do not treat as failures:

| Observation | Reason | Source |
|-------------|--------|--------|
| `(pytest not installed; using unittest)` | Informational; run_all_jarvey_tests falls back to unittest when pytest missing | run_all_jarvey_tests.py |
| `Error composing reply: LLM error` | Test intentionally mocks LLM failure to verify error handling; test passes | test_coordinator_listener.test_compose_error_does_not_send |
| `OK (skipped=1)` | One test skips when preconditions unmet (e.g. IMAP not configured, .env incomplete) | test_jarvey_wiring or test_scenario_regression |
| PowerShell stderr "RemoteException" for pytest message | PowerShell treats Python stderr as error; exit code is still 0 | Windows/PowerShell |

---

## Error entries

| Date | Component | Error | Resolution |
|------|-----------|-------|------------|
| 2026-02-26 | test_context_loader | test_truncates_at_sentence_end FAIL: 'First sentence.' not in result | Tests used wrong max_chars. reserve=len(TRUNCATE_SUFFIX)+5 (~26); cut_at must include ". " at index 15. Fixed: max_chars=45 (was 50). |
| 2026-02-26 | test_context_loader | test_truncates_at_space FAIL: 'word3' in result | With max_chars=45, cut_at=19 included "word3". Fixed: max_chars=34 so cut_at=8, truncates before word3. |

---

## How to avoid repeating errors

1. **Before reporting a failure:** Check "Expected observations" above. `Error composing reply: LLM error` during tests is normal.
2. **Skipped tests:** `test_read_replies_contract_four_values` skips when IMAP not configured. `test_validate_config_email_only` skips when COORDINATOR_EMAIL_TO unset. `test_scenario_regression` skips when no LLM (OLLAMA_URL/OPENAI_API_KEY). `test_openai_empty_content_raises` skips when openai package not installed. These are intentional.
3. **LLM/Benchmark:** Use `--llm` or `--all` to run LLM-dependent tests. They require API key or Ollama.
4. **PowerShell:** If exit code is 0, ignore stderr noise from Python's "(pytest not installed)".
5. **_truncate_at_boundary tests:** When adding or changing truncate tests, account for reserve=len(TRUNCATE_SUFFIX)+5. cut_at = max_chars - reserve. The search range is text[0:cut_at+1]. Ensure max_chars is small enough to trigger truncation (len(text) > max_chars) but large enough that cut_at includes the expected boundary.
