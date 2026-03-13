---
name: critique-data-minimize-slop
description: >-
  Critiques data and metadata for quality, consistency, and usefulness; identifies
  and reduces AI slop (generic, vague, or templated output). Use when reviewing
  data structures, metadata, exports, docs, or AI-generated content, or when the
  user asks to reduce slop, tighten copy, or improve content quality.
---

# Critique Data / Metadata & Minimize AI Slop

## When to Apply

- User asks to "reduce slop," "tighten copy," "critique data," or "clean up metadata."
- Reviewing exports, schemas, docs, or any text/data that may be generic or AI-generated.
- Before committing docs, feature briefs, or user-facing strings that should feel specific and human.

---

## What Counts as AI Slop

**AI slop** = output that sounds polished but is vague, generic, or interchangeable.

| Symptom | Prefer |
|--------|--------|
| Filler phrases | "leverage," "robust," "seamlessly," "comprehensive," "cutting-edge," "game-changing" |
| Vague adjectives | "various," "multiple," "several," "appropriate," "relevant" without saying which |
| Hedging without value | "it is important to note," "it should be noted that," "in order to" |
| Template openings | "In today's world…," "When it comes to…," "At the end of the day…" |
| Redundant meta | Metadata that restates the title or adds no queryable/actionable info |
| Boilerplate conclusions | "In conclusion," "To sum up," generic sign-offs that add no content |

**Not slop:** Necessary jargon, domain terms, or concise technical language.

---

## Critiquing Data and Metadata

1. **Structure**
   - Are fields necessary for querying, display, or policy (e.g. tier, date)? Remove or demote "nice to have" bloat.
   - Is the schema consistent (names, types, nullability)? Flag drift.

2. **Completeness**
   - Required fields present? Sensible defaults documented?
   - For exports: can a consumer use this without extra docs? Missing keys or ambiguous enums = problem.

3. **Redundancy**
   - Does metadata duplicate what’s already in the payload (e.g. "description" that just repeats the title)?
   - Merge or drop redundant fields; keep one source of truth.

4. **Clarity**
   - Names and descriptions unambiguous? No vague "misc" or "other" without a clear rule.
   - Human-facing strings: specific and actionable, not generic.

5. **Tier / sensitivity (if applicable)**
   - For this project: GOLD = human-only; synthetic stays PLATINUM/SILVER. Metadata should not blur tier or suggest human provenance for synthetic data.

---

## Minimizing Slop in Text

1. **Replace filler** with concrete terms or delete.
2. **Specify** instead of "various" or "multiple" — e.g. "three trip types" or list them.
3. **Shorten** — remove lead-in phrases that don’t add information.
4. **Prefer active voice** and clear subjects; avoid "it is possible to…" when "you can…" or "the system does…" works.
5. **One idea per sentence** where it improves scanability; avoid long "and… and…" chains.

---

## Quick Checklist

Use when reviewing data/metadata or copy:

- [ ] No filler adjectives or buzzwords (leverage, robust, seamlessly, comprehensive, etc.)
- [ ] No vague "various/multiple/several" without specificity
- [ ] No redundant metadata that only restates other fields
- [ ] Field/schema names and descriptions clear and consistent
- [ ] Required fields and enums documented; exports usable without guesswork
- [ ] Tier/labeling correct (e.g. GOLD vs synthetic) if applicable
- [ ] Lead-in and conclusion sentences add information or removed

---

## Output Format for Critiques

When providing a critique, use:

```markdown
## Data / metadata
- [Issue or ✅ OK]: brief note

## Slop / copy
- [Issue or ✅ OK]: brief note

## Suggestions
1. Concrete edit or removal
2. ...
```

Keep suggestions actionable (exact replacements or "remove X" rather than vague "improve clarity").

---

## Project Context (OutOfRouteBuddy)

- **Data tiers:** Human data = GOLD only; synthetic = PLATINUM or SILVER. Metadata and exports must not imply human provenance for synthetic data. See `docs/DATA_TIERS.md`.
- **Exports and schemas:** Prefer minimal, stable fields; avoid adding fields "for completeness" that nobody queries or displays.
- **Docs and briefs:** Follow existing templates (e.g. feature briefs); avoid generic marketing tone in technical or product docs.
