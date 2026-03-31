---
name: loopmaster-orchestrator
description: Orchestrates the single Loopmaster super-loop using one shared contract for gates, health, diagnostics, data bucket evidence, and device-aware design sharpening. Use when the user mentions loopmaster, super loop, giant loop, unified loop, or wants multiple loop tabs to follow one design. No fixed tab count or synchronous automation run is committed.
---

# Loopmaster Orchestrator

## Purpose

Use one authoritative orchestration model instead of treating each loop as a separate species.

Primary references:

- `docs/automation/LOOPMASTER_SUPER_LOOP_BLUEPRINT.md`
- `docs/automation/LOOPMASTER_TAB_AND_SPAWN_MODEL.md`
- `docs/automation/LOOP_MASTER_ROLE.md`
- `docs/automation/LOOP_GATES.md`
- `docs/automation/LOOP_HEALTH_CHECKS.md`
- `docs/automation/hyper-loop-bucket/README.md`
- `docs/automation/LOOP_ROLE_CUBE.md`

## Trigger

Use this skill when requests mention:

- "loopmaster"
- "super loop"
- "giant loop"
- "hyper loop"
- "one loop instead of many"
- "duplicate the loop across tabs"
- "grand design for loops"

## Core rule

Treat `Loopmaster` as the only real loop system.

Other loop identities become either:

- internal lanes under `Loopmaster`, or
- future replicas of the same contract in additional tabs

Do not design parallel tabs as custom one-off workflows.

## Workflow

1. **Define the run mode**
   - `Micro`, `Standard`, or `Hyper`
   - State the scope, success condition, and rollback intent

2. **Load the contract**
   - Read master, gates, health, shared-state, and role-cube docs
   - Identify the primary lane owner and any secondary helper lane

3. **Apply gates and health**
   - Start gate
   - Liveness/readiness expectations
   - Diagnostic sweep requirement
   - Continuity requirement if loop docs/scripts/shared-state changed

4. **Assign lanes**
   - Build/code lane
   - Diagnostic/security lane
   - Proof/consistency lane
   - Data/artifact lane
   - Design sharpening lane when a device is connected

5. **Use the tab model when splitting work**
   - `ArchitectTab` stays parent
   - `Builder`, `Optimizer`, and `Guard` get partitioned scopes
   - `Watcher` remains independent and usually readonly
   - Avoid multiple writers for the same file family

6. **Use the bucket**
   - Record manifests, screenshots, shared-state snapshots, and rollback notes in the hyper-loop bucket when the run produces durable artifacts

7. **Scale carefully**
   - For future multi-tab runs, give each tab the same contract with different scope
   - Parent tab remains accountable even if it spawns inline agents

## Design sharpening rule

Only enable design sharpening when there is real device evidence:

- connected Android device or instrument
- screenshot capture possible
- no unwarranted UI restructuring

Use screenshot evidence plus the pleasantness rubric before recommending beautification changes.

## Output format

```markdown
## Loopmaster Run Design

- Mode: [micro | standard | hyper]
- Primary lane: [name]
- Secondary lane: [name or none]
- Gates required: [list]
- Health model: [liveness/readiness/diagnostic]
- Bucket use: [what will be stored]
- Design sharpening: [enabled/skipped + reason]
- Scale path: [how this becomes a multi-tab run later]
```

## Guardrails

- Do not let a replica tab invent its own contract.
- Do not mark a run reversible unless artifacts and touched-file evidence were captured.
- Do not treat health checks as a replacement for diagnostics.
- Do not auto-ship major UI changes from design sharpening.
- Do not let multiple worker tabs edit the same file family at once.
- Do not let `Watcher` become the primary implementation tab.
