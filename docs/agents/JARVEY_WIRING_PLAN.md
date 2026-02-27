# Jarvey Wiring Plan — Framework Verification & Training Session

**Purpose:** Ensure every aspect of Jarvey's framework is correctly wired, build tests to verify wiring, document errors going forward, and run a training data quality session.

**Owner:** Coordinator / DevOps  
**Related:** [JARVEY_INTELLIGENCE_PLAN.md](JARVEY_INTELLIGENCE_PLAN.md), [TESTING_SUITE.md](TESTING_SUITE.md), [COORDINATOR_EMAIL_LISTENER_RUNBOOK.md](COORDINATOR_EMAIL_LISTENER_RUNBOOK.md), [JARVEY_WIRING_ERRORS.md](JARVEY_WIRING_ERRORS.md), [JARVEY_DATA_ACCESS_TESTS.md](JARVEY_DATA_ACCESS_TESTS.md)

---

## Part 1 — Wiring Diagram (Full Framework)

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                         JARVEY FRAMEWORK — DATA FLOW                              │
└─────────────────────────────────────────────────────────────────────────────────┘

  [User Email] ──► read_replies.py ──► (subject, body, date, message_id)
       │                    │
       │                    └──► last_reply.txt (optional)
       │
       ▼
  coordinator_listener.run_once()
       │
       ├──► config_schema.validate_config() ──► .env (SMTP, IMAP, LLM)
       │
       ├──► responded_state ──► last_responded_state.txt, last_sent_timestamp.txt
       │         │                    │
       │         ├── load_last_responded_id()  [dedupe]
       │         ├── last_sent_within_cooldown() [rate limit]
       │         └── save_responded_id(), write_sent_timestamp()
       │
       ├──► llm_backoff.is_circuit_open() ──► llm_backoff_state.json
       │
       ├──► template_registry.choose_response_with_confidence()
       │         │
       │         └──► templates/*.json (thanks, unclear, roadmap, version, etc.)
       │                   │
       │                   └── context_loader fetchers (roadmap, version, timeline)
       │
       ├──► context_loader
       │         │
       │         ├── load_context_for_user_message(subject, body)
       │         │     ├── coordinator-project-context.md
       │         │     ├── JARVEY_PROJECT_BRAIN.md
       │         │     ├── build_project_index()
       │         │     ├── KNOWN_TRUTHS (condensed)
       │         │     ├── detect_intents() ──► intents/intents.json
       │         │     ├── extract_entities() ──► entity path hints
       │         │     └── on-demand snippets per intent
       │         │
       │         ├── conversation_memory (if JARVEY_CONVERSATION_MEMORY=1)
       │         └── get_capability_options_text() ──► jarvey_capability_menu.json
       │
       ├──► compose_reply() [coordinator_listener]
       │         │
       │         ├── load_coordinator_system_prompt() ──► coordinator-instructions.md
       │         ├── LLM backend: OpenAI | Ollama | Anthropic | transformers
       │         └── _ensure_jarvey_signoff()
       │
       ├──► structured_output
       │         │
       │         ├── parse_structured_reply() ──► JSON action block
       │         ├── execute_action() ──► save_note, send_digest, clarify, assign_work, add_to_timeline, offer_options
       │         └── strip_structured_from_reply()
       │
       ├──► send_email.send() ──► SMTP
       │
       ├──► conversation_memory.append_exchange() (if enabled)
       └──► jarvey_choices.log_user_choice()

  [Startup] ──► send_opener.send_capability_opener() ──► options_menu template
```

---

## Part 2 — Component Wiring Checklist

### 2.1 Entry & Config

| Component | Path | Wired To | Verify |
|-----------|------|----------|--------|
| coordinator_listener | `coordinator_listener.py` | read_replies, send_email, config_schema | `python coordinator_listener.py` starts without config error |
| config_schema | `config_schema.py` | .env, EMAIL_REQUIRED, IMAP_OPTIONAL, LLM_KEYS | `python config_schema.py` exits 0 |
| .env | `scripts/coordinator-email/.env` | All scripts | Copy from .env.example; fill SMTP, IMAP, LLM |

### 2.2 Email I/O

| Component | Path | Wired To | Verify |
|-----------|------|----------|--------|
| read_replies | `read_replies.py` | IMAP, retry_utils, last_reply.txt | Returns (subject, body, date, message_id); test_read_replies |
| send_email | `send_email.py` | SMTP, retry_utils, COORDINATOR_DRY_RUN | test_send_email; dry run works |
| agent_email | `agent_email.py` | read_replies, send_email | `python agent_email.py read --json` |

### 2.3 State & Rate Limiting

| Component | Path | Wired To | Verify |
|-----------|------|----------|--------|
| responded_state | `responded_state.py` | last_responded_state.txt, last_sent_timestamp.txt, replies_sent_timestamps.txt | test_responded_state |
| llm_backoff | `llm_backoff.py` | llm_backoff_state.json | record_failure/record_success; is_circuit_open |

### 2.4 Context & Intent

| Component | Path | Wired To | Verify |
|-----------|------|----------|--------|
| context_loader | `context_loader.py` | coordinator-project-context, JARVEY_PROJECT_BRAIN, intents.json, KNOWN_TRUTHS, project_timeline.json | test_context_loader; load_context_for_user_message returns non-empty |
| intents | `intents/intents.json` | context_loader.load_snippet | detect_intents matches keywords |
| JARVEY_PROJECT_BRAIN | `docs/agents/JARVEY_PROJECT_BRAIN.md` | context_loader._build_expanded_base | Brain appears in context |

### 2.5 Templates

| Component | Path | Wired To | Verify |
|-----------|------|----------|--------|
| template_registry | `template_registry.py` | templates/*.json, context_loader fetchers | choose_response returns (subj, body, key) |
| templates | `templates/*.json` | thanks, unclear, roadmap, version, recent, options_menu, etc. | All JSON valid; keywords match |

### 2.6 LLM & Compose

| Component | Path | Wired To | Verify |
|-----------|------|----------|--------|
| compose_reply | `coordinator_listener.compose_reply` | load_coordinator_system_prompt, OpenAI/Ollama/Anthropic | compose_reply returns non-empty string |
| coordinator-instructions | `docs/agents/coordinator-instructions.md` | load_coordinator_system_prompt | File exists; prompt includes instructions |
| mock_llm | `mock_llm.py` | JARVEY_BENCHMARK_SIMULATE=1 | test_mock_llm; benchmark --simulate |

### 2.7 Structured Output

| Component | Path | Wired To | Verify |
|-----------|------|----------|--------|
| structured_output | `structured_output.py` | parse_structured_reply, execute_action, context_loader.append_to_notes, append_to_timeline | test_structured_output |
| save_note | structured_output | context_loader.append_to_notes → EMAIL_NOTES.md | Action appends to file |
| add_to_timeline | structured_output | context_loader.append_to_timeline → project_timeline.json | Action appends to file |

### 2.8 Optional / RAG

| Component | Path | Wired To | Verify |
|-----------|------|----------|--------|
| jarvey_rag | `jarvey_rag.py` | JARVEY_RAG_ENABLED, jarvey_embeddings.json | build_rag_index; search returns chunks |
| conversation_memory | `conversation_memory.py` | JARVEY_CONVERSATION_MEMORY | append_exchange; load_history |
| jarvey_choices | `jarvey_choices.py` | jarvey_choices.json | log_user_choice |
| send_opener | `send_opener.py` | JARVEY_SEND_OPENER_ON_STARTUP | send_capability_opener |

---

## Part 3 — Wiring Verification Tests

### 3.1 Existing Tests (run_all_jarvey_tests.py)

| Test Module | Covers | Command |
|--------------|--------|---------|
| test_check_and_respond | Template selection, state, dedupe, cooldown | `python run_all_jarvey_tests.py` |
| test_read_replies | get_body, _normalize_email, _is_agent_sent | — |
| test_send_email | send dry run | — |
| test_coordinator_listener | Strip quoted, sign-off, compose_reply, dedupe | — |
| test_mock_llm | compose_reply_mock | — |
| test_responded_state | load/save, cooldown | — |
| test_edge_case_scenarios | "Tell me something", "Update me", etc. | — |
| test_scenario_regression | LLM scenarios (requires API) | `--llm` |
| run_jarvey_benchmark | 10 scenarios | `--benchmark` |

### 3.2 Data–Model Link Tests (test_data_model_link.py)

Verifies that quality data reaches the model's prompt. No LLM required.

| Test | Verifies |
|------|----------|
| Roadmap data in prompt | "What's next?" → prompt contains Auto drive, Reports, History from ROADMAP.md |
| Recovery data in prompt | "How does trip recovery work?" → prompt contains TripCrashRecoveryManager, KNOWN_TRUTHS |
| Entity path in prompt | "Where is TripInputViewModel?" → prompt contains path (.kt, app/, viewmodel) |
| Recent/timeline in prompt | "What changed recently?" → prompt contains timeline or curated instruction |
| Base context always present | Every prompt contains Jarvey boundaries, brain/SSOT |
| Version intent | "What version?" → prompt contains version context |

### 3.3 New Wiring Tests (test_jarvey_wiring.py)

Create `test_jarvey_wiring.py` to verify:

1. **Config wiring:** validate_config("listener") does not raise when .env has required keys.
2. **Context wiring:** load_context_for_user_message("Re: X", "What's next?") contains "roadmap" or ROADMAP.
3. **Brain wiring:** load_context_for_user_message contains "JARVEY_PROJECT_BRAIN" or "Intent map" or "Entity → Location".
4. **Intent wiring:** detect_intents("Re: X", "what's next") includes "roadmap".
5. **Template wiring:** choose_response("Re: X", "Thanks!") returns thanks template.
6. **State wiring:** save_responded_id + load_last_responded_id round-trip.
7. **Structured output wiring:** parse_structured_reply finds JSON block; execute_action("save_note", {...}) appends to EMAIL_NOTES.
8. **Read contract:** read_replies returns exactly 4 values (subject, body, date, message_id).

### 3.3 Run Commands

```bash
# 1. Unit tests (no LLM)
cd scripts/coordinator-email
python run_all_jarvey_tests.py

# 2. Config validation
python config_schema.py
python config_schema.py --email-only

# 3. Compose dry run (if LLM configured)
python compose_reply.py "Re: OutOfRouteBuddy" "What's next?" --out /tmp/reply.txt

# 4. Benchmark (simulate, no LLM)
python run_jarvey_benchmark.py --simulate

# 5. Full benchmark (LLM)
python run_jarvey_benchmark.py --record --remove-failures
```

---

## Part 4 — Error Documentation (Going Forward)

### 4.1 Error Log Location

| Error Type | Where to Document | Format |
|------------|-------------------|--------|
| Config errors | config_schema validation | Print to stderr; exit 1 |
| Inbox/IMAP errors | read_replies | Raise; coordinator_listener catches, logs |
| LLM errors | llm_backoff.record_failure | llm_backoff_state.json |
| Compose errors | coordinator_listener | stderr + optional jarvey_workflow.log |
| Send errors | send_email | Raise; coordinator_listener catches |

### 4.2 Error Log File

Create `docs/agents/JARVEY_WIRING_ERRORS.md` (append-only):

```markdown
# Jarvey Wiring Errors — Log

| Date | Component | Error | Resolution |
|------|-----------|-------|------------|
| YYYY-MM-DD | read_replies | IMAP connection refused | Check COORDINATOR_IMAP_HOST |
```

### 4.3 Diagnostic Scripts

| Script | Purpose |
|--------|---------|
| diagnose_jarvey.py | Config, state, cooldown, circuit |
| diagnose_inbox.py | IMAP connection, message parsing |
| trace_jarvey_workflow.py | Step-by-step trace (dry run) |
| health_check.py | Quick health check |

---

## Part 5 — Training Data Quality Session

### 5.1 Pre-Session

1. **Audit:** `python audit_jarvey_training_data.py` (dry run)
2. **Remove failures:** `python run_jarvey_benchmark.py --simulate --remove-failures` (cleans failed outputs)
3. **Apply audit:** `python audit_jarvey_training_data.py --apply` (if out-of-scope found)

### 5.2 Training Session

1. **Run benchmark (simulate):** `python run_jarvey_benchmark.py --simulate --record`
   - Validates pipeline without LLM
   - Writes TRAINING_SESSION_RECORD.json

2. **Run benchmark (LLM):** `python run_jarvey_benchmark.py --record --remove-failures`
   - Uses real LLM (Ollama/OpenAI)
   - Moves failed outputs to removed/
   - Appends to TRAINING_DATA_REMOVED.md

3. **Record:** TRAINING_SESSION_RECORD.json updated with pass/fail, output paths

### 5.3 Post-Session

1. **Run all tests:** `python run_all_jarvey_tests.py`
2. **Optional:** `python send_jarvey_training_report.py` (email summary)

---

## Part 6 — Execution Plan (Ordered Steps)

| Step | Action | Success Criteria |
|------|--------|------------------|
| 1 | Create test_jarvey_wiring.py with 8 wiring tests | Tests exist; run passes |
| 2 | Create JARVEY_WIRING_ERRORS.md (empty table) | File exists |
| 3 | Run wiring verification: `python run_all_jarvey_tests.py` | All unit tests pass |
| 4 | Run config_schema.py | Config OK |
| 5 | Run audit_jarvey_training_data.py | No out-of-scope or apply if needed |
| 6 | Run benchmark --simulate --remove-failures --record | 10/10 pass; record written |
| 7 | Run benchmark --record --remove-failures (if LLM configured) | Or skip |
| 8 | Run run_all_jarvey_tests.py again | All pass |
| 9 | Document any errors in JARVEY_WIRING_ERRORS.md | Errors logged |

---

## Part 7 — Quick Reference

| Need | Command / Path |
|------|----------------|
| Start Jarvey | `python scripts/coordinator-email/coordinator_listener.py` |
| Validate config | `python scripts/coordinator-email/config_schema.py` |
| Run all tests | `python scripts/coordinator-email/run_all_jarvey_tests.py` |
| Benchmark (simulate) | `python scripts/coordinator-email/run_jarvey_benchmark.py --simulate` |
| Audit training data | `python scripts/coordinator-email/audit_jarvey_training_data.py` |
| Diagnose | `python scripts/coordinator-email/diagnose_jarvey.py` |
| Trace workflow | `python scripts/coordinator-email/trace_jarvey_workflow.py` |

---

*When wiring changes, re-run Part 6 steps and update this plan.*
