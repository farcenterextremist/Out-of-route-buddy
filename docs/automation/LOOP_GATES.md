# LOOP GATES (required)

**Purpose:** Mandatory start and end steps for **every loop run**. No loop may skip these. Ensures alignment with Loop Master, Hub, and shared state across loops.

**Reference:** [UNIVERSAL_LOOP_PROMPT.md](../agents/data-sets/hub/UNIVERSAL_LOOP_PROMPT.md), [LOOP_MASTER_ROLE.md](./LOOP_MASTER_ROLE.md), [LOOP_DYNAMIC_SHARING.md](./LOOP_DYNAMIC_SHARING.md), [LOOP_CONSISTENCY_STANDARD.md](./LOOP_CONSISTENCY_STANDARD.md)

---

## At loop start (before execution)

1. Read **`docs/automation/LOOP_MASTER_ROLE.md`**.
2. Read **`docs/automation/LOOPS_AND_IMPROVEMENT_FULL_AXIS.md`**.
3. Read **`docs/agents/data-sets/hub/UNIVERSAL_LOOP_PROMPT.md`**.
4. Then read **`docs/agents/data-sets/hub/README.md`**.
4a. Include research reads for **quality standards**, **data pruning**, and **automation-to-prompt conversion**:  
   - `docs/automation/QUALITY_STANDARDS_AND_PROOF.md`
   - `docs/automation/DATA_USEFULNESS_AND_PRUNING_RESEARCH.md`  
   - `docs/automation/AUTOMATION_TO_PROMPT_RESEARCH.md`
4b. Read **`docs/automation/LOOP_CONSISTENCY_STANDARD.md`** and define the run's consistency target (goal: 10/10).
5. Read **`docs/automation/loop_shared_events.jsonl`** (tail: last 50 lines or full file if smaller).
6. Read **`docs/automation/loop_latest/*.json`** (all files in that directory — other loops’ latest output).
7. **Log before execution:**  
   - **Hub consulted:** [list files or roles you read from Hub index].  
   - **Advice applied:** [1–3 bullets: what from the Hub / Loop Master / full axis you are using this run].

**Optional helper:** `scripts/automation/start_loop_run.ps1` can perform additive preflight validation, create a normalized run-context file, and emit the standard improvement/token start event with a stable `run_id`.

Only after completing steps 1–7 and logging **Hub consulted** and **Advice applied** may you proceed with the loop body.

---

## At loop end (after execution)

1. **Send to hub:** Automatically send all **completed, polished** outputs to **`docs/agents/data-sets/hub/`** using naming like **`YYYY-MM-DD_role-or-topic_short-description.ext`**.
2. **Update Hub index:** Add or update **one line** in **`docs/agents/data-sets/hub/README.md`** (Hub index table) for each deposited file.
3. **Append finished event:** Append one **finished** event to **`docs/automation/loop_shared_events.jsonl`** (e.g. `{"ts":"...", "loop":"token", "event":"finished", "run_id":"...", "summary_path":"...", "next_steps":[...]}`).
4. **Update latest file:** Write or overwrite **`docs/automation/loop_latest/<your_loop>.json`** with your loop’s latest output (e.g. `last_run_ts`, `summary_path`, `suggested_next_steps`, optional `checkpoint`).
5. **Consistency check:** Add the `Loop Consistency Check` block from `LOOP_CONSISTENCY_STANDARD.md` to your summary/progress report and record `Consistency score: X/10`.

### Optional council-entry gate (sandboxed multi-instance runs only)

If a future run uses the sandboxed `Loop Council` model from [LOOP_COUNCIL_SANDBOX.md](./LOOP_COUNCIL_SANDBOX.md), add one more end-stage gate **before** final publish/keep decisions:

- **Council-entry gate:** Confirm every active lane has reported one of `complete`, `timed_out`, `blocked`, or `proof_gap`, and confirm the evidence pack exists for each lane before fan-in judgment begins.

This gate is **design-only** right now and does not require a live scheduler.

**Optional helpers:**
- `scripts/automation/finish_loop_run.ps1` standardizes shared-state writing and can invoke the continuity suite after the summary is ready.
- `scripts/automation/audit_loop_shared_state.ps1` performs a readonly drift check between `loop_shared_events.jsonl` and `loop_latest/*.json`.
- `scripts/automation/finish_loop_run.ps1` also persists a `Loop Efficiency Score` block into the summary automatically when efficiency measurement is available.

See [LOOP_DYNAMIC_SHARING.md](./LOOP_DYNAMIC_SHARING.md) for the exact schema of `loop_shared_events.jsonl` and `loop_latest/<loop>.json`.

---

## Summary

| When | Action |
|------|--------|
| **Start** | Read LOOP_MASTER_ROLE, LOOPS_AND_IMPROVEMENT_FULL_AXIS, UNIVERSAL_LOOP_PROMPT → hub/README → quality standards + pruning + automation-to-prompt research docs + LOOP_CONSISTENCY_STANDARD → loop_shared_events.jsonl (tail) → loop_latest/*.json. Log **Hub consulted** and **Advice applied** before execution. |
| **End** | Send polished outputs to hub/ with names like `YYYY-MM-DD_role_description.ext`; add/update one line in hub/README.md; append finished event to loop_shared_events.jsonl; update loop_latest/<your_loop>.json; record loop consistency score (X/10). In future sandboxed council runs, pass the council-entry gate before final judgment. |

---

*Every loop run must pass these gates. Update this doc when new required reads or writes are added.*
