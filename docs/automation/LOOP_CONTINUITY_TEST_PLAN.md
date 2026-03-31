# Loop Continuity Test Plan

**Purpose:** Validate that loop/gate architecture remains continuous and interoperable across runs.

---

## Test suites

### 1) Gate contract suite

Script: `scripts/automation/test_loop_gate_contract.ps1`

Checks:
- Required governance docs exist
- Required key phrases for start/end gate obligations exist
- Improvement routine includes sandbox verification requirement

### 2) Shared-state contract suite

Script: `scripts/automation/test_shared_state_contract.ps1`

Checks:
- `loop_shared_events.jsonl` exists and latest line parses as JSON
- `loop_latest/{improvement,token,cyber,synthetic_data}.json` exist
- Each latest file has minimal required keys:
  - `summary_path`
  - `last_run_ts`
  - `suggested_next_steps`

### 3) Shared-state schema + dedupe suite

Script: `scripts/automation/test_shared_state_schema.ps1`

Checks:
- Recent `loop_shared_events.jsonl` window parses as valid JSON
- Required event fields and types are present (`ts`, `loop`, `event`; `run_id` + `summary_path` for `finished`)
- Duplicate `finished` keys (`loop|run_id`) are blocked in recent window
- `loop_latest` files satisfy type-safe schema expectations

**Note:** Dedupe validates a recent window (not entire history) to avoid legacy-data false failures while still catching current regressions.

### 4) Combined runner

Script: `scripts/automation/run_loop_continuity_tests.ps1`

Behavior:
- Runs suites 1, 2, 3, the writer dedupe test, the role-topology contract test, and the desktop-guide export simulation
- Exit 0 only when all pass

### 5) Writer dedupe behavior suite

Script: `scripts/automation/test_write_loop_shared_state.ps1`

Checks:
- Shared-state writer blocks duplicate append for same `loop|run_id`
- Latest-state file is still refreshed with current run values

### 6) Role-topology contract suite

Script: `scripts/automation/test_loop_role_topology_contract.ps1`

Checks:
- `ArchitectTab`, `Builder`, `Optimizer`, `Guard`, and `Watcher` remain present in the tab model
- one-writer-per-file-family and readonly-`Watcher` guardrails remain documented
- core master/blueprint docs still reference the role-based topology

### 7) Desktop guide export simulation

Script: `scripts/automation/test_loop_desktop_guide_export.ps1`

Checks:
- repo source guide can be exported successfully
- exported guide contains key human-facing operating terms
- desktop-guide update system remains testable instead of purely manual

### 8) Loop wrapper suite

Script: `scripts/automation/test_loop_run_wrappers.ps1`

Checks:
- `start_loop_run.ps1` records a normalized context file
- improvement and token start wrappers emit the correct start event with a stable `run_id`
- `finish_loop_run.ps1` updates shared-state output files and refreshes run identifiers

### 9) Shared-state audit suite

Script: `scripts/automation/test_loop_shared_state_audit.ps1`

Checks:
- `audit_loop_shared_state.ps1` passes on matching fixtures
- audit fails on stale `run_id` drift between finished events and `loop_latest/*.json`

### 10) Loop efficiency score suite

Script: `scripts/automation/test_loop_efficiency_score.ps1`

Checks:
- weighted score math stays stable
- ASCII progress bar output matches score percentage
- efficiency state file is written for later reporting

### 11) Efficiency block persistence suite

Scripts:
- `scripts/automation/test_write_loop_efficiency_block.ps1`
- `scripts/automation/test_loop_run_wrappers.ps1`

Checks:
- efficiency markdown block is written into the summary automatically
- rerunning the same finish wrapper replaces the block instead of duplicating it
- dedicated writer preserves one block per `run_id`

---

## Suggested run cadence

- At start of master loop (after Step 0.M reads)
- At end of loop before final summary
- Any time loop governance docs are edited
- Any time the desktop operator guide source or export logic changes

---

## Success criteria

- No missing required gate obligations
- Role-topology contract stays intact across docs
- Shared-state parse/key/schema checks pass
- No duplicate `finished` `loop|run_id` keys in recent event window
- Wrapper scripts preserve traceable run context and end-of-run shared-state updates
- Readonly shared-state audit catches stale latest-state drift
- Efficiency score output remains reproducible after loop design changes
- Desktop guide export path remains working and readable
- Clear failures with exact file and missing condition
