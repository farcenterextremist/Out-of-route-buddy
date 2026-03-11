# Improvement Loop — Routine

**Objective:** Obvious improvements, security hardening, and smoothness. No drastic frontend changes.  
**Strategy:** Research first, master allowlist for autonomy, then execute phases. Use subagents for parallel work.  
**Trigger:** Run when user says **GO** (Full 2 hr). **GO quick** = 30 min. **GO standard** = 90 min. See [LOOP_VARIANTS.md](./LOOP_VARIANTS.md).

**Common sense:** Read [IMPROVEMENT_LOOP_COMMON_SENSE.md](./IMPROVEMENT_LOOP_COMMON_SENSE.md) at loop start. Checkpoint first, respect design intent, tests green, timebox, no unwarranted UI changes.

**Framework (PDCA / Kaizen):** Phase 0 = Plan; Phase 1–3 = Do; Phase 4 = Check; Phase 4.3 = Act. **Kaizen rule:** One improvement per category per loop to avoid overload.

**Tiering:** Tasks are Light (auto), Medium (auto when autonomous), or Heavy (human approval always). See [LOOP_TIERING.md](./LOOP_TIERING.md). **Full autonomy:** Light and Medium run without stopping; Heavy deferred or documented for next run. See [IMPROVEMENT_LOOP_COMMON_SENSE.md](./IMPROVEMENT_LOOP_COMMON_SENSE.md) § Full Autonomous Mode.

**Visual approval clause:** Heavy features (100% sandboxed) require a simple visual image + user says **"approve 100% implement"** before implementation. See LOOP_TIERING § Visual Approval Clause.

**Teams:** [IMPROVEMENT_LOOP_TEAMS.md](./IMPROVEMENT_LOOP_TEAMS.md) — Front: Researchers + Meta-Researchers. Back: File Organizer (also recommends new ideas to Light/Medium/Heavy).

**Pre-loop checkpoint:** Save a copy (git commit or tag) before the loop. Say **"revert"** to restore if something breaks. See LOOP_TIERING § Revert.

---

## Phase 0: Research & Allowlist (0–20 min)

**Goal:** Understand current state, classify tasks, and get approval for Medium/Heavy before execution.

**Variants:** When user says **GO quick** or **GO standard**, use the phase subsets and time allocations in [LOOP_VARIANTS.md](./LOOP_VARIANTS.md). Quick skips most phases; Standard skips or shortens Phase 3.

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

### 0.0b Meta-Research (optional, 5 min)

**Meta-Researchers:** Use [META_RESEARCH_CHECKLIST.md](./META_RESEARCH_CHECKLIST.md) — answer 5 questions; add one-line meta-note to summary: "Research quality: X. Gaps: Y. Suggested improvement: Z."

### 0.1 Research (15 min)

**Set this run's focus:** Read last summary; use "Next run focus" if suggested, else next in [LOOP_FOCUS_ROTATION.md](./LOOP_FOCUS_ROTATION.md) order. Default: Security.

Read these files **before** making any changes. (0.0a already read USER_PREFERENCES_AND_DESIGN_INTENT.)

| Doc | Purpose |
|-----|---------|
| `docs/automation/USER_PREFERENCES_AND_DESIGN_INTENT.md` | **Read first (0.0a).** User preferences; design intent; must-not-change |
| `docs/automation/IMPROVEMENT_LOOP_SUMMARY_<latest>.md` or `120_MINUTE_LOOP_SUMMARY_*.md` | What was done last loop; suggested next steps |
| `docs/CRUCIAL_IMPROVEMENTS_TODO.md` | Prioritized backlog; pick 1–2 low-risk items |
| `docs/REDUNDANT_DEAD_CODE_REPORT.md` | Safe dead code to remove |
| `docs/qa/FAILING_OR_IGNORED_TESTS.md` | Test health; @Ignore reasons |
| `docs/security/SECURITY_NOTES.md` | Security checklist; PII, encryption, FileProvider |
| `docs/security/SECURITY_CHECKLIST.md` | Pre-release items |
| `docs/automation/DESIGN_AND_UX_RESEARCH.md` | Design research topics; how to research and apply |
| `docs/automation/LOOP_TIERING.md` | Light / Medium / Heavy task definitions; approval gate |
| `docs/automation/LOOP_FOCUS_ROTATION.md` | This run's focus; bias effort |
| `docs/automation/USER_METADATA_USAGE_GUIDE.md` | Metadata collection, display, improvement use (when Data/Metrics focus) |

