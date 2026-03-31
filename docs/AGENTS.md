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
| [docs/automation/LOOP_GATES.md](docs/automation/LOOP_GATES.md) | **Required at start and end of every loop** — read order, Hub/shared state, log "Hub consulted" & "Advice applied"; at end: hub deposit, hub/README.md, loop_shared_events.jsonl, loop_latest/&lt;loop&gt;.json |

**Improvement Loop — find everything:** [docs/automation/IMPROVEMENT_LOOP_INDEX.md](docs/automation/IMPROVEMENT_LOOP_INDEX.md) (master index of all loop docs, scripts, .cursor rules/skills).

### Android UX & Material 3

| Skill | Use when |
|-------|----------|
| [android-m3-design-study](.cursor/skills/android-m3-design-study/SKILL.md) | Structured intermediate study (weeks, habits, doc links; see `reference.md`). |
| [android-material-ui-audit](.cursor/skills/android-material-ui-audit/SKILL.md) | Theme/layout/a11y audit before or alongside frontend fixes. |
| [frontend-pleasantness-reviewer](.cursor/skills/frontend-pleasantness-reviewer/SKILL.md) | Rubric-based pleasantness and flow. |
| [frontend-screenshot-reviewer](.cursor/skills/frontend-screenshot-reviewer/SKILL.md) | Evidence-based visual review from screenshots. |

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

## Closing crew (Codey)

When you want **code beautification**, **structure polish**, or **micro refactors** (not loop runs):

**Rule:** [.cursor/rules/codey.mdc](.cursor/rules/codey.mdc). **Brief:** [docs/agents/data-sets/hub/2026-03-12_codey_code-structure-and-compiler-brief.md](docs/agents/data-sets/hub/2026-03-12_codey_code-structure-and-compiler-brief.md).

Codey is part of the **closing crew** — final code polish and micro changes after a feature or loop. Codey does **not** run loops or act as Loop Master; loop orchestration stays with [LOOP_MASTER_ROLE.md](docs/automation/LOOP_MASTER_ROLE.md).

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
2. **Research first.** Phase 0 must complete before Simulate. (A) Cyber security: CYBER_SECURITY_RESEARCH, web search, ATTACK_LIBRARY, CYBER_SECURITY_LOOP_AUDIT. (B) Loop improvement: LOOP_LESSONS_LEARNED, SELF_IMPROVING_LOOP_RESEARCH, CURSOR_SELF_IMPROVEMENT. Note what you applied.
3. Read [docs/automation/CYBER_SECURITY_LOOP_MASTER_PLAN.md](docs/automation/CYBER_SECURITY_LOOP_MASTER_PLAN.md) at loop start (completion %, next items).
4. Read [docs/automation/CYBER_SECURITY_LOOP_COMMON_SENSE.md](docs/automation/CYBER_SECURITY_LOOP_COMMON_SENSE.md) (checkpoint first, scope lock, no destructive actions)
5. Follow [docs/automation/CYBER_SECURITY_LOOP_ROUTINE.md](docs/automation/CYBER_SECURITY_LOOP_ROUTINE.md) (Research → Simulate → Purple → Improve)
6. Run `./gradlew securitySimulations` for automated attack simulations
7. Proof of work: [docs/agents/security-team-proof-of-work.md](docs/agents/security-team-proof-of-work.md)

---

## LLM loop / Token loop

When user says **"start llm loop"** or **"start token loop"** (or "run token reduction loop" / "token audit"):

**Skill:** Invoke **token-conservationist** (personal skill) for context compression and token-saving patterns.

1. **Record current state** — Run `.\scripts\automation\token_loop_state_snapshot.ps1 -RunId <run_id>`. Snapshot → `docs/automation/token_loop_snapshots/<run_id>.json` (rollback + progress tracking). Use the **same RunId** for all listener events this run.
2. **Start the loop entrypoint** — For the permanent top-level local-first path, prefer `.\scripts\automation\run_llm_loop.ps1`. For the stable token-audit lane directly, `.\scripts\automation\run_token_loop.ps1` is still valid. Both preserve the same token listener and state history.
3. Run **steps 0–7** from [docs/automation/TOKEN_REDUCTION_LOOP.md](docs/automation/TOKEN_REDUCTION_LOOP.md): Step 0 = deep research and analysis; Step 7 = organize results and recommend next tasks in TOKEN_LOOP_NEXT_TASKS.md. Invoke the listener at each step and at end (token_loop_end). No human in the loop.
4. **Goals:** Save token spend; manage context squish; keep local-first LLM workflow useful and scriptable in Cursor. Listener data (`token_loop_events.jsonl`) is used to improve the loop. All agents get Golden Storage Rules and Token Boss via the always-apply rule (self-improvement.mdc); [Token Initiative policy reference](docs/agents/TOKEN_INITIATIVE_BRIEFING.md). Token saving recommendations (easy to see): briefing §Token saving recommendations, [TOKEN_LOOP_NEXT_TASKS](docs/automation/TOKEN_LOOP_NEXT_TASKS.md), [TOKEN_LOOP_MASTER_PLAN](docs/automation/TOKEN_LOOP_MASTER_PLAN.md) (completion % and visual). Default local provider = `Ollama` on the desktop/Cursor side; the Android app itself is still just the codebase being edited.

---

## Synthetic Data Loop

When user says **"Start Synthetic data loop"**, **"START DATA LOOP"**, or "run synthetic data loop" / "data loop":

**Prepare/warm up:** [docs/automation/DATA_LOOP_WARMUP.md](docs/automation/DATA_LOOP_WARMUP.md) — checklist and rules before running the loop.

1. Read [docs/automation/SYNTHETIC_DATA_LOOP_FOR_OTHER_AGENTS.md](docs/automation/SYNTHETIC_DATA_LOOP_FOR_OTHER_AGENTS.md) and follow the best practices there.
2. Read [docs/automation/SYNTHETIC_DATA_LOOP_MASTER_PLAN.md](docs/automation/SYNTHETIC_DATA_LOOP_MASTER_PLAN.md) (trigger, scope, phases, data-tier linkage).
3. Follow [docs/automation/SYNTHETIC_DATA_LOOP_ROUTINE.md](docs/automation/SYNTHETIC_DATA_LOOP_ROUTINE.md) (phases 0–4).
4. Do not apply tier changes (`setTripTier`, `deleteTripsOlderThan`) until user approves the pruning/mesh proposal.
5. At the end of every run, append one block to [docs/automation/SYNTHETIC_DATA_LOOP_RUN_LEDGER.md](docs/automation/SYNTHETIC_DATA_LOOP_RUN_LEDGER.md) (template in that file).

*(Data tiers: [docs/DATA_TIERS.md](docs/DATA_TIERS.md).)*

---

## Send to hub

When the user says **"send to hub"**:

1. Save the **precious data** (your completed, polished output) to **`docs/agents/data-sets/hub/`**.
2. Name files clearly: **`YYYY-MM-DD_<role-or-topic>_<short-description>.<ext>`** (e.g. `2026-03-12_data-loop_loop2-proof-and-benefits.md`).
3. Optionally add a one-line entry to the Hub index in [docs/agents/data-sets/hub/README.md](docs/agents/data-sets/hub/README.md).
4. **Hub = this data folder.** Not GitHub. "Send to hub" means: write to `docs/agents/data-sets/hub/`.

*(Full prompt: [docs/agents/data-sets/hub/SEND_TO_HUB_PROMPT.md](docs/agents/data-sets/hub/SEND_TO_HUB_PROMPT.md).)*

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
