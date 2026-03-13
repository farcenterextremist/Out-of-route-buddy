# From today's build (2026-03-11): send-to-hub, data-sets index, Universal Loop Prompt — 37 TODOs

**Purpose:** Research-backed tasks from what we built today: send-to-hub in AGENTS.md and SEND_TO_HUB_PROMPT; data-sets index in hub (file-organizer); UNIVERSAL_LOOP_PROMPT. Use for next Improvement Loop or Master Loop.

**Source:** [HUB_AND_LOOP_FUTURE_TODOS.md](HUB_AND_LOOP_FUTURE_TODOS.md) — this file extends that list. Merge or cross-reference as needed.

---

## Hub & send-to-hub wiring

- [ ] **AGENTS.md Improvement Loop step:** Add "Read hub/README.md and UNIVERSAL_LOOP_PROMPT at loop start; note Hub consulted and Advice/rules applied" to the numbered Improvement Loop steps in AGENTS.md.
- [ ] **AGENTS.md Quick links row:** Add a Quick links row: "Hub & Universal Loop" → hub/README.md and docs/agents/data-sets/hub/UNIVERSAL_LOOP_PROMPT.md.
- [ ] **IMPROVEMENT_LOOP_INDEX §1 "Start here":** Add a row: "Hub + Universal Loop" → read hub/README.md and UNIVERSAL_LOOP_PROMPT.md before or with routine.
- [ ] **IMPROVEMENT_LOOP_ROUTINE Phase 0:** Add one bullet: "Read docs/agents/data-sets/hub/README.md and relevant Hub index files; note Hub consulted and Advice/rules applied (per UNIVERSAL_LOOP_PROMPT)."
- [ ] **IMPROVEMENT_LOOP_ROUTINE Phase 4 / end:** Add one bullet: "If run produced polished proof of work or report, consider depositing to docs/agents/data-sets/hub/ per SEND_TO_HUB_PROMPT (naming, optional index entry)."
- [ ] **SYNTHETIC_DATA_LOOP_ROUTINE end:** Add "Consider send to hub: if mesh report or quality report is polished, deposit to hub with YYYY-MM-DD_data-loop_<short-description>.md and index entry."
- [ ] **CYBER_SECURITY_LOOP_ROUTINE end:** Add "Deposit proof of work / loop report to hub per SEND_TO_HUB_PROMPT; add one-line entry to hub/README.md."
- [ ] **TOKEN_REDUCTION_LOOP Step 7:** Explicitly say "If report is polished, deposit to hub with naming YYYY-MM-DD_token-loop_<short-description>.md and optional index entry."
- [ ] **LOOP_MASTER_ROLE Step 0.M:** Add explicit bullet: "Read docs/agents/data-sets/hub/README.md and all relevant Hub index files; list 'Hub consulted' and 'Advice/rules applied' in Step 0.M output."
- [ ] **improvement-loop-wizard skill:** In "read first" or Phase 0, add: read UNIVERSAL_LOOP_PROMPT and hub/README; note Hub consulted.
- [ ] **2-hour-loop.mdc or self-improvement.mdc:** Add one line: "When running Improvement Loop, read hub/README and UNIVERSAL_LOOP_PROMPT at start; note Hub consulted and Advice applied."
- [ ] **docs/README.md:** Add "Send to hub" and "Hub & Universal Loop" under Quick links or Automation section, linking to hub/README.md and SEND_TO_HUB_PROMPT.md.
- [ ] **Trigger phrase doc:** Create or update a one-page "trigger phrases" list: GO, start master loop, start token loop, Run Cyber Security Loop, Start Synthetic data loop, send to hub, organize data and send to hub — with one-line meaning and doc path for each.
- [ ] **"Organize data and send to hub":** Document that this means: refresh/update the data-sets index (hub file 2026-03-11_file-organizer_data-sets-index-and-organization.md or successor), then deposit that or other polished output to hub and index.

## Data-sets index & File Organizer

