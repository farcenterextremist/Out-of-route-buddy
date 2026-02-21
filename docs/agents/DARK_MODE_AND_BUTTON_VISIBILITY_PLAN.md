# Dark mode persistence, button visibility & calendar dark mode — Agent consultation plan

**Created:** 2026-02-20  
**Coordinator:** Master Branch Coordinator  
**Purpose:** Plan improvements for (1) dark mode state persistence, (2) button visibility and contrast in light/dark themes, and (3) dark mode for the calendar view, including edge cases and logical implementation.

---

## 1. Executive summary (Coordinator)

- **Dark mode persistence:** Theme can be set in two places: (a) Trip input Settings dialog (Mode row → Light/Dark) and (b) SettingsFragment (ListPreference). Only the latter persists via PreferenceFragment; the **trip dialog does not write to SharedPreferences**. Application applies theme at startup from `app_settings` / `theme_preference` (default `"light"`). **Gap:** Choosing Dark in the trip Settings dialog is lost on process death or restart. **Fix:** Persist theme when user changes it in the trip dialog (e.g. via SettingsManager.setThemePreference) and align default with SettingsManager (`"system"` vs `"light"`).
- **Button visibility:** Main trip buttons use `button_3d_material` (hardcoded dark gray/black). In light mode they are visible; in dark mode they can have low contrast. Statistics/outline buttons use Material attributes. **Improve:** Use theme-aware button backgrounds or ensure sufficient contrast in both themes; consider `?attr/colorPrimary` or night drawables for primary actions.
- **Calendar dark mode:** CustomCalendarDialog uses `dialog_background` (drawable with `@android:color/white` fill) and layout colors `text_primary_adaptive` / `text_secondary_adaptive`. The **dialog background is always white**; in dark mode the calendar pops as a white card. MaterialCalendarView may use library defaults that don’t follow app dark theme. **Fix:** Use a theme-aware dialog background (e.g. `?attr/colorSurface` or `@color/background_adaptive`) and ensure calendar title/hint/controls and date text respect dark theme (night drawable or styles).
- **Edge cases:** Apply theme on first launch (system/default); theme change mid-session (dialog open, calendar open); process death after theme change (persist before apply); calendar opened when theme is dark (dialog and calendar should be dark); Settings in toolbar vs future Settings screen (single source of truth for persistence).

---

## 2. Current state (exploration summary)

### Theme storage and application

| Location | What it does | Persists? |
|----------|----------------|-----------|
| **OutOfRouteApplication.applyThemePreference()** | Reads `app_settings` / `theme_preference` (default `"light"`), sets `AppCompatDelegate.setDefaultNightMode()`. Called in `onCreate()`. | N/A (reader) |
| **SettingsManager** | `getThemePreference()` default `"system"`; `setThemePreference(theme)`. Uses `app_settings`. | Yes, when caller invokes setThemePreference |
| **TripInputFragment.showModeSelectDialog()** | Sets `AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES/NO)` on radio selection; updates summary text. | **No** — does not call SettingsManager.setThemePreference |
| **SettingsFragment** | ListPreference `theme_preference`; on change calls setDefaultNightMode + saveCurrentTripData + activity.recreate(). ListPreference auto-saves value. | Yes (PreferenceFragment saves to same prefs) |

**Inconsistency:** Application default is `"light"`; SettingsManager default is `"system"`. Trip dialog does not persist; SettingsFragment does (when used).

### Button styling

