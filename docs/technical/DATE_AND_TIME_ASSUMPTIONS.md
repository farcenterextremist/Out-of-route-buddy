# Date and time assumptions

**Purpose:** Central place for date/time/timezone behavior and edge cases. Ref: Blind Spot Plan §10.

---

## Storage and period boundaries

- **Trips** are stored with **device timezone at save time** (e.g. timestamps in Room reflect the device clock when the trip was ended/saved). No server time; all period logic is device-local.
- **Period boundaries** (STANDARD = 1st–last of month, CUSTOM = Thursday before first Friday) are **computed from the device clock** at the time of the query or UI update. See PeriodCalculationService, UnifiedTripService, and KNOWN_TRUTHS (period date boundaries).
- **Timezone change** is handled by **TimeZoneChangeReceiver** (`android.intent.action.TIMEZONE_CHANGED`). The app can refresh or recompute period/calendar when the device timezone changes.

---

## Known limitations / edge cases

- **Trip spanning midnight (or month boundary):** A trip that starts in one day/month and ends in another is attributed by **end time** (or save time) for period/calendar purposes. Start time is stored for display and history; period stats use the same boundary logic as above.
- **User changes device date/time while app is open:** Period and calendar are recomputed when the UI refreshes (e.g. from preferences or navigation). No explicit "device date changed" handler; behavior is best-effort from current device time.
- **Device set to a timezone that changes "today" during a trip:** Not fully tested. TimeZoneChangeReceiver handles timezone change; in practice, reopening the app or refreshing period stats will use the new timezone.
- **Changing date during an active trip:** Not fully tested. If the user changes the device date mid-trip, trip end time will reflect the new clock at save time; start time remains as recorded. No special handling documented.

---

*See [KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md](../agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md) for period SSOT; [TimeZoneChangeReceiver](../../app/src/main/java/com/example/outofroutebuddy/services/TimeZoneChangeReceiver.kt).*
