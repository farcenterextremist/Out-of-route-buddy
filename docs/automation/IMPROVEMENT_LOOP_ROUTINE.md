# Improvement Loop — Routine

**Objective:** Obvious improvements, security hardening, and smoothness. No drastic frontend changes.  
**Frontend vs backend:** Loop should be **mostly backend** (~75–85% by task); frontend only when ultimately obvious (accessibility, one useful string, one subtle consistency fix). See [LOOP_FRONTEND_VS_BACKEND_BREAKDOWN.md](./LOOP_FRONTEND_VS_BACKEND_BREAKDOWN.md).  
**Strategy:** Research first, master allowlist for autonomy, then execute phases. Use subagents for parallel work.  
**Trigger:** Run when user says **GO**. For **"start master loop"**, run [LOOP_MASTER_ROLE.md](./LOOP_MASTER_ROLE.md) Step 0.M first (research all loops, compare/analyze/scrutinize, update universal files), then this routine. Complete tasks on todo lists (CRUCIAL, suggested next steps). See [LOOP_VARIANTS.md](./LOOP_VARIANTS.md) for scope variants.

**Common sense:** Read [IMPROVEMENT_LOOP_COMMON_SENSE.md](./IMPROVEMENT_LOOP_COMMON_SENSE.md) at loop start. Checkpoint first, respect design intent, tests green, timebox, no unwarranted UI changes.

**For other agents:** Read [IMPROVEMENT_LOOP_FOR_OTHER_AGENTS.md](./IMPROVEMENT_LOOP_FOR_OTHER_AGENTS.md) at loop start and follow the best practices there. At the end of every run, append one block to [IMPROVEMENT_LOOP_RUN_LEDGER.md](./IMPROVEMENT_LOOP_RUN_LEDGER.md).

**Logic & reasoning:** Read [IMPROVEMENT_LOOP_REASONING.md](./IMPROVEMENT_LOOP_REASONING.md) at loop start. Apply reasoning checkpoints before research, task selection, each change, and summary. Think before you act.

**Framework (PDCA / Kaizen):** Phase 0 = Plan; Phase 1–3 = Do; Phase 4 = Check; Phase 4.3 = Act. **Kaizen rule:** One improvement per category per loop to avoid overload.

