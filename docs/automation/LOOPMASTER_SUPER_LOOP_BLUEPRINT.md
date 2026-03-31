# Loopmaster super-loop blueprint

**Purpose:** Define the single authoritative loop architecture for OutOfRouteBuddy so future multi-tab runs are replicas of one strong system instead of many overlapping systems.

**Status:** Design-first rollout. This doc establishes the control model for `Loopmaster` now. Additional automation or tab scaling can be layered on top later when the role-based pattern is stable.

**Related:** `LOOP_MASTER_ROLE.md`, `LOOP_GATES.md`, `LOOP_HEALTH_CHECKS.md`, `LOOP_DIAGNOSTIC_SWEEP.md`, `LOOP_ROLE_CUBE.md`, `LOOPMASTER_TAB_AND_SPAWN_MODEL.md`, `docs/ux/PLEASANTNESS_AND_FLOW_STANDARD.md`, `docs/ux/SCREENSHOT_REVIEW_WORKFLOW.md`.

---

## Core decision

Treat the project as having:

- **One real loop system:** `Loopmaster`
- **Several internal lanes:** improvement, security, consistency, quality proof, data curation, design sharpening
- **Optional neighboring tabs:** when the user opens additional tabs manually, each follows the same contract with a different role assignment (see tab model below).

Any replica tab should run the **same contract**:

1. same gates
2. same health monitors
3. same shared-state format
4. same evidence rules
5. different scoped assignments only

This removes redundancy. One loop operating system; additional tabs are role-based workers under the same contract, not separate loop species.

---

## Why this structure is safer

- One contract is easier to audit than several partially different routines.
- Health monitoring becomes predictable because every run emits the same signals.
- Rollback becomes possible because every run can write the same artifact bundle and change manifest.
- Parallel runs become less chaotic because each tab is a scoped replica, not a custom process.
- Inline subagents can be spawned intentionally by lane instead of improvising a brand-new workflow each time.

---

## Default tab operating model

When the user opens neighboring tabs manually, the recommended default layout is:

- `ArchitectTab` as the parent `Loopmaster` authority
- `Builder` for implementation work
- `Optimizer` for self-improvement and app-improvement work
- `Guard` for security and risky-drift review
- `Watcher` as an independent proof validator

Use `LOOPMASTER_TAB_AND_SPAWN_MODEL.md` as the reference for:

- role boundaries
- spawn timing
- one-writer-per-file-family guardrails
- readonly `Watcher` behavior

Role names make ownership clearer than generic numbered identities.

### Optional six-instance council sandbox

For a future **sandboxed** multi-instance pattern, `Loopmaster` may define a fixed six-instance council topology:

- `ArchitectTab`
- `BuilderTab`
- `OptimizerTab`
- `GuardTab`
- `WatcherTab`
- `DesignJudgeTab`

This is documented in [LOOP_COUNCIL_SANDBOX.md](./LOOP_COUNCIL_SANDBOX.md). It is **design-only** right now and does not commit the project to a live scheduler or synchronous six-tab automation.

---

## Loopmaster phases

## 0. Intake gate

Decide the run mode before any action:

- `Micro` = tiny checks, docs, cleanup, debugging
- `Standard` = normal guided improvement run
- `Hyper` = reserved for future use (no synchronous multi-tab automation committed)

Required outputs:

- run id
- run mode
- scope statement
- rollback intent
- success condition

## 1. Context gate

Read the required sources:

- `LOOP_MASTER_ROLE.md`
- `LOOP_GATES.md`
- `LOOP_HEALTH_CHECKS.md`
- `LOOP_DIAGNOSTIC_SWEEP.md`
- relevant Hub entries
- `loop_shared_events.jsonl`
- `loop_latest/*.json`

Then define:

- primary owner lane
- secondary helper lane
- what this run will not touch

## 2. Health gate

Run constant safety checks before real work:

- liveness
- readiness policy
- continuity baseline when loop docs/scripts changed
- diagnostic sweep baseline

This is the "do we trust the environment enough to proceed?" gate.

## 3. Planning gate

Select the execution bundle:

- code and architecture work
- tests and verification work
- security/problem hunting
- docs and artifact cleanup
- optional design sharpening if a device is connected

Every task should be assigned:

- a lane owner
- a proof target
- a rollback note

## 4. Execution lanes

The loop body is not one blob. It is a set of lanes under one master authority.

### Lane A: Build and code lane

- implementation
- refactors approved for the run
- test fixes
- low-risk cleanup

### Lane B: Diagnostic and security lane

- active problem hunt
- ignored/failing test review
- security grep and risk notes
- hotspot identification

### Lane C: Proof and consistency lane

- evidence gathering
- consistency score
- ledger quality
- summary tightening

### Lane D: Data and artifact lane

- run manifests
- shared-state updates
- bucket deposit
- reversible decision tracking

