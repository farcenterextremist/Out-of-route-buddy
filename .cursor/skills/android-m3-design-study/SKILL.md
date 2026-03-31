---
name: android-m3-design-study
description: >-
  Guides intermediate Android frontend design study using Material Design 3,
  typography, color roles, spacing rhythm, states, motion, and accessibility on
  Views/XML. Use when the user wants to learn Android UI design, study Material 3,
  improve design skills, follow a weekly curriculum, or prepare before changing
  the OutOfRouteBuddy frontend.
---

# Android M3 Design Study (Intermediate)

## Purpose

Turn “get better at Android UI” into a **repeatable study habit** aligned with **Material Design 3** and platform conventions—not generic web design. Targets **View-based / XML** projects (Compose concepts noted only where useful for future migration).

## When to use

- User says: study Material, learn Android UI, design curriculum, week N plan, design terminology, practice exercises, intermediate Android frontend.
- Before large UI refactors: skim **reference.md** and run a mental pass on hierarchy + roles.

## Relationship to other project skills

- **`frontend-pleasantness-reviewer`** — scores screens against project UX docs (pleasantness rubric). Use **after** you understand M3 basics; this skill is **how to learn**, that skill is **how we judge in-repo**.
- **`android-material-ui-audit`** — checklist when **auditing or fixing** layouts/themes. Use when moving from study to implementation.

## Agent workflow

1. **Confirm level** — Intermediate = comfortable with layouts/themes/Fragments; focus on **roles, states, a11y**, not “what is a TextView.”
2. **Point to `reference.md`** — Full 2-week outline, resources, habits, terminology.
3. **Assign one concrete deliverable** per request (e.g. “color role map for one screen,” “type scale audit,” “a11y pass on one fragment”).
4. **Respect project policy** — No unwarranted UI edits; study outputs are notes, diagrams, or **proposals** unless the user approves changes.

## Quick spine (detail in reference.md)

| Week | Focus |
|------|--------|
| 1 | M3 color roles, dynamic color awareness, type scale, shape/elevation consistency |
| 2 | Interactive states, touch targets (~48dp), purposeful motion, TalkBack/contrast |

## Output expectations

For study sessions, prefer:

- A **short checklist** the user can reuse.
- **One** prioritized “next experiment” (e.g. audit `themes.xml` roles only).
- Links to **official** Android / Material docs—not unvetted blogs.

## Guardrails

- Do not conflate **web CSS** patterns with **Android theme attributes** without mapping to `theme`, `attr`, or Compose `MaterialTheme` if relevant.
- Do not recommend UI file edits in OutOfRouteBuddy without explicit user approval for production screens.
