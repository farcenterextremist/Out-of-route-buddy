# Jarvey Data Access Tests — Read, Comprehend, Location

**Purpose:** Verify Jarvey can reach its data, read it, and that the data is in the correct locations with expected structure.

**Test module:** `scripts/coordinator-email/test_jarvey_data_access.py`

---

## 1. What is tested

| Category | Tests | What they verify |
|----------|-------|------------------|
| **Location** | 10 tests | Each data file exists at the path Jarvey expects |
| **Read** | 9 tests | Jarvey can open and read each file (no permission/encoding errors) |
| **Comprehension** | 10 tests | Loaded content has expected structure and key terms |
| **Integration** | 3 tests | Context loader actually returns the data for user queries |

---

## 2. Data paths (canonical locations)

| Data | Expected path | Used by |
|------|---------------|---------|
| Coordinator instructions | `docs/agents/coordinator-instructions.md` | load_coordinator_system_prompt |
| Project context | `docs/agents/coordinator-project-context.md` | context_loader._build_expanded_base |
| Jarvey brain | `docs/agents/JARVEY_PROJECT_BRAIN.md` | context_loader._build_expanded_base |
| Known truths (SSOT) | `docs/agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md` | context_loader, intent snippets |
| Roadmap | `docs/product/ROADMAP.md` | intent: roadmap |
| Project timeline | `scripts/coordinator-email/project_timeline.json` | intent: recent |
| Email notes | `docs/agents/EMAIL_NOTES.md` | intent: notes, structured_output |
| Intents config | `scripts/coordinator-email/intents/intents.json` | context_loader intent mapping |
| Capability menu | `scripts/coordinator-email/jarvey_capability_menu.json` | send_opener, get_capability_options_text |
| Templates | `scripts/coordinator-email/templates/*.json` | template_registry |

---

## 3. Test details

### 3.1 Location tests

- `test_coordinator_instructions_exists`
- `test_coordinator_project_context_exists`
- `test_jarvey_brain_exists`
- `test_known_truths_exists`
- `test_roadmap_exists`
- `test_project_timeline_exists`
- `test_intents_exists`
- `test_capability_menu_exists`
- `test_templates_dir_exists`
- `test_email_notes_exists_or_creatable` (file or parent dir writable)

### 3.2 Read tests

- Each file is opened with UTF-8 encoding
- Content is non-empty (where applicable)
- No OSError or UnicodeDecodeError

### 3.3 Comprehension tests

| Test | Expected content |
|------|------------------|
| instructions | "Jarvey", "coordinator" |
| project_context | "OutOfRouteBuddy", "Jarvey" |
| brain | Intent map or "User question", "Jarvey" |
| known_truths | "End trip" or "Clear trip", "TripCrashRecoveryManager" or "Room" |
| roadmap | At least 2 of: "Auto drive", "Reports", "History" |
| project_timeline | Valid JSON array |
| intents | Valid JSON array; each item has name, keywords, sources |
| capability_menu | Valid JSON with "options" array (3+ items) |
| templates | Each has keywords or condition; key; body |

### 3.4 Integration tests

- `load_context_for_user_message("Re: X", "What's next?")` includes "Auto" and roadmap data
- `load_context_for_user_message("Re: X", "How does trip recovery work?")` includes recovery data

---

## 4. Run commands

```bash
# Run data access tests only
cd scripts/coordinator-email
python -m unittest test_jarvey_data_access -v

# Run as part of full suite
python run_all_jarvey_tests.py
```

---

## 5. When to add or update tests

- **New data file:** Add a location test and read test; add comprehension test if structure is known
- **Path change:** Update JARVEY_DATA_PATHS in test_jarvey_data_access.py and this doc
- **Structure change:** Update comprehension assertions to match new schema

---

## 6. Related docs

- [JARVEY_PROJECT_BRAIN.md](JARVEY_PROJECT_BRAIN.md) — Bridge from user to project
- [JARVEY_DATA_QUALITY_SCORE.md](data-sets/JARVEY_DATA_QUALITY_SCORE.md) — Data quality dimensions
- [JARVEY_WIRING_PLAN.md](JARVEY_WIRING_PLAN.md) — Full framework wiring
