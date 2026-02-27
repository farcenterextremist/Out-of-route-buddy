# Accessibility checklist (one pass)

**Owner:** UI/UX Specialist  
**Purpose:** Single consolidated checklist for touch targets, labels, and contrast. Hand off to Front-end as one batch so we don't scatter fixes.  
**Related:** ACCESSIBILITY_TRIP_HISTORY.md, STATISTICS_SECTION_SPEC.md.

---

## Touch targets

- All interactive elements meet minimum 48dp touch target (or equivalent).
- No overlapping or too-close tap areas that cause mis-taps.
- Trip screen: Start, End, Clear, Continue, period/mode controls.
- History: list items, View/Details, any filters or actions.
- Statistics row and period selector: tappable areas clearly sized.

---

## Labels (contentDescription / TalkBack)

- Every interactive element has a concise contentDescription that describes the action or content.
- Trip screen: "Start trip", "End trip and save", "Clear trip without saving", "Continue driving", etc.
- History: list items announce date and miles; "View trip details" for detail actions.
- Statistics: values and section labeled so screen readers make sense of the row.
- Dialogs: primary and secondary actions clearly labeled.

---

## Contrast and visibility

- Text and icons meet contrast requirements (WCAG 2.1 Level AA or equivalent) against background.
- Light and dark themes: check both; use values/ and values-night/ consistently.
- No information conveyed by color alone; use icon or text in addition where needed.

---

## Focus order

- Logical focus order (top to bottom, primary before secondary).
- No focus traps; dismiss/Continue returns focus appropriately.
- End Trip dialog: End Trip, Clear, Continue order makes sense for keyboard/TalkBack.

---

When adding or changing UI, run through this list and update the relevant section. Front-end implements; UI/UX may add rows as new screens or controls are added.