- [ ] **Hub README "Quick start" or index:** Add link to data-sets index file (2026-03-11_file-organizer_data-sets-index-and-organization.md) in hub/README as "Single catalog of data-sets (hub, roles, aptitude, security, board-meeting)."
- [ ] **Data-sets index refresh owner:** In File Organizer data-set or WORKER_TODOS, add: "When new role data-set file or hub section is added, update the data-sets index in hub (or docs/agents/data-sets/README) so the catalog stays current."
- [ ] **DATA_SETS_AND_DELEGATION_PLAN:** Add one sentence: "Completed, polished outputs go to docs/agents/data-sets/hub/ per SEND_TO_HUB_PROMPT; the data-sets index (in hub) is the single catalog of what lives under data-sets."
- [ ] **Data-sets index "Last updated":** Keep the "Last updated: YYYY-MM-DD" line in the data-sets index file; refresh it when sections or links change.
- [ ] **When adding a new role data-set file:** Add a row to the data-sets index (hub version) and to docs/agents/data-sets/README.md role table so both stay in sync.
- [ ] **File Organizer WORKER_TODOS:** Add: "Propose a schedule for refreshing the data-sets index (e.g. after each Master Loop run or monthly)."
- [ ] **Board meeting and data-sets:** In board-meeting README or data-set, add: "Optional: review Hub index and data-sets index; note any new loop reports or catalog changes to align priorities."

## Universal Loop Prompt & slop

- [ ] **"Hub consulted" in loop templates:** Ensure IMPROVEMENT_LOOP_SUMMARY template (or RUN_LEDGER block) has optional lines: "Hub consulted: [list]. Advice/rules applied: [1–3 bullets]."
- [ ] **Slop checklist in UNIVERSAL_LOOP_PROMPT:** Add a 5-item "Critique before deposit" checklist (actionable? real artifacts? reusable? no padding? project-specific?) and reference it from SEND_TO_HUB_PROMPT.
- [ ] **Example good vs bad deposit:** Add one short "good" vs "bad" Hub deposit example (e.g. in hub/EXAMPLES.md or UNIVERSAL_LOOP_PROMPT) so agents have a concrete reference.
- [ ] **UNIVERSAL_LOOP_PROMPT "Paths to bookmark":** Add row: "Data-sets index" → docs/agents/data-sets/hub/2026-03-11_file-organizer_data-sets-index-and-organization.md (or current filename).
- [ ] **LOOP_MASTER_ROLE "read the Hub":** Clarify that "read the Hub" means: open hub/README.md, scan index table, then open each file relevant to the loop(s) being run (e.g. token-loop report for Token Loop).
- [ ] **No template without fill-in:** In UNIVERSAL_LOOP_PROMPT or SEND_TO_HUB_PROMPT, add: "Do not paste a template without filling in project-specific values (paths, run_id, file names, metrics)."
- [ ] **COMPREHENSIVE_AGENT_TODOS:** In the completion or handoff step for each role, add: "If this task produced a polished report or proof of work, consider depositing to hub per SEND_TO_HUB_PROMPT."
- [ ] **WORKER_TODOS_AND_IDEAS:** Add under Future plans or a new short section: "Loop outputs and proof of work live in docs/agents/data-sets/hub/; see hub/README.md and SEND_TO_HUB_PROMPT."
- [ ] **Coordinator instructions:** Add: "After a loop or major artifact, suggest 'send to hub' if the output is polished and reusable by other agents or the next run."

## Scripts & validation

- [ ] **Health or CI check:** Add a check that docs/agents/data-sets/hub/README.md exists and contains an index table (or allowlist); optional: check that UNIVERSAL_LOOP_PROMPT.md and SEND_TO_HUB_PROMPT.md exist in hub.
- [ ] **Script list_hub_for_loop:** Create list_hub_for_loop.ps1 (or similar) that accepts -Loop <name> and prints paths of Hub files matching that loop (e.g. token, cyber-security) so agents can read them in one go.
- [ ] **Validate hub deposit:** Small script or checklist that validates: filename matches YYYY-MM-DD_<role-or-topic>_<short-description>.<ext>; optional index entry present in README.
- [ ] **Hub index consistency:** Document or script: ensure every file in hub/ (except README, SEND_TO_HUB_PROMPT, UNIVERSAL_LOOP_PROMPT) is listed in the Hub index table or in an allowlist so nothing is orphaned.

## Metrics & maturity

- [ ] **Hub maturity checklist:** One doc or section: "Hub maturity" — SEND_TO_HUB_PROMPT exists, UNIVERSAL_LOOP_PROMPT exists, hub/README has index, data-sets index exists, AGENTS.md has Send to hub section; tick when done.
- [ ] **Count Hub deposits per month:** Simple metric (e.g. from git log or file list) to see Hub growth and which roles deposit most; add to METRICS.md or improvement summary.
- [ ] **Universal Loop Prompt version:** Add "Last updated: 2026-03-11" (or current date) at bottom of UNIVERSAL_LOOP_PROMPT.md and update when the prompt is revised.

---

**Total: 37 TODOs.** Use with [HUB_AND_LOOP_FUTURE_TODOS.md](HUB_AND_LOOP_FUTURE_TODOS.md) for Improvement Loop tiering and File Organizer backlog.
