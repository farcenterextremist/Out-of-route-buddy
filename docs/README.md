# Documentation index

Short index of the key docs for OutOfRouteBuddy. Start here for product, architecture, QA, deployment, and internal planning material that still applies to the Android app.

**Quick links:** [Goal & Mission](./GOAL_AND_MISSION.md) | [Self-Improvement Plan](./SELF_IMPROVEMENT_PLAN.md) | [Cursor Self-Improvement](./automation/CURSOR_SELF_IMPROVEMENT.md) | [Scope & Boundaries](./SCOPE_AND_BOUNDARIES.md) | [ROADMAP](./product/ROADMAP.md) | [CRUCIAL_IMPROVEMENTS](./CRUCIAL_IMPROVEMENTS_TODO.md) | [**Tasks index (all TODOs)**](./TASKS_INDEX.md) | [Known Truths](./agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md) | [ARCHITECTURE](./ARCHITECTURE.md)

**Structure:** Where everything lives and where to put new docs: [agents/FILE_STRUCTURE.md](./agents/FILE_STRUCTURE.md). One-off prompts and completed brainstorms: [archive/README.md](./archive/README.md).

---

## Where do I find…? (by audience)

| I am / I want… | Start here |
|----------------|------------|
| **Product / roadmap / feature briefs** | [product/ROADMAP.md](./product/ROADMAP.md), [product/](./product/) |
| **All tasks and TODOs** | [TASKS_INDEX.md](./TASKS_INDEX.md) |
| **Canonical app behavior (SSOT)** | [agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md](./agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md) |
| **Tests, coverage, failing tests** | [qa/README.md](./qa/README.md), [qa/FAILING_OR_IGNORED_TESTS.md](./qa/FAILING_OR_IGNORED_TESTS.md), [qa/TEST_FAILURES_DOCUMENTATION.md](./qa/TEST_FAILURES_DOCUMENTATION.md) |
| **Wiring, persistence, technical notes** | [technical/WIRING_MAP.md](./technical/WIRING_MAP.md), [technical/](./technical/) |
| **Improvement / token / cyber loops** | [automation/README.md](./automation/README.md), [automation/LOOP_MASTER_ROLE.md](./automation/LOOP_MASTER_ROLE.md) |
| **Security** | [security/SECURITY_NOTES.md](./security/SECURITY_NOTES.md) |
| **Release / store / go-no-go** | [release/](./release/) |
| **Dev setup (Python for scripts)** | [DEV_SETUP.md](./DEV_SETUP.md) |
| **Old prompts or completed brainstorms** | [archive/README.md](./archive/README.md), [archive/prompts/](./archive/prompts/) |
| **One-off or multi-phase plans** | [plans/README.md](./plans/README.md) |

**Rule:** New docs go in the right subfolder (product, agents, qa, technical, ux, security, release, automation, readiness, comms, archive, plans). Keep `docs/` root for this index and north-star docs only. See [agents/FILE_STRUCTURE.md](./agents/FILE_STRUCTURE.md) for the full map and rules.

---

## Core docs

| Doc | Description |
|-----|-------------|
| [TASKS_INDEX.md](./TASKS_INDEX.md) | **Single entry point for all TODOs and tasks** — CRUCIAL summary, worker todos, in-code TODOs, no duplicates. |
| [CRUCIAL_IMPROVEMENTS_TODO.md](./CRUCIAL_IMPROVEMENTS_TODO.md) | Prioritized improvements and cleanup work for the app. |
| [product/ROADMAP.md](./product/ROADMAP.md) | Product themes, feature priorities, and near-term direction. |
| [agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md](./agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md) | Canonical behavior for persistence, recovery, calendar, GPS, and settings. |
| [ARCHITECTURE.md](./ARCHITECTURE.md) | Short overview of app layers, persistence, and recovery. |

---

## Product & design

