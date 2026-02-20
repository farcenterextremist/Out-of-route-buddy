# End Trip flow — UX note

**Owner:** UI/UX Specialist  
**Purpose:** Confirm copy and button order for End Trip so they match user expectations.  
**Related:** 25-point list #5, `docs/agents/APP_IMPROVEMENT_25_POINT_BRAINSTORM.md`.

---

## Recommended pattern

- **Primary action:** **End Trip** — ends the trip and saves; one tap, clear outcome.
- **Secondary / destructive:** **Clear** (or "Discard") — abandon without saving; use cautious copy so users don’t tap by mistake.
- **Dismiss:** **Continue** (or "Keep driving") — closes the dialog and returns to trip in progress; no data change.

---

## Button order

- Prefer: **End Trip** first (primary), then **Clear**, then **Continue** (or bottom: Continue as tertiary).
- Rationale: Most users want to end and save; Clear is secondary; Continue is "cancel this dialog."

---

## Copy

- **End Trip:** Clear and affirmative; avoid "Submit" or "OK."
- **Clear:** Explicit that trip will not be saved (e.g. "Clear and don’t save" or "Discard trip").
- **Continue:** "Continue" or "Keep driving" so the user knows the trip stays active.

---

*Front-end: align `strings.xml` and dialog layout with this order and copy. QA: verify in manual test checklist.*
