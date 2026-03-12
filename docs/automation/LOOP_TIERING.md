# Improvement Loop — Task Tiering

**Purpose:** Categorize loop tasks into Light, Medium, and Heavy. **Light** and **Medium** may run autonomously. **Heavy** always requires human-in-the-loop approval before implementation.

**Primary goal:** Get ready to ship to Google Play Store for Android. Stay on track; do not drift from the original UI layout.

**Kaizen rule:** One improvement per category per loop (Phase 1–3). Small incremental changes compound. See [LOOP_FOCUS_ROTATION.md](./LOOP_FOCUS_ROTATION.md).

**Established:** 2025-03-11  
**References:** [IMPROVEMENT_LOOP_ROUTINE.md](./IMPROVEMENT_LOOP_ROUTINE.md), [IMPROVEMENT_LOOP_COMMON_SENSE.md](./IMPROVEMENT_LOOP_COMMON_SENSE.md), [IMPROVEMENT_LOOP_TEAMS.md](./IMPROVEMENT_LOOP_TEAMS.md), [LOOP_FOCUS_ROTATION.md](./LOOP_FOCUS_ROTATION.md), [CRUCIAL_IMPROVEMENTS_TODO.md](../CRUCIAL_IMPROVEMENTS_TODO.md), [FUTURE_IDEAS.md](../product/FUTURE_IDEAS.md), [SANDBOX_TESTING.md](./SANDBOX_TESTING.md), [BRAINSTORM_AND_TASKS.md](./BRAINSTORM_AND_TASKS.md)

---

## When User Says "GO"

0. **Pre-loop checkpoint (Medium)** — Save a copy before the loop: `git add -A && git commit -m "Pre-improvement-loop checkpoint"` or create a tag. If something breaks or you want to go back, say **"revert"** to restore from this checkpoint.
1. **Phase 0.1 Research** — Run first. Read all docs; identify tasks from CRUCIAL, REDUNDANT_DEAD_CODE, FAILING_OR_IGNORED_TESTS, suggested next steps.
2. **Classify tasks** — Assign each identified task to Light, Medium, or Heavy.
3. **If any Heavy tasks exist:**
   - **Never run the entire Heavy tier by itself.** Heavy features are implemented **one by one**.
   - **Per-feature gate:** Before implementing each Heavy feature, ask: "Are you ready to implement this new feature? [Feature name]"
   - **Wait for user response** before implementing that feature. Then move to the next (if any); ask again.
4. **Full autonomous mode:** Light and Medium run **without stopping**. No prompts. Execute all Light and Medium tasks. **During execution:** Check TODOs (CRUCIAL, suggested next steps, TASKS_INDEX or COMPREHENSIVE_AGENT_TODOS); tick off completed items; add new TODOs when you discover work to track. Heavy tasks → document in summary for next run; do not implement without user approval.
5. **User response options (when Heavy exists):**
   - "Implement all" (light + medium + heavy)
   - "Light and medium only" (skip heavy; run autonomously)
   - "Light only" (skip medium and heavy)
   - Or any custom combination (e.g. "Implement heavy task X but not Y")
6. **Execute** — Run only the tasks in scope. In autonomous mode: Light + Medium run; Heavy deferred.

---

## Tier Definitions

### Light (automatic)

| Task type | Examples |
|-----------|----------|
| **Verification only** | Doc links, PII grep, CRUCIAL_IMPROVEMENTS linked from README |
| **Documentation** | Add cross-link, document test in FAILING_OR_IGNORED_TESTS, sandbox index update |
| **Strings / accessibility** | Add contentDescription, add string resource, wire existing string to UI |
| **Research & logging** | Phase 0 research, design research, summary write |
| **Research: safety / security** | Research prompt-injection protection, secure coding practices, AI-assisted development security |
| **Infrastructure** | Pulse check, unit test run, lint run |
| **Research: future light tasks** | At end of loop: research and document new Light task ideas for next runs |
| **Metadata research** | Research metadata methods; document one new suggestion in [USER_METADATA_USAGE_GUIDE.md](./USER_METADATA_USAGE_GUIDE.md) (when Data/Metrics focus) |
| **Preference capture** | When user clarifies a preference this run, add to [USER_PREFERENCES_AND_DESIGN_INTENT.md](./USER_PREFERENCES_AND_DESIGN_INTENT.md) § Subtle Preferences |
| **Brainstorming & idea generation** | Based on current context, user feedback, and research: document possible ideas, suggestions, and future improvements. Add to [BRAINSTORM_AND_TASKS.md](./BRAINSTORM_AND_TASKS.md) or suggested next steps. |
| **Populate task list** | Classify brainstormed ideas into Light/Medium/Heavy; add to CRUCIAL, LOOP_TIERING examples, or FUTURE_IDEAS (sandboxed). Update as we learn. |
| **Debugging** | Document debug steps, add debug logs (additive), verify debug paths, document in DEBUGGING.md. No logic changes. |

**Rule:** No code removal, no refactor, no new logic. Additive only (strings, docs, comments).

---

### Medium (user approval) — **Get ready to ship for Google Play**

