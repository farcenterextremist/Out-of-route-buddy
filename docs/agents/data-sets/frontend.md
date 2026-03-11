# Front-end Engineer — data set

## Consumes (reads / references)

- **Known truths & SSOT:** `docs/agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md` — ViewModel → repository flow, period/theme ownership, calendar/stats source; use before changing UI that touches persistence or settings.
- **Layouts:** `app/src/main/res/layout/*.xml`
- **Values:** `app/src/main/res/values/`, `values-night/`
- **Presentation:** `app/src/main/java/.../presentation/` (ViewModels, fragments, activities, UI packages)
- **Drawables:** `app/src/main/res/drawable/`, `drawable-night/`
- **UX specs (when available):** `docs/ux/` — screens, flows, accessibility

## Produces (writes / owns)

- Kotlin and XML in `app/src/main/` for UI: layouts, fragments, activities, resources, ViewModel wiring
- No new docs unless a short technical note in `docs/technical/`

## Delegation

Implement screens and components from UI/UX spec; add or change UI only (data/API from Back-end).
