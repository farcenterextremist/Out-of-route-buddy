# Loop/Gate Architecture Blueprint

**Purpose:** Translate respected reliability and workflow design patterns into practical loop architecture for OutOfRouteBuddy.

---

## Research sources used

- Google SRE Book, "The Evolution of Automation at Google"
  - Automation as consistency + platform + faster repair
  - Prefer idempotent fixes and platformized automation
- Temporal docs (Retry Policy)
  - Activity retries by default, workflow retries not default
  - Emphasis on deterministic workflow logic and scoped retries
- NIST SP 800-160 Vol. 2 Rev. 1
  - Resilient systems should anticipate, withstand, recover, adapt
- Confluent schema evolution guidance
  - In-place compatible evolution for additive changes
  - Versioned streams for breaking changes

---

## Current strengths in this repo

- Start/end loop gates are already documented and enforced
- Shared-state files exist (`loop_shared_events.jsonl`, `loop_latest/*.json`)
- Quality and consistency contracts exist
- Loop summaries and ledgers already capture evidence

---

## High-respect design patterns to adopt

### 1) Deterministic loop state machine (workflow-style)

Treat each loop as a finite state machine:
- `init -> research -> execute -> verify -> publish -> close`
- Persist state transitions as events
- Explicit transition guards (no jump to execute without research/gates)

**Why:** Deterministic transitions reduce drift and simplify continuity tests.

### 2) Idempotent gate actions

Every gate action should be safe to rerun:
- Rewriting `loop_latest/<loop>.json` should be overwrite-safe
- Appending `finished` events should guard against duplicate run IDs

**Why:** Retries should not corrupt state.

### 3) Contract-first shared-state schema

Define JSON schema contracts for:
- `loop_shared_events.jsonl` event object
- `loop_latest/<loop>.json` state object

Use additive schema evolution by default; version streams/files for breaking changes.

**Why:** Prevent silent breakage across concurrently running loops.

### 4) Verification gates as testable runbooks

Treat gate checks as executable runbook tests:
- "Gate preconditions met"
- "End-state artifacts written"
- "Traceability links complete"

**Why:** Reliable architecture needs executable verification, not only docs.

### 5) Resilience-by-design

Apply NIST-style resilience outcomes:
- Anticipate: detect stale/missing shared state early
- Withstand: continue with degraded mode if non-critical artifact missing
- Recover: retry gate writes and report reconciliation steps
- Adapt: feed failures into next-run tasks automatically

---

## Recommended implementation roadmap

### Phase A (now, low-risk docs/tooling)

1. Add continuity tests for gate and shared-state contracts
2. Add architecture and test-plan docs
3. Add skills for architecture planning and continuity testing

### Phase B (medium, script logic)

1. Add duplicate run-ID protection in shared event writes
2. Add explicit schema validation script for shared-state files
3. Add continuity test runner into loop checklists

**Status:** Items 1-3 now have concrete script support in the hardening layer:
- `write_loop_shared_state.ps1` enforces dedupe on finished events and refreshes both `run_id` and `last_run_id`
- `start_loop_run.ps1` and `finish_loop_run.ps1` provide additive gate wrappers around run start/end
- `audit_loop_shared_state.ps1` and the continuity suites check for stale latest-state drift before heavier orchestration is attempted
- `measure_loop_efficiency.ps1` turns the current hardening maturity into a repeatable `X/100` score plus progress bar so each loop-design improvement can be benchmarked against the same rubric

### Phase C (heavy, architecture)

1. Introduce formal loop state machine runner
2. Add run replay and recovery mechanism
3. Add structured observability spans/events for loop phases

**Approval gate:** Phase C is the next heavier architecture phase and must stay approval-gated. Do not implement the formal runner, replay/recovery, or breaking shared-state changes until the current wrapper/audit path has stayed stable and explicit human approval is given for the heavier orchestration move.

**Why the gate stays in place:**
- state-machine execution changes how loops run, not just how they report
- replay/recovery can accidentally reapply end-state writes if idempotence is incomplete
- richer observability often pushes schema growth into shared-state or ledger formats

---

## New skills/rules/tests proposed

- Skills:
  - `loop-architecture-blueprinter`
  - `loop-continuity-test-engineer`
- Rule:
  - `loop-continuity-enforcement.mdc`
- Tests:
  - `test_loop_gate_contract.ps1`
  - `test_shared_state_contract.ps1`
  - `run_loop_continuity_tests.ps1`

---

## Continuity test objectives

- Ensure required gate docs/rules still contain required obligations
- Ensure shared-state files are parseable and contain required keys
- Ensure every loop has a `loop_latest` file and it includes traceability fields
- Fail fast with actionable messages

---

## Guardrails

- No automatic UI changes from these architecture changes
- No destructive modifications to historical ledger/event data
- Favor additive migration and backward compatibility