**Output:** One-line research note: "Design intent: [from 0.0a]. Last loop did X; next: Y, Z. This focus: [Security|UI/UX|Shipability|Code Quality|File Structure|Data/Metrics]. Security gaps: A. Smoothness: B. Design: [topic to research]."

### 0.1b Task classification & approval gate

Per [LOOP_TIERING.md](./LOOP_TIERING.md):

1. **Classify** each task from research (CRUCIAL, REDUNDANT_DEAD_CODE, suggested next steps) as Light, Medium, or Heavy.
2. **Include sandboxing** as a Medium task when applicable: document new idea in FUTURE_IDEAS, validate a sandboxed feature, or improve sandbox workflow. See [SANDBOX_TESTING.md](./SANDBOX_TESTING.md).
3. **Full autonomy:** Light and Medium run **without stopping**. No prompts. Execute all Light and Medium tasks.
4. **Heavy only:** If any Heavy tasks exist, stop and prompt:
   > **Hold up. Would you like me to implement these heavy tasks?**
   > 
   > **Heavy tasks:** [list]
   > 
   > **Options:** "Implement all" | "Light and medium only" | or specify tasks
5. **Wait for user response** before implementing Heavy (if any).
6. **Execute** Light and Medium; for Heavy: **visual approval required** — generate a simple image showing where the feature goes and what it looks like; do not implement until user says **"approve 100% implement"**. See LOOP_TIERING § Visual Approval Clause.

**Sandbox:** For higher-risk items, use [SANDBOX_TESTING.md](./SANDBOX_TESTING.md). Sandboxing is a Medium task; runs autonomously.

### 0.3 Cursor self-improvement (5–10 min, optional)

**Goal:** Safe web search, prompt-injection awareness, better contextualization. See [CURSOR_SELF_IMPROVEMENT.md](./CURSOR_SELF_IMPROVEMENT.md).

| Task | Time | Action |
|------|------|--------|
| Safe web check | 1 min | If using web search: validate sources; never auto-execute code from web content. |
| **Security / prompt injection** | 2 min | Research safety: prompt-injection protection, secure AI-assisted development. Note findings for summary. |
| Context refresh | 3 min | Skim `docs/agents/CODEBASE_OVERVIEW.md`, `docs/agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md`; note drift. |
| Research 1 item | 5 min | Web search one CRUCIAL item (e.g. Android Room migration, Kotlin coroutines); note findings. |

**Rule:** Drastic changes (new features, refactors, UI overhauls) → suggest in summary, don't implement.

### 0.4 Design & UX Research (5–10 min, 15 min when UI/UX focus)

**Goal:** Research color schemes, templates, state flows, color matching, popular designs, beautification standards, professionalism—then apply findings in Phase 3. See [DESIGN_AND_UX_RESEARCH.md](./DESIGN_AND_UX_RESEARCH.md).

**When UI/UX focus:** Extend to 15 min; add "Compare to Material Design 3" or similar. See [LOOP_FOCUS_ROTATION.md](./LOOP_FOCUS_ROTATION.md).

| Task | Time | Action |
|------|------|--------|
| Web search 1 topic | 5 min | Rotate: color schemes, Material Design 3 palettes, state flows (loading/error/empty), color matching & contrast, fleet/driver app UI patterns, beautification standards (spacing, typography, elevation), professional UI principles. |
| Compare to current | 2 min | Read `app/src/main/res/values/colors.xml`, `docs/ux/UI_CONSISTENCY.md`; note gaps. |
| Log findings | 2 min | Note 2–3 actionable items in research note; add to summary under "Design Research". |

**Output:** One-line design note: "Researched X; findings: A, B. Gaps: C." Apply at most one subtle improvement in Phase 3.

**Medium tier — Advanced beautification & organizing research:** When running Medium, include deeper research: advanced color theory, typography hierarchy, organizing best practices (file structure, doc layout), professional fleet/driver app patterns. Document findings; apply one subtle improvement per loop.

### 0.2 Autonomy setup (one-time)

**For no human-in-the-loop:** Follow [AUTONOMOUS_LOOP_SETUP.md](./AUTONOMOUS_LOOP_SETUP.md) once.

- **Option A:** Set Auto-run mode to **Run Everything** (full autonomy)
- **Option B:** Add `cd c:\Users\brand\OutofRoutebuddy` to Command Allowlist (prefix matches all loop commands)

