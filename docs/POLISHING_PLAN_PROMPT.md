# Prompt: Build a Polishing Plan for OutOfRouteBuddy

**Use this prompt** with an agent or as a brief to produce a **step-by-step polishing plan** (tasks, phases, file order, dependencies). Do not implement yet—only output the plan.

---

## Context

OutOfRouteBuddy is an Android app (Kotlin, MVVM, Hilt, Room) for tracking out-of-route miles. The codebase has been audited and improved in weak areas. This plan is about **polish**: making good things better—readability, consistency, maintainability, and small refinements—**without** changing behavior or adding new features unless explicitly called out below.

**Polishing** = refinement of existing code and UX (naming, comments, structure, logging clarity, UI consistency, docs, dead-code removal).  
**Not polishing** = fixing broken tests, fixing security holes, or implementing missing features (use `docs/WEAKEST_AREAS_IMPROVEMENT_PLAN_PROMPT.md` for those).

Reference docs (use as evidence, not as a full list):

- `docs/qa/COVERAGE_SCORE_AND_CRITICAL_AREAS.md`
- `docs/QUALITY_AND_ROBUSTNESS_PLAN.md`
- `docs/PROJECT_AUDIT_2025_02_27.md`
- `docs/product/FEATURE_BRIEF_stat_cards_calendar_history.md` (for existing UI/UX consistency)
- `docs/CRUCIAL_IMPROVEMENTS_TODO.md` (for “nice to have” / polish items only)

---

## Polishing Areas (Evidence-Based)

### 1. Code readability and structure

- **Naming and comments:** Inconsistent or vague names (e.g. `D1`, `T4` in docs vs. code); classes or methods that would benefit from a one-line KDoc or clearer names. No behavior change—only renames and comments.
- **Dead or redundant code:** Unused methods, unreachable branches, duplicate logic that could be shared. Remove or consolidate without changing public behavior.
- **File/module organization:** Long files that could be split for readability; packages that could be clearer. Prefer small, reviewable moves.
- **Formatting and style:** Consistent use of Kotlin style (e.g. trailing commas, line length, `when` formatting). Can be automated (e.g. ktlint/Detekt) or manual where tooling doesn’t cover.

**Ask in the plan:** Ordered tasks for renames, KDoc, dead-code removal, and optional style/formatting pass, with clear “definition of done” (e.g. “no new lint/format issues in touched files”).

---

### 2. Logging and diagnostics (polish only)

- **Log levels and messages:** Ensure `Log.d`/`Log.v` for debug, `Log.w`/`Log.e` for recoverable/real errors. Messages should be actionable (e.g. include key IDs or state). No new logs in hot paths unless they add clear value.
- **Structured logging:** If the app uses tags or conventions, align new or touched logs to that. Avoid log sprawl (e.g. one log per frame or per tiny callback).
- **Sensitive data:** Ensure no PII or secrets in log messages (align with `docs/security/` or CRUCIAL §7 if present). Polish = review and fix only; no new infrastructure unless already planned.

**Ask in the plan:** A small “logging polish” checklist (levels, message quality, PII check) and which files/modules to touch first.

---

### 3. UI/UX consistency (no unwarranted layout changes)

- **Copy and strings:** Typos, unclear labels, inconsistent terminology (e.g. “Out of route” vs “OOR” vs “out-of-route”). No layout/theme changes unless required for accessibility or consistency with existing patterns.
- **Accessibility:** Labels, content descriptions, and focus order where missing or inconsistent. Only what’s needed to align with existing screens.
- **Visual consistency:** Colors, spacing, or icons that diverge from the rest of the app (e.g. stat cards vs. other cards). Document what “consistent” means (e.g. reference one screen as the source of truth).

**Ask in the plan:** Tasks limited to strings, accessibility fixes, and documented consistency rules—**no unwarranted UI/layout changes** without explicit approval.

---

### 4. Documentation and onboarding

- **README and high-level docs:** README up to date (build, run, test commands); any “getting started” or architecture overview accurate.
- **Code-level docs:** Critical public APIs (repositories, key ViewModels, persistence) have minimal KDoc so future contributors know intent and threading/scope.
- **Changelog and release notes:** CHANGELOG or release notes updated for recent work so polish plan outputs can be reflected there.

**Ask in the plan:** Ordered documentation tasks (README, architecture snippet, KDoc targets, CHANGELOG) with owners and definition of done.

---

### 5. Test and build polish (non-blocking)

- **Test names and structure:** Test method names that clearly describe scenario and expected outcome; remove or consolidate redundant tests; improve readability of test data setup.
- **Build and CI:** Comments in Gradle/CI for non-obvious choices; clear separation of “quick check” vs “full verification” so new contributors know what to run. No change to actual pass/fail criteria unless already agreed.
- **Flaky or noisy tests:** Document known flaky tests and add a one-line “why” or skip reason; avoid introducing new flakiness.

**Ask in the plan:** Test/build polish tasks that do **not** change coverage gates or fix failing tests (that stays in the improvement plan).

---

## What to Produce

1. **Phased polishing plan** with:
   - **Phases** (e.g. Phase 1: Code readability and dead code; Phase 2: Logging and diagnostics; Phase 3: UI/UX consistency; Phase 4: Documentation; Phase 5: Test and build polish).
2. **Per phase:** Concrete **tasks** with:
   - **Owner** (e.g. Back-end, Front-end, QA, DevOps, Docs).
   - **Files or modules** to touch.
   - **Dependencies** (what must be done before).
   - **Definition of done** (e.g. “KDoc added for Repository X” or “Strings in module Y reviewed and consistent”).
3. **References:** Point each task to the relevant doc (QUALITY_AND_ROBUSTNESS_PLAN, PROJECT_AUDIT, COVERAGE_SCORE_AND_CRITICAL_AREAS, etc.) where applicable.
4. **Risks and trade-offs:** Note any “polish vs. churn” (e.g. large rename that touches many call sites) and recommend whether to do it now or defer.

---

## Constraints

- **No unwarranted UI/layout changes**—only those needed for consistency, accessibility, or strings. Get explicit approval before changing screens or themes.
- **No new features**—polish only. If something is “feature-like,” move it to the product backlog or improvement plan.
- **No behavior change** unless it’s a documented bug fix already in scope (e.g. typo in a calculation). Renames and comments must not change semantics.
- Prefer **small, reviewable steps**; avoid large refactors in a single phase.
- If the plan suggests renames or API doc changes, call out **call-site impact** and suggest migration order (e.g. internal first, then public).

---

## Optional: One-Page Summary

After the full plan, add a **one-page summary** table:

| Phase | Goal | Key tasks | Owner(s) | Depends on |
|-------|------|-----------|----------|-------------|
| 1 | … | … | … | — |
| 2 | … | … | … | Phase 1 |
| … | … | … | … | … |

Use this prompt as-is or adapt the “Polishing Areas” and “What to Produce” sections to match your priorities (e.g. docs first vs. code readability first).
