# OutOfRouteBuddy — Start Here

**Mission:** OutOfRouteBuddy gives drivers advanced analytics and tracking for out-of-route miles.

**Success:** Downloads, useful data for users, iPhone requests.

**Never:** Social features, ads, cloud-first.

---

## Quick links

| Doc | Purpose |
|-----|---------|
| [docs/README.md](docs/README.md) | Documentation index |
| [docs/GOAL_AND_MISSION.md](docs/GOAL_AND_MISSION.md) | Goal, mission, success criteria |
| [docs/agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md](docs/agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md) | Canonical behavior (persistence, recovery, calendar, GPS) |
| [docs/CRUCIAL_IMPROVEMENTS_TODO.md](docs/CRUCIAL_IMPROVEMENTS_TODO.md) | Prioritized improvements |
| [docs/SELF_IMPROVEMENT_PLAN.md](docs/SELF_IMPROVEMENT_PLAN.md) | Self-improvement pillars (run when user directs) |
| [docs/automation/CURSOR_SELF_IMPROVEMENT.md](docs/automation/CURSOR_SELF_IMPROVEMENT.md) | Safe web search, prompt-injection protections, contextualization |
| [docs/automation/SANDBOX_TESTING.md](docs/automation/SANDBOX_TESTING.md) | Feature testing before merge; sandbox phase |
| [docs/automation/AUTONOMOUS_LOOP_SETUP.md](docs/automation/AUTONOMOUS_LOOP_SETUP.md) | Full automation (zero human intervention) |

**Improvement Loop — find everything:** [docs/automation/IMPROVEMENT_LOOP_INDEX.md](docs/automation/IMPROVEMENT_LOOP_INDEX.md) (master index of all loop docs, scripts, .cursor rules/skills).

---

## Improvement Loop

When user says **GO** (or "run improvement loop"):

**Skill:** Invoke **improvement-loop-wizard** (`.cursor/skills/improvement-loop-wizard/SKILL.md`) for step-by-step wizard flow.

1. **Check todos upon initiation** — CRUCIAL_IMPROVEMENTS_TODO, TOKEN_LOOP_NEXT_TASKS, in-progress todos.
2. Read [docs/automation/IMPROVEMENT_LOOP_FOR_OTHER_AGENTS.md](docs/automation/IMPROVEMENT_LOOP_FOR_OTHER_AGENTS.md) and follow the best practices there.
3. Read [docs/automation/IMPROVEMENT_LOOP_COMMON_SENSE.md](docs/automation/IMPROVEMENT_LOOP_COMMON_SENSE.md) (checkpoint first, tests green, full autonomy)
4. Read [docs/automation/IMPROVEMENT_LOOP_REASONING.md](docs/automation/IMPROVEMENT_LOOP_REASONING.md) (logic and reasoning; think before you act)
5. Follow [docs/automation/IMPROVEMENT_LOOP_ROUTINE.md](docs/automation/IMPROVEMENT_LOOP_ROUTINE.md)
6. At the end of every run, append one block to [docs/automation/IMPROVEMENT_LOOP_RUN_LEDGER.md](docs/automation/IMPROVEMENT_LOOP_RUN_LEDGER.md) (see template in that file).
7. For autonomy: [docs/automation/AUTONOMOUS_LOOP_SETUP.md](docs/automation/AUTONOMOUS_LOOP_SETUP.md)

*(Full index: [docs/automation/IMPROVEMENT_LOOP_INDEX.md](docs/automation/IMPROVEMENT_LOOP_INDEX.md).)*

---

## Master Loop (Loop Master)

When user says **"start master loop"**:

1. **You are the Loop Master.** Read [docs/automation/LOOP_MASTER_ROLE.md](docs/automation/LOOP_MASTER_ROLE.md).
2. **Step 0.M** — Research all other loops (Improvement, Token, Cyber Security, Synthetic Data). Compare, analyze, scrutinize their routines and ledgers.
3. **Update universal files** so all loop-bearing agents can follow: IMPROVEMENT_LOOP_BEST_PRACTICES.md, IMPROVEMENT_LOOP_FOR_OTHER_AGENTS.md, RUN_LEDGER template, LOOP_TIERING.md as needed.
4. **Then run the full Improvement Loop** (checkpoint → Phase 0–4 → summary → append RUN_LEDGER). Optionally add "Loop Master findings" to the summary.

---

## Cyber Security Loop

When user says **"Run Cyber Security Loop"** (or "GO security"):

