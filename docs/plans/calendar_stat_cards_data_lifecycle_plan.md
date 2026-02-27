# Plan: Calendar Range, Extensible Stat Cards, and Local-Delete / Server-Retention Path

## Overview

1. **Calendar:** Allow users to view 1 year prior and 1 year into the future in the calendar dialog (Current Period → View).
2. **Stat cards:** Make the statistics rows (Total Miles, OOR Miles, OOR %) highly versatile and editable so more data can be added later.
3. **Data lifecycle:** Provide a path for old/residual data to be deleted on the user’s device while optionally being kept on the server for data training sets.

---

## 1. Calendar: 1 year prior and 1 year future

### Current behavior

- [CustomCalendarDialog](app/src/main/java/com/example/outofroutebuddy/presentation/ui/dialogs/CustomCalendarDialog.kt) sets the calendar’s min/max from `calculatePeriodBoundaries(referenceDate)`, so only the **current period** (one month for STANDARD, or one custom period) is visible. Users cannot scroll to other months.

### Changes

- **Min date:** First day of the month that is **1 year before** the month of `referenceDate` (or 1 year before period start for CUSTOM).
- **Max date:** Last day of the month that is **1 year after** the month of `referenceDate` (or 1 year after period end for CUSTOM).
- **Reference:** Use `referenceDate` (or today when opening from Current Period) and compute:
  - `minDate` = same day/month, year - 1 (then adjust to first day of that month if desired for consistency).
  - `maxDate` = same day/month, year + 1 (then adjust to last day of that month).
- **Decorators:** Keep existing behavior:
  - **Period boundaries** (green start, red end): still apply to the period that was used to open the dialog (e.g. current month). When the user navigates to another month, the library will show other months; boundary decorators only apply to dates that fall in the original period (no change to decorator logic).
  - **Days with trips:** Continue to pass `datesWithTrips` from the ViewModel; these can span the full ±1 year range if the ViewModel provides them (see below).
- **ViewModel / data:** Ensure `datesWithTripsInPeriod` (or the list passed to the calendar) can cover the extended range when loading trips for “all months in range” so that dots on the calendar appear for any month the user navigates to. Today this may be scoped to the current period; extend the query to trips from (minDate) to (maxDate) when opening the calendar so all visible months can show trip dots. Implement in [TripInputViewModel](app/src/main/java/com/example/outofroutebuddy/presentation/viewmodel/TripInputViewModel.kt) (e.g. when building args for the calendar, request trips for ±1 year and pass those dates to the dialog).

### Files to touch

- [CustomCalendarDialog.kt](app/src/main/java/com/example/outofroutebuddy/presentation/ui/dialogs/CustomCalendarDialog.kt): In `setupCalendar()`, compute `minDate` and `maxDate` as ±1 year from `referenceDate` (by month), set them on the calendar state; keep period boundary and “days with trips” decorators as-is.
- [TripInputFragment.kt](app/src/main/java/com/example/outofroutebuddy/presentation/ui/trip/TripInputFragment.kt): When calling `CustomCalendarDialog.newInstance(...)`, pass a `datesWithTrips` list that covers the full ±1 year range (e.g. from a new ViewModel method that returns trips for that range).
- [TripInputViewModel.kt](app/src/main/java/com/example/outofroutebuddy/presentation/viewmodel/TripInputViewModel.kt): Add a method such as `getDatesWithTripsForCalendarRange(minDate: Date, maxDate: Date)` or expose a flow/state that the fragment can use to get trip dates for the calendar’s min/max so the dialog receives the correct list.

---

## 2. Stat cards: versatile and editable for more data

### Current behavior

- [SummaryStatistics](app/src/main/java/com/example/outofroutebuddy/presentation/viewmodel/TripInputViewModel.kt) is a fixed data class: `totalTrips`, `totalMiles`, `oorMiles`, `oorPercentage`.
- [statistics_row.xml](app/src/main/res/layout/statistics_row.xml) has three fixed rows: Total Miles, OOR Miles, OOR %.
- [TripInputFragment.updateStatisticsRow()](app/src/main/java/com/example/outofroutebuddy/presentation/ui/trip/TripInputFragment.kt) binds only those three values.

### Changes

- **Extensible data model:**
  - Add to `SummaryStatistics` an optional field, e.g. `extraFields: Map<String, String> = emptyMap()`, so new metrics (e.g. average trip length, fuel cost, custom KPI) can be added without changing the core fields. Keep existing constructor and `totalTrips`, `totalMiles`, `oorMiles`, `oorPercentage` for backward compatibility.
  - Where `SummaryStatistics` is built (e.g. `mapToSummary`, `mapPeriodToSummary`), pass through `extraFields` if/when you add new metrics from `TripStatistics` or elsewhere.

- **Extensible layout:**
  - In [statistics_row.xml](app/src/main/res/layout/statistics_row.xml), add a **container** (e.g. a vertical `LinearLayout` with `id="@+id/stats_extra_rows_container"`) after the three existing rows. This container is initially empty; at runtime the fragment (or a small helper) will inflate **extra rows** (label + value) for each entry in `extraFields`.
  - Use a single reusable row layout (e.g. `statistics_extra_row.xml`) with two TextViews (label, value) and the same styling as existing rows (padding, `text_secondary_adaptive` / `text_primary_adaptive`), and inflate one per extra field.

- **Binding logic:**
  - In `updateStatisticsRow()`, after setting the three main stats, clear `stats_extra_rows_container` and, for each entry in `stats.extraFields`, inflate one row, set label and value, and add it to the container. If `extraFields` is empty, the container stays empty.

