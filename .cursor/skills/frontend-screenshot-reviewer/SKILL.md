---
name: frontend-screenshot-reviewer
description: Reviews Android UI using screenshots, before/after pairs, and theme/state comparisons. Use when the user wants visual evidence that a screen looks good, pleasant, polished, professional, or when checking frontend flow with screenshots or device captures.
---

# Frontend Screenshot Reviewer

## Purpose

Judge UI quality using real screenshots so frontend feedback is based on visible evidence instead of memory or taste alone.

Primary references:

- `docs/ux/SCREENSHOT_REVIEW_WORKFLOW.md`
- `docs/ux/PLEASANTNESS_AND_FLOW_STANDARD.md`
- `docs/ux/UI_CONSISTENCY.md`
- `docs/ux/ACCESSIBILITY_CHECKLIST.md`

## Trigger

Use this skill when requests mention:

- "review this screenshot"
- "does this look good"
- "before and after"
- "visual review"
- "screen looks off"
- "pleasant to look at"
- "compare these UI states"

## Workflow

1. **Collect the right evidence**
   - Prefer before/after pairs, light/dark theme captures, and critical states.
   - If screenshots are missing, ask for them or capture from device if appropriate.

2. **Review the screenshot, not assumptions**
   - Judge what is visibly present on screen.
   - Avoid inventing hidden behavior from a static image.

3. **Score the screen**
   - Apply the pleasantness rubric from `PLEASANTNESS_AND_FLOW_STANDARD.md`.

4. **Review flow clues**
   - Check whether the primary action, secondary action, and current state are visually obvious.

5. **Give evidence-backed feedback**
   - Name the strongest trait, weakest trait, and whether the flow looks clear.

## Output format

```markdown
## Screenshot Review

- Screen: [name]
- Evidence set: [before/after, light/dark, state names]
- Pleasantness score: X/20
- Strongest visual trait: [one line]
- Weakest visual trait: [one line]
- Flow clarity: [clear | mostly clear | confusing]
- Recommendation: [acceptable | polish next | structural proposal only]
```

## Guardrails

- Do not call a UI "better" without naming what changed visually.
- Do not infer hidden functionality from a screenshot alone.
- Respect the project rule: no unwarranted UI changes without user approval.
