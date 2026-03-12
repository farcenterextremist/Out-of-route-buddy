# OutOfRouteBuddy — Beautification Implementation Map

**Locked direction:** Version B — Balanced modernization  
**Visual philosophy:** Calm industrial clarity  
**Created:** 2025-03-11

---

## Part 1 — Implementation Map

### Phase 1 — Highest-value quick wins

| # | File | Change | Risk | Identity-sensitive |
|---|------|--------|------|---------------------|
| 1 | `res/values/colors.xml` | Add `page_background` (#F5F5F5) | Low | No |
| 2 | `res/values-night/colors.xml` | Add `page_background` (#121212) | Low | No |
| 3 | `res/drawable/background_page.xml` | **New** — flat solid @color/page_background | Low | No |
| 4 | `res/layout/fragment_trip_input.xml` | background: gradient_grey_appbar → background_page | Low | No |
| 5 | `res/layout/fragment_trip_details.xml` | background: gradient_grey_appbar → background_page | Low | No |
| 6 | `res/layout/fragment_trip_input.xml` | padding 10dp → 12dp on inputs; todays_info_card padding 10dp → 16dp; statistics_content padding 16dp (keep); marginTop 24dp → 24dp (keep) | Low | No |
| 7 | `res/layout/fragment_trip_input.xml` | todays_info_card elevation 4dp (keep); statistics_content elevation — add 4dp if missing | Low | No |
| 8 | `res/layout/fragment_trip_input.xml` | todays_info_title 20sp → 18sp bold (section title) | Low | No |
| 9 | `res/drawable/card_white_rounded.xml` | corners 12dp (keep); already correct | — | — |
| 10 | `res/layout/item_trip_history_stat_card.xml` | cardElevation 6dp, cardCornerRadius 12dp (keep) | — | — |
| 11 | `res/layout/fragment_trip_details.xml` | card padding 20dp → 16dp; layout padding 24dp (keep for section) | Low | No |

### Phase 2 — Button system

| # | File | Change | Risk | Identity-sensitive |
|---|------|--------|------|---------------------|
| 12 | `res/drawable/button_primary_material.xml` | **New** — simplified dark filled, single 2dp shadow, 8dp radius | Low | Yes — primary action feel |
| 13 | `res/drawable/button_secondary_material.xml` | **New** — medium grey, lower weight, 8dp radius | Low | Yes |
| 14 | `res/layout/fragment_trip_input.xml` | start_trip_button: button_3d_material → button_primary_material; elevation 8dp → 6dp | Medium | Yes |
| 15 | `res/layout/fragment_trip_input.xml` | statistics_button: button_3d_material → button_secondary_material; elevation 8dp → 4dp | Medium | Yes |
| 16 | `res/layout/fragment_trip_input.xml` | statistics_calendar_button: same secondary | Low | No |
| 17 | `res/layout/fragment_trip_input.xml` | pause_button: button_primary_material (same as Start when visible) | Low | No |

### Phase 3 — Input polish

| # | File | Change | Risk | Identity-sensitive |
|---|------|--------|------|---------------------|
| 18 | `res/drawable/input_background_adaptive.xml` | **New** — solid @color/card_background_adaptive, 8dp radius, no stroke | Low | No |
| 19 | `res/layout/fragment_trip_input.xml` | inputs: card_white_rounded → input_background_adaptive; padding 10dp → 12dp vertical | Low | No |

### Phase 4 — Toolbar refinement

| # | File | Change | Risk | Identity-sensitive |
|---|------|--------|------|---------------------|
| 20 | `res/drawable/toolbar_background_cracked_road.xml` | Add layer-list with dark overlay (e.g. 40% black) over bitmap | Medium | **Yes** — core identity |
| 21 | OR create `toolbar_background_cracked_road_soft.xml` | New drawable with overlay | Medium | Yes |

### Phase 5 — Secondary screens

| # | File | Change | Risk | Identity-sensitive |
|---|------|--------|------|---------------------|
| 22 | `res/layout/fragment_trip_history.xml` | Add background @drawable/background_page; style Filter/Export buttons | Low | No |
| 23 | `res/layout/dialog_trip_recovery.xml` | Use button_primary_material, button_secondary_material | Low | No |
| 24 | `res/layout/dialog_period_onboarding.xml` | Style confirm button | Low | No |
| 25 | `res/values/styles.xml` | SettingsHelpButton — consider secondary style | Low | No |

---

## Safe first pass scope (Part 2)

**Implement now:**
- Phase 1 items 1–8, 11 (background, spacing, typography, card padding)
- Phase 3 item 18–19 (input background — use card_background_adaptive, already adaptive)
- Create button_primary_material (simplified) and button_secondary_material
- Phase 2 items 14–17 (button swap) — **defer** if we want to test Phase 1 first

**Defer for approval:**
- Phase 4 (toolbar) — identity-sensitive
- Phase 5 (secondary screens) — can follow after Phase 1–3 validated

---

## Files to modify (safe pass)

1. `res/values/colors.xml` — add page_background
2. `res/values-night/colors.xml` — add page_background
3. `res/drawable/background_page.xml` — new
4. `res/layout/fragment_trip_input.xml` — background, padding, elevation, typography
5. `res/layout/fragment_trip_details.xml` — background, card padding
6. `res/drawable/button_primary_material.xml` — new (simplified primary)
7. `res/drawable/button_secondary_material.xml` — new (secondary)
8. `res/drawable/input_background_adaptive.xml` — new (for inputs)
9. `res/layout/fragment_trip_input.xml` — inputs use input_background_adaptive, button styles

---

## What remains for later pass

- Toolbar softening (Phase 4)
- Additional dialogs (dialog_trip_recovery, dialog_period_onboarding) — button styling
- Danger button tint for delete actions
- Focus/error state drawables for inputs (if not already wired in code)

---

## Implementation Summary (Safe Pass — Completed)

### Files changed

| File | Changes |
|------|---------|
| `res/values/colors.xml` | Added `page_background` (#F5F5F5) |
| `res/values-night/colors.xml` | Added `page_background` (#121212) |
| `res/drawable/background_page.xml` | **New** — flat adaptive background |
| `res/drawable/button_primary_material.xml` | **New** — simplified primary button |
| `res/drawable/button_secondary_material.xml` | **New** — secondary button |
| `res/drawable-night/button_primary_material.xml` | **New** — dark mode primary |
| `res/drawable-night/button_secondary_material.xml` | **New** — dark mode secondary |
| `res/drawable/input_background_adaptive.xml` | **New** — 8dp radius adaptive input |
| `res/layout/fragment_trip_input.xml` | Background, inputs, buttons, padding, typography |
| `res/layout/fragment_trip_details.xml` | Background, card padding |
| `res/layout/fragment_trip_history.xml` | Background |
| `res/values/styles.xml` | SettingsHelpButton → button_secondary_material |
| `TripInputFragment.kt` | End Trip dialogs: primary/secondary button hierarchy |
| `TripEndedOverlayService.kt` | Overlay button → button_primary_material |

### Visual improvements made

1. **Flat page background** — Replaced grey gradient with calm neutral (#F5F5F5 / #121212)
2. **Button hierarchy** — Primary (Start Trip, End Trip) vs secondary (Statistics, Change Period, Help & Info)
3. **Simplified primary button** — Single 2dp shadow, no layered 3D effect
4. **Input styling** — Adaptive background, 8dp radius, 12dp padding
5. **Spacing** — Today's Info card 16dp padding; section title 18sp
6. **Elevation** — Primary 6dp, secondary 4dp
