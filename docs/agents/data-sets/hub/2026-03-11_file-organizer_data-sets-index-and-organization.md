# Data sets — index and organization

**Purpose:** Single reference for what lives under `docs/agents/data-sets/`, how it’s organized, and where to add new outputs. Use when delegating work, onboarding roles, or when the user says "organize data" or "send to hub."

**Path:** `docs/agents/data-sets/`  
**Hub (completed outputs):** `docs/agents/data-sets/hub/`  
**Last updated:** 2026-03-16

---

## 1. Hub — completed, polished outputs

**Path:** [docs/agents/data-sets/hub/](.)

All agents deposit **finished** reports, exports, and artifacts here. When the user says **"send to hub"**, write output here with naming: `YYYY-MM-DD_<role-or-topic>_<short-description>.<ext>` and optionally add a one-line entry to [hub/README.md](./README.md) (this README). Hub = this folder; not GitHub.

| File | Role/topic | Description |
|------|------------|-------------|
| [2026-03-11_token-loop_loop5-report-proof-of-work-and-benefits.md](2026-03-11_token-loop_loop5-report-proof-of-work-and-benefits.md) | token-loop | Loop #5: proof of work, benefits, rule output, next TODOs |
| [2026-03-11_cyber-security_loop3-proof-of-work-and-benefits.md](2026-03-11_cyber-security_loop3-proof-of-work-and-benefits.md) | cyber-security | Loop #3: proof of work, 4/4 validation, benefits |
| [2026-03-11_cyber-security_purple-training.json](2026-03-11_cyber-security_purple-training.json) | cyber-security | Structured training: validation_simulations, synthetic_scenarios |
| [2026-03-11_cyber-security_data-summary-and-utilization.md](2026-03-11_cyber-security_data-summary-and-utilization.md) | cyber-security | Data collected, how to use (regression, few-shot, metrics) |
| [2026-03-13_data-loop_loop3-quality-summary.md](2026-03-13_data-loop_loop3-quality-summary.md) | data-loop | Loop #3: quality summary, proof of work, benefits, provenance-oriented validation. |
| [2026-03-13_master-loop_loop-gates-summary.md](2026-03-13_master-loop_loop-gates-summary.md) | master-loop | LOOP GATES run: policy wiring, production-stage progress, test/lint verification, shared-state sync. |
| [2026-03-13_master-loop_mini-architecture-guard-summary.md](2026-03-13_master-loop_mini-architecture-guard-summary.md) | master-loop | Mini loop: architecture guard, regression checks, focused readiness proof, shared-state sync. |
| [2026-03-13_master-loop_ready-metrics-summary.md](2026-03-13_master-loop_ready-metrics-summary.md) | master-loop | Ready-metrics run: architecture checklist increment, neat metrics, shared-state/hub updates. |
| [2026-03-13_cyber-security_loop5-proof-of-work-and-benefits.md](2026-03-13_cyber-security_loop5-proof-of-work-and-benefits.md) | cyber-security | Loop #5: proof of work, benefits, validation baseline, shared-state/hub completion. |
| [2026-03-13_cyber-security_purple-training.json](2026-03-13_cyber-security_purple-training.json) | cyber-security | Loop #5 structured training: validation simulations and synthetic scenarios for regression. |
| [2026-03-14_data-loop_loop4-quality-summary.md](2026-03-14_data-loop_loop4-quality-summary.md) | data-loop | Loop #4: quality summary with provenance standard and quality-feedback research. |
| [2026-03-15_master-loop_role-cube-and-mini-loop-summary.md](2026-03-15_master-loop_role-cube-and-mini-loop-summary.md) | master-loop | Role cube and mini-loop summary: listener, liveness, continuity, and shared-state findings. |

**Agent prompt:** [SEND_TO_HUB_PROMPT.md](SEND_TO_HUB_PROMPT.md)

---

## 2. Role data sets — what each role consumes/produces

**Path:** `docs/agents/data-sets/*.md` (sibling to hub)

