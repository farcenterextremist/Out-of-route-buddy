# Improvement Loop — Master Index (Find Everything)

**Purpose:** Single entry point for all Improvement Loop–related folders, files, and scripts. Use this when you need to find where something lives or what to read when you say **GO**.

**Trigger:** User says **GO**, "run improvement loop", "improvement loop wizard", or **"start master loop"** (Loop Master: research all loops, update universal files, then run Improvement Loop).

---

## 1. Start here (when you say GO)

| What | Where | Notes |
|------|--------|--------|
| **Master Loop** | [LOOP_MASTER_ROLE.md](./LOOP_MASTER_ROLE.md) | Trigger: **"start master loop"**. Research all loops, update universal files, then run Improvement Loop. |
| **Routine (phases 0–4)** | [IMPROVEMENT_LOOP_ROUTINE.md](./IMPROVEMENT_LOOP_ROUTINE.md) | Main playbook; read first after common sense & reasoning. |
| **Common sense** | [IMPROVEMENT_LOOP_COMMON_SENSE.md](./IMPROVEMENT_LOOP_COMMON_SENSE.md) | Checkpoint first, tests green, no unwarranted UI. Read at loop start. |
| **Reasoning** | [IMPROVEMENT_LOOP_REASONING.md](./IMPROVEMENT_LOOP_REASONING.md) | Decision checkpoints; think before you act. |
| **User preferences** | [USER_PREFERENCES_AND_DESIGN_INTENT.md](./USER_PREFERENCES_AND_DESIGN_INTENT.md) | Design intent, must-not-change. **Read first** (Phase 0.0a). |
| **Cursor rule (trigger)** | [.cursor/rules/2-hour-loop.mdc](../../.cursor/rules/2-hour-loop.mdc) | Fires when user says GO; points to common sense, routine, tiering. |
| **Skill (wizard)** | [.cursor/skills/improvement-loop-wizard/SKILL.md](../../.cursor/skills/improvement-loop-wizard/SKILL.md) | Step-by-step wizard flow. |
| **AGENTS.md** | [docs/AGENTS.md](../AGENTS.md) | Root briefing; Improvement Loop section links to common sense, reasoning, routine, autonomy. |

---

## 2. Core routine & tiering (this folder: `docs/automation/`)

| File | Purpose |
|------|--------|
| [IMPROVEMENT_LOOP_ROUTINE.md](./IMPROVEMENT_LOOP_ROUTINE.md) | Phases 0–4, subagents, summary template, out-of-scope. |
| [IMPROVEMENT_LOOP_COMMON_SENSE.md](./IMPROVEMENT_LOOP_COMMON_SENSE.md) | Non-negotiable rules; full autonomous mode. |
| [IMPROVEMENT_LOOP_REASONING.md](./IMPROVEMENT_LOOP_REASONING.md) | Reasoning checkpoints and output in summary. |
| [LOOP_TIERING.md](./LOOP_TIERING.md) | Light / Medium / Heavy; question lock; visual approval; revert. |
| [IMPROVEMENT_LOOP_TEAMS.md](./IMPROVEMENT_LOOP_TEAMS.md) | Researchers, Meta-Researchers, File Organizer. |
| [LOOP_METRICS_TEMPLATE.md](./LOOP_METRICS_TEMPLATE.md) | Metrics to capture per run (tests, lint, checkpoint, etc.). |
| [LOOP_FOCUS_ROTATION.md](./LOOP_FOCUS_ROTATION.md) | Focus areas (Security, UI/UX, Shipability, etc.) and rotation order. |
| [LOOP_VARIANTS.md](./LOOP_VARIANTS.md) | Quick / Standard / Full scope and time. *(If missing, use routine § Variants.)* |
| [LOOP_LISTENER.md](./LOOP_LISTENER.md) | Event recording (loop_listener.ps1, loop_events.jsonl). *(If missing, see routine § Loop listener.)* |

---

## 3. Context & inputs (read in Phase 0)