**Tiering:** Tasks are Light (auto), Medium (auto when autonomous), or Heavy (human approval always). See [LOOP_TIERING.md](./LOOP_TIERING.md). **Full autonomy:** Light and Medium run without stopping; Heavy deferred or documented for next run. See [IMPROVEMENT_LOOP_COMMON_SENSE.md](./IMPROVEMENT_LOOP_COMMON_SENSE.md) § Full Autonomous Mode. **Production-stage (100% approved):** Approved Heavy items are reclassified to **Medium execution queue** and must run first each loop (current: #7, #8, #9, #17, #20). They may not complete in one run; require incremental progress.

**Frontend implementation authority:** Only **Master Loop** may implement frontend/UI changes. Non-master loops can research and propose only. Any automated frontend change must pass [FRONTEND_CHANGE_AUTOMATION_GATE.md](./FRONTEND_CHANGE_AUTOMATION_GATE.md) (hard-stop checks + OBS/SSS thresholds + proof-of-quality evidence).

**Visual approval clause:** Heavy features (100% sandboxed) require a simple visual image + user says **"approve 100% implement"** before implementation. See LOOP_TIERING § Visual Approval Clause.

**Teams:** [IMPROVEMENT_LOOP_TEAMS.md](./IMPROVEMENT_LOOP_TEAMS.md) — Front: Researchers + Meta-Researchers. Back: File Organizer (also recommends new ideas to Light/Medium/Heavy).

**Pre-loop checkpoint:** Save a copy (git commit or tag) before the loop. Say **"revert"** to restore if something breaks. See LOOP_TIERING § Revert.

**Loop listener:** Record events for data and improvement. `pulse_check.ps1` invokes the listener. When running phases (agent-driven), invoke `loop_listener.ps1` at phase boundaries. See [LOOP_LISTENER.md](./LOOP_LISTENER.md). Run `test_loop_listener.ps1` to verify wiring.

**Task-based execution:** The agent completes tasks from CRUCIAL, suggested next steps, and todo lists. No fixed duration. Run `pulse_check.ps1` at phase boundaries.

**Consistency contract (required):** Follow [LOOP_CONSISTENCY_STANDARD.md](./LOOP_CONSISTENCY_STANDARD.md). Every run must include `Loop Consistency Check` and record `Consistency score: X/10` in the summary.

**Diagnostic sweep (required for substantial runs):** Pair health checks with [LOOP_DIAGNOSTIC_SWEEP.md](./LOOP_DIAGNOSTIC_SWEEP.md). Loops must actively search for problems (lints, ignored tests, hotspots, residual risk), not only run tests.

---

## Before First Loop (One-Time Setup)

**Autonomy setup:** For no human-in-the-loop, follow [AUTONOMOUS_LOOP_SETUP.md](./AUTONOMOUS_LOOP_SETUP.md) once. Option A: Set Auto-run mode to **Run Everything**. Option B: Add `cd c:\Users\brand\OutofRoutebuddy` to Command Allowlist. Skip if already configured.

---

## Phase 0: Research & Allowlist

**Goal:** Understand current state, classify tasks, and get approval for Medium/Heavy before execution.

**When trigger was "start master loop":** Step 0.M (Loop Master) has already been run: other loops researched, universal files updated. Proceed with checkpoint and Phase 0 below.

**Variants:** When user says **GO quick** or **GO standard**, use the phase subsets and time allocations in [LOOP_VARIANTS.md](./LOOP_VARIANTS.md). Quick skips most phases; Standard skips or shortens Phase 3.

### 0.0 Health (liveness) at loop start

**Run once at loop start.** Execute `.\scripts\automation\loop_health_check.ps1 -Quick`. If exit code is non-zero, log "Liveness check failed; consider fixing before continuing" and optionally abort or fix environment. See [LOOP_HEALTH_CHECKS.md](./LOOP_HEALTH_CHECKS.md). This keeps health checks running at a constant during the loop (liveness at every phase boundary; readiness = pulse_check at phase end).

### 0.0f Diagnostic baseline (recommended every run; required for substantial runs)

Run a focused diagnostic sweep per [LOOP_DIAGNOSTIC_SWEEP.md](./LOOP_DIAGNOSTIC_SWEEP.md):

- read lints for touched files or likely target areas,
- review failing or ignored tests relevant to the run,
- search for one likely hotspot using targeted search,
- and note one residual risk.

When you want a repeatable baseline before doing the judgment-heavy part, run:

```powershell
.\scripts\automation\run_loop_diagnostic_baseline.ps1
```

Use the script output as the starting point, then add the run-specific interpretation that the script cannot know on its own.

For substantial coding runs or when the loop is about to touch build, test, or automation wiring, prefer one consolidated readiness sweep early in the run:

```powershell
.\scripts\automation\run_simple_debug_cleanup.ps1
```

Use this as a heavier-but-repeatable debugging baseline, not as a replacement for every phase-end pulse. The normal pulse remains the lightweight default; the consolidated script is the deep-debug pass when broader proof is useful.

Add a `Diagnostic Sweep` block to the summary.

### 0.0a User preferences & design intent (first, before any changes)

**Get personal with the code.** Read [USER_PREFERENCES_AND_DESIGN_INTENT.md](./USER_PREFERENCES_AND_DESIGN_INTENT.md) **first**—before any other research or changes. Learn the subtle details the user prefers so the loop does not stray from the original design.

| Action | Purpose |
|--------|---------|
| Read USER_PREFERENCES_AND_DESIGN_INTENT | Must-not-change list; Design intent; Subtle preferences |
| Note in research output | "Design intent: X. Must not change: Y. User preferences: Z." |
| Apply throughout loop | Before any UI change: check against preferences. When uncertain: suggest, don't implement. |

**Rule:** Do not drift from the original design. When in doubt, ask.

### 0.0 Pre-loop checkpoint (before any changes)

**Medium tier.** Save a copy before the Improvement Loop so you can revert if something breaks:

- **Option A:** `git add -A && git commit -m "Pre-improvement-loop checkpoint YYYY-MM-DD"` (if working tree clean)
- **Option B:** `git tag improvement-loop-pre-$(date +%Y%m%d-%H%M)` (tag current HEAD)
- **Note checkpoint** in summary (commit hash or tag name) so "revert" can restore it.

**Revert:** When user says **"revert"**, restore from the checkpoint: `git reset --hard <commit>` or `git checkout <tag>`.

### 0.0b Meta-Research (optional)

**Meta-Researchers:** Use [META_RESEARCH_CHECKLIST.md](./META_RESEARCH_CHECKLIST.md) if it exists; else answer inline: (1) Research quality? (2) Gaps? (3) Suggested improvement? Add one-line meta-note to summary: "Research quality: X. Gaps: Y. Suggested improvement: Z."

### 0.0c Read Hub at loop start (required)

**Apply to your loop:** Per [UNIVERSAL_LOOP_PROMPT.md](../agents/data-sets/hub/UNIVERSAL_LOOP_PROMPT.md): at loop start, open **`docs/agents/data-sets/hub/README.md`**, scan the Hub index table, and read or skim each listed file relevant to this loop (e.g. improvement-loop reports, file-organizer index). Note in your research output: **Hub consulted:** [list files or roles you read]. **Advice/rules applied:** [1–3 bullets from the Hub for this run]. This keeps the loop aligned with what other agents have already established.

### 0.0d Read shared state (required when loops may run together)

**Apply to your loop:** Per [LOOP_DYNAMIC_SHARING.md](./LOOP_DYNAMIC_SHARING.md): at loop start, read **`docs/automation/loop_shared_events.jsonl`** (tail: last 50 lines) and **`docs/automation/loop_latest/token.json`**, **`loop_latest/cyber.json`**, **`loop_latest/synthetic_data.json`** (other loops; skip improvement.json). Note in your research output: **Shared state (other loops):** [what you will use from others — e.g. Token last run summary path, Cyber next_steps, suggested_next_steps]. This ensures when all loops run at the same time, data is shared dynamically and you can react/act on the latest from other loops.

### 0.0e Continuity baseline (recommended; required after loop/gate/shared-state edits)

Run:

```powershell
.\scripts\automation\run_loop_continuity_tests.ps1
```

If any suite fails, either fix the drift before proceeding or log why this run is documentation-only and not changing behavior.

### 0.1 Research

**Reasoning checkpoint (before research):** "What do I need to know to make good decisions this run? What did last run miss?" See [IMPROVEMENT_LOOP_REASONING.md](./IMPROVEMENT_LOOP_REASONING.md).

**Set this run's focus:** Read last summary; use "Next run focus" if suggested, else next in [LOOP_FOCUS_ROTATION.md](./LOOP_FOCUS_ROTATION.md) order. Default: Security.

**Self-improvement and loop-improvement (required):** Every loop run must include in research the **self-improvement and loop-improvement** docs. Read or skim: [LOOP_LESSONS_LEARNED.md](./LOOP_LESSONS_LEARNED.md), [SELF_IMPROVING_LOOP_RESEARCH.md](./SELF_IMPROVING_LOOP_RESEARCH.md), [CURSOR_SELF_IMPROVEMENT.md](./CURSOR_SELF_IMPROVEMENT.md), and [QUALITY_STANDARDS_AND_PROOF.md](./QUALITY_STANDARDS_AND_PROOF.md). Note in your research output what you applied or will apply (e.g. "Avoid X per LOOP_LESSONS_LEARNED"; "Single highest-impact change per SELF_IMPROVING_LOOP_RESEARCH"; "Proof of quality gate evidence for this run: Y"). **Light and Medium** = auto-implement; **drastic loop improvements** (routine changes, new phases, new loops) = **Heavy** — document only, no auto implementation. See [UNIVERSAL_LOOP_PROMPT](../agents/data-sets/hub/UNIVERSAL_LOOP_PROMPT.md) and [LOOP_TIERING.md](./LOOP_TIERING.md).

Read these files **before** making any changes. (0.0a already read USER_PREFERENCES_AND_DESIGN_INTENT.)

| Doc | Purpose |
|-----|---------|
| `docs/automation/USER_PREFERENCES_AND_DESIGN_INTENT.md` | **Read first (0.0a).** User preferences; design intent; must-not-change |
| `docs/automation/IMPROVEMENT_LOOP_SUMMARY_<latest>.md` | What was done last loop; suggested next steps |
| `docs/CRUCIAL_IMPROVEMENTS_TODO.md` | Prioritized backlog; pick 1–2 low-risk items |
| `docs/REDUNDANT_DEAD_CODE_REPORT.md` | Safe dead code to remove |
| `docs/qa/FAILING_OR_IGNORED_TESTS.md` | Test health; @Ignore reasons |
| `docs/security/SECURITY_NOTES.md` | Security checklist; PII, encryption, FileProvider |
| `docs/security/SECURITY_CHECKLIST.md` | Pre-release items |
| `docs/automation/DESIGN_AND_UX_RESEARCH.md` or `docs/ux/UI_CONSISTENCY.md` | Design research; fallback to UI_CONSISTENCY if DESIGN_AND_UX missing |
| `docs/automation/LOOP_TIERING.md` | Light / Medium / Heavy task definitions; approval gate |
| `docs/automation/LOOP_FOCUS_ROTATION.md` | This run's focus; bias effort |
| `docs/automation/HEAVY_IDEAS_FAVORITES.md` | User favorites for Heavy ideas; surface favorites first; **§ Production stage (100% approved):** when Light/Medium run, include incremental work on these critical features — time and patience; may not be complete in one go. |
| `docs/automation/LOOP_LESSONS_LEARNED.md` | Self-improvement: lessons from past runs; read and append 1–3 bullets per run. **Required in research.** Skip if missing. |
| `docs/automation/SELF_IMPROVING_LOOP_RESEARCH.md` | Methods to make the loop self-improving; **required in research** when running any loop. |
| `docs/automation/CURSOR_SELF_IMPROVEMENT.md` | Safe web, prompt-injection awareness, Phase 0.3; **required in research** per universal loop rules. |
| `docs/automation/QUALITY_STANDARDS_AND_PROOF.md` | Quality standards baseline + mandatory proof-of-quality evidence format for summary grading. |
| `docs/automation/DATA_USEFULNESS_AND_PRUNING_RESEARCH.md` | How to evaluate data usefulness and what to prune; Hub/deposit criteria; optional when deciding what to keep or archive. |
| `docs/automation/AUTOMATION_TO_PROMPT_RESEARCH.md` | How to convert regular automations into prompt-driven LOOP GATES; include one applied note when process automation is touched. |
| `docs/automation/USER_METADATA_USAGE_GUIDE.md` | Metadata (when Data/Metrics focus); skip if missing |
| `docs/TASKS_INDEX.md` or `docs/agents/COMPREHENSIVE_AGENT_TODOS.md` | Consolidated TODOs; check and update during run. Skip if missing. |

**Output:** One-line research note: "Design intent: [from 0.0a]. **Hub consulted:** [from 0.0c]. **Shared state (other loops):** [from 0.0d — what you will use from Token/Cyber/Synthetic Data latest]. Last loop did X; next: Y, Z. This focus: [Security|UI/UX|…]. [Optional: Last run conclusion / This run we will not repeat: … per SELF_IMPROVING_LOOP_RESEARCH.] Security gaps: A. Smoothness: B. Design: [topic]."

### 0.1b Task classification & approval gate

**Reasoning checkpoint (before task selection):** "Given this focus and backlog, which 1–2 tasks give the highest value for lowest risk? Why?" See [IMPROVEMENT_LOOP_REASONING.md](./IMPROVEMENT_LOOP_REASONING.md).

Per [LOOP_TIERING.md](./LOOP_TIERING.md):

1. **Classify** each task from research (CRUCIAL, REDUNDANT_DEAD_CODE, suggested next steps) as Light, Medium, or Heavy.
2. **Include sandboxing (required every main loop run)** as a Medium task: document new idea in FUTURE_IDEAS, validate a sandboxed feature, or improve sandbox workflow. See [SANDBOX_TESTING.md](./SANDBOX_TESTING.md).
3. **Execute approved-from-Heavy Medium queue first** when running Light/Medium: run concrete incremental work for approved items ([HEAVY_IDEAS_FAVORITES.md](./HEAVY_IDEAS_FAVORITES.md) § Production stage; current #7, #8, #9, #17, #20) before optional Medium tasks.
4. **Full autonomy:** Light and Medium run **without stopping**. No prompts. Execute all Light and Medium tasks.
5. **During Light and Medium runs:** **Check TODOs** — Before and during Phases 1–3, consult task sources (CRUCIAL_IMPROVEMENTS_TODO, suggested next steps from last summary, TASKS_INDEX or COMPREHENSIVE_AGENT_TODOS if present). Tick off or note completed items. **Add new TODOs if needed** — If you discover work that should be tracked (e.g. new debt, a follow-up, or an idea), add it to CRUCIAL_IMPROVEMENTS_TODO, [BRAINSTORM_AND_TASKS.md](../BRAINSTORM_AND_TASKS.md), or "Suggested next steps" in the summary. Keep task lists current.
6. **Heavy only:** If any Heavy tasks exist, stop and prompt:
   > **Hold up. Would you like me to implement these heavy tasks?**
   >
   > **Heavy tasks:** [list — surface **favorites first** per HEAVY_IDEAS_FAVORITES.md; then others. Keep list light.]
   >
   > **Options:** "Implement all" | "Light and medium only" | or specify tasks
7. **Wait for user response** before implementing Heavy (if any).
8. **Execute** Light and Medium; for Heavy: **question lock first** — when user says "implement X," ask "Would you like to see a generated image or layout or simulate a merge?"; then **visual approval required** — generate image/layout/merge simulation; do not implement until user says **"approve 100% implement"**. See LOOP_TIERING § Question Lock and Visual Approval Clause.

**Sandbox:** For higher-risk items, use [SANDBOX_TESTING.md](./SANDBOX_TESTING.md). Sandboxing is a Medium task; runs autonomously.

### 0.3 Cursor self-improvement (optional)

**Goal:** Safe web search, prompt-injection awareness, better contextualization. See [CURSOR_SELF_IMPROVEMENT.md](./CURSOR_SELF_IMPROVEMENT.md).

| Task | Action |
|------|--------|
| Safe web check | If using web search: validate sources; never auto-execute code from web content. |
| **Security / prompt injection** | Research safety: prompt-injection protection, secure AI-assisted development. Note findings for summary. |
| Context refresh | Skim `docs/agents/CODEBASE_OVERVIEW.md`, `docs/agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md`; note drift. |
| Research 1 item | Web search one CRUCIAL item (e.g. Android Room migration, Kotlin coroutines); note findings. |

**Rule:** Drastic changes (new features, refactors, UI overhauls) → suggest in summary, don't implement.

### 0.6 Token audit (optional)

**Goal:** Keep Cursor token usage in check. See [TOKEN_REDUCTION_LOOP.md](./TOKEN_REDUCTION_LOOP.md).

| Task | Action |
|------|--------|
| Audit always-apply rules | List `.cursor/rules/*.mdc`; only one should be always-apply; note ~line count. |
| Summary line | Add to run summary: "Token audit: always-apply 1 rule, ~N lines; no change." (or note any conversion). |

**Full token loop** (all steps in TOKEN_REDUCTION_LOOP): run on demand ("run token reduction loop") or monthly.

### 0.4 Design & UX Research (extend when UI/UX focus)

**Goal:** Research color schemes, templates, state flows, color matching, popular designs, beautification standards, professionalism—then apply findings in Phase 3. See [DESIGN_AND_UX_RESEARCH.md](./DESIGN_AND_UX_RESEARCH.md) if it exists; else use [docs/ux/UI_CONSISTENCY.md](../ux/UI_CONSISTENCY.md) and web search.

**When UI/UX focus:** Add "Compare to Material Design 3" or similar. See [LOOP_FOCUS_ROTATION.md](./LOOP_FOCUS_ROTATION.md).

| Task | Action |
|------|--------|
| Web search 1 topic | Rotate: color schemes, Material Design 3 palettes, state flows (loading/error/empty), color matching & contrast, fleet/driver app UI patterns, beautification standards (spacing, typography, elevation), professional UI principles. |
| Compare to current | Read `app/src/main/res/values/colors.xml`, `docs/ux/UI_CONSISTENCY.md`; note gaps. |
| Log findings | Note 2–3 actionable items in research note; add to summary under "Design Research". |

**Output:** One-line design note: "Researched X; findings: A, B. Gaps: C." Apply at most one subtle improvement in Phase 3.

**When judging UI quality:** Use [docs/ux/PLEASANTNESS_AND_FLOW_STANDARD.md](../ux/PLEASANTNESS_AND_FLOW_STANDARD.md) so "pleasant" and "professional" are scored instead of guessed.

**Medium tier — Advanced beautification & organizing research:** When running Medium, include deeper research: advanced color theory, typography hierarchy, organizing best practices (file structure, doc layout), professional fleet/driver app patterns. Document findings; apply one subtle improvement per loop.

---

## Phase 1: Quick Wins + Security + Smoothness

**Health (liveness):** At start of phase run `.\scripts\automation\loop_health_check.ps1 -Quick`. If non-zero, log and consider fixing. See [LOOP_HEALTH_CHECKS.md](./LOOP_HEALTH_CHECKS.md).

**Listener:** Invoke `.\scripts\automation\loop_listener.ps1 -Event phase_start -Phase "1" -Note "Quick wins"` at start; `phase_end` at end. See [LOOP_LISTENER.md](./LOOP_LISTENER.md).

**Reasoning checkpoint (before each change):** "What is my goal? What could go wrong? Is there a simpler option?" See [IMPROVEMENT_LOOP_REASONING.md](./IMPROVEMENT_LOOP_REASONING.md). Apply throughout Phases 1–3.

**Goal:** Low-risk fixes, one security improvement, one smoothness improvement.

**Kaizen rule:** One improvement per category per loop. Bias effort toward this run's focus (see [LOOP_FOCUS_ROTATION.md](./LOOP_FOCUS_ROTATION.md)).

### 1.1 Quick wins

- **Dead code:** Remove 1–2 safest items from REDUNDANT_DEAD_CODE_REPORT §2 (grep first; no injected params).
- **Constants:** Align any remaining BuildConfig/service constant drift.
- **Doc links:** Verify CRUCIAL_IMPROVEMENTS_TODO is linked from docs/README.md.

### 1.2 Security (full checklist when Security focus)

**When Security focus:** Run full security checklist — PII grep, FileProvider, Keystore, secrets, dependency audit. Use [SECURITY_NOTES.md](../security/SECURITY_NOTES.md) and [SECURITY_CHECKLIST.md](../security/SECURITY_CHECKLIST.md). Optionally run `./gradlew dependencyUpdates` or document "last dependency audit" in summary.

**Cyber Security Loop integration:** When Security focus, run `./gradlew :app:securitySimulations` to execute attack simulations (SecuritySimulationTest + Purple training JSON). Log results to proof of work; add metrics to summary (simulations_run, passed, failed). See [CYBER_SECURITY_LOOP_ROUTINE.md](./CYBER_SECURITY_LOOP_ROUTINE.md).

**Else,** pick **one** per SECURITY_NOTES:

| Item | Action |
|------|--------|
| No PII in logs | Grep for `Log.*lat\|lon\|coordinates\|tripId`; remove or redact. |
| FileProvider scope | Verify export/share flows use app-private or safe paths only. |
| StandaloneOfflineService key | Add KDoc note: "TODO: Migrate to Keystore + EncryptedSharedPreferences" if not present. |
| SECURITY_CHECKLIST | Ensure no new secrets in logs; one-line verification. |

### 1.3 Smoothness

Pick **one**:

| Item | Action |
|------|--------|
| Error handling | Ensure trip save/recovery uses ErrorHandler; add fallback log if missing. |
| Null safety | Add one `?.` or `?:` in a critical path (e.g. TripRepository adapter). |
| Logging | Add one clear log in trip start → save path for production debugging. |
| Animation | Ensure stat card expand/collapse has `animateLayoutChanges` or smooth transition. |

### 1.4 Pulse check

- Run: `.\scripts\automation\pulse_check.ps1 -Note "Phase 1: Quick wins, security, smoothness"`
- Optional deeper first pass: `.\scripts\automation\pulse_check.ps1 -UseSimpleDebugCleanup -Note "Phase 1: consolidated debug cleanup"`
- Ensure tests pass.

---

## Phase 2: Test Health & Documentation

**Health (liveness):** At start of phase run `.\scripts\automation\loop_health_check.ps1 -Quick`. If non-zero, log and consider fixing. See [LOOP_HEALTH_CHECKS.md](./LOOP_HEALTH_CHECKS.md).

**Listener:** Invoke `loop_listener.ps1 -Event phase_start -Phase "2"` at start; `phase_end` at end.

**Goal:** Fix or document one test; align docs.

### 2.1 Test health

- Per FAILING_OR_IGNORED_TESTS.md — ensure all @Ignore have clear reason.
- Optionally fix one trivial ignored test (e.g. LocationValidationServiceTest) or document why deferred.
- Do NOT spend >10 min on any single test.

### 2.2 Documentation

- Update IMPROVEMENT_LOOP_SUMMARY or create new `IMPROVEMENT_LOOP_SUMMARY_<date>.md` stub with "In progress."
- Add one cross-link if CRUCIAL_IMPROVEMENTS or ROADMAP is missing from docs index.

### 2.3 Sandboxing (when Medium approved)

**Every loop:** Medium tier improves on sandboxed ideas. Run **one** sandbox action per [SANDBOX_TESTING.md](./SANDBOX_TESTING.md) and (if present) [SANDBOX_COMPLETION_PERCENTAGE.md](./SANDBOX_COMPLETION_PERCENTAGE.md):

| Action | Example |
|--------|---------|
| Document new idea | Add one item to FUTURE_IDEAS.md (from ROADMAP or backlog) |
| Validate sandboxed feature | Create design brief, feature branch stub, or validation checklist for one FUTURE_IDEAS item |
| **Improve sandboxed idea** | Add design brief, validation checklist, or advance completion % for 1–2 ideas in [HEAVY_IDEAS_FAVORITES.md](./HEAVY_IDEAS_FAVORITES.md) / FUTURE_IDEAS. Use SANDBOX_COMPLETION_PERCENTAGE if available. Merging should not be taken lightly. |
| **Sandbox testing for merge** | Test new features in a branch or build variant before merging into main; validate behavior; merge only when safe |
| Improve sandbox | Update FUTURE_IDEAS index, add cross-link, or document promotion flow |

**Rule:** Sandboxing is additive (docs, branches, briefs). No implementation of Heavy features without user confirmation. No merge into main without sandbox validation. **Every loop:** Medium tier improves 1–2 sandboxed ideas; report progress in summary (e.g. completion % if tracked, or "improved N ideas").

### 2.4 Sandboxing verification (required every main loop run)

- Record one explicit verification line in working notes and summary:
  - `Sandboxing status: pass/fail`
  - `Sandbox action executed: [what was done]`
  - `Sandbox artifact path: [file/path]`
- When the sandbox action involves `virtual fleet` or shared-pool work, also record:
  - `Synthetic batch ids: [ids or none]`
  - `Reference datasets used: [source names or none]`
  - `Contamination check: [pass/fail + what was checked]`
- If no sandbox action was executed, mark as `fail` and add a top-priority next step to restore compliance next run.

### 2.5 Unit tests

- Run: `.\gradlew.bat :app:testDebugUnitTest --no-daemon`
- Log result.

### 2.6 Pulse check

- Run: `.\scripts\automation\pulse_check.ps1 -Note "Phase 2: Test health, doc links"`

---

## Phase 3: UI Polish + Smoothness (80–105 min)

**Listener:** Invoke `loop_listener.ps1 -Event phase_start -Phase "3"` at start; `phase_end` at end.

**Goal:** Sharpen, pop, useful info. Apply design research from Phase 0.4. One smoothness tweak.

**Kaizen rule:** Apply one subtle improvement only. No more than one to avoid drift (user rule).

### 3.1 Strings / accessibility

- One string fix: typo, contentDescription, or clearer label per TERMINOLOGY_AND_COPY.md.
- Example: Add contentDescription to a key icon if missing.
- **Master Loop only:** Non-master loops must log this as a proposal, not implement.

### 3.2 Stat card / UI consistency (10 min)

- Per UI_CONSISTENCY.md — verify 6dp elevation, 12dp corner radius, 16dp padding.
- **Apply design research:** Use findings from Phase 0.4 (DESIGN_AND_UX_RESEARCH.md). One subtle improvement: OOR contrast, divider, spacing, or color tweak for professionalism/beautification.
- Examples: Adjust one color for better contrast; add 1–2dp spacing on 8dp grid; improve state flow (loading/empty) per research.
- **Automation gate required:** If implementing, run the frontend gate and record OBS/SSS + decision in summary.

### 3.3 Useful information

- One small useful info: e.g. version in Settings (if not done), or period mode hint.
- **Constraint:** No unwarranted UI changes without user permission. Drastic design changes → suggest in summary, don't implement.
- **Master Loop only:** Non-master loops must defer implementation to Master Loop.

### 3.4 Pulse check

- Run: `.\scripts\automation\pulse_check.ps1 -Note "Phase 3: UI polish, smoothness"`
- If script-heavy or build-health changes were made, use `-UseSimpleDebugCleanup` once before ending the phase so detekt joins the readiness evidence.

---

## Phase 4: Final Pulse & Summary (105–120 min)

**Health (liveness):** At start of phase run `.\scripts\automation\loop_health_check.ps1 -Quick`. If non-zero, log and consider fixing. See [LOOP_HEALTH_CHECKS.md](./LOOP_HEALTH_CHECKS.md).

**Listener:** Invoke `loop_listener.ps1 -Event phase_start -Phase "4"` at start; `phase_end` at end.

**Goal:** Lint, summary, suggested next steps.

### 4.1 Lint

- Run: `.\gradlew.bat :app:lintDebug --no-daemon`
- Fix only **obvious** new issues from this session.

### 4.1b Shipability check (optional)

**When Shipability focus:** Run shipability check — 7 signals, 10-min timebox. Use [STORE_CHECKLIST.md](../STORE_CHECKLIST.md) if no SHIPABILITY_CHECKLIST. Run before pre-release loops or when user says "shipability focus."

### 4.2 Final pulse

- Run: `.\scripts\automation\pulse_check.ps1 -Note "Phase 4: Final. Lint reviewed."`
- Use `-UseSimpleDebugCleanup` here only when the run materially changed build, lint, or automation behavior and you want one final consolidated proof pass.

### 4.3 Write summary

**Output:** `docs/automation/IMPROVEMENT_LOOP_SUMMARY_<date>.md`

**Reasoning checkpoint (before summary):** "What did I learn? What should the next run do differently?" See [IMPROVEMENT_LOOP_REASONING.md](./IMPROVEMENT_LOOP_REASONING.md).

**Contents (A-grade format):** See [LOOP_METRICS_TEMPLATE.md](./LOOP_METRICS_TEMPLATE.md).

1. **Phase 0 research note** — One-line: design intent, last loop, this focus
2. **Run metadata** — Date, focus area, variant (Quick/Standard/Full)
3. **PDCA phase summary** — Plan (0), Do (1–3), Check (4), Act (4.3)
4. **Metrics** — Tests (pass/fail), lint (errors/warnings), files changed, checkpoint. **When Security focus:** Add Cyber Security metrics (simulations_run, passed, failed) from `./gradlew :app:securitySimulations`.
5. **Frontend vs backend** — Brief line: "Backend: [N] tasks/files; Frontend: [N] tasks/files." Per [LOOP_FRONTEND_VS_BACKEND_BREAKDOWN.md](./LOOP_FRONTEND_VS_BACKEND_BREAKDOWN.md); target mostly backend.
6. **What was done** — Table: Phase, Task, Status, Details
7. **Reasoning (this run)** — Table: Decision, Rationale. Per [IMPROVEMENT_LOOP_REASONING.md](./IMPROVEMENT_LOOP_REASONING.md). Makes logic traceable for next run.
8. **Research findings** — Design, security, meta-research; metadata (if Data/Metrics focus)
9. **Files modified** — List with one-line change
10. **Suggested next steps** — 4–6 items for next loop (see template below); actionable (include commands where helpful)
11. **File Organizer: recommended new ideas (required every run)** — **Every run must recommend at least 1–2 new ideas** (Light, Medium, or Heavy) **or** (when Heavy list at cap) judge/critique existing Heavy ideas. **Heavy list cap:** Count Heavy ideas in [FUTURE_IDEAS.md](../product/FUTURE_IDEAS.md) or HEAVY_IDEAS_FAVORITES. If **count ≥ 50** → **judge/critique mode:** do **not** add new Heavy ideas; instead **judge and critique** 1–2 existing ideas (e.g. score vs quality bar, suggest merge/remove/defer, improve description). If **count < 50** → **produce mode:** add **at least 1–2 new Heavy ideas** that meet the quality bar in [HEAVY_IDEAS_FAVORITES.md](./HEAVY_IDEAS_FAVORITES.md); document in FUTURE_IDEAS and list in summary. Surface **favorites first** (✅ in HEAVY_IDEAS_FAVORITES). Light/Medium ideas: add to LOOP_TIERING, CRUCIAL, or suggested next steps. Sandbox completion % — Report true % for 1–2 improved ideas when applicable. See [IMPROVEMENT_LOOP_TEAMS.md](./IMPROVEMENT_LOOP_TEAMS.md).
12. **Proof of quality** — Required evidence bundle per [QUALITY_STANDARDS_AND_PROOF.md](./QUALITY_STANDARDS_AND_PROOF.md): liveness, readiness (tests/lint), change proof, residual risks, and traceability links.
13. **Loop effectiveness** — What worked vs what did not; completion ratio of planned tasks; one bottleneck.
14. **Useful data generated** — Key artifacts/data produced this run and why each is reusable.
15. **Loop performance & health** — Liveness/readiness status, test/lint durations (if available), failures fixed, gate pass/fail.
16. **Interesting metrics** — 3–5 extra insights (e.g., warning delta, hub artifacts count, shared-state freshness, production-stage progress increments).
17. **Next run focus** — Suggested focus for next loop (from File Organizer or metrics)
18. **Quality Grade** — A/B/C with rationale and one improvement for next run
19. **Self-improvement (optional):** Append 1–3 bullets to [LOOP_LESSONS_LEARNED.md](./LOOP_LESSONS_LEARNED.md): Learned …; Avoid …; Next run try …. See [SELF_IMPROVING_LOOP_RESEARCH.md](./SELF_IMPROVING_LOOP_RESEARCH.md). Optionally add **Single highest-impact change this run:** [one sentence] and **One thing next run must consider:** [one line].
20. **Frontend automation gate (when UI touched)** — Include `Frontend Automation Gate` block from [FRONTEND_CHANGE_AUTOMATION_GATE.md](./FRONTEND_CHANGE_AUTOMATION_GATE.md): hard-stop checks, OBS/SSS, decision, evidence.
21. **Loop consistency check** — Include `Loop Consistency Check` block from [LOOP_CONSISTENCY_STANDARD.md](./LOOP_CONSISTENCY_STANDARD.md) and record `Consistency score: X/10`.
22. **Sandboxing verification** — Include `Sandboxing status (pass/fail)`, `Sandbox action executed`, and `Sandbox artifact path` to verify sandboxing ran this main loop.

**4.3b Record run (required):** Append this run to [IMPROVEMENT_LOOP_RUN_LEDGER.md](./IMPROVEMENT_LOOP_RUN_LEDGER.md). Add a new **Run YYYY-MM-DD** section with: Focus, Variant, Summary (link to this summary file), Metrics one-liner (tests, lint, files changed, checkpoint), **Sandboxing (pass/fail, action, artifact path)**, Next (1–2 bullets). Use the template in the ledger. This keeps a shared record for us and for other agents. See [IMPROVEMENT_LOOP_FOR_OTHER_AGENTS.md](./IMPROVEMENT_LOOP_FOR_OTHER_AGENTS.md).

**4.3c Consider send to hub when done (optional):** Per [UNIVERSAL_LOOP_PROMPT.md](../agents/data-sets/hub/UNIVERSAL_LOOP_PROMPT.md): if this run produced **completed, polished** output that other agents or the next run should use (e.g. proof of work, loop report, index, or a concise summary), consider depositing it to **`docs/agents/data-sets/hub/`** with naming **`YYYY-MM-DD_<role-or-topic>_<short-description>.<ext>`** and optionally add a one-line entry to **`hub/README.md`**. Hub = this data folder; not GitHub. See [SEND_TO_HUB_PROMPT.md](../agents/data-sets/hub/SEND_TO_HUB_PROMPT.md).

**4.3d Write shared state (required):** Per [LOOP_DYNAMIC_SHARING.md](./LOOP_DYNAMIC_SHARING.md): append one **finished** event to **`docs/automation/loop_shared_events.jsonl`** (ts, loop=`improvement`, event=`finished`, run_id, summary_path, next_steps from this run, optional checkpoint) and overwrite **`docs/automation/loop_latest/improvement.json`** with this run's latest output (last_run_ts, last_run_id, summary_path, suggested_next_steps, optional checkpoint and key_decisions). This ensures other loops (Token, Cyber, Synthetic Data) can read your output when they run and react/act appropriately.

Copy into each summary. Update checkboxes as items are completed.

```markdown
## Suggested Next Steps for Next Loop

- [ ] Resolve pre-existing build issues — `./gradlew clean assembleDebug` if AAPT/dependency errors
- [ ] Dead code cleanup — REDUNDANT_DEAD_CODE_REPORT §2 (CustomCalendarDialog, etc.)
- [ ] LocationValidationServiceTest — Fix ignored test in unit suite or document with reason (no instrumented tests in this environment)
- [ ] Gradle 9 readiness — Run `--warning-mode all`, document in GRADLE_9_MIGRATION_NOTES
- [ ] Security: StandaloneOfflineService — Migrate encryption key to Keystore (larger task)
- [ ] Security: PII audit — Re-grep logs for coordinates, trip IDs; redact if found
- [ ] Design research — Next topic: color schemes / state flows / contrast / fleet app patterns (rotate)
- [ ] **Readiness** — Update [docs/readiness/GRAND_PROGRESS_BAR.md](../readiness/GRAND_PROGRESS_BAR.md) with current Status/Notes; add one line to this summary: "Grand progress bar: N/10 green, M amber, K red."
```

---

## Subagent Spawn Reference

**Purpose:** Delegate parallelizable work to specialized subagents. See [AGENT_USAGE_RESEARCH.md](./AGENT_USAGE_RESEARCH.md) for when/where/how.

| Phase | Subagent | Task | Parallel? |
|-------|----------|------|-----------|
| 0.0b | GeneralPurpose (Meta-Research) | META_RESEARCH_CHECKLIST; one-line meta-note | Optional; can run with 0.1 |
| 0.4 | GeneralPurpose | Design & UX research (color, typography, state flows, fleet app patterns) | Yes, with 0.1 |
| 1.1 | GeneralPurpose | Dead code removal; BuildConfig alignment; doc links | Yes |
| 1.1 | Shell | Unit tests: `.\gradlew.bat :app:testDebugUnitTest --no-daemon` | Yes |
| 1.2 | GeneralPurpose | Security grep; SECURITY_LOOP_CHECKLIST when Security focus | Yes |
| 2.1 | GeneralPurpose | Test health; @Ignore review per FAILING_OR_IGNORED_TESTS | Yes |
| 2.5 | Shell | Unit tests | After 2.1 |
| 3.2 | GeneralPurpose | Stat card review; apply design research; one subtle improvement | Yes |
| 4.3 | File Organizer | Recommend new ideas to Light/Medium/Heavy; metrics-based next focus | After summary |

**Model selection:** Fast for Shell; more capable for code reasoning (GeneralPurpose: security, tests, dead code).

**Delegation rules:**
- Give subagent clear scope: file paths, constraints, "do not touch X"
- Pass constraints: no unwarranted UI changes, tests must pass, timebox 10 min per test
- Timebox: "Do not spend >10 min on any single test"
- Subagent returns: what changed, why, test result

---

## Out of Scope (Do Not Do)

- OfflineDataManager load/save (CRUCIAL §2)
- Location jump detection (CRUCIAL §3)
- Trip history → TripDetails navigation (CRUCIAL §4)
- Statistics monthly-only refactor (CRUCIAL §9) — user approval needed
- Gradle 9 migration
- New features or major refactors
- **Instrumented tests** — Loop runs in a full-autonomous environment with **unit tests only** (no device/emulator). Do not run `connectedAndroidTest` or suggest "move to instrumented suite" as a fix. For ignored tests: fix in unit suite, document with reason, or defer. Instrumented tests are for CI/pre-release when a device or emulator is available.

---

## Routine Cadence

- **Sprint:** Run every 1–2 weeks.
- **Pre-release:** Run + SECURITY_CHECKLIST before ship.
- **After major changes:** Run to catch regressions and polish.

---

**Audit:** For blind spots and loose ends, see [IMPROVEMENT_LOOP_AUDIT.md](./IMPROVEMENT_LOOP_AUDIT.md). Re-run audit after major changes.

---

*Routine created for repeatable Improvement Loops. Research first, allowlist for autonomy, then execute.*