**Skill:** Invoke **purple-orchestrator-skill** (`.cursor/skills/purple-orchestrator-skill/SKILL.md`) for full Purple flow when running agent-driven exercises.

1. **Check todos upon initiation** — CRUCIAL_IMPROVEMENTS_TODO, TOKEN_LOOP_NEXT_TASKS, CYBER_SECURITY_LOOP_AUDIT, in-progress todos.
2. Read [docs/automation/CYBER_SECURITY_LOOP_COMMON_SENSE.md](docs/automation/CYBER_SECURITY_LOOP_COMMON_SENSE.md) (checkpoint first, scope lock, no destructive actions)
2. Follow [docs/automation/CYBER_SECURITY_LOOP_ROUTINE.md](docs/automation/CYBER_SECURITY_LOOP_ROUTINE.md) (Research → Simulate → Purple → Improve)
3. Run `./gradlew securitySimulations` for automated attack simulations
4. Proof of work: [docs/agents/security-team-proof-of-work.md](docs/agents/security-team-proof-of-work.md)

---

## Token loop

When user says **"start token loop"** (or "run token reduction loop" / "token audit"):

**Skill:** Invoke **token-conservationist** (personal skill) for context compression and token-saving patterns.

1. **Record current state** — Run `.\scripts\automation\token_loop_state_snapshot.ps1 -RunId <run_id>`. Snapshot → `docs/automation/token_loop_snapshots/<run_id>.json` (rollback + progress tracking). Use the **same RunId** for all listener events this run.
2. **Start the listener** — Run `.\scripts\automation\run_token_loop.ps1` (it runs state snapshot + token_loop_start and prints RunId), or invoke `token_loop_listener.ps1 -Event token_loop_start` with the same RunId.
3. Run **steps 0–7** from [docs/automation/TOKEN_REDUCTION_LOOP.md](docs/automation/TOKEN_REDUCTION_LOOP.md): Step 0 = deep research and analysis; Step 7 = organize results and recommend next tasks in TOKEN_LOOP_NEXT_TASKS.md. Invoke the listener at each step and at end (token_loop_end). No human in the loop.
4. **Goals:** Save token spend; manage context squish. Listener data (`token_loop_events.jsonl`) is used to improve the loop. All agents get Golden Storage Rules and Token Boss via the always-apply rule (self-improvement.mdc); [Token Initiative policy reference](docs/agents/TOKEN_INITIATIVE_BRIEFING.md). Token saving recommendations (easy to see): briefing §Token saving recommendations, [TOKEN_LOOP_NEXT_TASKS](docs/automation/TOKEN_LOOP_NEXT_TASKS.md), [TOKEN_LOOP_MASTER_PLAN](docs/automation/TOKEN_LOOP_MASTER_PLAN.md) (completion % and visual).

---

## Synthetic Data Loop

When user says **"Start Synthetic data loop"**, **"START DATA LOOP"**, or "run synthetic data loop" / "data loop":

1. Read [docs/automation/SYNTHETIC_DATA_LOOP_FOR_OTHER_AGENTS.md](docs/automation/SYNTHETIC_DATA_LOOP_FOR_OTHER_AGENTS.md) and follow the best practices there.
2. Read [docs/automation/SYNTHETIC_DATA_LOOP_MASTER_PLAN.md](docs/automation/SYNTHETIC_DATA_LOOP_MASTER_PLAN.md) (trigger, scope, phases, data-tier linkage).
3. Follow [docs/automation/SYNTHETIC_DATA_LOOP_ROUTINE.md](docs/automation/SYNTHETIC_DATA_LOOP_ROUTINE.md) (phases 0–4).
4. Do not apply tier changes (`setTripTier`, `deleteTripsOlderThan`) until user approves the pruning/mesh proposal.
5. At the end of every run, append one block to [docs/automation/SYNTHETIC_DATA_LOOP_RUN_LEDGER.md](docs/automation/SYNTHETIC_DATA_LOOP_RUN_LEDGER.md) (template in that file).

*(Data tiers: [docs/DATA_TIERS.md](docs/DATA_TIERS.md).)*

---

## Pre-release / Ship

**Skills:** Invoke **shipping-specialist** (versioning, release notes, Play Store) and **security-analyst-agent** (pre-release checklist). For ATT&CK/ATLAS coverage report: **coverage-report-skill** (`.cursor/skills/coverage-report-skill/SKILL.md`).

---

## Build & test

```bash
./gradlew assembleDebug
./gradlew :app:testDebugUnitTest
```

---

*Solo drivers first; fleet management later. See docs/GOAL_AND_MISSION.md for full context.*
