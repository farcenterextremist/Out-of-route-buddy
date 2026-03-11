# Feature Brief: Stat Cards in Calendar History

**Owner:** Product / Design  
**Created:** 2026-02-27  
**Status:** Planning prompt — use to build implementation plan  
**Related:** [CustomCalendarDialog](../../app/src/main/java/com/example/outofroutebuddy/presentation/ui/dialogs/CustomCalendarDialog.kt), [TripHistoryByDateDialog](../../app/src/main/java/com/example/outofroutebuddy/presentation/ui/dialogs/TripHistoryByDateDialog.kt), [TripHistoryAdapter](../../app/src/main/java/com/example/outofroutebuddy/presentation/ui/history/TripHistoryAdapter.kt)

---

## The Prompt

Copy the prompt below and use it with an AI assistant or planning session to generate a detailed implementation plan.

---

```
You are a technical product planner and UX designer. I need a detailed implementation plan for building out "Stat Cards" in the calendar history flow for OutOfRouteBuddy.

## Context

OutOfRouteBuddy is an Android app (Kotlin, MVVM, Hilt, Room) for tracking out-of-route miles. The app has:

1. **CustomCalendarDialog** — A calendar picker for selecting period boundaries (STANDARD/CUSTOM) and viewing trip history. It uses MaterialCalendarView with:
   - Green circle = period start date
   - Red circle = period end date
   - Grey circle = today (when not a boundary)
   - **DaysWithTripsDecorator** — Currently shows a small **blue dot** on dates that have at least one saved trip

2. **TripHistoryByDateDialog** — Opens when user clicks a non-boundary date. Shows a list of trips for that date via TripHistoryAdapter.

3. **TripHistoryAdapter** — Displays trips as clickable cards with: date, total miles, OOR (over/under). Click navigates to TripDetailsFragment.

4. **Data model:**
   - Trip (domain): id, loadedMiles, bounceMiles, actualMiles, oorMiles, oorPercentage, startTime, endTime, gpsMetadata
   - TripEntity (Room): date, tripStartTime, tripEndTime, plus GPS metadata (avgGpsAccuracy, totalGpsPoints, validGpsPoints, tripDurationMinutes, avgSpeedMph, maxSpeedMph, locationJumpsDetected, etc.)
   - Trips are queried by `date` field (single date); tripStartTime/tripEndTime exist but are not used for date-range queries

## Feature Requirements

### 1. Calendar Visual Indicator
- Change the indicator for dates with trips from the current **blue dot** to a **translucent yellowish circle** (per user spec)
- Ensure the circle is visible on both light and dark themes
- Dates with trips remain clickable; clicking opens the trips list

### 2. Trips List (inside TripHistoryByDateDialog)
- When user clicks a date, show a list of trips for that day
- Each trip is a **clickable container** (Stat Card) that displays:
   - **Summary view (collapsed/default):** Key metrics at a glance (date/time range, miles, OOR)
   - **Full metadata view (expanded/on click):** All data and metadata for that trip

### 3. Stat Card Content
Define what "all data and metadata" means. Current fields available:
- **Core:** loadedMiles, bounceMiles, actualMiles, oorMiles, oorPercentage, dispatchedMiles
- **Time:** startTime, endTime, durationMinutes
- **GPS metadata:** avgGpsAccuracy, totalGpsPoints, validGpsPoints, rejectedGpsPoints, tripDurationMinutes, avgSpeedMph, maxSpeedMph, locationJumpsDetected, accuracyWarnings, speedAnomalies
- **State:** wasInterrupted, interruptionCount

### 4. Edge Case: Trips Spanning Midnight
- **Scenario:** User drives from 11:00 PM Feb 28 to 1:00 AM Mar 1. The trip spans two calendar days.
- **Requirement:** The trip must be displayed on **both** Feb 28 and Mar 1 when the user clicks either date.
- **Current behavior:** Trips are stored with a single `date` field (typically start date). Query uses `date >= startOfDay AND date <= endOfDay` for a single day. A midnight-spanning trip would only appear on one day.
- **Required behavior:** 
  - When computing "dates with trips" for calendar decoration: include a day if ANY trip has that day within its [startTime, endTime] range
  - When loading trips for a clicked date: show trips where the selected date falls within [startTime, endTime] (overlap logic)

## Clarifying Questions (Answer Before Implementation)

Ask the product owner or use reasonable defaults:

1. **Stat Card interaction:** Should the Stat Card expand inline (accordion) or navigate to TripDetailsFragment? Current flow: click → TripDetailsFragment. Do we keep that, or add an expandable card that shows metadata without leaving the dialog?

2. **Metadata depth:** Which metadata fields should be shown in the expanded Stat Card? All GPS fields, or a curated subset (e.g., duration, avg accuracy, speed, quality %)?

3. **Yellowish circle spec:** Exact color (hex), opacity, size? Should it replace the dot entirely or sit alongside the date number? Any accessibility contrast requirements?

4. **Empty state:** When user clicks a date that has no trips (e.g., misclick, or date was marked due to midnight-spanning logic but no trips "start" that day): show "No trips for this date" or different messaging?

5. **Multiple trips per day:** If there are 3 trips on one day, should they be ordered by start time, end time, or creation order? Any grouping (e.g., "Trip 1 of 3")?

6. **Midnight-spanning display:** When a trip appears on both days, should the card indicate "Started Feb 28, ended Mar 1" or similar to avoid confusion?

7. **Backward compatibility:** Older trips may have null tripStartTime/tripEndTime. Fallback to `date` for those? How should we handle trips with only `date`?

8. **Performance:** For datesWithTripsInPeriod, we currently use start-of-day of trip dates. With midnight-spanning logic, we need to iterate trips and add all days in [start, end]. Is that acceptable, or do we need a DB index/query optimization?

## Implementation Phases (Suggested)

1. **Phase 1 — Data layer:** Add/use tripStartTime and tripEndTime for overlap queries. Update getTripsByDateRange (or add getTripsOverlappingDay) and datesWithTrips logic. Handle null fallback.

2. **Phase 2 — Calendar decorator:** Change DaysWithTripsDecorator from blue dot to translucent yellowish circle. Verify visibility.

3. **Phase 3 — Stat Card UI:** Design and implement the clickable container with summary + expandable metadata. Define layout and fields.

4. **Phase 4 — Edge cases:** Midnight-spanning display, empty states, ordering, backward compatibility.

5. **Phase 5 — Tests:** Unit tests for overlap logic, integration tests for calendar → dialog flow.

## Constraints

- Do NOT make unwarranted UI changes without product owner approval (per project rules)
- Preserve existing navigation (TripDetailsFragment) unless explicitly changing the flow
- Ensure dark theme compatibility
- Follow existing patterns (MaterialCardView, ConstraintLayout, ViewModel/StateFlow)
```

