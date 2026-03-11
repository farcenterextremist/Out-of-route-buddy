# Settings and services — single source of truth

**Purpose:** Map every Settings preference key to the component(s) that read it and when the value takes effect. Ref: Advanced Settings and Workflow Plan Phase 1.3 and 2.2.

---

## Settings keys and readers

| Key | UI label (category) | Default | Reader(s) | Takes effect when |
|-----|----------------------|---------|-----------|--------------------|
| `gps_update_frequency` | GPS Update Frequency (GPS Settings) | 10 | SettingsManager.getGpsUpdateFrequency() | Next trip start or when GPS service reads config; SettingsFragment.updateGpsFrequency() logs only |
| `high_accuracy_mode` | High Accuracy Mode (GPS Settings) | true | SettingsManager.isHighAccuracyMode() | Next trip start or when location service uses it |
| `gps_preset` | GPS mode (GPS Settings) | balanced | SettingsManager.getGpsPreset(), setGpsPreset(); applies gps_update_frequency + high_accuracy_mode | **Immediate** — on change, SettingsFragment calls setGpsPreset() which writes frequency and high_accuracy; effect next trip/service read |
| `distance_units` | Distance Units (Display) | miles | SettingsManager.getDistanceUnits(), isKilometers() | Next screen refresh; used for labels and conversion in UI |
| `theme_preference` | Theme (Display) | light | SettingsManager.getThemePreference(); OutOfRouteApplication.applyThemePreference() at startup; TripInputFragment (settings dialog) | **Immediate** — activity recreated on change in SettingsFragment |
| `notifications_enabled` | Enable Notifications (Notifications) | true | SettingsManager.areNotificationsEnabled() | Next notification; SettingsFragment.updateNotificationSettings() logs only |
| `notification_sound` | Notification Sound (Notifications) | false | SettingsManager.isNotificationSoundEnabled() | Next notification that can play sound |
| `auto_start_trip` | Auto-Start Trip (Trip Settings) | false | SettingsManager.isAutoStartTripEnabled() | Next app launch (checked when app opens) |
| `auto_save_trip` | Auto-Save Trips (Trip Settings) | true | SettingsManager.isAutoSaveTripEnabled() | When user ends trip (save vs prompt behavior if ever wired); persistence/End flow |
| `overlay_permission` | Trip-ended overlay (Trip Settings) | — | System; opens ACTION_MANAGE_OVERLAY_PERMISSION | N/A — opens system settings |
| `battery_optimization` | Battery Optimization (Advanced) | true | SettingsManager.isBatteryOptimizationEnabled(); BatteryOptimizationService.getRecommendedGpsInterval() | Next interval calculation (BatteryOptimizationService) |
| `clear_cache` | Clear Cache (Advanced) | — | Action only — SettingsFragment.clearAppCache() | **Immediate** — clears context.cacheDir contents (export/temp files); not Room or SharedPreferences |
| `about` | About (Advanced) | — | Action only — SettingsFragment.showAboutDialog() | N/A |
| `verbose_logging` | Verbose logging (Developer, debug only) | false | SettingsManager.isVerboseLoggingEnabled(); app can elevate log level when true (no PII) | Next log call; gated by BuildConfig.DEBUG in UI |
| `export_app_logs` | Export app logs (Developer, debug only) | — | Action only — SettingsFragment.exportAppLogsIfAvailable(); shares .log file from cache if present | N/A; Developer category removed when !BuildConfig.DEBUG |
| `delete_old_data_from_device` | Delete old data from device (Data & privacy) | — | DataManagementViewModel.deleteOldDataFromDevice(12) | On confirm — local delete only |
| `clear_all_data_from_device` | Clear all trip data from device (Data & privacy) | — | DataManagementViewModel.clearAllDataFromDevice() | On confirm — local delete only |

---

## Settings propagation

- **Flow:** `preferences.xml` (PreferenceScreen) → user taps/changes → **PreferenceFragmentCompat** (SettingsFragment) writes to **SharedPreferences** `app_settings` (same file as SettingsManager). **SettingsManager** reads from `app_settings`; ViewModels and services receive settings via **SettingsManager** (injected), not by reading SharedPreferences directly.
- **Rule:** Fragments do not write to SharedPreferences except through the preference framework (which writes to `app_settings`). New settings should add a key to SettingsManager and to preferences.xml; listeners in SettingsFragment update any immediate UI (e.g. theme → recreate). Align with board-meeting note: new settings follow SettingsManager pattern.

---

## What “Clear cache” clears

- **Cleared:** Contents of `context.cacheDir` (export files, e.g. TripExporter CSV/report; temporary files). Subdirectories and files are deleted recursively.
- **Not cleared:** Room database, SharedPreferences (`app_settings`, `trip_persistence`, etc.), or any data in app internal storage outside cache.

---

*Ref: [KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md](../agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md), [SettingsManager](../../app/src/main/java/com/example/outofroutebuddy/data/SettingsManager.kt), [SettingsFragment](../../app/src/main/java/com/example/outofroutebuddy/presentation/ui/settings/SettingsFragment.kt).*
