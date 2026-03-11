# Strings audit — Trip and History

**Owner:** Front-end Engineer  
**Purpose:** One audit task: move hardcoded strings to `strings.xml`.  
**Related:** 25-point #9, `docs/archive/APP_IMPROVEMENT_25_POINT_BRAINSTORM.md`.

---

## Scope

- **Trip screen:** Labels, button text, hints, error messages, dialog titles/copy.
- **History screen and dialogs:** List headers, empty state text, button labels (e.g. "View", "Delete"), dialog titles.
- **Statistics:** Section title, value labels (e.g. "Miles this month").

---

## How to audit

1. Search `app/src/main/java` and `app/src/main/res/layout` for string literals that are user-facing (no need to move log messages or debug strings).
2. Add entries to `res/values/strings.xml` (and `strings.xml` in any locale folders if present).
3. Replace literals in Kotlin with `getString(R.string.xxx)` and in XML with `@string/xxx`.

---

## Files to check first

- `TripInputFragment.kt`, trip-related layouts, `TripHistoryByDateDialog.kt`, history layouts, statistics row/section layouts.
- `res/values/strings.xml` — ensure no new hardcoded copy in layouts or code for Trip/History.

---

*Single audit task; mark done in 25-point list when complete.*