| File | Purpose |
|------|--------|
| [USER_PREFERENCES_AND_DESIGN_INTENT.md](./USER_PREFERENCES_AND_DESIGN_INTENT.md) | Design intent, must-not-change, subtle preferences. **Phase 0.0a first read.** |
| [LOOP_FRONTEND_VS_BACKEND_BREAKDOWN.md](./LOOP_FRONTEND_VS_BACKEND_BREAKDOWN.md) | Target ~75–85% backend; frontend only when obvious. |
| [CURSOR_SELF_IMPROVEMENT.md](./CURSOR_SELF_IMPROVEMENT.md) | Safe web search, Phase 0.3/0.5 self-improvement, prompt-injection awareness. |
| [DESIGN_AND_UX_RESEARCH.md](./DESIGN_AND_UX_RESEARCH.md) | Design research for Phase 0.4 / Phase 3. *(If missing, use docs/ux/UI_CONSISTENCY.md.)* |
| [TOKEN_REDUCTION_LOOP.md](./TOKEN_REDUCTION_LOOP.md) | Token audit; optional Phase 0.6. *(If missing, skip.)* |
| **Backlog & health (outside this folder)** | |
| [docs/CRUCIAL_IMPROVEMENTS_TODO.md](../CRUCIAL_IMPROVEMENTS_TODO.md) | Prioritized improvements; pick 1–2 low-risk items. |
| [docs/REDUNDANT_DEAD_CODE_REPORT.md](../REDUNDANT_DEAD_CODE_REPORT.md) | Safe dead code to remove. |
| [docs/qa/FAILING_OR_IGNORED_TESTS.md](../qa/FAILING_OR_IGNORED_TESTS.md) | Test health; @Ignore reasons. |
| [docs/security/SECURITY_NOTES.md](../security/SECURITY_NOTES.md) | Security checklist; PII, FileProvider. |

---

## 4. Sandbox & heavy ideas (this folder)

| File | Purpose |
|------|--------|
| [SANDBOX_TESTING.md](./SANDBOX_TESTING.md) | Feature testing before merge; sandbox phase in loop. |
| [SANDBOX_COMPLETION_PERCENTAGE.md](./SANDBOX_COMPLETION_PERCENTAGE.md) | True completion % for sandboxed ideas. |
| [HEAVY_TIER_IDEAS.md](./HEAVY_TIER_IDEAS.md) | Heavy ideas list (cap 50); add 1–2 per run when below 50. |

---

## 5. Autonomy & scripts

| Item | Location | Purpose |
|------|----------|--------|
| **One-time autonomy** | [AUTONOMOUS_LOOP_SETUP.md](./AUTONOMOUS_LOOP_SETUP.md) | Run Everything or allowlist; no human prompts. |
| **Allowlist** | [LOOP_MASTER_ALLOWLIST.md](./LOOP_MASTER_ALLOWLIST.md) | Commands for allowlist; `cd c:\...\OutofRoutebuddy` prefix. |
| **Pulse** | `scripts/automation/pulse_check.ps1` | Unit tests, lint, append to pulse_log.txt. |
| **Loop listener** | `scripts/automation/loop_listener.ps1` | Record phase/events to loop_events.jsonl. |
| **Test listener** | `scripts/automation/test_loop_listener.ps1` | Verify listener wiring. |
| **120 min timer** | `scripts/automation/run_120min_loop.ps1` | Optional timer; actual work is IMPROVEMENT_LOOP_ROUTINE. |
| **8 hr run** | `scripts/automation/run_8hr_automation.ps1` | Long run: pulse every 30 min; ship instructions in last 2 hr. |
| **Ship instructions** | `scripts/automation/write_ship_instructions.ps1` | Write OUTOFROUTEBUDDY_SHIP_INSTRUCTIONS.txt. |

---

## 6. Outputs & logs (this folder)

| File / pattern | Purpose |
|----------------|--------|
| **IMPROVEMENT_LOOP_SUMMARY_&lt;date&gt;.md** | A-grade summary per run. Primary name. |
| **120_MINUTE_LOOP_SUMMARY_&lt;date&gt;.md** | Same content; alternate name for compatibility. |
| [120_MINUTE_LOOP_SUMMARY_2025-03-11.md](./120_MINUTE_LOOP_SUMMARY_2025-03-11.md) | Example: latest run summary. |
| **pulse_log.txt** | Appended by pulse_check.ps1 (tests, lint, note). |
| **loop_events.jsonl** | Structured events from loop_listener.ps1. |
| **OUTOFROUTEBUDDY_SHIP_INSTRUCTIONS.txt** | Generated ship steps; copy to Desktop if desired. |

