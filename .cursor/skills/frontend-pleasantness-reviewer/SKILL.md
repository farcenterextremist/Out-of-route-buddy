---
name: frontend-pleasantness-reviewer
description: Reviews Android screens and user flows for visual pleasantness, calmness, and task flow using the project rubric. Use when the user asks whether a UI looks good, pleasant, polished, professional, smooth, or when evaluating frontend design and flow changes.
---

# Frontend Pleasantness Reviewer

## Purpose

Review UI changes using a repeatable rubric so "pleasant to look at" becomes measurable instead of purely subjective.

Primary references:

- `docs/ux/PLEASANTNESS_AND_FLOW_STANDARD.md`
- `docs/ux/UI_CONSISTENCY.md`
- `docs/ux/ACCESSIBILITY_CHECKLIST.md`
- `docs/ux/USER_WORKFLOWS.md`
- `docs/automation/FRONTEND_CHANGE_AUTOMATION_GATE.md`

## Trigger

Use this skill when requests mention:

- "pleasant"
- "polished"
- "looks good"
- "front end design"
- "screen flow"
- "professional UI"
- "smooth UI"
- "verify this design"

## Workflow

1. **Baseline the screen or flow**
   - Identify the primary task, main focal point, and user path.
   - Check related workflow docs before making claims.

2. **Apply the pleasantness score**
   - Score visual hierarchy, spacing rhythm, state clarity, task flow, and polish/calmness out of 20 using `PLEASANTNESS_AND_FLOW_STANDARD.md`.

3. **Check hard-stop rules**
   - Accessibility, contrast, touch target safety, and no unwarranted structural UI change.

4. **Name the strongest and weakest areas**
   - Be specific: spacing drift, competing CTAs, unclear state, noisy section, weak hierarchy.

5. **Recommend only the right level of change**
   - Small/subtle changes: may be proposed as polish.
   - Structural UI or navigation changes: proposal only unless user already approved.

## Output format

```markdown
## Pleasantness and Flow Review

- Screen or flow: [name]
- Pleasantness score: X/20
- Strongest area: [one line]
- Weakest area: [one line]
- Evidence: [docs, screenshots, code, or checks]
- Decision: [acceptable | improve before shipping | proposal only]
```

## Guardrails

- Do not confuse personal taste with evidence.
- Do not approve a UI change that hurts accessibility or flow clarity.
- Respect project policy: no unwarranted UI changes without user approval.
