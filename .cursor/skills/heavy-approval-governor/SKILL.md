---
name: heavy-approval-governor
description: Enforces Heavy-tier approval gates, question lock, and visual approval rules before implementation. Use when tasks may be Heavy, when approving drastic loop changes, or when validating compliance with LOOP_TIERING.
---

# Heavy Approval Governor

## Purpose

Prevent unauthorized Heavy changes and keep approvals explicit, traceable, and safe.

Primary references:

- `docs/automation/LOOP_TIERING.md`
- `docs/automation/HEAVY_IDEAS_FAVORITES.md`
- `docs/automation/IMPROVEMENT_LOOP_ROUTINE.md`
- `docs/automation/FRONTEND_CHANGE_AUTOMATION_GATE.md`

---

## Trigger

Use this skill when requests mention:

- "heavy task"
- "drastic change"
- "approval gate"
- "question lock"
- "approve 100% implement"

---

## Workflow

1. **Classify scope**
   - Confirm whether task is Light/Medium/Heavy by current tier definitions.

2. **Enforce gate**
   - For Heavy: stop and ask for approval options.
   - Apply question lock and visual approval clause where required.

3. **Validate authority**
   - For frontend/UI changes, enforce Master Loop-only rule and frontend gate.

4. **Record evidence**
   - Capture approval wording, selected items, and any exclusions in summary/ledger.

5. **Proceed safely**
   - Implement only approved scope; defer everything else with explicit next steps.

---

## Output checklist

- [ ] Tier classification recorded
- [ ] Heavy approval request shown (if needed)
- [ ] Question lock + visual clause applied (if needed)
- [ ] Frontend authority/gate validated (if UI touched)
- [ ] Approval evidence written to summary/ledger

---

## Guardrails

- Never treat missing approval as implicit approval.
- Keep Heavy execution bounded to exact approved items.
- If approval is ambiguous, ask before implementing.