| Task type | Examples |
|-----------|----------|
| **Ship readiness** | Version bump, changelog, release build smoke, lint fixes, test fixes |
| **Backend wiring** | Persistence fixes, repository wiring, sync logic, error handling |
| **Guaranteed frontend improvements** | Accessibility, contentDescription, subtle contrast/spacing (no layout drift) |
| **Dead code removal** | Remove 1–2 safest items from REDUNDANT_DEAD_CODE_REPORT §2 |
| **Test fix** | Fix one trivial ignored test |
| **KDoc / comment** | Add KDoc note (e.g. StandaloneOfflineService Keystore migration) |
| **Logging** | Add one clear log in trip start → save path |
| **Ship instructions** | Generate or update `Desktop\OUTOFROUTEBUDDY_SHIP_INSTRUCTIONS.txt` with final steps |
| **Sandboxing** | Add Heavy ideas to FUTURE_IDEAS (100% sandboxed); improve index/cross-links; validate sandboxed features. Runs when you run Medium tier. See [SANDBOX_TESTING.md](./SANDBOX_TESTING.md) |
| **Sandbox improvement (every loop)** | **Improve on sandboxed ideas each run.** Add design brief, validation checklist, or advance completion % for 1–2 ideas in [HEAVY_TIER_IDEAS.md](./HEAVY_TIER_IDEAS.md). Merging should not be taken lightly; use [true completion %](./SANDBOX_COMPLETION_PERCENTAGE.md). |
| **Sandbox testing for merge** | Test new features in sandbox (branch, build variant) before merging into main project. Validate behavior; merge only when safe. |
| **Advanced beautification & organizing research** | Research color schemes, typography, spacing, elevation, professional UI patterns; document organizing best practices; apply one subtle improvement per loop. |
| **Code structure review** | Assess one module for feature-based vs layer-based organization; document finding in summary (no change unless user approves). See [REDUNDANT_DEAD_CODE_REPORT.md](../REDUNDANT_DEAD_CODE_REPORT.md) refactor priority quadrant. |
| **Medium refactors** | Small, localized refactors: extract one function, rename one class, move one file. Low risk. One module or fewer. |
| **Metadata collection/display** | Add one opt-in metadata collection point (e.g., trip-end rating) or one metadata display (e.g., "Trips this week" in Settings). See [USER_METADATA_USAGE_GUIDE.md](./USER_METADATA_USAGE_GUIDE.md). Store on-device; no PII. |
| **Pre-loop checkpoint** | Save a copy (git commit/tag) before the Improvement Loop so you can **revert** if something breaks. Say **"revert"** to restore. |
| **Research improvements & populate tasks** | Research more ways to improve (from user feedback, summaries, industry patterns). Add ideas to [BRAINSTORM_AND_TASKS.md](./BRAINSTORM_AND_TASKS.md); classify and populate CRUCIAL, LOOP_TIERING, or FUTURE_IDEAS. Update task lists as we learn. |
| **Idea classification & placement** | Take brainstormed ideas; divide into Light (docs, verification), Medium (small code changes, sandboxing), Heavy (new features → FUTURE_IDEAS). Add 1–2 new items per loop to keep backlog fresh. |

**Rule:** Small, localized change. Low risk. Supports shipping. **Do not drift from original UI layout.**

---

### Heavy (user approval) — **New features / future ideas only**

| Task type | Examples |
|-----------|----------|
| **Featured features** | New features from [FUTURE_IDEAS.md](../product/FUTURE_IDEAS.md) (multi-user sharing, driver ranking, route deviation map, sandboxed virtual fleet, optional email signup for updates, etc.) |
| **UI polish (icon beautification)** | Trash can / delete icons — make them more professional; **select the most aesthetic icon** with care (e.g. Material Design 3 trash variant). Heavy: requires visual approval. See FUTURE_IDEAS § 5.1. |
| **Navigation / app chrome** | Scrolling top toolbar/taskbar; hamburger menu to the left of the "Out of route" title. See FUTURE_IDEAS § 6.1, § 6.2. |
| **Branding** | Possible app name change — ideas to be thought through later. See FUTURE_IDEAS § 7.1. |
| **ROADMAP features** | Auto drive, Reports screen, address input — **only after sandbox validation** |
| **Architecture** | Gradle 9 migration, schema changes, new persistence paths |
| **Large refactors** | Statistics monthly-only; repository interface changes; multi-file refactors; cross-module changes |