- **Documentation:**
  - Add a short comment in `statistics_row.xml` and in `TripInputViewModel.SummaryStatistics` explaining that new metrics should be added via `extraFields` and the extra-rows container, so stat cards remain versatile and editable without changing the core layout each time.

### Files to touch

- [TripInputViewModel.kt](app/src/main/java/com/example/outofroutebuddy/presentation/viewmodel/TripInputViewModel.kt): Add `extraFields` to `SummaryStatistics`; in mappers, optionally populate it from domain/DB when new metrics exist.
- [statistics_row.xml](app/src/main/res/layout/statistics_row.xml): Add `stats_extra_rows_container` (e.g. `LinearLayout`).
- New layout: [statistics_extra_row.xml](app/src/main/res/layout/statistics_extra_row.xml) (optional single row for label + value).
- [TripInputFragment.kt](app/src/main/java/com/example/outofroutebuddy/presentation/ui/trip/TripInputFragment.kt): In `updateStatisticsRow()`, bind `extraFields` to the container by inflating and adding rows.

---

## 3. Data lifecycle: delete on device, keep on server for training

### Current behavior

- Trips are stored only in Room (local). There is no server sync.
- User can delete a single trip (TripHistoryViewModel.deleteTrip) or clear all trips (TripRepository.clearAllTrips); both only remove data locally.

### Goal

- Support a **path** where the user can delete data **on the device** (free space, privacy) while the app (or a future backend) **keeps a copy on the server** for analytics/training. No server implementation in this plan; only the app-side contract and UI path.

### Changes

- **Interface: “export/sync before local delete”**
  - Define an interface, e.g. `TripArchiveService` or `TripSyncBeforeDelete`, in the app (e.g. under `domain` or `data`):
    - Method such as `suspend fun exportBeforeLocalDelete(trips: List<Trip>): Result<Unit>` (or `uploadForTraining(trips)`). Implementation can be a **no-op** that logs and returns success, or a stub that writes to a file for manual upload. Later, replace with a real API call that sends data to your backend.
  - Inject this into the ViewModel or use case that performs “delete from device (keep on server)”. When the user triggers “Delete from device”:
    1. Call `exportBeforeLocalDelete(trips)` (or upload selected/old trips).
    2. On success, call existing `deleteTrip`/`clearAllTrips` (or a new method that deletes by date range / id list).
    3. On failure, show an error and do not delete locally (or offer “delete anyway” with a warning).

- **UI path**
  - **Option A – Settings / Data Management:** Add a section such as “Data & privacy” or “Storage” with:
    - “Delete old data from device” (e.g. delete trips older than 12 months): runs export (if implemented) then deletes local trips in that range.
    - “Clear all trip data from device”: runs export then `clearAllTrips()`. Copy can say “Data may be retained on the server for product improvement and training.”
  - **Option B – In Trip History:** Add a “Delete from device (keep on server)” action that exports selected (or all) trips then deletes them locally.

- **Residual/old data**
  - “Old and residual data” can be defined as “trips older than X months” or “all trips.” Expose a date range or “all” in the UI; the same `exportBeforeLocalDelete` + local delete flow applies. Server-side retention and use for “data training sets” is a policy/backend concern; the app only needs to send the data before deleting locally.

- **Documentation**
  - In code: KDoc on the interface and the no-op implementation stating that the server side is responsible for retaining and using data for training; the app only guarantees “upload then delete locally” when the user chooses that path.
  - Optional: a short `docs/data_lifecycle.md` describing the flow (user deletes on device → app exports to server if configured → local delete; server keeps data for training).

### Files to touch

- New: `TripArchiveService` (or `TripSyncBeforeDelete`) interface and a no-op/default implementation (e.g. `DefaultTripArchiveService` that logs and returns success).
- [TripInputViewModel](app/src/main/java/com/example/outofroutebuddy/presentation/viewmodel/TripInputViewModel.kt) or a new use case / repository layer: call archive service before local delete when user chooses “delete from device (keep on server).”
- [TripRepository](app/src/main/java/com/example/outofroutebuddy/domain/repository/TripRepository.kt): Optional new method `deleteTripsOlderThan(date: Date)` or `deleteTripsByIds(ids: List<String>)` if you want to delete by range or selection without clearing everything.
- Settings or Data Management UI: add “Delete old data from device” and/or “Clear all from device (keep on server)” that use the above flow.
- Optional: `docs/data_lifecycle.md` summarizing the design.

---

## Implementation order

| Step | Task |
|------|------|
| 1 | Calendar: extend CustomCalendarDialog min/max to ±1 year; ensure datesWithTrips covers that range when opening the dialog. |
| 2 | Stat cards: add `extraFields` to SummaryStatistics, add extra-rows container and optional statistics_extra_row.xml, and bind in updateStatisticsRow. |
| 3 | Data lifecycle: add TripArchiveService (no-op impl), repository method(s) for delete-by-range or delete-by-ids if needed, and Settings/Data Management UI for “delete from device (keep on server)” with documentation. |

---

## Verification

- **Calendar:** Open Current Period → View, confirm you can navigate to months up to 1 year back and 1 year forward; period boundaries and trip dots behave as before in the current period.
- **Stat cards:** Add a test extra field via `extraFields` and confirm it appears in the statistics section; confirm existing Total Miles / OOR Miles / OOR % unchanged.
- **Data lifecycle:** Trigger “Delete from device (keep on server)” (or “Delete old data”); confirm archive is called (log or stub) and local data is removed; document that server retention is for training.
