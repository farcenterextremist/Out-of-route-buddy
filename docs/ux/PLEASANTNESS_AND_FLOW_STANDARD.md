# Pleasantness and flow standard

**Purpose:** Turn "pleasant to look at" from a vague opinion into a repeatable review standard for OutOfRouteBuddy screens and flows.

**Why this exists:** Pleasantness is partly subjective, but the app can still enforce a strong baseline using visual hierarchy, spacing rhythm, clear interaction states, and low-friction task flow.

**Related:** `docs/ux/UI_CONSISTENCY.md`, `docs/ux/ACCESSIBILITY_CHECKLIST.md`, `docs/ux/USER_WORKFLOWS.md`, `docs/ux/SCREENSHOT_REVIEW_WORKFLOW.md`, `docs/automation/FRONTEND_CHANGE_AUTOMATION_GATE.md`.

---

## Core idea

Treat pleasantness as a blend of four qualities:

1. **Clarity** — The eye knows where to look first.
2. **Rhythm** — Spacing, sizing, and alignment feel consistent.
3. **Calmness** — The UI does not feel noisy, cramped, or visually conflicting.
4. **Flow** — The next action is obvious and the screen supports the user's task path.

If a screen is accessible but still feels "off," the issue is usually one of those four.

---

## Pleasantness score (0-20)

Score each category from `0-4`, then sum for a total score out of `20`.

| Category | 0 | 2 | 4 |
|---|---|---|---|
| **Visual hierarchy** | No clear focal point | Mostly clear, some competition | Strong primary focal point and obvious secondary content |
| **Spacing rhythm** | Inconsistent gaps/padding | Mostly consistent with minor drift | Consistent spacing scale; screen breathes naturally |
| **State clarity** | Buttons/sections unclear | States mostly readable | Interactive states are obvious in light/dark themes |
| **Task flow** | User pauses to interpret next step | Mostly understandable | Next action and sequence feel natural and low-friction |
| **Polish / calmness** | Noisy, cluttered, mismatched | Mostly calm with rough edges | Cohesive, balanced, and visually comfortable |

### Interpretation

- `17-20` = Pleasant and trustworthy
- `13-16` = Good, but has visible polish gaps
- `9-12` = Functional, but not yet pleasant enough
- `0-8` = Visually or flow-wise rough; needs targeted improvement

---

## Hard-stop rules

Pleasantness does **not** override correctness or accessibility.

These must pass first:

- Touch targets meet `48dp` minimum
- Contrast remains safe in light and dark themes
- Important actions are labeled clearly
- No new flow confusion is introduced just to make a screen "prettier"
- No unwarranted structural UI changes without user approval

---

## Review checklist

Use this checklist when reviewing or proposing frontend changes:

- Is there one obvious focal point on the screen?
- Do related elements feel visually grouped?
- Does spacing follow a small repeatable scale instead of random values?
- Are primary, secondary, and destructive actions easy to distinguish?
- Does the screen still read clearly in dark mode?
- Can a user predict the next step without extra explanation?
- Does any section feel crowded, noisy, or over-styled?
- Is any motion purposeful rather than decorative?

---

## Flow review checklist

Use this for screen-to-screen or section-to-section task flow:

- Entry point is obvious
- Primary task is visible without hunting
- Secondary actions do not compete with the main task
- Error, empty, loading, and success states are understandable
- User can recover from mistakes without confusion
- The screen supports the workflow documented in `USER_WORKFLOWS.md`

---

## Evidence sources

When possible, support the score with evidence instead of taste alone:

- Accessibility checklist pass/fail
- UI consistency doc comparison
- User workflow alignment
- Light/dark screenshots
- Existing lint or test signals related to the touched UI
- A brief note explaining which specific friction point improved

For screenshot-based review, follow `SCREENSHOT_REVIEW_WORKFLOW.md`.

For the sandboxed [LOOP_COUNCIL_SANDBOX.md](../automation/LOOP_COUNCIL_SANDBOX.md), this score becomes the visual side of a hybrid judgment and must be paired with durability/build evidence before a strong keep recommendation is made.

---

## Required output block

When this standard is used in review, proposal, or loop summary output, use:

```markdown
## Pleasantness and Flow Review

- Screen or flow: [name]
- Pleasantness score: X/20
- Strongest area: [one line]
- Weakest area: [one line]
- Evidence: [docs, screenshots, code, or checks]
- Decision: [acceptable | improve before shipping | proposal only]
```

---

## Anti-slop rule

Do not say a screen is "clean," "smooth," "professional," or "pleasant" without naming:

- what improved,
- how it was judged,
- and what still feels weak.

Use: `claim -> evidence -> residual weakness -> next step`.