### Lane E: Design sharpening lane

Run only when a device or instrument is connected and the scope allows UI review.

Checks:

- `adb devices` shows a usable target
- screenshots can be captured
- no unwarranted structural UI change is made automatically

Actions:

- collect screenshots
- run screenshot review
- apply pleasantness rubric
- log visual evidence and residual weakness

This lane is for **informed beautification**, not random UI churn.

### Optional council fan-in

In the sandboxed council model, the execution lanes above do not gain separate authority. They fan into a final `Loop Council` judgment stage that compares hybrid evidence:

- visual/design proof
- durability/build proof
- residual risk

The council is a judgment layer, not a seventh writer lane.

## 5. Validation gate

Before publish, `Loopmaster` must answer:

- did the intended work finish?
- what evidence proves it?
- what failed or stayed risky?
- can we safely keep these changes?

Minimum proof pack:

- changed files
- tests/lints/health result
- diagnostic note
- residual risk
- next-step recommendation

## 6. Publish or rollback gate

Every run ends with a decision:

- `keep`
- `partial keep`
- `revert selected changes`
- `document only`

The loop should never end with "something changed, hopefully it was fine."

---

## Health monitoring model

Use three layers, not one:

1. **Liveness**
   - cheap
   - local
   - phase-boundary health
2. **Readiness**
   - tests/lint/pulse
   - "safe to continue?" signal
3. **Diagnostic monitoring**
   - active problem hunt
   - hotspot/risk identification

In the council sandbox, these health layers are still required before council fan-in. A council verdict cannot substitute for liveness, readiness, or diagnostics.

When multiple tabs are used, each should emit the same health structure so a human can compare quickly.

---

## Shared-state model for neighboring tabs

If the user opens multiple tabs manually, each tab should still follow the same contract while taking on a different assignment.

Recommended named tabs:

- `ArchitectTab` = conductor and final decision owner
- `Builder` = implementation-heavy worker
- `Optimizer` = improvement-heavy worker
- `Guard` = security/risk-heavy worker
- `Watcher` = proof-heavy validator

Optional extras may exist later for data curation or screenshot-heavy work, but the default pattern should stay role-based and human-readable.

Important:

- These are **assignments**, not different loop species.
- Every tab still follows the same gates and same output contract.
- Tabs can spawn inline agents, but their parent tab remains accountable for quality.

---

## Design sharpening rule

If an instrument or Android device is connected during the run, `Loopmaster` may enable the design sharpening lane.

Design sharpening must:

- gather screenshot evidence first
- score with `PLEASANTNESS_AND_FLOW_STANDARD.md`
- avoid taste-only claims
- avoid major UI structure changes without user approval
- store evidence in the bucket for later comparison

If no device is connected, the lane is skipped cleanly and logged as skipped, not failed.

---

## Data bucket rule

The loop needs a local artifact bucket so changes can be judged and selectively reverted after a larger run.

The bucket should store:

- run manifests
- summaries
- screenshot evidence
- shared-state snapshots
- decision notes
- touched-file lists

This bucket is a **recovery and intelligence layer**. It is not just archive clutter.

---

## Rollback philosophy

Full automatic revert is dangerous in a dirty workspace.

So the safer model is:

1. record what the run changed
2. record why each change exists
3. keep evidence in the bucket
4. let the human choose:
   - keep all
   - keep some
   - revert selected changes

This creates controlled reversibility without pretending every revert should be one-button automatic.

---

## Practical rollout order

1. **Now:** establish the `Loopmaster` blueprint, skill, and user guide
2. **Next:** route current loop docs so they behave like lanes under `Loopmaster` and use the tab-and-spawn model
3. **Later:** add a lightweight run manifest writer for bucket snapshots
4. **Then:** document sandboxed council patterns such as the six-instance `Loop Council` model before any live multi-instance scheduler is attempted
5. **Then:** add scheduled `Micro` or `Standard` runs
6. **Finally:** add neighboring tabs only when the role-based operating pattern is stable; tab count and automation run shape are not fixed by this blueprint.

---

## Anti-chaos rules

- Do not create a new loop unless it cannot fit as a `Loopmaster` lane.
- Do not let a replica tab invent its own summary format.
- Do not let design sharpening mutate UI without evidence and approval.
- Do not let "health green" replace a diagnostic sweep.
- Do not let the bucket fill with unlabeled artifacts.

---

## Short operating summary

`Loopmaster` is the single operating system.

The old named loops become:

- lanes inside one run
- or optional scoped replicas of the same run contract when the user opens additional tabs

That gives the project:

- less redundancy
- better health visibility
- safer reversibility
- no commitment to a fixed number of tabs or synchronous automation runs; the app’s current theme and user preferences stay governed by existing UX and settings docs, not by a separate judging system.
