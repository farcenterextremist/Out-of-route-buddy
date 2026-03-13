# Automation — Improvement loops, pulse, and ship instructions

This folder holds **all Improvement Loop docs** (routine, tiering, autonomy, summaries) and backs the **8-hour pulse run** and **ship instructions** for OutOfRouteBuddy.

---

## Find everything → [IMPROVEMENT_LOOP_INDEX.md](./IMPROVEMENT_LOOP_INDEX.md)

**Use the index for maximum findability.** It lists every related file, script, and folder with purpose and location. When you say **GO**, start from the index § 1 "Start here."

---

## Quick start

1. **Improvement loop (when user says GO)**
   - **First time:** [AUTONOMOUS_LOOP_SETUP.md](./AUTONOMOUS_LOOP_SETUP.md) for no-human-intervention.
   - **Every run:** [IMPROVEMENT_LOOP_COMMON_SENSE.md](./IMPROVEMENT_LOOP_COMMON_SENSE.md) → [IMPROVEMENT_LOOP_REASONING.md](./IMPROVEMENT_LOOP_REASONING.md) → [IMPROVEMENT_LOOP_ROUTINE.md](./IMPROVEMENT_LOOP_ROUTINE.md).

2. **8-hour run** (pulse every 30 min, ship doc in last 2 hr):
   ```powershell
   cd c:\Users\brand\OutofRoutebuddy
   .\scripts\automation\run_8hr_automation.ps1
   ```

3. **Pulse or ship instructions only:** see [IMPROVEMENT_LOOP_INDEX.md](./IMPROVEMENT_LOOP_INDEX.md) § 5 Scripts.

---

## Files in this folder (by group)

### Start here & core routine
| File | Purpose |
|------|--------|
| [LOOPS_AND_IMPROVEMENT_FULL_AXIS.md](./LOOPS_AND_IMPROVEMENT_FULL_AXIS.md) | **Full axis** — all loops (Improvement, Token, Cyber Security), hub, sandbox; triggers, phases, outputs, logic. |
| [ALL_LOOPS_FOR_AGENTS_AND_WORKFLOW.md](./ALL_LOOPS_FOR_AGENTS_AND_WORKFLOW.md) | **All loops for each agent** — who runs which loop, full workflow (trigger → phases/steps → outputs). |
| [IMPROVEMENT_LOOP_INDEX.md](./IMPROVEMENT_LOOP_INDEX.md) | **Master index** — find every loop-related file and script. |
| [LOOP_MASTER_ROLE.md](./LOOP_MASTER_ROLE.md) | **Loop Master** — trigger: "start master loop". Research all loops, update universal files, then run Improvement Loop. |
| [IMPROVEMENT_LOOP_ROUTINE.md](./IMPROVEMENT_LOOP_ROUTINE.md) | **Routine** — phases 0–4, run when user says **GO**. |
| [IMPROVEMENT_LOOP_COMMON_SENSE.md](./IMPROVEMENT_LOOP_COMMON_SENSE.md) | Checkpoint first, tests green, full autonomy for Light+Medium. |
| [IMPROVEMENT_LOOP_REASONING.md](./IMPROVEMENT_LOOP_REASONING.md) | Logic and reasoning checkpoints; think before you act. |
| [LOOP_HEALTH_CHECKS.md](./LOOP_HEALTH_CHECKS.md) | **Health checks** — liveness at phase start, readiness (pulse) at phase end; run constantly during loops. |
| [LOOPS_FULL_VERBOSE_GUIDE.md](./LOOPS_FULL_VERBOSE_GUIDE.md) | **Full verbose guide** — where loops exist, how they're controlled, what task types they request. |
| [USER_PREFERENCES_AND_DESIGN_INTENT.md](./USER_PREFERENCES_AND_DESIGN_INTENT.md) | Design intent, must-not-change. Read first (Phase 0.0a). |

