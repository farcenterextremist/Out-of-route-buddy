# Automation-to-Prompt Research

**Purpose:** Standardize how we convert regular automation scripts/routines into prompt-driven LOOP GATES workflows that agents can execute consistently.

**Related:** `LOOP_GATES.md`, `UNIVERSAL_LOOP_PROMPT.md`, `AUTONOMOUS_LOOP_SETUP.md`, `DATA_USEFULNESS_AND_PRUNING_RESEARCH.md`.

---

## Why convert automation into prompts

- Prompts are easier to audit in run summaries/ledgers than opaque script logic.
- Prompt steps can be shared across all loop types (Improvement, Token, Cyber, Synthetic Data).
- Prompt-based gates reduce drift by forcing start/end checks (master files, shared state, hub writeback).
- Conversion supports gradual rollout: keep scripts for execution, use prompts for control policy and reporting.

---

## Conversion pattern (recommended)

1. **Extract intent** from script ("what outcome is required").
2. **Define LOOP GATES** at start/end:
   - Start: read master files + hub + shared state.
   - End: publish polished output to hub + update shared events/latest state.
3. **Translate operations** into explicit checklist steps:
   - Preconditions, actions, validations, outputs, failure handling.
4. **Map validations** to measurable checks (tests, lint, liveness, event append, hub index row).
5. **Track incremental progress** for long-running features (production-stage items).

---

## Prompt skeleton for converted automations

```text
LOOP GATES:
Start: read LOOP_MASTER_ROLE, LOOPS_AND_IMPROVEMENT_FULL_AXIS, UNIVERSAL_LOOP_PROMPT, hub README, loop_shared_events tail, loop_latest/*.json.
Execute: perform scoped tasks + validations.
End: summarize results, publish polished artifacts to hub, append loop_shared_events finished event, update loop_latest/<loop>.json.
```

Use this skeleton in universal prompts/rules so every agent follows the same control surface.

---

## Data-pruning tie-in

When converting automations to prompts, include pruning criteria from `DATA_USEFULNESS_AND_PRUNING_RESEARCH.md`:

- Keep only artifacts that are reusable/actionable.
- Archive stale or duplicate outputs.
- Require provenance + one-line value statement for hub deposits.

This prevents automation-to-prompt conversion from increasing noise/slop.

---

## Recommended adoption in loops

- Require this doc in Phase 0 research for any loop changing process automation.
- Add one "automation-to-prompt conversion note" in the summary when applicable.
- For production-stage #17/#20, treat conversion as incremental work: one gate/checklist improvement per run.
