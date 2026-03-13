# Cyber Security Loop & Hub — Research-Derived TODOs (2026-03-11)

**Purpose:** TODOs derived from what was built today: Cyber Security Loop (Master Plan, tests, scripts, Loop #3), Hub deposits, Universal Loop Prompt. Use for prioritization in Improvement Loop, Cyber Security Loop, or board meetings.

**What we built today:**
- Cyber Security Loop: Master Plan 100%, SecuritySimulationTest + TripExporterTest wired, purpleScriptTests, diff_training_runs, prompt_injection_harness, audit_rules, playbook discovery, CI rules audit
- Loop #3 run with proof of work, benefits report, ledger
- Hub: loop3-proof-of-work, purple-training.json, data-summary-and-utilization
- Universal Loop Prompt: Hub consulted, Loop Master, anti-slop

---

## Cyber Security Loop — Follow-ups

- [ ] **Use diff_training_runs in Phase 3 Improve** when multiple training JSONs exist (e.g. different dates); add to routine.
- [ ] **Expand prompt-injection harness** with OWASP techniques: encoding, typoglycemia, best-of-N, multi-modality (see prompt-injection-techniques.md).
- [ ] **Fix CYBER_SECURITY_LOOP_MASTER_PLAN Phase 2 checkboxes** — diff_training_runs, prompt_injection_harness, audit_rules exist but table shows [ ]; update to [x].
- [ ] **Add export-path-traversal to playbook discovery** — TripExporterTest is unit_test type; ensure run_purple_simulations can report it when using --discover-playbooks.
- [ ] **Document validation-only vs full** in scripts/purple-team/README.md — validation-only skips Gradle; CI/loop use --full.
- [ ] **Add InputValidatorTest to security gate** if it covers sanitizeMiles/sanitizeFilePath edge cases not in SecuritySimulationTest.
- [ ] **HTTP simulations** — when sync endpoint exists, implement --with-http and add http_simulations to training JSON.
- [ ] **Add more synthetic scenarios** — encoding_base64, best_of_n, indirect_doc from SYNTHETIC_ATTACK_SCENARIOS.
- [ ] **Multi-stage scenarios (future)** — inject_then_exec, poison_then_export; document in audit as deferred.
- [ ] **Instrumented security tests** — optional; document in SIMULATION_ENVIRONMENT if in scope.
- [ ] **Regression baseline in Phase 3** — run diff_training_runs.py before/after Improve; add to CYBER_SECURITY_LOOP_ROUTINE.
- [ ] **Training JSON run_id field** — add run_id or loop_number for cross-run correlation.
- [ ] **Purple script tests: add timeout test** — verify run_purple_simulations --full completes within N seconds.
- [ ] **CI: upload purpleScriptTests output** — optional artifact for script test results.
- [ ] **Playbook discovery: support unit_test type** — parse simulation_type: unit_test from attack-playbooks for TripExporter coverage.

---

## Hub & Universal Loop — Follow-ups

- [ ] **Loop # in every loop report** — Token §4.4, Cyber Loop #3; ensure Improvement Loop and Synthetic Data Loop also report Loop #.
- [ ] **Hub consulted at loop start** — add "Hub consulted: [files]" and "Advice/rules applied: [bullets]" to first step of each loop routine.
- [ ] **AGENTS.md link to Universal Loop** — add line: "All agents running loops: read UNIVERSAL_LOOP_PROMPT and LOOP_MASTER_ROLE."
- [ ] **Send to hub after Cyber Loop** — add to CYBER_SECURITY_LOOP_ROUTINE Phase 3: "If polished, deposit to hub per SEND_TO_HUB_PROMPT."
- [ ] **Hub index: remove duplicate row** — README had "*(none yet)*" alongside token-loop; clean up.
- [ ] **Hub manifest JSON** — machine-readable hub/manifest.json for agents to load without parsing README.
- [ ] **Shared progress report shape** — Loop #, proof of work, benefits, next TODOs; reference from UNIVERSAL_LOOP_PROMPT.
- [ ] **Slop checklist in rule** — add 3–5 bullet checklist to universal-loop.mdc.
- [ ] **Example good vs bad Hub deposit** — add to hub/ or UNIVERSAL_LOOP_PROMPT for concrete reference.
- [ ] **Trigger phrase index** — one-page doc: "start master loop", "GO", "start token loop", "send to hub", "run cyber security loop".
- [ ] **docs/README: Loops and Hub** — add under Quick links: LOOP_MASTER_ROLE, Hub, UNIVERSAL_LOOP_PROMPT.
- [ ] **TASKS_INDEX: link this file** — add to "Where tasks live" or "In-code TODOs" section.
- [ ] **CRUCIAL: Hub/Loop line** — already references HUB_AND_LOOP_FUTURE_TODOS; add this file as "Cyber Security & Hub 2026-03-11 follow-ups".
- [ ] **Hub retention policy** — define how long to keep loop reports; archive older to hub/archive/.
- [ ] **Pre-deposit slop check script** — warn on vague phrases, missing paths, "consider X" without project tie.

---

## Cross-loop & Integration

- [ ] **Loop # format standard** — e.g. `{loop}-YYYYMMdd-N` (Token Loop 5, Cyber Loop 3); use in ledger and Hub.
- [ ] **Improvement Loop: Hub consulted** — add Phase 0 step: read hub/README, relevant Hub files; note in summary.
- [ ] **Synthetic Data Loop: Hub consulted** — same; add to SYNTHETIC_DATA_LOOP_ROUTINE.
- [ ] **Token Loop: already has §4.4** — Loop #, proof of work, benefits; use as template for Cyber and others.
- [ ] **Master Loop Step 0.M: read Hub** — ensure Step 0.M explicitly reads Hub index and relevant files.
- [ ] **Board meeting: Hub review** — add "Review latest Hub index" to board meeting template.
- [ ] **Coordinator: suggest send to hub** — after loop or major artifact, suggest deposit if polished.
- [ ] **COMPREHENSIVE_AGENT_TODOS: send to hub** — add "If polished, send to hub" to task completion template.

---

## Scripts & Automation

- [ ] **list_hub_for_loop.ps1** — list Hub files for a given loop (e.g. token, cyber-security).
- [ ] **validate_hub_deposit.ps1** — check filename format, optional front matter, index entry.
- [ ] **CI: Hub index consistency** — check every hub/ file is in README index.
- [ ] **run_cyber_security_loop.ps1** — wire listener events if not already (cyber_security_simulate, etc.).
- [ ] **purpleScriptTests: add to CI explicitly** — already runs via securitySimulations dependency; optional: separate step for visibility.

---

## Documentation & Discoverability

- [ ] **LOOPS_START_HERE.md** — "Running a loop? Read UNIVERSAL_LOOP_PROMPT, LOOP_MASTER_ROLE, Hub index, then your loop doc."
- [ ] **CYBER_SECURITY_DATA_SUMMARY: update runs count** — "Runs so far: 3" (include Loop #3).
- [ ] **TRAINING_DATA_INDEX: add 2026-03-11 run** — validation_passed 4/4; note TripExporterTest now in loop.
- [ ] **IMPROVEMENT_LOOP_INDEX: Cyber section** — already has 10c; verify links work.
- [ ] **docs/README: Cyber Security links** — already has CYBER_SECURITY_LOOP_MASTER_PLAN, CYBER_SECURITY_DATA_SUMMARY; verify.

---

## Summary count

| Section | TODOs |
|---------|-------|
| Cyber Security Loop follow-ups | 15 |
| Hub & Universal Loop follow-ups | 15 |
| Cross-loop & integration | 8 |
| Scripts & automation | 5 |
| Documentation & discoverability | 5 |
| **Total** | **48** |

---

*Use in Improvement Loop tiering, Cyber Security Loop Phase 3, or board meetings. Link from TASKS_INDEX and CRUCIAL.*
