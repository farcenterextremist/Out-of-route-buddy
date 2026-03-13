# Terminology and copy audit

**Purpose:** Single reference for user-facing terminology (Out of route, OOR, miles, etc.) so copy stays consistent. Part of Polishing Plan Phase 3.1.

---

## Chosen convention

| Term | UI / user-facing | Code / docs |
|------|-------------------|-------------|
| Out of route | "Out of route" or "Out of route (OOR)" when space allows | `out-of-route`, `oorMiles`, `OOR` in identifiers |
| Miles | "miles" (lowercase) in labels; "Total Miles", "Loaded Miles", "Actual Miles" as section labels where used | `loadedMiles`, `actualMiles`, `totalMiles` in code |
| Trip | "trip" / "Trip" as appropriate (e.g. "Trip started", "Trip saved") | `Trip`, `trip` in code |

- **Consistency:** Use the same label for the same concept across trip input, history, stat cards, and settings.
- **Typos:** Fix any found in user-facing strings (e.g. "Calcuation" → "Calculation"); no layout changes.

---

## Current usage (reference)

- **App name:** OutOfRouteBuddy (no spaces).
- **Code:** `oorMiles`, `oorPercentage`, `Trip`, `TripState`, `loadedMiles`, `bounceMiles`, `actualMiles` (see domain models and ViewModels).
- **Current string resources:** `app/src/main/res/values/strings.xml`. Convention above aligns with existing usage (e.g. "OOR Miles", "Out of Route", "Total Miles", "View Monthly OOR").

---

## When adding or editing copy

1. Prefer the table above for terminology.
2. Use "Out of route" or "OOR" consistently in the same screen; avoid mixing "out of route", "Out Of Route", "OOR" randomly.
3. Fix typos and unclear labels only; no unwarranted UI/layout changes.

---

*Ref: [POLISHING_PLAN_PROMPT.md](../archive/prompts/POLISHING_PLAN_PROMPT.md) §3; [FEATURE_BRIEF_stat_cards_calendar_history.md](../product/FEATURE_BRIEF_stat_cards_calendar_history.md).*

---

## Locale and localization (Blind Spot Plan §3)

- **Current scope:** English only (single locale). No localization roadmap for the next 6–12 months.
- **RTL:** The app manifest has `android:supportsRtl="true"`. RTL is not explicitly tested; layout is built for LTR.
- **If scope changes later:** Plan for string extraction (e.g. keep all user-facing text in `res/values/strings.xml` or locale-specific `values-*`), and for date/number formats by locale. No roadmap item added; this is documentation only.
