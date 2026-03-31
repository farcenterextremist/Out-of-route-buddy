# Loop role cube

**Purpose:** Organize loop roles methodically so the project treats loop operations like a solved system instead of a pile of overlapping helpers.

**Why "cube":** A Rubik's cube works because each face has a clear identity, but every move affects the whole. The loop system should work the same way: separate responsibilities, shared center, coordinated movement.

**Center of the cube:** `Loop Master` / `Loopmaster` and the universal loop contract.

**Related:** `LOOP_MASTER_ROLE.md`, `LOOP_GATES.md`, `LOOP_CONSISTENCY_STANDARD.md`, `LOOP_HEALTH_CHECKS.md`, `LOOP_DIAGNOSTIC_SWEEP.md`, `LOOPMASTER_TAB_AND_SPAWN_MODEL.md`, `docs/ux/PLEASANTNESS_AND_FLOW_STANDARD.md`, `docs/ux/SCREENSHOT_REVIEW_WORKFLOW.md`.

---

## Research basis used

This role map is grounded in cautious web research from reputable guidance:

- **Architecture governance:** AWS guidance favors distributed governance, asynchronous review, versioned blueprints, and architecture decision records instead of brittle one-time signoff.
- **Health management:** Kubernetes-style liveness/readiness separation is strong practice; liveness should stay cheap and local, readiness can include external dependencies and proceed/no-proceed decisions.
- **Consistency auditing:** Checklist-based governance works best when it validates actual operations, not just strategy or intent.
- **Diagnostics:** Root-cause-analysis guidance emphasizes precise problem statements, evidence gathering, sequence mapping, and residual-risk tracking.
- **Frontend pleasantness:** Android/Material guidance emphasizes consistent spacing, containment, alignment, reachable primary actions, and clear interaction states.
- **Screenshot review:** Current best practice supports screenshot evidence and future visual regression baselines; in this repo, screenshot review is manual-evidence-first until a Paparazzi-style system is enabled.

---

## Cube overview

| Face | Role | Primary question | Main artifact |
|---|---|---|---|
| **Center** | Loop Master | Are all loops aligned under one authority? | master-loop summaries / universal files |
| **Front** | Loop Architect | Is the structure of the loop system sound? | architecture review / gate design notes |
| **Right** | Loop Health Manager | Can the loop proceed safely and what health risk remains? | loop health report |
| **Left** | Loop Consistency Auditor | Did this run follow the universal contract? | consistency check / score |
| **Top** | Loop Diagnostic Sweeper | What problems or weak spots are hiding? | diagnostic sweep |
| **Bottom** | Loop Quality Proof | What evidence proves this run is good enough? | proof-of-quality block |
| **Back** | Frontend Pleasantness Reviewer | Does the UI feel clear, calm, and low-friction? | pleasantness review |
| **Visual lens** | Frontend Screenshot Reviewer | What does the UI actually look like in evidence, not memory? | screenshot review |

---

## Role-by-role placement

### 1. Loop Master

**Job:** Own the center. Define authority, universal reads/writes, hub usage, and cross-loop alignment.

**Use when:**
- running `start master loop`
- aligning all loops
- updating universal rules or standards

**Should care about:**
- start/end gates
- hub usage
- shared-state policy
- role overlap and drift

### 2. Loop Architect

**Job:** Own structure and evolution.

**Research-backed responsibilities:**
- prefer standards and reusable blueprints over ad hoc exceptions
- use explicit contracts and versioned changes
- shift from brittle one-time signoff toward distributed, reviewable governance

**Owns:**
- phase structure
- gate semantics
- shared-state evolution strategy
- fallback / rollback planning

### 3. Loop Health Manager

**Job:** Own operational health.

**Research-backed responsibilities:**
- keep liveness cheap and deterministic
- keep readiness meaningful and stage-aware
- separate "alive" from "ready"
- report residual risk even when checks are green

