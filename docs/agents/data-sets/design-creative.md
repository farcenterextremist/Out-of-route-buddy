# Project Design / Creative Manager — data set

## Consumes (reads / references)

### Primary
- **Worker todos and ideas:** `docs/agents/WORKER_TODOS_AND_IDEAS.md` — current todo lists and each role’s proposed ideas; use to align roadmap and prioritization.
- **Team parameters:** `docs/agents/team-parameters.md` — workdays (e.g. Sunday 3–4 hr), user preferences, secret word; use to scope what can be done and when to ask the user.
- **Emulator / feature context:** `phone-emulator/EMULATOR_PERFECTION_PLAN.md` — list of UI/feature gaps and priorities; use to decide what “done” looks like and what to prioritize next.
- **Existing plans:** Any `*_PLAN.md`, `ROADMAP*.md`, or `FEATURE_BRIEF_*.md` in `docs/` or `docs/product/` — avoid contradicting; extend or supersede with a note.

### Secondary
- **Known truths & SSOT:** `docs/agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md` — high-level persistence/recovery/calendar so briefs don’t contradict SSOT.
- **App surface area:** High-level only — e.g. trip input, GPS tracking, statistics/periods, history, settings. Use to keep briefs grounded; do not read code.
- **Coordinator plan:** `docs/agents/DATA_SETS_AND_DELEGATION_PLAN.md` — who does what; use when suggesting handoffs (e.g. “then UI/UX, then Front-end”).

## Produces (writes / owns)

### Primary
- **Roadmap:** `docs/product/ROADMAP.md` — prioritization, themes, and “what’s next” in plain language. Update when the user or team agrees on priorities.
- **Feature briefs:** `docs/product/FEATURE_BRIEF_<name>.md` — one brief per major feature (e.g. `FEATURE_BRIEF_AUTO_DRIVE.md`). Include: problem, user value, high-level behavior, out of scope, and “hand off to UI/UX + Back-end for spec/impl.”
- **Prioritization notes:** Short notes the coordinator can use (e.g. “Next 3: Auto drive, emulator polish, Reports screen”) in `docs/product/` or in the roadmap.

### Secondary
- **Clarifications for Human-in-the-Loop:** When the user must make a product decision, write a one-paragraph summary or options so the Human-in-the-Loop Manager can email the user clearly (e.g. “Option A vs B vs defer”).

## Delegation (when to assign to this role)

- “What should we build next?” / “Prioritize these features.”
- “Write a one-page brief for [feature]” (e.g. Auto drive).
- “Align the roadmap with workdays” or “Suggest what we can do in the next 3–4 hr block.”
- “The user asked for [X]; how does it fit the product?” — Design frames the answer; Human-in-the-Loop can send it.

## Out of scope (do not assign here)

- Pixel-level UI/UX or wireframes → **UI/UX Specialist**.
- Code, build, tests → **Front-end / Back-end / DevOps / QA**.
- Security or compliance wording → **Security Specialist**.
