# Improvement Loop — Task Tiering

**Purpose:** Categorize loop tasks into Light, Medium, and Heavy. **Light** and **Medium** may run autonomously. **Heavy** always requires human-in-the-loop approval before implementation.

**Primary goal:** Get ready to ship to Google Play Store for Android. Stay on track; do not drift from the original UI layout.

**Kaizen rule:** One improvement per category per loop (Phase 1–3). Small incremental changes compound. See [LOOP_FOCUS_ROTATION.md](./LOOP_FOCUS_ROTATION.md).

**Established:** 2025-03-11  
**References:** [IMPROVEMENT_LOOP_ROUTINE.md](./IMPROVEMENT_LOOP_ROUTINE.md), [IMPROVEMENT_LOOP_TEAMS.md](./IMPROVEMENT_LOOP_TEAMS.md), [LOOP_FOCUS_ROTATION.md](./LOOP_FOCUS_ROTATION.md), [CRUCIAL_IMPROVEMENTS_TODO.md](../CRUCIAL_IMPROVEMENTS_TODO.md), [FUTURE_IDEAS.md](../product/FUTURE_IDEAS.md), [SANDBOX_TESTING.md](./SANDBOX_TESTING.md)

---

## When User Says "GO"

0. **Pre-loop checkpoint (Medium)** — Save a copy before the loop: `git add -A && git commit -m "Pre-improvement-loop checkpoint"` or create a tag. If something breaks or you want to go back, say **"revert"** to restore from this checkpoint.
1. **Phase 0.1 Research** — Run first. Read all docs; identify tasks from CRUCIAL, REDUNDANT_DEAD_CODE, FAILING_OR_IGNORED_TESTS, suggested next steps.
2. **Classify tasks** — Assign each identified task to Light, Medium, or Heavy.
3. **If any Medium or Heavy tasks exist:**
   - **Prompt:** "Hold up. Would you like me to implement these medium or heavy tasks?"
   - **List** the Medium and Heavy tasks for the user.
   - **Wait for user response** before proceeding.
4. **User response options:**
   - "Implement light and medium tasks only"
   - "Implement all" (light + medium + heavy)
   - "Light only" (skip medium and heavy)
   - "Light and heavy" (skip medium)
   - Or any custom combination (e.g. "Implement medium task X but not Y")
5. **Execute** — Run only the tasks in the user-approved scope.

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
| **Sandbox testing for merge** | Test new features in sandbox (branch, build variant) before merging into main project. Validate behavior; merge only when safe. |
| **Advanced beautification & organizing research** | Research color schemes, typography, spacing, elevation, professional UI patterns; document organizing best practices; apply one subtle improvement per loop. |
| **Code structure review** | Assess one module for feature-based vs layer-based organization; document finding in summary (no change unless user approves). See [REDUNDANT_DEAD_CODE_REPORT.md](../REDUNDANT_DEAD_CODE_REPORT.md) refactor priority quadrant. |
| **Metadata collection/display** | Add one opt-in metadata collection point (e.g., trip-end rating) or one metadata display (e.g., "Trips this week" in Settings). See [USER_METADATA_USAGE_GUIDE.md](./USER_METADATA_USAGE_GUIDE.md). Store on-device; no PII. |
| **Pre-loop checkpoint** | Save a copy (git commit/tag) before the Improvement Loop so you can **revert** if something breaks. Say **"revert"** to restore. |

**Rule:** Small, localized change. Low risk. Supports shipping. **Do not drift from original UI layout.**

---

### Heavy (user approval) — **New features / future ideas only**

| Task type | Examples |
|-----------|----------|
| **Featured features** | New features from [FUTURE_IDEAS.md](../product/FUTURE_IDEAS.md) (multi-user sharing, driver ranking, route deviation map, sandboxed virtual fleet, optional email signup for updates, etc.) |
| **ROADMAP features** | Auto drive, Reports screen, address input — **only after sandbox validation** |
| **Architecture** | Gradle 9 migration, schema changes, new persistence paths |
| **Refactors** | Statistics monthly-only; repository interface changes |

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

## Visual Approval Clause (Heavy Implementation Gate)

**Features that are 100% finished and 100% sandboxed in the Heavy tier** must pass a visual approval gate before implementation during the Improvement Loop:

1. **Generate a simple visual image** — Show where the feature is being implemented and what it looks like (e.g. mockup, wireframe, or screenshot of placement).
2. **Present to user** — Display the image and a brief description of the feature and its placement.
3. **Wait for explicit approval** — Do **not** implement until the user says: **"approve 100% implement"** (exact phrase).
4. **Implement only after approval** — If the user says "approve 100% implement," proceed with implementation. Otherwise, do not implement.

**Rule:** No Heavy feature implementation without this visual approval step and the exact phrase "approve 100% implement."

---

## Quick Reference

| Tier | Focus | Autonomous? | Human approval |
|------|-------|-------------|-----------------|
| **Light** | Verification, docs, strings, security research, future light-task research | Yes | Not required |
| **Medium** | Ship readiness, backend, sandboxing, guaranteed frontend | Yes | Not required |
| **Heavy** | New features (sandboxed first) | No | **Required** — always human-in-the-loop |

**Rule:** File Organizer (back team) recommends new ideas to Light/Medium/Heavy. Heavy ideas must be approved by a human before implementation. See [IMPROVEMENT_LOOP_TEAMS.md](./IMPROVEMENT_LOOP_TEAMS.md).

---

## Prompt Template (when Heavy tasks exist)

```
Hold up. Would you like me to implement these heavy tasks?

**Heavy tasks:**
- [Task 1]
- [Task 2]

**Visual approval required:** Before implementing any Heavy task, I will generate a simple image showing where the feature goes and what it looks like. You must say "approve 100% implement" before I proceed.

**Options:**
- "Implement all" — I'll show the visual(s), then implement after you say "approve 100% implement"
- "Light and medium only" — I'll skip heavy (light and medium run autonomously)
- Or specify: "Implement heavy task X but not Y"
```

---

---

## Revert

**When you say "revert"** — Restore from the pre-loop checkpoint (commit or tag created before the Improvement Loop). Use `git reset --hard <checkpoint>` or `git checkout <tag>`. Do not revert unless the user explicitly says "revert."

---

*Integrates with IMPROVEMENT_LOOP_ROUTINE.md. Update when new task types are added.*