Each file defines what a role **reads** and **writes**. Used by the coordinator to delegate and point roles at the right paths. Full plan: [docs/agents/DATA_SETS_AND_DELEGATION_PLAN.md](../../DATA_SETS_AND_DELEGATION_PLAN.md).

| Role | File | One-line |
|------|------|----------|
| Project Design / Creative Manager | [design-creative.md](../design-creative.md) | Vision, roadmap, feature briefs |
| UI/UX Specialist | [ui-ux.md](../ui-ux.md) | Screens, flows, accessibility, design system |
| Front-end Engineer | [frontend.md](../frontend.md) | Android UI implementation (layouts, fragments, ViewModels) |
| Back-end Engineer | [backend.md](../backend.md) | Data, services, repositories, business logic |
| DevOps Engineer | [devops.md](../devops.md) | Build, CI/CD, Gradle, deployment |
| QA Engineer | [qa.md](../qa.md) | Test strategy, automation, regression |
| Security Specialist | [security.md](../security.md) | Security review, threat model, compliance |
| Email Editor / Market Guru | [email-editor.md](../email-editor.md) | Email copy, marketing messaging |
| File Organizer | [file-organizer.md](../file-organizer.md) | Repo/doc structure, reorg proposals |

---

## 3. Aptitude — training and scoring

**Path:** `docs/agents/data-sets/aptitude-responses/` and root-level reports

| Item | Path | Description |
|------|------|-------------|
| Role responses (numbered) | [aptitude-responses/](../aptitude-responses/) | Per-role aptitude answers (e.g. `01_coordinator_simple.md`, `23_red_team_simple.md`) |
| Priority report | [aptitude-training-priority-report.md](../aptitude-training-priority-report.md) | Ranked by priority; scope/data/output/handoff/voice recommendations |
| Scorecard | [agent-aptitude-scorecard.md](../agent-aptitude-scorecard.md) | Source for priority report; dimension scores |

**Use:** Onboard roles, refine role cards, improve handoffs and data-set files.

---

## 4. Security exercises

**Path:** `docs/agents/data-sets/security-exercises/`

| Item | Path | Description |
|------|------|-------------|
| Training data index | [security-exercises/TRAINING_DATA_INDEX.md](../security-exercises/TRAINING_DATA_INDEX.md) | Lists Purple training JSON files; format, use cases (few-shot, regression) |
| Artifacts | [security-exercises/artifacts/](../security-exercises/artifacts/) | `*-purple-training.json` outputs from runs |

**Produce new:** `python scripts/purple-team/run_purple_simulations.py --full` or `./gradlew :app:securitySimulations`.

---

## 5. Board meeting

**Path:** `docs/agents/data-sets/board-meeting/`

| Item | Path | Description |
|------|------|-------------|
| README | [board-meeting/README.md](../board-meeting/README.md) | Naming, latest transcript/summary |
| Transcript | [board-meeting-2025-03-15.md](../board-meeting/board-meeting-2025-03-15.md) | Full run transcript |
| Summary | [board-meeting-2025-03-15-summary.md](../board-meeting/board-meeting-2025-03-15-summary.md) | One-run summary |

**Plan:** [docs/agents/BOARD_MEETING_PLAN.md](../../BOARD_MEETING_PLAN.md)

---

## 6. Other shared data

| Item | Path | Description |
|------|------|-------------|
| Employee roundtable | [employee-roundtable-transcript.md](../employee-roundtable-transcript.md) | Simulated roundtable; use to train handoffs and "who does what" |
| Project examination / assignment | [project-examination-and-agent-assignment.md](../project-examination-and-agent-assignment.md) | Agent assignment and project context |
| Benchmark cases | [enronsr_style_benchmark_cases.json](../enronsr_style_benchmark_cases.json) | Benchmark data (e.g. email/style) |

---

## Quick rules

- **Finished output** → Put in **hub/** with `YYYY-MM-DD_<role-or-topic>_<short-description>.<ext>`; optionally index in hub/README.md.
- **Role inputs/outputs** → Keep defined in the role data-set files (design-creative.md, backend.md, etc.).
- **Work-in-progress** → Keep in role-specific or working docs; do not put in hub until polished and completed.