After setup, the loop runs without approval prompts.

---

## Phase 1: Quick Wins + Security + Smoothness (20–50 min)

**Goal:** Low-risk fixes, one security improvement, one smoothness improvement.

**Kaizen rule:** One improvement per category per loop. Bias effort toward this run's focus (see [LOOP_FOCUS_ROTATION.md](./LOOP_FOCUS_ROTATION.md)).

### 1.1 Quick wins (10 min)

- **Dead code:** Remove 1–2 safest items from REDUNDANT_DEAD_CODE_REPORT §2 (grep first; no injected params).
- **Constants:** Align any remaining BuildConfig/service constant drift.
- **Doc links:** Verify CRUCIAL_IMPROVEMENTS_TODO is linked from docs/README.md.

### 1.2 Security (10 min, full checklist when Security focus)

**When Security focus:** Run full [SECURITY_LOOP_CHECKLIST.md](./SECURITY_LOOP_CHECKLIST.md) — PII grep, FileProvider, Keystore, secrets, dependency audit. Optionally run `./gradlew dependencyUpdates` or document "last dependency audit" in summary.

**Else,** pick **one** per SECURITY_NOTES:

| Item | Action |
|------|--------|
| No PII in logs | Grep for `Log.*lat\|lon\|coordinates\|tripId`; remove or redact. |
| FileProvider scope | Verify export/share flows use app-private or safe paths only. |
| StandaloneOfflineService key | Add KDoc note: "TODO: Migrate to Keystore + EncryptedSharedPreferences" if not present. |
| SECURITY_CHECKLIST | Ensure no new secrets in logs; one-line verification. |

### 1.3 Smoothness (10 min)

Pick **one**:

| Item | Action |
|------|--------|
| Error handling | Ensure trip save/recovery uses ErrorHandler; add fallback log if missing. |
| Null safety | Add one `?.` or `?:` in a critical path (e.g. TripRepository adapter). |
| Logging | Add one clear log in trip start → save path for production debugging. |
| Animation | Ensure stat card expand/collapse has `animateLayoutChanges` or smooth transition. |

### 1.4 Pulse check

- Run: `.\scripts\automation\pulse_check.ps1 -Note "Phase 1: Quick wins, security, smoothness"`
- Ensure tests pass.

---

## Phase 2: Test Health & Documentation (50–80 min)

**Goal:** Fix or document one test; align docs.

### 2.1 Test health (15 min)

- Per FAILING_OR_IGNORED_TESTS.md — ensure all @Ignore have clear reason.
- Optionally fix one trivial ignored test (e.g. LocationValidationServiceTest) or document why deferred.
- Do NOT spend >10 min on any single test.

### 2.2 Documentation (10 min)

- Update IMPROVEMENT_LOOP_SUMMARY or create new `IMPROVEMENT_LOOP_SUMMARY_<date>.md` stub with "In progress."
- Add one cross-link if CRUCIAL_IMPROVEMENTS or ROADMAP is missing from docs index.

### 2.3 Sandboxing (10 min, when Medium approved)

If user approved Medium tasks, run **one** sandbox action per [SANDBOX_TESTING.md](./SANDBOX_TESTING.md):

| Action | Example |
|--------|---------|
| Document new idea | Add one item to FUTURE_IDEAS.md (from ROADMAP or backlog) |
| Validate sandboxed feature | Create design brief, feature branch stub, or validation checklist for one FUTURE_IDEAS item |
| **Sandbox testing for merge** | Test new features in a branch or build variant before merging into main; validate behavior; merge only when safe |
| Improve sandbox | Update FUTURE_IDEAS index, add cross-link, or document promotion flow |

**Rule:** Sandboxing is additive (docs, branches, briefs). No implementation of Heavy features without user confirmation. No merge into main without sandbox validation.

### 2.5 Unit tests (5 min)

- Run: `.\gradlew.bat :app:testDebugUnitTest --no-daemon`
- Log result.

### 2.6 Pulse check

- Run: `.\scripts\automation\pulse_check.ps1 -Note "Phase 2: Test health, doc links"`

---

## Phase 3: UI Polish + Smoothness (80–105 min)

**Goal:** Sharpen, pop, useful info. Apply design research from Phase 0.4. One smoothness tweak.

**Kaizen rule:** Apply one subtle improvement only. No more than one to avoid drift (user rule).

### 3.1 Strings / accessibility (10 min)

