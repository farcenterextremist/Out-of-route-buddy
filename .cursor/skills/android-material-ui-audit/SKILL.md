---
name: android-material-ui-audit
description: >-
  Audits Android View/XML UIs against Material Design 3 and accessibility
  expectations: color roles, typography scale, spacing, component states, touch
  targets, motion, TalkBack, and contrast. Use when auditing a screen, preparing
  a frontend fix list, reviewing themes and layouts, or before shipping UI changes
  in OutOfRouteBuddy.
---

# Android Material UI Audit (M3 + A11y)

## Purpose

Produce a **structured, evidence-based audit** of Android **View/XML** screens so frontend fixes are **prioritized** and aligned with **Material 3** and **accessibility**—without relying on vague “make it prettier.”

## When to use

- “Audit this screen,” “review layout XML,” “theme consistency,” “a11y pass,” “before we change the frontend,” “M3 compliance,” “touch targets,” “dark mode issues.”
- Pair with **`android-m3-design-study`** when the user is still learning; this skill is for **evaluation and fix lists**.

## Preconditions

- Respect **project policy**: no unwarranted production UI changes without user approval; output **findings and recommendations**, apply edits only when user asks.
- Prefer **file paths** and **attribute names** in findings (e.g. `res/layout/fragment_trip_input.xml`, `@style/...`).

## Audit dimensions (check each)

1. **Color & theme**
   - Semantic roles: primary, surface, error, on-*; avoid raw unrelated hex for the same meaning.
   - Night: `values-night` / `-night` drawables; borders visible on surface.

2. **Typography**
   - Titles vs body vs captions use a **consistent scale**; avoid one-off `textSize` spam without a style.

3. **Layout & spacing**
   - Rhythm (e.g. 8dp grid); section separation; no accidental crowding on small widths.

4. **Components & states**
   - Buttons/text fields show **disabled**, **error**, **loading** where applicable.
   - **Primary action** is visually dominant; no competing CTAs at same weight.

5. **Touch & hit targets**
   - ~**48dp** minimum for primary interactions (guideline; note exceptions with reason).

6. **Motion**
   - Transitions purposeful; no gratuitous animation on critical paths.

7. **Accessibility**
   - **contentDescription** for icon-only controls; **contrast** for text vs background; **focus order** sanity for TalkBack (order in XML / importantForAccessibility).

8. **Project rubric (OutOfRouteBuddy)**
   - Cross-check `docs/ux/PLEASANTNESS_AND_FLOW_STANDARD.md`, `docs/ux/ACCESSIBILITY_CHECKLIST.md` when doing a full pass.

## Workflow

1. Identify **scope** (one fragment/layout vs whole flow).
2. Read **layout XML**, **themes**, **colors**, **styles** referenced by the screen.
3. Walk the **audit dimensions** above; note **severity**: blocker / should-fix / nice-to-have.
4. If screenshots or device state available, reference them; otherwise be explicit about assumptions.
5. **Do not** implement broad UI refactors unless user approves; deliver a **fix list** first.

## Output format

```markdown
## Android Material UI Audit

- **Scope:** [screen/flow]
- **Files reviewed:** [paths]
- **Blockers:** [or none]
- **Should-fix:** [bullets with file + suggestion]
- **Nice-to-have:** [bullets]
- **A11y notes:** [contrast, labels, targets]
- **Suggested order of work:** [1–2–3]
```

## Integration

- After structural fixes, run **frontend-pleasantness-reviewer** for project-specific pleasantness scoring.
- For screenshot-based review, use **frontend-screenshot-reviewer** when the user supplies images.

## Guardrails

- Do not claim WCAG compliance without checking contrast numbers or official criteria.
- Do not mix **Compose** APIs into a **pure XML** audit unless the screen uses Compose.
