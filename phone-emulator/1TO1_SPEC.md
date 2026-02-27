# Emulator 1:1 specification

Defines how the emulator matches the real OutOfRouteBuddy app. See also [EMULATOR_PERFECTION_PLAN.md](EMULATOR_PERFECTION_PLAN.md) and [cursor-exporter.js](cursor-exporter.js) for the design-state-to-strings mapping.

---

## 1. Screens in scope

| Screen | Status | Notes |
|--------|--------|--------|
| **Trip screen (main)** | In scope | Toolbar, loaded/bounce miles, Start/End Trip, Pause, Today's Info, Statistics. |
| **Settings** | Partial | Modal (Mode, Templates, Help & Info) in emulator; full app may have more. |
| **Trip History** | Future | Not yet in emulator. |
| **Other fragments** | Future | Add to this table when represented. |

---

## 2. Source of spacing and colors

- **Toolbar (1:1):** Height 80dp → 80px; start/end margin 16dp → 16px. Matches `custom_toolbar.xml` and `fragment_trip_input.xml` (include overrides layout to 80dp).
- **Card:** Padding 10dp vertical and horizontal (Today's Info). Matches layout padding.
- **Inputs:** numberDecimal / digits 0-9 and .; no stroke on light (card_white_rounded); elevation 2dp → box-shadow in CSS.
- **Statistics:** Divider uses `light_gray_background` (#F5F5F5 light, #2C2C2C dark). Row labels secondary text color, values primary.
- **Dark mode:** Card #2C2C2C; appbar gradient #1E1E1E → #121212; toolbar #1E1E1E.
- **Colors:** Emulator uses CSS variables in [styles.css](styles.css) under `[data-theme="light"]` and `[data-theme="dark"]`, aligned with app `values/colors.xml` and `values-night/colors.xml`.
- **Icons:** SVG in [app-renderer.js](app-renderer.js): gear (ic_settings), chevron down/up (ic_arrow_down/up, right of text when collapsed/expanded), pause/play (ic_pause, ic_play). Pause button 48dp, right of Start when trip active.

---

## 3. String-key parity

Every string name in the sync mapping (see [cursor-exporter.js](cursor-exporter.js) `EMULATOR_TO_PROJECT`) must have an editable element in the emulator.

| stringName (strings.xml) | Design path | Editable in emulator |
|--------------------------|-------------|----------------------|
| oor | toolbar.title | Yes (toolbar title) |
| loaded_miles | loadedMiles.hint | Yes (input placeholder) |
| bounce_miles | bounceMiles.hint | Yes (input placeholder) |
| start_trip | startButton.text | Yes (Start Trip button) |
| todays_info | todaysInfo.title | Yes (Today's Info title) |
| total_miles | totalMiles.label | Yes (card row label) |
| oor_miles | oorMiles.label | Yes |
| oor_percent | oorPercent.label | Yes |
| statistics | statisticsButton.text | Yes (Statistics button) |
| statistics_period_label | statisticsPeriod.label | Yes (period label) |
| statistics_change_period_button | statisticsPeriod.button | Yes (View button) |
| statistics_period_value | statisticsPeriod.value | Yes (period value) |
| weekly_statistics | weeklyStats.title | Yes |
| monthly_statistics | monthlyStats.title | Yes |
| yearly_statistics | yearlyStats.title | Yes |

---

## 4. Load from project / Sync to project

- **Sync to project:** Emulator sends current design state to the sync service (POST /sync); the service writes `app/src/main/res/values/strings.xml`. See [scripts/emulator-sync-service/README.md](../scripts/emulator-sync-service/README.md).
- **Load from project:** Emulator can GET current strings from the sync service (GET /design or /strings); the response is merged into design state so the emulator shows the app’s current copy. Use the "Load from project" button in the toolbar.

---

## 5. Icon source

Icons in the emulator are inline SVGs in app-renderer.js, chosen to match the app’s VectorDrawable/ic_* drawables (gear for settings, chevron for stats expand/collapse, pause/play for trip). No automatic conversion; if the app’s drawables change, update the SVGs here for 1:1.

## 6. Security (no secrets)

Design state and sync payloads contain only UI strings (labels, titles, placeholders). They must **not** contain credentials, API keys, or other secrets. The sync service reads/writes only `strings.xml`; no secrets are stored or transmitted. If you add new design fields, keep them limited to user-visible copy.
