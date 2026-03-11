# Start/End Trip button — loading and disabled state

**Owner:** Front-end Engineer  
**Purpose:** Ensure Start Trip and End Trip show visible disabled or loading state when appropriate.  
**Related:** 25-point #8, `docs/archive/APP_IMPROVEMENT_25_POINT_BRAINSTORM.md`.

---

## Requirement

- When a start/end action is in progress (e.g. saving trip, syncing), the button should show a **loading state** (e.g. progress indicator or spinner) or be clearly **disabled** so the user doesn’t double-tap.
- When input is invalid (e.g. empty or invalid loaded/bounce miles), the Start Trip button can be **disabled** with visible styling (e.g. dimmed or disabled alpha).

---

## Where to implement

- Trip input fragment layout and ViewModel: bind `isLoading` or `isBusy` from ViewModel to button `enabled` and/or a progress indicator.
- Use existing Material/AppCompat disabled state and a small progress indicator (e.g. `ProgressBar` or circular indicator) next to or inside the button when loading.

---

*No UI change requested without your permission; this doc is the spec for FE to implement when ready.*
