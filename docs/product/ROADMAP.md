# OutOfRouteBuddy — Product Roadmap

**Owner:** Project Design / Creative Manager  
**Created:** 2025-02-19  
**References:** `docs/agents/WORKER_TODOS_AND_IDEAS.md`, `docs/agents/COMPREHENSIVE_AGENT_TODOS.md`, `docs/CRUCIAL_IMPROVEMENTS_TODO.md`

---

## High-level themes

1. **Reduce friction for drivers** — Fewer taps to start/end trips; optional auto-detection when driving.
2. **Trustworthy tracking** — Reliable trip and out-of-route data; statistics that match how the user thinks (e.g. monthly only).
3. **Clear feedback and history** — Trip history, reports, and a single source of truth so the user can review and share.
4. **Stable foundation** — Emulator as visual spec, build/DevOps/docs aligned, tests and security in place before scaling features.

---

## What's next (from worker todos and ideas)

- **Auto drive detected** — Detect when the driver is likely on the road (movement/speed or similar); show a clear state and one-tap start. See `docs/product/FEATURE_BRIEF_auto_drive.md`.
- **Reports screen** — Dedicated view for trip/out-of-route reports (period, export, or share). See `docs/product/FEATURE_BRIEF_reports.md`.
- **History improvements** — Navigate from history to trip details; clearer list and filters. (CRUCIAL #4 implemented.)
- **Emulator polish** — Settings dialog (Mode, Templates, Help), End Trip confirmation (End / Clear / Continue), period picker for Statistics, long-press for edit. Emulator then serves as the visual spec for the real app.
- **Emulator Phase A/B/C complete** — Undo/Redo, clear Save vs Sync, 1:1 spec (spacing, string-key parity), Load from project. See `docs/agents/EMULATOR_PHASE_ABC_100_TODOS.md` and `phone-emulator/1TO1_SPEC.md`.
- **Open line** — Keep email ask/reply flow; optional weekly one-liner and "reply with a number" for priorities.
- **Backlog ideas** — Driver check-in, Trip summary card, Haptic on start/end, Smart bounce default, Nightly digest, Screenshot on trip end, App PIN to end trip, Changelog for you, etc. (see WORKER_TODOS_AND_IDEAS).

---

## Prioritization: next 3 features

After **emulator polish** and **IMAP/open-line** work are in a good place, the recommended order for the next three is:

| Priority | Feature              | Rationale |
|----------|----------------------|-----------|
| **1**    | **Auto drive detected** | Highest user value: less friction for drivers who forget to tap Start; one brief exists; Back-end, Front-end, UI/UX, QA, Security already have tasks. |
| **2**    | **Reports screen**   | Gives a clear "where did I drive / how much OOR" view; builds on statistics (monthly) and history. |
| **3**    | **History improvements** | Trip history → trip details navigation (CRUCIAL #4) and better list/filters; unblocks "view past trip" workflows. |

*Re-prioritize with the user if needed (e.g. "reply with a number": 1=Auto drive, 2=Reports, 3=History).*

---

## Workdays and batching (sprint note)

- **Primary work block:** **Sunday, 3–4 hours** (see `docs/agents/team-parameters.md`).
- **Biweekly sprint:** Plan work in **two-Sunday chunks**. Batch design briefs, UI/UX specs, and implementation tasks so each sprint has a clear goal (e.g. "Sprint 1: Auto drive brief + Back-end trigger logic; Sprint 2: Auto drive UI + QA cases"). This keeps scope realistic and gives the team a single place to look for "what we're doing next."
- **Current sprint focus:** App improvement & polish per 25-point list ([docs/agents/APP_IMPROVEMENT_25_POINT_BRAINSTORM.md](../agents/APP_IMPROVEMENT_25_POINT_BRAINSTORM.md)); then next 3 = Auto drive, Reports, History.

---

*When priorities change, update this file and optionally notify via Human-in-the-Loop.*

---

## Design process (Board-adopted)

- **Prioritize from one source.** Keep this ROADMAP and `docs/CRUCIAL_IMPROVEMENTS_TODO.md` (and any 25-point list) in sync; when the user picks "next," update the roadmap and hand off in order (Design → UI/UX → Eng → QA).
- **Briefs reference SSOT.** Every feature brief in this folder should state that persistence, recovery, and calendar follow `docs/agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md` so UI/UX and Eng don't invent alternate flows.
- **User decisions via Human-in-the-Loop.** When we have "Option A vs B vs defer," hand off to the Human-in-the-Loop Manager with a one-paragraph summary so the user gets a clear email, not jargon.