**Owns:**
- liveness
- readiness
- continuity preconditions
- health report quality

### 4. Loop Consistency Auditor

**Job:** Own contract compliance.

**Research-backed responsibilities:**
- use repeatable checklists
- verify evidence, not claims
- score drift visibly so weak runs are corrected next cycle

**Owns:**
- consistency score
- contract pass/fail evidence
- summary/ledger/shared-state alignment

### 5. Loop Diagnostic Sweeper

**Job:** Own proactive problem hunting.

**Research-backed responsibilities:**
- define the problem precisely
- gather logs, lints, failing tests, and hotspot evidence
- map likely failure paths
- record one residual risk and one next diagnostic action

**Owns:**
- hotspot search
- ignored/failing test triage
- residual-risk naming
- debug-oriented findings

### 6. Loop Quality Proof

**Job:** Own proof, not optimism.

**Owns:**
- evidence bundle quality
- claim -> evidence -> risk -> next-step discipline
- final quality grading

### 7. Frontend Pleasantness Reviewer

**Job:** Own the "pleasant and professional" standard.

**Research-backed responsibilities:**
- judge hierarchy, spacing, state clarity, task flow, and calmness
- apply hard-stop accessibility rules first
- avoid taste-only judgments

**Owns:**
- pleasantness score
- flow score
- strongest/weakest area naming

### 8. Frontend Screenshot Reviewer

**Job:** Own visual evidence.

**Research-backed responsibilities:**
- prefer before/after and theme/state comparisons
- review what is visibly present, not hidden assumptions
- support future screenshot regression workflows

**Owns:**
- screenshot evidence packs
- visual comparisons
- screenshot review blocks

---

## Methodical organization rule

When a loop task appears, decide which face of the cube owns it **first**:

1. **Structure problem?** -> `Loop Architect`
2. **Health or readiness problem?** -> `Loop Health Manager`
3. **Contract drift problem?** -> `Loop Consistency Auditor`
4. **Hidden-risk/problem-hunt problem?** -> `Loop Diagnostic Sweeper`
5. **Evidence/grade problem?** -> `Loop Quality Proof`
6. **UI feel problem?** -> `Frontend Pleasantness Reviewer`
7. **Visual evidence problem?** -> `Frontend Screenshot Reviewer`

If more than one face applies, pick:

- one **primary owner**
- one **secondary helper**

Avoid assigning three owners unless the run is explicitly a broad audit.

---

## What this means in practice

- `Loop Master` / `Loopmaster` is the center and should not do every specialist task personally.
- `Loop Architect` designs change.
- `Loop Health Manager` keeps the run alive and honest.
- `Loop Diagnostic Sweeper` searches for what tests alone miss.
- `Loop Consistency Auditor` prevents process drift.
- `Loop Quality Proof` prevents fake confidence.
- `Frontend Pleasantness Reviewer` prevents "looks good to me" slop.
- `Frontend Screenshot Reviewer` turns UI review into visible evidence.

In the unified super-loop model, these roles become lanes or inline helpers under one `Loopmaster` authority instead of separate competing loop identities.

In the default neighboring-tab pattern:

- `ArchitectTab` combines `Loop Master` and `Loop Architect`
- `Builder` handles execution-heavy work
- `Optimizer` handles improvement-heavy work
- `Guard` handles defense and risky-drift review
- `Watcher` stays independent and maps most closely to `Loop Quality Proof`

---

## Current mini-loop findings

- `improvement`, `cyber_security`, and `synthetic_data` latest-state files exist and are usable.
- `token.json` still has placeholder `null` fields and needs a fresh token-loop refresh.
- `file_organizer.json` is missing, which is a minor shared-state completeness gap.

---

## Next upgrades

1. Add a `loop role org chart` reference from the main loop docs.
2. Refresh stale loop latest-state files during the next relevant run.
3. Eventually add screenshot-regression tooling so the screenshot reviewer can move from manual evidence to baseline verification.