---

## 7. Analysis & priming

| File | Purpose |
|------|--------|
| [IMPROVEMENT_LOOP_ANALYSIS_AND_IMPROVEMENTS.md](./IMPROVEMENT_LOOP_ANALYSIS_AND_IMPROVEMENTS.md) | Analysis, suggested improvements, priming for next run. Optional Phase 0 read. |
| [IMPROVEMENT_LOOP_AUDIT.md](./IMPROVEMENT_LOOP_AUDIT.md) | Blind spots, loose ends; re-run after major changes. |

---

## 7b. For other agents & recorded data

| File | Purpose |
|------|--------|
| [IMPROVEMENT_LOOP_FOR_OTHER_AGENTS.md](./IMPROVEMENT_LOOP_FOR_OTHER_AGENTS.md) | **Entry point** for other agents building their own loops: best practices, our recorded data, key docs. |
| [IMPROVEMENT_LOOP_BEST_PRACTICES.md](./IMPROVEMENT_LOOP_BEST_PRACTICES.md) | Shareable best practices: principles, phase structure, what to record every run, checklist. |
| [IMPROVEMENT_LOOP_RUN_LEDGER.md](./IMPROVEMENT_LOOP_RUN_LEDGER.md) | **Run ledger** — human-readable log; **append one block per run** in Phase 4.3. |
| [loop_events.jsonl](./loop_events.jsonl) | Machine-readable events (phase/pulse); see [LOOP_LISTENER.md](./LOOP_LISTENER.md). |

---

## 8. Legacy & variants

| File | Purpose |
|------|--------|
| [120_MINUTE_IMPROVEMENT_LOOP.md](./120_MINUTE_IMPROVEMENT_LOOP.md) | Legacy 2‑hr plan; superseded by IMPROVEMENT_LOOP_ROUTINE. |
| [8_HOUR_IMPROVEMENT_PLAN.md](./8_HOUR_IMPROVEMENT_PLAN.md) | 8‑hour run objectives and pulse; separate from the routine loop. |

---

## 9. Related outside `docs/automation/`

