# Loops — Full and Verbose Guide

**Purpose:** One place that explains **where loops exist**, **how they are controlled**, and **what types of tasks they request**. Use this when you need the complete picture: file locations, triggers, authority, and task tiers.

**Related:** [ALL_LOOPS_FOR_AGENTS_AND_WORKFLOW.md](./ALL_LOOPS_FOR_AGENTS_AND_WORKFLOW.md) (workflow per loop), [LOOPS_AND_IMPROVEMENT_FULL_AXIS.md](./LOOPS_AND_IMPROVEMENT_FULL_AXIS.md) (register and triggers).

---

## Part 1 — Where Loops Exist

Loops are **not** a single code path. They are **documented workflows** plus **scripts and rules** that agents follow. They "exist" in three places: **documentation**, **scripts**, and **Cursor rules/skills**.

### 1.1 Documentation (definitions and routines)

| Location | What it defines |
|----------|------------------|
| **docs/automation/** | Central home for all loop definitions, routines, and indexes. |
| **docs/automation/IMPROVEMENT_LOOP_ROUTINE.md** | The main 2-hour Improvement Loop: Phase 0 (Research) through Phase 4 (Summary). Defines health checks, checkpoint, Hub read, shared state, and every phase step. |
| **docs/automation/LOOP_TIERING.md** | Task tiers: **Light** (auto), **Medium** (auto when autonomous), **Heavy** (human approval). Defines what each tier may do and the Question Lock / Visual Approval for Heavy. |
| **docs/automation/LOOP_MASTER_ROLE.md** | Authority for how loops run. Defines who is the Loop Master ("start master loop") and **Step 0.M** (research all loops, update universal files, read Hub, then run Improvement Loop). |
| **docs/automation/LOOPS_AND_IMPROVEMENT_FULL_AXIS.md** | **Loop register:** every loop, its trigger(s), main outputs, and doc/script pointers. |
| **docs/automation/ALL_LOOPS_FOR_AGENTS_AND_WORKFLOW.md** | Per-loop workflow: who runs it, trigger, goal, step-by-step, outputs. |
| **docs/automation/TOKEN_REDUCTION_LOOP.md** | Token Loop: steps 0–7, listener, ledger, NEXT_TASKS. |
| **docs/automation/LOOP_HEALTH_CHECKS.md** | Liveness (at phase start) vs readiness (pulse at phase end); when to run loop_health_check.ps1 and pulse_check.ps1. |
| **docs/automation/LOOP_DYNAMIC_SHARING.md** | How loops share data when run together: loop_shared_events.jsonl and loop_latest/*.json. |
| **docs/agents/data-sets/hub/UNIVERSAL_LOOP_PROMPT.md** | **The one rule that binds every loop:** seven obligations (Hub, Loop Master, self-improvement research, tiers, slop minimization, send to hub, shared state). |
| **docs/agents/purple-team-protocol.md** | Cyber Security Loop (Red/Blue/Purple): scope, attack, detect, fix, proof of work. |
| **docs/agents/data-sets/README.md** | Data-sets and file-organizer loop context; Hub index. |

So: **Improvement Loop** is fully specified in `IMPROVEMENT_LOOP_ROUTINE.md`; **Master Loop** in `LOOP_MASTER_ROLE.md`; **Token Loop** in `TOKEN_REDUCTION_LOOP.md`; **Cyber** in purple-team protocol; **Synthetic Data** and **File-organizer** in the full axis and workflow doc.

### 1.2 Scripts (execution and automation)

| Location | Purpose |
|----------|---------|
| **scripts/automation/pulse_check.ps1** | **Readiness** check at phase end: runs unit tests, lint, appends to pulse_log.txt; invokes loop_listener. |
| **scripts/automation/loop_health_check.ps1** | **Liveness** check at loop start and at start of every phase: repo, gradlew, app, docs, writable; writes loop_health_state.json. |
| **scripts/automation/run_120min_loop.ps1** | Wrapper to run the 2-hour Improvement Loop (agent may call this or follow the routine manually). |
| **scripts/automation/run_8hr_automation.ps1** | Long-run automation (e.g. pulse every 30 min for 8 hours). |
| **scripts/automation/write_ship_instructions.ps1** | Generates ship instructions (e.g. Desktop\OUTOFROUTEBUDDY_SHIP_INSTRUCTIONS.txt). |

Token and Cyber loops reference scripts in their docs (e.g. run_token_loop.ps1, token_loop_listener.ps1, token_loop_state_snapshot.ps1); if those scripts live elsewhere (or are documented only), the **routine** still lives in the docs above.

### 1.3 Shared state and logs (where loop data lives)

| Location | Purpose |
|----------|---------|
| **docs/automation/loop_shared_events.jsonl** | Append-only log of loop events (start, finished) so other loops can see what ran. |
| **docs/automation/loop_latest/** | One JSON per loop (e.g. improvement.json, token.json, cyber.json, synthetic_data.json) with latest summary path, next_steps, timestamp. |
| **docs/automation/pulse_log.txt** | Human-readable pulse log (tests, lint, note) from pulse_check.ps1. |
| **docs/automation/loop_health_state.json** | Last liveness result (status, checks, lastCheck) from loop_health_check.ps1. |
| **docs/automation/IMPROVEMENT_LOOP_RUN_LEDGER.md** | One block per Improvement Loop run (date, summary path, key outcomes). |
| **docs/automation/IMPROVEMENT_LOOP_SUMMARY_&lt;date&gt;.md** | Per-run summary (metrics, reasoning, quality grade, suggested next steps). |
| **docs/agents/data-sets/hub/** | **Hub:** polished outputs from any loop (reports, indexes, proof of work). Index in hub/README.md. |

### 1.4 Cursor rules and skills (how the IDE enforces loops)

| Location | Purpose |
|----------|---------|
| **.cursor/rules/2-hour-loop.mdc** | Rule for Improvement Loop: when user says "GO" or "start improvement loop"; points to LOOP_MASTER_ROLE, Step 0.M, IMPROVEMENT_LOOP_ROUTINE, tiering, summary. |
| **.cursor/rules/self-improvement.mdc** | Safe web search, prompt-injection awareness, project context (GOAL_AND_MISSION, KNOWN_TRUTHS, CODEBASE_OVERVIEW), research-first; drastic changes require approval. |
| **.cursor/rules/universal-loop.mdc** | (If present) Enforces the seven universal loop obligations in the IDE. |
| **.cursor/skills/improvement-loop-wizard/SKILL.md** | Skill invoked when running the Improvement Loop; guides step-by-step flow. |

So: **where loops exist** = docs/automation (and hub, purple-team) for definitions; scripts/automation for runs; docs/automation again for shared state and ledgers; .cursor/rules and .cursor/skills for how Cursor runs them.

---

## Part 2 — How Loops Are Controlled

Control is through **triggers** (what starts a loop), **authority** (who says how loops run), **autonomy** (what the agent may run without asking), and **shared state** (what every loop must read/write).

### 2.1 Triggers (what starts each loop)

| Loop | Trigger(s) | Who acts |
|------|------------|----------|
| **Master** | User says **"start master loop"** | The agent becomes Loop Master; runs Step 0.M then full Improvement Loop. |
| **Improvement** | **"GO"**, **"start improvement loop"**, **"run improvement loop"**; or after Step 0.M when user said "start master loop" | Any agent (often general-purpose or Improvement Loop agent). |
| **Token** | **"start token loop"**, **"run token reduction loop"**, **"token audit"** | Any agent; runs autonomously (no human in the loop). |
| **Cyber Security** | User says run Cyber Security Loop or Purple Team exercise; or script (e.g. run_cyber_security_loop.ps1); or **./gradlew :app:securitySimulations** | Red/Blue/Purple agents; user or Lead sets scope. |
| **Synthetic Data** | User asks for synthetic data generation/curation; or when aligning with UNIVERSAL_LOOP_PROMPT and Hub | Any agent. |
| **File-organizer** | **"organize data"**, or data-sets index task | File Organizer or any agent. |
| **Send to hub** | User says **"send to hub"** or agent finishes polished output | Any agent; deposits to hub with YYYY-MM-DD_&lt;role&gt;_&lt;description&gt;.ext. |

There is no single "loop scheduler" in code; triggers are **user or agent utterances** (or, for scheduled runs, Cursor Automations / webhook as per AUTONOMOUS_LOOP_SETUP.md).

### 2.2 Authority (who defines how loops run)

- **Loop Master** ([LOOP_MASTER_ROLE.md](./LOOP_MASTER_ROLE.md)) is the **authority**. When the user says "start master loop", the agent does **Step 0.M**: research all loops from LOOPS_AND_IMPROVEMENT_FULL_AXIS, compare/scrutinize, update universal files (e.g. IMPROVEMENT_LOOP_FOR_OTHER_AGENTS, LOOP_TIERING), **read the Hub**, then run the full Improvement Loop.
- **Universal Loop Prompt** ([UNIVERSAL_LOOP_PROMPT.md](../agents/data-sets/hub/UNIVERSAL_LOOP_PROMPT.md)) binds **every** loop: read Loop Master doc, read Hub, include self-improvement research, auto-implement Light/Medium, treat drastic loop changes as Heavy, minimize slop, consider send to hub, **read/write shared state**. So control is "authority (Loop Master) + universal rules (Hub prompt)."
- **Hub** (docs/agents/data-sets/hub/) is where advice and polished outputs live; every loop must **read the Hub at start** and may **deposit** when output is polished. So the Hub is part of control: "what other agents and the Loop Master have already decided."

### 2.3 Autonomy (what the agent may run without asking)

- **Cursor Settings → Agents → Auto-Run:** Either **Run Everything** (all commands run) or **Command Allowlist** (only allowlisted commands run). See [AUTONOMOUS_LOOP_SETUP.md](./AUTONOMOUS_LOOP_SETUP.md).
- **Allowlist** ([LOOP_MASTER_ALLOWLIST.md](./LOOP_MASTER_ALLOWLIST.md)): Prefix matching. One entry `cd c:\Users\brand\OutofRoutebuddy` covers all commands that start with that (gradlew, pulse_check.ps1, loop_health_check.ps1, run_120min_loop.ps1, loop_listener, etc.). So **control** = "only commands that start with the repo path (and optionally other explicit entries) are allowed when not in Run Everything."
- **Tiering** controls **what** the agent may do: **Light** and **Medium** run without asking; **Heavy** always requires human approval (and Question Lock + Visual Approval before implementation). So **task-level control** = LOOP_TIERING.md.

### 2.4 Health checks (continuous during the loop)

- **Liveness** (loop_health_check.ps1 -Quick): at **loop start** and at **start of every phase** (0, 1, 2, 3, 4). If it fails, agent logs and may abort or fix; with **-Gate** the script exits 1 and the agent should not start the next phase.
- **Readiness** (pulse_check.ps1): at **end of every phase** (1, 2, 3, 4). Runs tests and lint; logs to pulse_log. So **control** = "environment and build health are checked at fixed points during the loop."

### 2.5 Shared state (dynamic data between loops)

- **At loop start:** Read `docs/automation/loop_shared_events.jsonl` (tail) and `docs/automation/loop_latest/<other_loop>.json` for other loops.
- **At loop end:** Append one "finished" event to loop_shared_events.jsonl and overwrite loop_latest/&lt;your_loop&gt;.json.
- So **control** = "every loop must read/write these so that when loops run together, others can react." Defined in [LOOP_DYNAMIC_SHARING.md](./LOOP_DYNAMIC_SHARING.md) and UNIVERSAL_LOOP_PROMPT §7.

---

## Part 3 — What Types of Tasks Loops Request

Tasks are classified in **LOOP_TIERING.md** as **Light**, **Medium**, or **Heavy**. The Improvement Loop (and, where applicable, other loops) **requests** work from backlog docs (CRUCIAL_IMPROVEMENTS_TODO, REDUNDANT_DEAD_CODE_REPORT, FAILING_OR_IGNORED_TESTS, HEAVY_IDEAS_FAVORITES, suggested next steps). The **type** of task requested is determined by that classification and by the **phase** of the loop.

### 3.1 Light (automatic — no approval)

- **Verification only:** Doc links, PII grep, CRUCIAL linked from README.
- **Documentation:** Cross-links, document test in FAILING_OR_IGNORED_TESTS, sandbox index update.
- **Strings / accessibility:** contentDescription, string resources, wire existing string to UI.
- **Research & logging:** Phase 0 research, design research, summary write.
- **Research: safety/security:** Prompt-injection protection, secure coding, AI-assisted dev security.
- **Infrastructure:** Pulse check, unit test run, lint run.
- **Research: future light tasks:** At end of loop, document new Light task ideas.
- **Metadata research:** Document one suggestion in USER_METADATA_USAGE_GUIDE (when Data/Metrics focus).
- **Preference capture:** When user clarifies a preference, add to USER_PREFERENCES_AND_DESIGN_INTENT § Subtle Preferences.
- **Brainstorming & idea generation:** Document ideas in BRAINSTORM_AND_TASKS or suggested next steps.
- **Populate task list:** Classify ideas into Light/Medium/Heavy; add to CRUCIAL, LOOP_TIERING examples, or FUTURE_IDEAS (sandboxed).
- **Debugging:** Document debug steps, additive debug logs, verify paths, document in DEBUGGING.md; no logic changes.

**Rule:** Additive only (strings, docs, comments). No code removal, refactor, or new logic.

### 3.2 Medium (automatic when autonomous — ship readiness)

- **Ship readiness:** Version bump, changelog, release build smoke, lint fixes, test fixes.
- **Backend wiring:** Persistence fixes, repository wiring, sync logic, error handling.
- **Guaranteed frontend improvements:** Accessibility, contentDescription, subtle contrast/spacing (no layout drift).
- **Dead code removal:** Remove 1–2 safest items from REDUNDANT_DEAD_CODE_REPORT §2.
- **Test fix:** Fix one trivial ignored test.
- **KDoc / comment:** e.g. StandaloneOfflineService Keystore migration note.
- **Logging:** One clear log in trip start → save path.
- **Ship instructions:** Generate/update Desktop\OUTOFROUTEBUDDY_SHIP_INSTRUCTIONS.txt.
- **Sandboxing:** Add Heavy ideas to FUTURE_IDEAS (sandboxed); improve index/cross-links; validate sandboxed features.
- **Sandbox improvement (every loop):** Improve 1–2 ideas in HEAVY_IDEAS_FAVORITES (design brief, validation checklist, completion %).
- **Sandbox testing for merge:** Test in sandbox (branch/build variant) before merging.
- **Advanced beautification & organizing research:** Color, typography, spacing, elevation; document organizing best practices; one subtle improvement per loop.
- **Code structure review:** Assess one module; document in summary (no change unless user approves).
- **Medium refactors:** Small, localized: extract one function, rename one class, move one file.
- **Metadata collection/display:** One opt-in metadata point or one display (e.g. "Trips this week"); on-device, no PII.
- **Pre-loop checkpoint:** git commit or tag before the loop for revert.
- **Research improvements & populate tasks:** Add ideas to BRAINSTORM_AND_TASKS; classify and populate CRUCIAL, LOOP_TIERING, FUTURE_IDEAS.
- **Idea classification & placement:** Divide brainstormed ideas into Light/Medium/Heavy; add 1–2 new items per loop.

**Rule:** Small, localized change. Low risk. Supports shipping. No drift from original UI layout.

### 3.3 Heavy (human approval always — new features / future ideas)

- **Featured features:** From FUTURE_IDEAS (multi-user sharing, driver ranking, route deviation map, sandboxed virtual fleet, optional email signup, etc.).
- **UI polish (icon beautification):** Trash/delete icons — professional choice; Heavy and requires visual approval.
- **Navigation / app chrome:** Scrolling toolbar, hamburger menu (e.g. FUTURE_IDEAS §6).
- **Branding:** App name change ideas.
- **ROADMAP features:** Auto drive, Reports screen, address input — only after sandbox validation.
- **Architecture:** Schema changes, new persistence paths, major toolchain upgrades (e.g. AGP 9).
- **Large refactors:** Statistics monthly-only; repository interface changes; multi-file/cross-module refactors.
- **Drastic loop improvements:** Routine changes, add/remove phases, change tier definitions, new loops, major process/automation changes. Document only; add to backlog or FUTURE_IDEAS; require human approval.

**Rule:** New features only. Must be sandboxed and confirmed before promotion. **Question Lock:** when user says "implement X", ask: "Would you like to see a generated image or layout or simulate a merge?" **Visual Approval:** generate simple visual (or layout/merge sim), present to user, wait for **"approve 100% implement"** before implementing. Heavy features are implemented **one at a time**; per-feature gate.

### 3.4 What each loop requests (by phase or step)

- **Improvement Loop**
  - **Phase 0:** Research (CRUCIAL, last summary, security, design, HEAVY_IDEAS_FAVORITES, self-improvement docs); classify tasks Light/Medium/Heavy; if Heavy exist, prompt user; optional self-improvement, token audit, design/UX research.
  - **Phase 1:** Quick wins (dead code 1–2, BuildConfig alignment, doc links), security (PII grep, FileProvider, or full checklist when Security focus; securitySimulations when Security focus), one smoothness fix; pulse.
  - **Phase 2:** Test health (FAILING_OR_IGNORED_TESTS), doc cross-links, sandboxing (improve 1–2 ideas in HEAVY_IDEAS_FAVORITES/FUTURE_IDEAS); pulse.
  - **Phase 3:** One string/accessibility fix; stat card/UI consistency; one useful info; one subtle improvement; pulse.
  - **Phase 4:** Lint; final pulse; write summary; append ledger; write shared state; optional send to hub.

- **Token Loop:** Audit rules, doc references, conversation hygiene, update token docs, TOKEN_LOOP_NEXT_TASKS, progress report (Loop #, proof of work, benefits).

- **Cyber Security Loop:** Red attacks; Blue check (did alarm go off?); fix if missed; proof of work; training data; optional Hub deposit.

- **Synthetic Data Loop:** Research what data is needed; create/gather; prune and mesh; quality report; user approval if required; ledger; shared state; send to hub when polished.

- **File-organizer:** Review data-sets, hub, security-exercises; update/create index; deposit to Hub with file-organizer naming.

---

## Part 4 — Quick Reference

| Question | Answer |
|----------|--------|
| **Where do loops "live"?** | Docs: docs/automation/*.md (routines, tiering, axis, workflow); Hub: docs/agents/data-sets/hub/; scripts: scripts/automation/*.ps1; state: docs/automation/loop_shared_events.jsonl, loop_latest/, pulse_log.txt, loop_health_state.json; Cursor: .cursor/rules/*.mdc, .cursor/skills. |
| **How are they started?** | By user or agent saying the trigger phrase ("GO", "start master loop", "start token loop", etc.) or by running a script / Gradle task (e.g. securitySimulations). |
| **Who is in charge?** | Loop Master (when "start master loop"); UNIVERSAL_LOOP_PROMPT binds all loops; LOOP_TIERING controls what may be done without approval. |
| **What may run without approval?** | Commands that match the allowlist (e.g. prefix cd to repo); all Light and Medium tasks. |
| **What always needs approval?** | Heavy tasks (one-by-one, with Question Lock and "approve 100% implement"). |
| **What do loops request?** | Improvement: research, quick wins, security, smoothness, test health, docs, UI polish, summary. Token: rule audit, doc refs, NEXT_TASKS. Cyber: attack, detect, fix, proof of work. Synthetic: create/curate data. File-organizer: index and organize. |

---

*This guide is the full and verbose reference. Keep it in sync with ALL_LOOPS_FOR_AGENTS_AND_WORKFLOW.md, LOOPS_AND_IMPROVEMENT_FULL_AXIS.md, LOOP_TIERING.md, and UNIVERSAL_LOOP_PROMPT.md when adding or changing loops.*