---

## Current Code References

| Component | Path |
|-----------|------|
| CustomCalendarDialog | `app/.../dialogs/CustomCalendarDialog.kt` |
| DaysWithTripsDecorator | Same file, line ~401 |
| TripHistoryByDateDialog | `app/.../dialogs/TripHistoryByDateDialog.kt` |
| TripHistoryByDateViewModel | `app/.../history/TripHistoryByDateViewModel.kt` |
| TripHistoryAdapter | `app/.../history/TripHistoryAdapter.kt` |
| item_trip_history.xml | `app/.../res/layout/item_trip_history.xml` |
| TripEntity | `app/.../data/entities/TripEntity.kt` |
| Trip (domain) | `app/.../domain/models/Trip.kt` |
| TripDao.getTripsForDateRange | `app/.../data/dao/TripDao.kt` |
| DomainTripRepositoryAdapter | `app/.../data/repository/DomainTripRepositoryAdapter.kt` |

---

## Notes

- The `datesWithTripsInPeriod` is computed in TripInputViewModel.refreshStatisticsAfterSave and passed to CustomCalendarDialog via TripInputFragment.
- TripEntity.date is set from trip.startTime when saving (DomainTripRepositoryAdapter).
- TripEntity has tripStartTime and tripEndTime; these are populated by TripTrackingService when a trip ends.