**Rule:** New features only. **Must be sandboxed and confirmed working before promotion to Heavy.** See [Sandboxing for new features](#sandboxing-for-new-features) below.

---

## Sandboxing for New Features

New feature ideas **must not** go directly to Heavy. They must be:

1. **Documented** in [FUTURE_IDEAS.md](../product/FUTURE_IDEAS.md) (sandboxed).
2. **Validated** — e.g. feature branch, build variant, or design brief; user confirms behavior.
3. **Confirmed** — User explicitly approves: "This is ready to implement."
4. **Then** promoted to Heavy (or ROADMAP) for implementation.

**Goal:** 100% confidence that a feature is ready before implementation. No surprises.

---

## Question Lock (Heavy Implementation Gate)

**When the user says "implement X feature" (or any Heavy feature name), do not proceed to implementation.** Instead, respond with:

> **Would you like to see a generated image or layout or simulate a merge?**

**Options:**

| User choice | Response |
|-------------|----------|
| **Generated image** | Create a simple image or mockup showing where the feature goes and what it looks like. |
| **Layout** | Show a layout or wireframe description (e.g. XML, placement, hierarchy). |
| **Simulate a merge** | Show what would change (diff, file list, affected modules) without applying. |

**Rule:** Do not implement until the user answers this question and then proceeds to "approve 100% implement." Even if the user says "implement X," the agent must ask this question first.

---

## Visual Approval Clause (Heavy Implementation Gate)

**Features that are 100% finished and 100% sandboxed in the Heavy tier** must pass a visual approval gate before implementation during the Improvement Loop:

1. **Question lock first** — When user says "implement X" or "implement [feature name]," ask: "Would you like to see a generated image or layout or simulate a merge?" See [Question Lock](#question-lock-heavy-implementation-gate) above.
2. **Generate a simple visual image** (or layout or merge simulation) — Show where the feature is being implemented and what it looks like (e.g. mockup, wireframe, or screenshot of placement).
3. **Present to user** — Display the image and a brief description of the feature and its placement.
4. **Wait for explicit approval** — Do **not** implement until the user says: **"approve 100% implement"** (exact phrase).
5. **Implement only after approval** — If the user says "approve 100% implement," proceed with implementation. Otherwise, do not implement.

**Rule:** No Heavy feature implementation without this question lock and visual approval step and the exact phrase "approve 100% implement."

---

## Quick Reference

| Tier | Focus | Autonomous? | Human approval |
|------|-------|-------------|-----------------|
| **Light** | Verification, docs, strings, brainstorming, idea generation, populate tasks | Yes | Not required |
| **Medium** | Ship readiness, backend, sandboxing, research improvements, populate & classify tasks | Yes | Not required |
| **Heavy** | New features (sandboxed first) | No | **Required** — always human-in-the-loop |

**Rule:** File Organizer (back team) recommends new ideas to Light/Medium/Heavy. Heavy ideas must be approved by a human before implementation. See [IMPROVEMENT_LOOP_TEAMS.md](./IMPROVEMENT_LOOP_TEAMS.md). Light and Medium tiers research improvements and populate [BRAINSTORM_AND_TASKS.md](./BRAINSTORM_AND_TASKS.md); promote 1–2 ideas per loop to CRUCIAL or FUTURE_IDEAS. **Add at least 1–2 Heavy ideas per run** when [HEAVY_TIER_IDEAS.md](./HEAVY_TIER_IDEAS.md) is below 50.

---

## Heavy Tier One-by-One Clause

**Never run the entire Heavy tier by itself.** Heavy features are implemented **one at a time**.

Before implementing **each** Heavy feature:
1. Ask: **"Are you ready to implement this new feature? [Feature name]"**
2. Wait for user response.
3. If yes: **ask the question lock first:** "Would you like to see a generated image or layout or simulate a merge?" See [Question Lock](#question-lock-heavy-implementation-gate).
4. Do not implement until user answers this question and then says **"approve 100% implement"** for that feature only.
5. If no: skip or defer; move to next feature (if any) and ask again.

---

## Prompt Template (when Heavy tasks exist)

```
Hold up. Would you like me to implement any of these heavy tasks?

**Heavy tasks (one by one):** List **favorites first** per [HEAVY_IDEAS_FAVORITES.md](./HEAVY_IDEAS_FAVORITES.md); keep list lightly populated.
- [Task 1]
- [Task 2]

**Per-feature gate:** I will ask "Are you ready to implement this new feature?" before each one. I will never implement the entire Heavy tier at once.

**Question lock:** When you say "implement X" or "implement [feature name]," I will first ask: "Would you like to see a generated image or layout or simulate a merge?" I will not proceed to implementation until you answer this and then say "approve 100% implement."

**Visual approval required:** Before implementing any Heavy task, I will generate a simple image (or layout or merge simulation) showing where the feature goes and what it looks like. You must say "approve 100% implement" before I proceed.

**Options:**
- "Implement [feature X] only" — I'll ask for that one, then ask the question lock
- "Light and medium only" — I'll skip heavy (light and medium run autonomously)
- Or specify: "Implement heavy task X but not Y"
```

---

---

## Revert

**When you say "revert"** — Restore from the pre-loop checkpoint (commit or tag created before the Improvement Loop). Use `git reset --hard <checkpoint>` or `git reset --hard <tag>`. Do not revert unless the user explicitly says "revert."

**If no checkpoint exists:** Say "No checkpoint found. Create one next run with `git add -A && git commit -m "Pre-improvement-loop checkpoint"` or `git tag improvement-loop-pre-$(date +%Y%m%d-%H%M)`."

---

*Integrates with IMPROVEMENT_LOOP_ROUTINE.md. Update when new task types are added.*
