---
name: loop-ledger-normalizer
description: Normalizes loop ledger entries to a consistent structure across Improvement, Token, Cyber Security, and Synthetic Data loops. Use when updating ledger templates, appending run blocks, or fixing cross-loop ledger drift.
---

# Loop Ledger Normalizer

## Purpose

Keep ledger entries consistent so runs are easy to compare and audit.

Primary references:

- `docs/automation/IMPROVEMENT_LOOP_RUN_LEDGER.md`
- `docs/automation/TOKEN_LOOP_RUN_LEDGER.md`
- `docs/automation/CYBER_SECURITY_LOOP_RUN_LEDGER.md`
- `docs/automation/SYNTHETIC_DATA_LOOP_RUN_LEDGER.md`
- `docs/automation/LOOP_CONSISTENCY_LEDGER_SNIPPET.md`

---

## Trigger

Use this skill when requests mention:

- "ledger format"
- "normalize run blocks"
- "run log consistency"
- "standardize loop ledgers"
- "template drift"

---

## Workflow

1. **Read current templates**
   - Open all loop ledger templates and recent run blocks.

2. **Align required fields**
   - Ensure each ledger block includes: focus/summary, proof of quality, next steps.
   - Ensure each includes `Loop Consistency Check` (or links to snippet).

3. **Preserve loop-specific details**
   - Keep unique fields (e.g. Token rule output, Cyber validation count, Synthetic tier changes).

4. **Patch minimal surface**
   - Prefer template-level fixes over rewriting historical run blocks.

5. **Verify**
   - Confirm new run blocks can be filled without ambiguity.

---

## Guardrails

- Do not delete historical run data unless user asks.
- Keep edits additive and backward compatible.
- Keep templates concise and copy/paste friendly.
