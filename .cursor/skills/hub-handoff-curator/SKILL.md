---
name: hub-handoff-curator
description: Curates what should be sent to the Hub and enforces polished, reusable handoff quality. Use when preparing loop outputs for hub deposit, updating hub index entries, or minimizing slop in shared artifacts.
---

# Hub Handoff Curator

## Purpose

Keep Hub deposits high-signal: polished, traceable, and reusable by other loops.

Primary references:

- `docs/agents/data-sets/hub/UNIVERSAL_LOOP_PROMPT.md`
- `docs/agents/data-sets/hub/README.md`
- `docs/automation/LOOP_GATES.md`

---

## Trigger

Use this skill when requests mention:

- "send to hub"
- "hub summary"
- "polished output"
- "reduce slop in reports"
- "update hub index"

---

## Workflow

1. **Select artifact**
   - Confirm output is complete and polished (not draft).
   - Confirm it includes concrete paths, metrics, and next steps.

2. **Apply quality filter**
   - Reject generic boilerplate and vague claims.
   - Keep one clear "why useful" line per artifact.

3. **Standard naming**
   - Use `YYYY-MM-DD_<role-or-topic>_<short-description>.<ext>`.

4. **Index update**
   - Add one concise row/line in `hub/README.md`.
   - Include what it is and why it is reusable.

5. **Traceability check**
   - Ensure the loop summary and ledger reference the hub artifact.

---

## Output checklist

- [ ] Artifact is polished and project-specific
- [ ] Naming follows hub convention
- [ ] Hub index updated
- [ ] Summary/ledger references artifact
- [ ] No slop language

---

## Guardrails

- Do not deposit unfinished drafts.
- Prefer one excellent artifact over many weak artifacts.
- Keep entries compact and practical for next-loop reuse.
