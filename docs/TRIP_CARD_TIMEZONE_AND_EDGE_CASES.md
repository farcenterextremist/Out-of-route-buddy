# Trip card: timezone and edge cases

## Timezone display

- **Stored**: When a trip is ended, the app stores the device’s timezone at that moment (IANA id, e.g. `America/Chicago`) in `tripTimeZoneId`.
- **Display**: Trip start/end times are shown in that trip’s timezone so they match “when it happened” there.
- **When we show the label**: The timezone abbreviation (e.g. **CST**, **EST**) is shown on the card **only when the trip’s timezone is different from the user’s current timezone**. If the user is already in that timezone, we do **not** show it (e.g. no “EST” when you’re in Eastern).

Examples:

- User in Eastern, trip was in Central → subtitle like `2:00 PM - 5:00 PM CST`.
- User in Central, trip was in Central → subtitle `2:00 PM - 5:00 PM` (no CST).

## Edge cases

- **Legacy trips** (no `tripTimeZoneId`): Times are shown in the device’s current default timezone; no timezone abbreviation is shown.
- **Same zone**: Trip timezone equals current default → times in that zone, no abbreviation.
- **Midnight-spanning / partial trips**: Date range and timezone logic still apply; partial trip line can show e.g. `Partial Trip Mar 2–Mar 3 CST` when in a different zone.
- **Update path**: Trips updated via the data-layer `updateTrip` that don’t pass `tripTimeZoneId` keep existing behavior (timezone can be lost on that path; new saves always set it).

## Data flow

- **Save**: `TripStatePersistence.saveCompletedTrip` sets `gpsMetadata["tripTimeZoneId"] = TimeZone.getDefault().id` → `TripRepository.insertTrip` → `TripEntity.tripTimeZoneId`.
- **Load**: `DomainTripRepositoryAdapter.mapTripEntityToDomain` maps `entity.tripTimeZoneId` → `Trip.timeZoneId`.
- **UI**: `TripHistoryStatCardAdapter` uses `trip.timeZoneId` for formatting and only appends the short display name when `trip.timeZoneId != TimeZone.getDefault().id`.
