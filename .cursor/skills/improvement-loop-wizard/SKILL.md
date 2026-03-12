---
name: improvement-loop-wizard
description: >-
  Guides the agent through the OutOfRouteBuddy Improvement Loop: research,
  tiering (Light/Medium/Heavy), autonomous execution, and A-grade summary. Use
  when the user says GO, "run improvement loop", "start improvement loop", or
  asks for the improvement loop wizard.
---

# Improvement Loop Wizard

## Trigger

When user says **GO**, **"run improvement loop"**, **"start improvement loop"**, **"improvement loop wizard"**, or **"start master loop"** — guide through the loop step by step.

**If "start master loop":** You are the Loop Master. First read `docs/automation/LOOP_MASTER_ROLE.md` and run Step 0.M (research all other loops, compare/analyze/scrutinize, update universal files); then run the full Improvement Loop (checkpoint → phases 0–4 → summary → ledger).

---

## Wizard Flow

### Step 0 — Before Any Changes

| Action | Doc |
|--------|-----|
| Read user preferences & design intent | `docs/automation/USER_PREFERENCES_AND_DESIGN_INTENT.md` |
| Read common sense | `docs/automation/IMPROVEMENT_LOOP_COMMON_SENSE.md` |
| Read reasoning | `docs/automation/IMPROVEMENT_LOOP_REASONING.md` |
| Create pre-loop checkpoint | `git add -A && git commit -m "Pre-improvement-loop checkpoint"` or tag |

**Rule:** Checkpoint first. No changes before checkpoint.

---

### Step 1 — Research (Phase 0)

Read in order:

| Doc | Purpose |
|-----|---------|
| `docs/automation/IMPROVEMENT_LOOP_SUMMARY_<latest>.md` | Last run; suggested next steps |
| `docs/CRUCIAL_IMPROVEMENTS_TODO.md` | Prioritized backlog |
| `docs/REDUNDANT_DEAD_CODE_REPORT.md` | Safe dead code |
| `docs/qa/FAILING_OR_IGNORED_TESTS.md` | Test health |
| `docs/security/SECURITY_NOTES.md` | Security checklist |
| `docs/automation/LOOP_TIERING.md` | Light/Medium/Heavy definitions |
| `docs/automation/HEAVY_IDEAS_FAVORITES.md` | User favorites for Heavy; surface favorites first; keep list light |

---

### Step 2 — Tiering

Classify each task as **Light**, **Medium**, or **Heavy** per `docs/automation/LOOP_TIERING.md`:

| Tier | Autonomous? | Rule |
|------|-------------|------|
| Light | Yes | Additive only; no code removal |
| Medium | Yes | Small, localized; ship readiness |
| Heavy | No | User approval; one-by-one; question lock |

**If Heavy exists:** Ask user before implementing. Never run entire Heavy tier at once.

---

### Step 3 — Self-Improvement (Phase 0.5)

Per `docs/automation/CURSOR_SELF_IMPROVEMENT.md`:

- Safe web check (if using web search)
- Context refresh (CODEBASE_OVERVIEW, KNOWN_TRUTHS)
- Research 1 CRUCIAL item via web
- Add "Suggested for user approval" if drastic

---

### Step 4 — Execute

- **Light + Medium:** Run autonomously. No prompts.
- **Heavy:** Defer or ask user. Per-feature gate: "Are you ready to implement [feature]?" → Question lock → Visual approval → "approve 100% implement".
- **Tests must pass.** Revert changes that break tests.
- **Timebox:** ≤10 min per task. If stuck → document and defer.

Full routine: `docs/automation/IMPROVEMENT_LOOP_ROUTINE.md` (Phases 1–4).

---

### Step 5 — Summary

Write `docs/automation/IMPROVEMENT_LOOP_SUMMARY_<date>.md` (or `120_MINUTE_LOOP_SUMMARY_<date>.md`):

- Phase 0 note
- Metrics block (build, tests, lint)
- Quality Grade
- Self-improvement section (web search, context drift, suggested drastic)
- Suggested next steps

---

## Quick Reference

| Command | Action |
|---------|--------|
| **GO** | Start full loop |
| **revert** | Restore from pre-loop checkpoint |
| **Light and medium only** | Skip Heavy; run autonomously |

---

## Additional Resources

- **Find everything:** [IMPROVEMENT_LOOP_INDEX.md](../../../docs/automation/IMPROVEMENT_LOOP_INDEX.md) — master index of all loop docs, scripts, .cursor rules/skills.
- Full routine: [IMPROVEMENT_LOOP_ROUTINE.md](../../../docs/automation/IMPROVEMENT_LOOP_ROUTINE.md)
- Tiering details: [LOOP_TIERING.md](../../../docs/automation/LOOP_TIERING.md)
- Common sense: [IMPROVEMENT_LOOP_COMMON_SENSE.md](../../../docs/automation/IMPROVEMENT_LOOP_COMMON_SENSE.md)