### Tiering, focus, metrics
| File | Purpose |
|------|--------|
| [LOOP_TIERING.md](./LOOP_TIERING.md) | Light / Medium / Heavy; question lock; visual approval; revert. |
| [IMPROVEMENT_LOOP_TEAMS.md](./IMPROVEMENT_LOOP_TEAMS.md) | Researchers, File Organizer; Heavy needs human approval. |
| [LOOP_FOCUS_ROTATION.md](./LOOP_FOCUS_ROTATION.md) | Focus areas and rotation (Security, UI/UX, Shipability, etc.). |
| [LOOP_METRICS_TEMPLATE.md](./LOOP_METRICS_TEMPLATE.md) | Metrics per run: tests, lint, checkpoint. |
| [LOOP_FRONTEND_VS_BACKEND_BREAKDOWN.md](./LOOP_FRONTEND_VS_BACKEND_BREAKDOWN.md) | Target mostly backend (~75–85%); frontend only when obvious. |

### Self-improvement, design, token
| File | Purpose |
|------|--------|
| [CURSOR_SELF_IMPROVEMENT.md](./CURSOR_SELF_IMPROVEMENT.md) | Safe web, Phase 0.3 self-improvement, prompt-injection awareness. |
| [DESIGN_AND_UX_RESEARCH.md](./DESIGN_AND_UX_RESEARCH.md) | Design research for Phase 0.4 / Phase 3. *(If missing, use docs/ux/UI_CONSISTENCY.md.)* |
| [TOKEN_REDUCTION_LOOP.md](./TOKEN_REDUCTION_LOOP.md) | Token audit; optional Phase 0.6. *(If missing, skip.)* |

### Sandbox & heavy ideas
| File | Purpose |
|------|--------|
| [SANDBOX_TESTING.md](./SANDBOX_TESTING.md) | Feature testing before merge; sandbox phase. |
| [SANDBOX_COMPLETION_PERCENTAGE.md](./SANDBOX_COMPLETION_PERCENTAGE.md) | True completion % for sandboxed ideas. |
| [HEAVY_IDEAS_FAVORITES.md](./HEAVY_IDEAS_FAVORITES.md) | Favorites table; cap ~50; produce vs judge/critique. |
| [HEAVY_TIER_TODO_LIST_REFINED.md](./HEAVY_TIER_TODO_LIST_REFINED.md) | **Refined list** of all Heavy-tier todos (FUTURE_IDEAS + CRUCIAL + tiering). |

### Autonomy & allowlist
| File | Purpose |
|------|--------|
| [AUTONOMOUS_LOOP_SETUP.md](./AUTONOMOUS_LOOP_SETUP.md) | One-time: Run Everything or allowlist. |
| [LOOP_MASTER_ALLOWLIST.md](./LOOP_MASTER_ALLOWLIST.md) | Commands for allowlist; `cd c:\...\OutofRoutebuddy` prefix. |
| [LOOP_LISTENER.md](./LOOP_LISTENER.md) | Event recording (loop_listener.ps1 → loop_events.jsonl). *(If missing, see routine.)* |
| [LOOP_VARIANTS.md](./LOOP_VARIANTS.md) | Quick / Standard / Full. *(If missing, see routine § Variants.)* |

### Analysis, audit, summaries
| File | Purpose |
|------|--------|
| [IMPROVEMENT_LOOP_ANALYSIS_AND_IMPROVEMENTS.md](./IMPROVEMENT_LOOP_ANALYSIS_AND_IMPROVEMENTS.md) | Analysis, improvements, priming for next run. |
| [IMPROVEMENT_LOOP_AUDIT.md](./IMPROVEMENT_LOOP_AUDIT.md) | Blind spots, loose ends; re-run after major changes. |
| **IMPROVEMENT_LOOP_SUMMARY_&lt;date&gt;.md** | A-grade summary per run. |
| **120_MINUTE_LOOP_SUMMARY_&lt;date&gt;.md** | Same; alternate naming. Example: [120_MINUTE_LOOP_SUMMARY_2025-03-11.md](./120_MINUTE_LOOP_SUMMARY_2025-03-11.md). |

### For other agents & recorded data (append every run)

