# Design & UX Research

**Purpose:** Design research topics for the Improvement Loop — color schemes, templates, state flows, beautification, professionalism. Integrated into Phase 0.4 and Phase 3.

**References:** [docs/ux/UI_CONSISTENCY.md](../ux/UI_CONSISTENCY.md), [docs/ux/TERMINOLOGY_AND_COPY.md](../ux/TERMINOLOGY_AND_COPY.md), [docs/ux/PLEASANTNESS_AND_FLOW_STANDARD.md](../ux/PLEASANTNESS_AND_FLOW_STANDARD.md)

---

## Research topics (rotate per run)

- Color schemes, Material Design 3 palettes
- State flows (loading/error/empty)
- Color matching & contrast
- Fleet/driver app UI patterns
- Beautification standards (spacing, typography, elevation)
- Professional UI principles

---

## How to use

1. **Phase 0.4:** Web search one topic; compare to `app/src/main/res/values/colors.xml` and [UI_CONSISTENCY.md](../ux/UI_CONSISTENCY.md).
2. **Phase 0.4b:** Score one relevant screen or flow using [PLEASANTNESS_AND_FLOW_STANDARD.md](../ux/PLEASANTNESS_AND_FLOW_STANDARD.md) so the loop records what feels pleasant vs rough in a repeatable way.
3. **Phase 3:** Apply at most one subtle improvement per loop. No layout drift.

## Required output

Add a short review block when design research is used:

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

*See [IMPROVEMENT_LOOP_ROUTINE.md](./IMPROVEMENT_LOOP_ROUTINE.md) Phase 0.4 and Phase 3.*
