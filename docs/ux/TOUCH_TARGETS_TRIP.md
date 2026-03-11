# Trip screen touch targets — UX note

**Owner:** UI/UX Specialist  
**Purpose:** Ensure all interactive elements on the Trip screen meet minimum touch target size.  
**Related:** 25-point list #3, `docs/archive/APP_IMPROVEMENT_25_POINT_BRAINSTORM.md`.

---

## Requirement

- **Minimum size:** 48×48 dp for all tappable controls (Start Trip, End Trip, Clear, Continue, and any secondary buttons).
- **Quick win:** If a button’s visual size is smaller, add `minWidth="48dp"` and `minHeight="48dp"` (or use `minTouchTargetSize` / padding) so the hit area is at least 48dp; keep label text unchanged.

---

## Where to check

- Trip input fragment layout(s): primary action buttons.
- End Trip dialog / bottom sheet: End Trip, Clear, Continue.
- Any FAB or icon buttons on the Trip screen.

---

## Reference

- Material: [Touch target size](https://material.io/design/usability/accessibility.html#layout-and-typography) — minimum 48dp.
- Same rule applies to Statistics section (see `docs/ux/STATISTICS_SECTION_SPEC.md`).

---

*Front-end: apply in `app/src/main/res/layout/` (trip-related XML). QA: verify with manual tap tests or accessibility scanner.*