| File | Purpose |
|------|--------|
| [LOOP_DYNAMIC_SHARING.md](./LOOP_DYNAMIC_SHARING.md) | **Shared state** — when loops run together: read loop_shared_events.jsonl (tail) and loop_latest/*.json at start; append event and update loop_latest/&lt;your_loop&gt;.json at end. Research: [LOOP_DYNAMIC_SHARING_RESEARCH.md](./LOOP_DYNAMIC_SHARING_RESEARCH.md). |
| [IMPROVEMENT_LOOP_FOR_OTHER_AGENTS.md](./IMPROVEMENT_LOOP_FOR_OTHER_AGENTS.md) | **Entry point** for other agents: best practices, recorded data, key docs. |
| [IMPROVEMENT_LOOP_BEST_PRACTICES.md](./IMPROVEMENT_LOOP_BEST_PRACTICES.md) | Best practices for building your own loop; what to record every run. |
| [IMPROVEMENT_LOOP_RUN_LEDGER.md](./IMPROVEMENT_LOOP_RUN_LEDGER.md) | **Run ledger** — append one block per run in Phase 4.3. |
| **loop_events.jsonl** | Events from loop_listener.ps1. |

### Synthetic Data Loop (trigger: "Start Synthetic data loop" / "START DATA LOOP")

| File | Purpose |
|------|--------|
| [SYNTHETIC_DATA_LOOP_MASTER_PLAN.md](./SYNTHETIC_DATA_LOOP_MASTER_PLAN.md) | Trigger, scope (Hybrid), phases, data-tier linkage, initiation checklist. |
| [SYNTHETIC_DATA_LOOP_FOR_OTHER_AGENTS.md](./SYNTHETIC_DATA_LOOP_FOR_OTHER_AGENTS.md) | Entry point: read at start; best practices; append ledger at end. |
| [SYNTHETIC_DATA_LOOP_ROUTINE.md](./SYNTHETIC_DATA_LOOP_ROUTINE.md) | Phases 0–4: Research & checkpoint → Create/gather → Prune & mesh → Quality report → User approval & ledger. |
| [SYNTHETIC_DATA_LOOP_RUN_LEDGER.md](./SYNTHETIC_DATA_LOOP_RUN_LEDGER.md) | Run ledger — append one block per data-loop run. |
| [SYNTHETIC_DATA_LOOP_RESEARCH.md](./SYNTHETIC_DATA_LOOP_RESEARCH.md) | Research & self-improvement: data-loop best-practice findings; suggested improvements (update at loop start). |
| **docs/DATA_TIERS.md** | SILVER / PLATINUM / GOLD; used by data loop for create/prune/tier changes. |

### Legacy & other
| File | Purpose |
|------|--------|
| [120_MINUTE_IMPROVEMENT_LOOP.md](./120_MINUTE_IMPROVEMENT_LOOP.md) | Legacy 2‑hr plan; superseded by IMPROVEMENT_LOOP_ROUTINE. |
| [8_HOUR_IMPROVEMENT_PLAN.md](./8_HOUR_IMPROVEMENT_PLAN.md) | 8‑hour run objectives and pulse. |
| **pulse_log.txt** | Appended by pulse script (tests, lint, note). |
| **loop_events.jsonl** | Events from loop_listener.ps1. |
| **OUTOFROUTEBUDDY_SHIP_INSTRUCTIONS.txt** | Generated ship steps; copy to Desktop if desired. |

---

## Scripts (run from repo root)

| Script | Purpose |
|--------|--------|
| **scripts/automation/pulse_check.ps1** | Unit tests, lint, append to pulse_log.txt. |
| **scripts/automation/loop_listener.ps1** | Record phase/events to loop_events.jsonl. |
| **scripts/automation/test_loop_listener.ps1** | Verify listener. |
| **scripts/automation/run_120min_loop.ps1** | Optional timer; work = IMPROVEMENT_LOOP_ROUTINE. |
| **scripts/automation/run_8hr_automation.ps1** | 8 hr: pulse every 30 min; ship instructions in last 2 hr. |
| **scripts/automation/write_ship_instructions.ps1** | Write OUTOFROUTEBUDDY_SHIP_INSTRUCTIONS.txt. |

Full paths and one-line purposes: [IMPROVEMENT_LOOP_INDEX.md](./IMPROVEMENT_LOOP_INDEX.md) § 5.

---

## Related outside this folder

| Where | What |
|-------|------|
| **docs/AGENTS.md** | Improvement Loop subsection; links to common sense, routine, autonomy. |
| **docs/README.md** | Doc index; "Automation & improvement loops" → this folder and index. |
| **.cursor/rules/2-hour-loop.mdc** | Trigger when user says GO. |
| **.cursor/skills/improvement-loop-wizard/SKILL.md** | Wizard flow. |

All listed in [IMPROVEMENT_LOOP_INDEX.md](./IMPROVEMENT_LOOP_INDEX.md) § 9.