| Doc / section | Description |
|---------------|-------------|
| **product/** | Product artifacts such as `ROADMAP.md` and `FEATURE_BRIEF_*.md`. |
| **product/FEATURE_BRIEF_*.md** | One brief per major feature: problem, value, behavior, and handoffs. |

---

## Team docs

| Doc / section | Description |
|---------------|-------------|
| **agents/** | Team roles, handoff docs, known truths, and planning material. |
| **agents/README.md** | Team roster, role cards, and coordination docs. |
| [agents/TOKEN_INITIATIVE_BRIEFING.md](./agents/TOKEN_INITIATIVE_BRIEFING.md) | Token Initiative: Golden Storage Rules, Token Boss; full policy reference (all agents get essentials via always-apply rule). |
| **agents/team-structure.md** | Role responsibilities and handoff rules. |
| **agents/roles/** | One role card per team role. |
| **agents/data-sets/** | Role-specific reference material. |

---

## QA & deployment

| Doc / section | Description |
|---------------|-------------|
| **qa/** | Test strategy and test plans, including JaCoCo coverage docs. |
| [DEPLOYMENT.md](./DEPLOYMENT.md) | Build, run, and deployment guide. |

---

## Automation & improvement loops

| Doc / section | Description |
|---------------|-------------|
| **automation/** | Improvement Loop routine and self-improvement system. |
| [automation/IMPROVEMENT_LOOP_ROUTINE.md](./automation/IMPROVEMENT_LOOP_ROUTINE.md) | Phased routine: research, quick wins, tests, UI polish, summary. |
| [automation/IMPROVEMENT_LOOP_FOR_OTHER_AGENTS.md](./automation/IMPROVEMENT_LOOP_FOR_OTHER_AGENTS.md) | Best practices for any agent running the improvement loop; read at start; append to run ledger at end. |
| [automation/IMPROVEMENT_LOOP_RUN_LEDGER.md](./automation/IMPROVEMENT_LOOP_RUN_LEDGER.md) | Run ledger: append one block per improvement loop run (Focus, Variant, Summary link, Metrics, Next). |
| [automation/IMPROVEMENT_LOOP_REASONING.md](./automation/IMPROVEMENT_LOOP_REASONING.md) | Logic and reasoning: decision checkpoints, reasoning framework, traceable rationale in summaries. |
| [automation/LOOP_TIERING.md](./automation/LOOP_TIERING.md) | Task tiering: Light (auto), Medium/Heavy (user approval). |
| [automation/HEAVY_IDEAS_FAVORITES.md](./automation/HEAVY_IDEAS_FAVORITES.md) | User favorites for Heavy ideas; surface favorites first; keep Heavy list lightly populated. |
| [automation/SELF_IMPROVING_LOOP_RESEARCH.md](./automation/SELF_IMPROVING_LOOP_RESEARCH.md) | Self-improving loop methods: lessons-learned, single-improvement, metrics delta. |
| [automation/LOOP_LESSONS_LEARNED.md](./automation/LOOP_LESSONS_LEARNED.md) | Lessons from past runs; read in Phase 0, append in Phase 4. |
| [automation/DESIGN_AND_UX_RESEARCH.md](./automation/DESIGN_AND_UX_RESEARCH.md) | Design research: color schemes, templates, state flows, beautification, professionalism—integrated into loops. |
| [automation/CURSOR_SELF_IMPROVEMENT.md](./automation/CURSOR_SELF_IMPROVEMENT.md) | Safe web search, prompt-injection protections, contextualization. |
| [automation/TOKEN_REDUCTION_LOOP.md](./automation/TOKEN_REDUCTION_LOOP.md) | Token reduction: research, strategies, audit loop; run on demand or Phase 0.6. |
| [automation/TOKEN_LOOP_LISTENER.md](./automation/TOKEN_LOOP_LISTENER.md) | Token loop listener: events, wiring, test; output token_loop_events.jsonl. |
| [automation/TOKEN_LOOP_NEXT_TASKS.md](./automation/TOKEN_LOOP_NEXT_TASKS.md) | Recommended TODO tasks for next token loop (updated at Step 7 each run; read at Step 0). |
| [automation/TOKEN_LOOP_MASTER_PLAN.md](./automation/TOKEN_LOOP_MASTER_PLAN.md) | Master plan to improve the token loop; completion % and visual; all agents can see progress. |
| [automation/SANDBOX_TESTING.md](./automation/SANDBOX_TESTING.md) | Feature testing before merge; sandbox phase. |
| [automation/AUTONOMOUS_LOOP_SETUP.md](./automation/AUTONOMOUS_LOOP_SETUP.md) | Full automation (zero human intervention); Cursor Automations. |
| [automation/HEAVY_TIER_IDEAS.md](./automation/HEAVY_TIER_IDEAS.md) | Heavy-tier ideas list (cap 50) with state and true completion %. |
| [automation/LOOP_LISTENER.md](./automation/LOOP_LISTENER.md) | Loop event recording (JSONL) for data and improvement. |
| [automation/IMPROVEMENT_LOOP_AUDIT.md](./automation/IMPROVEMENT_LOOP_AUDIT.md) | Blind spots, loose ends, scope summary; re-run after major changes. |
| [qa/SIMULATIONS_AND_MOCKS.md](./qa/SIMULATIONS_AND_MOCKS.md) | Simulation and mock strategy for tests; MockGpsSynchronizationService, MockTripRepository, etc. |
| [automation/CYBER_SECURITY_LOOP_MASTER_PLAN.md](./automation/CYBER_SECURITY_LOOP_MASTER_PLAN.md) | Cyber Security Loop: completion %, phases, visual roadmap. |
| [automation/CYBER_SECURITY_DATA_SUMMARY.md](./automation/CYBER_SECURITY_DATA_SUMMARY.md) | **Cyber Security data: what we collected, what it means, how to use it.** |
| [automation/LOOP_MASTER_ROLE.md](./automation/LOOP_MASTER_ROLE.md) | **Loop Master:** authority for how all loops run; Step 0.M when "start master loop". |
| [automation/LOOPS_AND_IMPROVEMENT_FULL_AXIS.md](./automation/LOOPS_AND_IMPROVEMENT_FULL_AXIS.md) | Full axis: all loops, triggers, outputs; used by Loop Master (Step 0.M). |
| [agents/data-sets/hub/README.md](./agents/data-sets/hub/README.md) | **Hub:** shared polished data; index of reports and proof of work; "send to hub" = write here. |
| [agents/data-sets/hub/UNIVERSAL_LOOP_PROMPT.md](./agents/data-sets/hub/UNIVERSAL_LOOP_PROMPT.md) | **Universal Loop Prompt:** every loop consults Hub + Loop Master, minimizes slop. |
| [agents/HUB_AND_LOOP_FUTURE_TODOS.md](./agents/HUB_AND_LOOP_FUTURE_TODOS.md) | **63 future TODOs:** metadata loading, Hub evolution, Loop Master, cross-loop consistency, slop minimization, onboarding, scripts, metrics. |

---

## Readiness — plateau, finished state, progress bar

| Doc / section | Description |
|---------------|-------------|
| **readiness/** | When the Improvement Loop has plateaued and shipping is easy; what "finished" or "perfected" looks like; grand progress bar. |
| [readiness/README.md](./readiness/README.md) | Index: plateau, finished state, grand progress bar. |
| [readiness/PLATEAU_AND_SHIPPING_EASY.md](./readiness/PLATEAU_AND_SHIPPING_EASY.md) | When we've plateaued on the loop and shipping is low-friction. |
| [readiness/WHAT_FINISHED_LOOKS_LIKE.md](./readiness/WHAT_FINISHED_LOOKS_LIKE.md) | What "finished" / "perfected" looks like — success state and quality bar. |
| [readiness/GRAND_PROGRESS_BAR.md](./readiness/GRAND_PROGRESS_BAR.md) | Grand progress bar: 10 parameters, green/amber/red; update at milestones or each loop summary. |

---

## Security & ops

| Doc / section | Description |
|---------------|-------------|
| **security/** | Security notes, checklists, and hardening guidance. |
| **comms/** | General communication copy and subject-line templates. |
| **ONEDRIVE_AND_GIT_SETUP.md** | OneDrive and Git setup notes for this workspace. |
