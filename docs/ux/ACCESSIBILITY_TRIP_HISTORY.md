# Accessibility: Trip and History — one pass

**Owner:** UI/UX Specialist  
**Purpose:** One pass for TalkBack labels and focus order on Trip and History screens.  
**Related:** 25-point list #6, `docs/agents/APP_IMPROVEMENT_25_POINT_BRAINSTORM.md`.

---

## TalkBack (contentDescription)

- **Trip screen:** Every interactive element (Start Trip, End Trip, Clear, Continue, period or mode controls) must have a concise `contentDescription` (or `android:contentDescription`) that describes the action, e.g. "Start trip", "End trip and save", "Clear trip without saving", "Continue driving".
- **History screen:** List items and headers should announce meaningfully (e.g. "Trip on February 19, 2025, 12.3 miles"); "View" or "Details" buttons: "View trip details".
- **Statistics:** See `docs/ux/STATISTICS_SECTION_SPEC.md` (labels for values and section).

---

## Focus order

- Logical tab order: top to bottom, primary action before secondary. On End Trip dialog: End Trip → Clear → Continue (or per END_TRIP_FLOW_UX).
- Ensure no focus traps; dismiss button (Continue) returns focus to the trip screen.

---

## Quick checklist

- [ ] Trip: all buttons have contentDescription.
- [ ] History: list items and actions have contentDescription.
- [ ] Focus order is logical on Trip and in End Trip dialog.
- [ ] No invisible focusable elements without labels.

---

*Front-end: add or review `contentDescription` in layouts and composables. QA: run TalkBack and verify announcements and order.*