| Location | What |
|----------|------|
| **docs/AGENTS.md** | Start-here for agents; Improvement Loop subsection. |
| **docs/README.md** | Doc index; "Automation & improvement loops" and "Readiness" sections. |
| **docs/SELF_IMPROVEMENT_PLAN.md** | Strategic layer; when to run loop vs ad-hoc. |
| **docs/readiness/** | Plateau & shipping easy; what finished/perfected looks like; [GRAND_PROGRESS_BAR.md](../readiness/GRAND_PROGRESS_BAR.md) — update at milestones or each loop summary. |
| **.cursor/rules/2-hour-loop.mdc** | Trigger rule: GO → common sense, routine, tiering. |
| **.cursor/rules/self-improvement.mdc** | Safe web, contextualization; applies during loop. |
| **.cursor/skills/improvement-loop-wizard/SKILL.md** | Wizard flow: Step 0–5, checkpoint, tiering, summary. |

---

## 9b. Agent & Loop Skills (quick reference)

| Agent / Loop | Skills |
|--------------|--------|
| **Coordinator** | coordinator-delegation, improvement-loop-wizard |
| **Design** | feature-brief-writer, coordinator-delegation |
| **UI/UX** | frontend-ui-ux-specialist |
| **Front-end** | frontend-ui-ux-specialist, kotlin-android-specialist |
| **Back-end** | kotlin-android-specialist |
| **DevOps** | gradle-build-specialist, shipping-specialist |
| **QA** | test-qa-specialist |
| **Security** | security-analyst-agent, red-team-skill, blue-team-skill |
| **File Organizer** | coordinator-delegation, improvement-loop-wizard |
| **Red Team** | red-team-skill, attack-library-skill |
| **Blue Team** | blue-team-skill |
| **Improvement Loop** | improvement-loop-wizard, token-conservationist |
| **Purple Team** | purple-orchestrator-skill, red-team-skill, blue-team-skill |
| **Token Loop** | token-conservationist |
| **Pre-release** | shipping-specialist, security-analyst-agent, coverage-report-skill |

---

## 10b. Synthetic Data Loop (trigger: "Start Synthetic data loop" / "START DATA LOOP")

| File | Purpose |
|------|--------|
| [SYNTHETIC_DATA_LOOP_MASTER_PLAN.md](./SYNTHETIC_DATA_LOOP_MASTER_PLAN.md) | Trigger, scope (Hybrid), phases, data-tier linkage. |
| [SYNTHETIC_DATA_LOOP_FOR_OTHER_AGENTS.md](./SYNTHETIC_DATA_LOOP_FOR_OTHER_AGENTS.md) | Entry point; read at start; append ledger at end. |
| [SYNTHETIC_DATA_LOOP_ROUTINE.md](./SYNTHETIC_DATA_LOOP_ROUTINE.md) | Phases 0–4: Research → Create/gather → Prune & mesh → Quality report → User approval & ledger. |
| [SYNTHETIC_DATA_LOOP_RUN_LEDGER.md](./SYNTHETIC_DATA_LOOP_RUN_LEDGER.md) | Run ledger — append one block per run. |
| **docs/DATA_TIERS.md** | SILVER / PLATINUM / GOLD. |

---

## 10c. Cyber Security Loop (trigger: "Run Cyber Security Loop" / "GO security")

| File | Purpose |
|------|---------|
| [CYBER_SECURITY_LOOP_MASTER_PLAN.md](./CYBER_SECURITY_LOOP_MASTER_PLAN.md) | Completion %, phases, visual roadmap; read at loop start. |
| [CYBER_SECURITY_LOOP_ROUTINE.md](./CYBER_SECURITY_LOOP_ROUTINE.md) | Phases 0–3: Research → Simulate → Purple → Improve. |
| [CYBER_SECURITY_LOOP_COMMON_SENSE.md](./CYBER_SECURITY_LOOP_COMMON_SENSE.md) | Checkpoint first, scope lock, no destructive actions. |
| [CYBER_SECURITY_LOOP_AUDIT.md](./CYBER_SECURITY_LOOP_AUDIT.md) | Blind spots, gaps, loose ends. |
| [CYBER_SECURITY_LOOP_RUN_LEDGER.md](./CYBER_SECURITY_LOOP_RUN_LEDGER.md) | Pre/during/post per run; rollback and improvement. |
| [CYBER_SECURITY_DATA_SUMMARY.md](./CYBER_SECURITY_DATA_SUMMARY.md) | **Data collected, what it means, how to use it.** |
| **docs/agents/data-sets/** | ATTACK_LIBRARY, attack-playbooks/, security-exercises/, SYNTHETIC_ATTACK_SCENARIOS. |
| **scripts/purple-team/** | run_purple_simulations.py, audit_rules.py, diff_training_runs.py, prompt_injection_harness.py. |

---

## 10. Folder layout (quick reference)

```
docs/automation/                    ← All routine docs, summaries, logs
├── IMPROVEMENT_LOOP_INDEX.md       ← This file (find everything)
├── IMPROVEMENT_LOOP_ROUTINE.md     ← Main playbook
├── IMPROVEMENT_LOOP_COMMON_SENSE.md
├── IMPROVEMENT_LOOP_REASONING.md
├── USER_PREFERENCES_AND_DESIGN_INTENT.md
├── LOOP_*.md                       ← Tiering, focus, metrics, variants, listener, allowlist
├── IMPROVEMENT_LOOP_*.md           ← Teams, audit, analysis
├── *SUMMARY*.md                    ← Run summaries (date in filename)
├── pulse_log.txt | loop_events.jsonl | OUTOFROUTEBUDDY_SHIP_INSTRUCTIONS.txt
└── README.md                       ← Folder overview; points to this index

scripts/automation/                 ← All loop-related scripts
├── pulse_check.ps1
├── loop_listener.ps1
├── test_loop_listener.ps1
├── run_120min_loop.ps1
├── run_8hr_automation.ps1
└── write_ship_instructions.ps1

.cursor/rules/                      ← Trigger & context
├── 2-hour-loop.mdc                ← GO → routine
└── self-improvement.mdc            ← Safe web, project context

.cursor/skills/improvement-loop-wizard/
└── SKILL.md                        ← Wizard steps
```

---

*Use this index to find any Improvement Loop–related file or script. For a quick "what do I read when user says GO?" use § 1 Start here.*
