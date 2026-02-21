# UI/UX Specialist — data set

## Consumes (reads / references)

- **Known truths & SSOT:** `docs/agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md` — End vs Clear, what drives calendar/stats, recovery; use so flows and copy match actual behavior.
- **Layouts:** `app/src/main/res/layout/*.xml` (e.g. `fragment_trip_input.xml`, `statistics_row.xml`, `custom_toolbar.xml`).
- **Strings:** `app/src/main/res/values/strings.xml` — copy and keys; suggest changes, do not edit code.
- **Emulator (visual spec):** `phone-emulator/` — use as reference for screens and interactions; suggest parity or improvements.
- **Feature briefs:** `docs/product/FEATURE_BRIEF_*.md` when available — to design flows and placement (e.g. Auto drive button).
- **Colors/themes:** `app/src/main/res/values/colors.xml`, `values-night/` — for consistency and accessibility.

## Produces (writes / owns)

- **Flow/screen docs:** `docs/ux/SCREENS_AND_FLOWS.md` or per-feature notes — what screens exist, user flow, where new UI lives.
- **Accessibility:** `docs/ux/ACCESSIBILITY_CHECKLIST.md` — labels, contrast, touch targets, and quick wins.
- **Copy suggestions:** In markdown or as a list for Front-end/Design; no direct edits to `strings.xml` (Front-end applies).

## Delegation

Design flows for a feature; improve accessibility for a section; propose where a control lives (e.g. Auto drive button); write UI copy for a screen.