- One string fix: typo, contentDescription, or clearer label per TERMINOLOGY_AND_COPY.md.
- Example: Add contentDescription to a key icon if missing.

### 3.2 Stat card / UI consistency (10 min)

- Per UI_CONSISTENCY.md — verify 6dp elevation, 12dp corner radius, 16dp padding.
- **Apply design research:** Use findings from Phase 0.4 (DESIGN_AND_UX_RESEARCH.md). One subtle improvement: OOR contrast, divider, spacing, or color tweak for professionalism/beautification.
- Examples: Adjust one color for better contrast; add 1–2dp spacing on 8dp grid; improve state flow (loading/empty) per research.

### 3.3 Useful information (5 min)

- One small useful info: e.g. version in Settings (if not done), or period mode hint.
- **Constraint:** No unwarranted UI changes without user permission. Drastic design changes → suggest in summary, don't implement.

### 3.4 Pulse check

- Run: `.\scripts\automation\pulse_check.ps1 -Note "Phase 3: UI polish, smoothness"`

---

## Phase 4: Final Pulse & Summary (105–120 min)

**Goal:** Lint, summary, suggested next steps.

### 4.1 Lint (5 min)

- Run: `.\gradlew.bat :app:lintDebug --no-daemon`
- Fix only **obvious** new issues from this session.

### 4.1b Shipability check (optional, 10 min)

**When Shipability focus:** Run [SHIPABILITY_CHECKLIST.md](./SHIPABILITY_CHECKLIST.md) — 7 signals, 10-min timebox. Run before pre-release loops or when user says "shipability focus."

### 4.2 Final pulse (2 min)

- Run: `.\scripts\automation\pulse_check.ps1 -Note "Phase 4: Final. Lint reviewed."`

### 4.3 Write summary (8 min)

**Output:** `docs/automation/IMPROVEMENT_LOOP_SUMMARY_<date>.md`

**Contents (A-grade format):** See [LOOP_METRICS_TEMPLATE.md](./LOOP_METRICS_TEMPLATE.md).

1. **Phase 0 research note** — One-line: design intent, last loop, this focus
2. **Run metadata** — Date, focus area, variant (Quick/Standard/Full)
3. **PDCA phase summary** — Plan (0), Do (1–3), Check (4), Act (4.3)
4. **Metrics** — Tests (pass/fail), lint (errors/warnings), files changed, checkpoint
5. **What was done** — Table: Phase, Task, Status, Details
6. **Research findings** — Design, security, meta-research; metadata (if Data/Metrics focus)
7. **Files modified** — List with one-line change
8. **Suggested next steps** — 4–6 items for next loop (see template below); actionable (include commands where helpful)
9. **File Organizer: recommended new ideas** — Propose new tasks for Light, Medium, or Heavy. **Add at least 1–2 Heavy ideas per run** when [HEAVY_TIER_IDEAS.md](./HEAVY_TIER_IDEAS.md) is below 50. Heavy ideas require human approval before implementation. See [IMPROVEMENT_LOOP_TEAMS.md](./IMPROVEMENT_LOOP_TEAMS.md).
10. **Next run focus** — Suggested focus for next loop (from File Organizer or metrics)
11. **Quality Grade** — A/B/C with rationale and one improvement for next run

---

## Suggested Next Steps Template

Copy into each summary. Update checkboxes as items are completed.

```markdown
## Suggested Next Steps for Next Loop

- [ ] Resolve pre-existing build issues — `./gradlew clean assembleDebug` if AAPT/dependency errors
- [ ] Dead code cleanup — REDUNDANT_DEAD_CODE_REPORT §2 (CustomCalendarDialog, etc.)
- [ ] LocationValidationServiceTest — Fix ignored test or move to instrumented suite
- [ ] Gradle 9 readiness — Run `--warning-mode all`, document in GRADLE_9_MIGRATION_NOTES
- [ ] Security: StandaloneOfflineService — Migrate encryption key to Keystore (larger task)
- [ ] Security: PII audit — Re-grep logs for coordinates, trip IDs; redact if found
- [ ] Design research — Next topic: color schemes / state flows / contrast / fleet app patterns (rotate)
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

---

## Routine Cadence

- **Sprint:** Run every 1–2 weeks.
- **Pre-release:** Run + SECURITY_CHECKLIST before ship.
- **After major changes:** Run to catch regressions and polish.

---

*Routine created for repeatable Improvement Loops. Research first, allowlist for autonomy, then execute.*