- **fragment_trip_input.xml:** Start/End, Pause, Statistics use `android:background="@drawable/button_3d_material"` (and `app:backgroundTint="@null"`). `button_3d_material` is a selector with hardcoded grays (#666, #333, #000, shadows). Same drawable in light and dark.
- **Visibility logic:** Pause button `VISIBLE` when `state.isTripActive`, else `GONE`. Statistics section toggles visibility. Days-with-trips row visibility from `datesWithTripsInPeriod`. No theme-driven visibility change.
- **Other dialogs:** Material outline buttons (`?attr/materialButtonOutlinedStyle`) — these follow theme.

### Calendar dialog

- **dialog_custom_calendar.xml:** Root has `android:background="@drawable/dialog_background"`. Title and hint use `@color/text_primary_adaptive` and `@color/text_secondary_adaptive` (these have values-night variants). MaterialCalendarView has `app:mcv_dateTextAppearance="@style/CalendarDateTextAppearance"` — style not defined in app (library default or missing).
- **CustomCalendarDialog.onStart():** `dialog?.window?.setBackgroundDrawableResource(R.drawable.dialog_background)` — again forces drawable; drawable uses `@android:color/white` and `@color/primary_text`. So **dialog surface is always white**; stroke uses primary_text (adaptive in theory but drawable not in values-night).
- **values-night/colors.xml:** Defines text_primary_adaptive, background_adaptive, etc. Layout TextViews would use them, but the **window background drawable** is not theme-aware.

### Resources

- **values-night:** themes.xml (DayNight), colors.xml (dark palette, adaptive colors). No `drawable-night` or `dialog_background` night variant found.
- **drawable/dialog_background.xml:** Solid white, stroke primary_text — no night variant.

---

## 3. Design / Creative Manager

- **Intent:** One consistent theme (Light / Dark / System) across the app, including all dialogs (Settings, Mode select, Calendar, Help, History). Buttons should be clearly visible and on-brand in both themes. Calendar should not appear as a bright white box in dark mode.
- **Handoff:** UI/UX for contrast and accessibility; Front-end for persistence and drawables; Back-end for single source of truth (SettingsManager + Application).

---

## 4. UI/UX Specialist

- **Persistence:** User expects “Dark” chosen in Settings (from toolbar) to stay Dark after closing the app. Same for “Light” and “System”. Ensure one settings entry point persists and that both (trip dialog and any Settings screen) show the same current value.
- **Button visibility:** Primary actions (Start/End trip, Pause, Statistics) must meet contrast guidelines in both themes. If `button_3d_material` is dark-gray, in dark mode consider a lighter border or elevated surface so the button doesn’t blend into background. Option: theme-aware drawable (e.g. light surface + dark text in light mode, dark surface + light text in dark mode).
- **Calendar:** In dark mode, calendar dialog background and all text (title, hint, dates, buttons) should use dark surface and light text. Avoid white dialog surface in dark theme.
- **Edge cases:** (1) User opens Settings → changes to Dark → opens Calendar before closing Settings: calendar should already be dark (theme applied). (2) User opens Calendar in Light, then changes theme to Dark: calendar was already created; either recreate activity so calendar is re-shown in dark, or document that “change theme applies to next open”. (3) First launch: use system or a single default (e.g. “system”) so we don’t force light on a system-dark user.

---

## 5. Front-end Engineer

### 5.1 Dark mode persistence

- **Unify storage:** Use SettingsManager for all theme reads/writes. Application and all UI (trip dialog, SettingsFragment) must read/write the same key and defaults.
- **TripInputFragment.showModeSelectDialog():**
  - When user selects Light: call `SettingsManager.setThemePreference("light")`, then `AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)`, update summary, optionally recreate activity so all dialogs reflect theme.
  - When user selects Dark: call `SettingsManager.setThemePreference("dark")`, then `AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)`, update summary, optionally recreate.
  - If adding “System”: `setThemePreference("system")`, `setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)`.
- **Defaults:** Align Application and SettingsManager: either both default to `"system"` or both to `"light"`. Recommendation: default `"system"` so device theme is respected unless user overrides.
- **Settings dialog summary:** On open, read current theme from SettingsManager.getThemePreference() (or from AppCompatDelegate if using system) and set modeSummary text and Mode row summary so they match persisted value.

### 5.2 Button visibility

- **Option A (minimal):** Keep `button_3d_material` but add a `drawable-night` version (e.g. lighter fill or border) so in dark mode the button stands out from background. Ensure no `backgroundTint` override that would fight the drawable.
- **Option B (theme-aware):** Replace with theme attributes where possible, e.g. primary button style using `?attr/colorPrimary` and a shape drawable that references theme colors. MaterialButton with `backgroundTint="?attr/colorPrimary"` and no custom drawable for primary actions.
- **Document:** Which buttons are “primary” (Start/End, Confirm) vs “secondary” (Pause, Cancel, Outline). Ensure outline/secondary remain visible in both themes (Material handles this if using attributes).

### 5.3 Calendar dark mode

- **Dialog background:** Stop using a drawable that forces white. Use a theme-aware drawable or set window background to `?attr/colorSurface` or `@color/background_adaptive`. Options: (1) Create `drawable/dialog_background.xml` that uses `@color/card_background_adaptive` or `?attr/colorSurface` for fill and an adaptive stroke; (2) Or create `drawable-night/dialog_background.xml` with dark fill so the resource system picks it in night mode.
- **CustomCalendarDialog:** In onStart(), prefer setting window background from theme (e.g. `dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)` and let the root layout background handle it) or use a drawable that references `@color/background_adaptive` so it resolves to night variant when appropriate.
- **MaterialCalendarView:** If the library doesn’t respect DayNight, consider setting calendar background and date text color programmatically when in dark mode (e.g. read `UiModeManager` or current theme and set colors). Alternatively define `CalendarDateTextAppearance` in styles.xml and a night variant in values-night so date text is light in dark mode.
- **Title and hint:** Already use adaptive colors; verify in night that they are light text.

---

## 6. Back-end / Data

- **Single source of truth:** Theme preference is app settings, not “trip state”. SettingsManager (`app_settings`, `theme_preference`) is the only writer. Application reads on startup; UI (trip dialog, SettingsFragment) must call SettingsManager.setThemePreference when user changes theme.
- **No backend API:** Theme is local-only; no sync required.

---

## 7. DevOps

- **Build:** No CI change. If new drawables or values-night resources are added, ensure they are in the same module and that release build uses them.
- **Testing:** Consider a UI test or screenshot test that toggles theme and reopens app to confirm persisted theme.

---

## 8. QA Engineer

### Test cases

1. **Persistence**
   - Set theme to Dark in trip Settings dialog → kill app → reopen → theme should be Dark.
   - Set theme to Light → kill app → reopen → theme should be Light.
   - If “System” exists: set System → change device theme → app should follow.

2. **Default**
   - Fresh install → theme should be System (or Light per product decision); no crash.

3. **Button visibility**
   - Light mode: Start trip, Pause, Statistics, Calendar buttons clearly visible and readable.
   - Dark mode: Same buttons clearly visible; no “disappearing” or unreadable text.

4. **Calendar dark mode**
   - In Dark theme, open Calendar (Statistics → View) → dialog background and calendar should be dark (no large white panel); date text readable.
   - In Light theme, open Calendar → dialog and calendar light as today.

5. **Edge cases**
   - Open Calendar → change theme in Settings (without closing calendar) → behavior defined (recreate vs next open).
   - Set Dark in trip dialog → open Help & Info → Help should be dark (follows activity theme).

---

## 9. Security

- No sensitive data in theme preference. Stored in app-private SharedPreferences. No change.

---

## 10. File Organizer

- **Docs:** This plan at `docs/agents/DARK_MODE_AND_BUTTON_VISIBILITY_PLAN.md`. After implementation: update if new settings screens or theme options are added.
- **Related:** `docs/agents/CURRENT_WIRING_PLAN.md`, `docs/technical/GPS_AND_LOCATION_WIRING.md` (no direct link; theme is separate).

---

## 11. Human-in-the-Loop

- **Recommendation for user:** Implement persistence from the trip Settings dialog first (quick win). Then add theme-aware calendar dialog background and, if needed, button drawable for dark mode. Align default to “system” unless product explicitly wants “light” as default.

---

## 12. Implementation checklist (logical order)

| # | Task | Owner | Notes |
|---|------|--------|------|
| 1 | Persist theme in trip Settings dialog (SettingsManager.setThemePreference) | Front-end | **Done:** showModeSelectDialog calls settingsManager.setThemePreference("light"\|"dark") then setDefaultNightMode; activity.recreate() |
| 2 | Align default theme (Application + SettingsManager) | Front-end / Back-end | **Done:** Application default "system" (was "light"); MODE_NIGHT_FOLLOW_SYSTEM for system |
| 3 | Settings dialog: read current theme from SettingsManager on open; show in Mode summary | Front-end | **Done:** modeSummary from getThemePreference() → "Light" / "Dark" / "System" |
| 4 | Theme-aware dialog background (calendar and optionally others) | Front-end | **Done:** dialog_background.xml uses card_background_adaptive + divider_adaptive |
| 5 | Calendar dialog: ensure MaterialCalendarView / date text respect dark theme | Front-end | **Done:** CalendarDateTextAppearance already uses text_primary_adaptive (night in values-night); dialog surface now adaptive |
| 6 | Button visibility in dark mode (button_3d_material or primary style) | Front-end | **Done:** drawable-night/button_3d_material.xml with lighter gradient for dark theme |
| 7 | Optional: activity.recreate() after theme change in trip dialog | Front-end | **Done:** activity?.recreate() in showModeSelectDialog after theme change |
| 8 | QA: persistence, default, calendar dark, button contrast, edge cases | QA | Per test cases above |

---

## 13. Edge cases and scenarios

| Scenario | Desired behavior |
|----------|-------------------|
| User sets Dark in trip dialog only | Persist "dark"; on next launch Application applies dark. |
| User sets Light in SettingsFragment (if used) | Already persists; same key so Application applies light on next launch. |
| User never sets theme | Default (system or light) applied at first launch. |
| Theme changed while Calendar is open | Either recreate activity (calendar closes, re-open is dark) or leave as-is and document “theme applies to next open”. |
| Theme changed while Mode select dialog is open | Dialog is recreated when activity recreates; otherwise current dialog may still show old theme until dismissed. |
| System theme changes (when app uses "system") | App should follow on next resume or restart (AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM). |
| First launch after install | Apply default theme (no crash); no stale value. |

---

*When implementing, keep a single code path for “save theme” (SettingsManager) and one for “apply theme” (Application onCreate + UI when user changes).*
