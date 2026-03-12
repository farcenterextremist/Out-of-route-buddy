# Cyber Security Loop — Data Summary

**Purpose:** Single place to understand what data we've collected, what it means, and how to use it. Updated after each loop run.

**Last updated:** 2026-03-11

---

## 1. Data We've Collected

### Run Ledger (Pre / During / Post)

| Source | Location | What it records |
|--------|----------|-----------------|
| **CYBER_SECURITY_LOOP_RUN_LEDGER** | [CYBER_SECURITY_LOOP_RUN_LEDGER.md](./CYBER_SECURITY_LOOP_RUN_LEDGER.md) | Checkpoint (git hash), git status, timestamp; Phase 0–3 status; proof of work path; validation passed; next steps; component verification |

**Runs so far:** 2 (2026-03-11 21:48, 2026-03-11 22:23)

---

### Training JSON (Structured Results)

| Source | Location | Contents |
|--------|----------|----------|
| **purple-training.json** | `docs/agents/data-sets/security-exercises/artifacts/YYYY-MM-DD-purple-training.json` | `exercise_id`, `date`, `unit_tests_passed`, `validation_simulations[]`, `synthetic_scenarios[]`, `http_simulations[]`, `summary{}` |

**Fields:**
- `validation_simulations`: attack, expected, actual, passed, blue_visibility
- `synthetic_scenarios`: scenario_id, category, technique, payload_sample, expected, automated
- `summary`: validation_passed, validation_total, synthetic_scenarios count, http_passed, http_total

---

### Proof of Work (Human-Readable Logs)

| Source | Location | Contents |
|--------|----------|----------|
| **purple-simulations.md** | `docs/agents/data-sets/security-exercises/YYYY-MM-DD-purple-simulations.md` | Unit test pass/fail; validation simulation results (PASS/FAIL per playbook); synthetic scenario list |

---

### Attack Library & Playbooks

| Source | Location | Contents |
|--------|----------|----------|
| **ATTACK_LIBRARY** | [docs/agents/data-sets/ATTACK_LIBRARY.md](../agents/data-sets/ATTACK_LIBRARY.md) | Index of playbooks: id, name, surface, simulation_type, expected, blue_alarm |
| **attack-playbooks/** | `docs/agents/data-sets/attack-playbooks/*.md` | One .md per playbook (trip-validation-bypass, path-traversal, export-path-traversal, rules-backdoor, prompt-injection-readme) |
| **SYNTHETIC_ATTACK_SCENARIOS** | [docs/agents/data-sets/SYNTHETIC_ATTACK_SCENARIOS.md](../agents/data-sets/SYNTHETIC_ATTACK_SCENARIOS.md) | Scenario catalog: app validation, prompt injection, multi-stage (future) |

---

### Loop Summaries

| Source | Location | Contents |
|--------|----------|----------|
| **CYBER_SECURITY_LOOP_SUMMARY** | [CYBER_SECURITY_LOOP_SUMMARY_2026-03-11.md](./CYBER_SECURITY_LOOP_SUMMARY_2026-03-11.md) | Metrics, what was done, artifacts, next steps, verification table |

---

### Scripts & Artifacts

| Script | Purpose | Output |
|--------|---------|--------|
| **run_purple_simulations.py** | Run Gradle tests + validation; produce training JSON | purple-simulations.md, purple-training.json |
| **audit_rules.py** | Grep .cursor/rules for suspicious patterns | Exit 0/1; stdout |
| **diff_training_runs.py** | Compare two training JSONs for regression | Exit 0 (no regression) / 1 (regression); diff summary |
| **prompt_injection_harness.py** | Produce prompt-injection test cases | JSON: test_cases[] |

---

## 2. What the Data Means

### Validation Simulations

- **4 playbooks (hardcoded)** or **2 playbooks (discovered)** depending on `--discover-playbooks`:
  - trip_validation_rejects_nan, trip_validation_rejects_negative, trip_validation_rejects_out_of_range, input_validator_rejects_path_traversal
  - Or from attack-playbooks/*.md: trip_validation_bypass, path_traversal (when discovery finds validation-type playbooks)
- **Pass** = app correctly rejected malicious input (NaN, negative, path traversal) → Blue team is working
- **Fail** = app accepted malicious input → Blue fix needed

### Synthetic Scenarios

- **7 scenarios** in training JSON: 3 automated (trip_nan, trip_negative, path_traversal_parent), 4 agent-driven (direct_ignore_previous, indirect_readme, typoglycemia, rules_backdoor)
- **Automated** = covered by SecuritySimulationTest
- **Agent-driven** = Red/Blue agent must verify manually; no automated pass/fail

### Run Ledger

- **Pre-loop** = checkpoint for rollback; if simulations break something, revert to this commit
- **During** = phase status; helps diagnose where a run failed
- **Post-loop** = artifact paths; validation passed; next improvement items

### Component Verification

- **What Works** table = confirms each script and CI step does what it says
- Use for onboarding, debugging, and CI health checks

---

## 3. How to Utilize the Data

### Regression Detection

Use `diff_training_runs.py` when you have multiple training JSONs:

```bash
python scripts/purple-team/diff_training_runs.py artifacts/2026-03-10-purple-training.json artifacts/2026-03-11-purple-training.json
```

Exit 0 = no regression; exit 1 = fewer validations passed than baseline.

### Few-Shot Examples for Agents

Include `validation_simulations` and `synthetic_scenarios` from training JSON in Red/Blue agent prompts:

- "Here are attacks that passed validation: reject NaN, reject negative..."
- "Here are agent-driven scenarios to flag: direct_ignore_previous, indirect_readme..."

### Dashboards & Metrics

Parse `summary` from training JSON:

- `validation_passed` / `validation_total` → pass rate trend
- `synthetic_scenarios` count → coverage breadth
- `http_passed` / `http_total` → when HTTP simulations are enabled

### Rollback & Improvement

- **Rollback:** Use `Checkpoint` from ledger to revert to pre-loop state
- **Improvement:** Use `Next` from ledger and summary to pick next tasks

### Prompt Injection Test Cases

Run `prompt_injection_harness.py -o test_cases.json` and feed the JSON to Red/Blue agents for manual verification.

### CI Integration

- `audit_rules.py` runs before `securitySimulations` in `android-tests.yml`
- Training JSON artifacts are uploaded as CI artifacts

---

## 4. Quick Reference

| I want to... | Go to |
|------------|-------|
| See run history | [CYBER_SECURITY_LOOP_RUN_LEDGER.md](./CYBER_SECURITY_LOOP_RUN_LEDGER.md) |
| See latest metrics | [CYBER_SECURITY_LOOP_SUMMARY_2026-03-11.md](./CYBER_SECURITY_LOOP_SUMMARY_2026-03-11.md) |
| See attack catalog | [ATTACK_LIBRARY](../agents/data-sets/ATTACK_LIBRARY.md), [SYNTHETIC_ATTACK_SCENARIOS](../agents/data-sets/SYNTHETIC_ATTACK_SCENARIOS.md) |
| Run simulations | `./gradlew :app:securitySimulations` |
| Compare runs | `python scripts/purple-team/diff_training_runs.py <baseline> <current>` |
| Generate prompt-injection cases | `python scripts/purple-team/prompt_injection_harness.py -o out.json` |

---

*Update this doc when new data types or use cases are added.*
