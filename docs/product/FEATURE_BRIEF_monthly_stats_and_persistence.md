# Feature brief: Monthly statistics and trip persistence

**Owner:** Project Design / Creative Manager  
**Created:** 2026-02-20  
**Related:** `docs/agents/MONTHLY_STATS_PERSISTENCE_CALENDAR_PLAN.md`, `docs/ux/END_TRIP_FLOW_UX.md`, `docs/ux/STATISTICS_SECTION_SPEC.md`.

---

## What counts, what doesn’t

- **End Trip** — Saves the trip to storage (Room). The trip **counts toward monthly statistics** and appears in trip history and calendar. User should use this when the trip is real and they want it recorded.
- **Clear Trip** — Does **not** save the trip. The trip **does not count** toward monthly stats and **does not appear** in history or calendar. Use for bad trips, test runs, or when the user wants to discard this trip.

---

## Monthly statistics

- One aggregate view: **current month** only (no weekly/yearly in UI per STATISTICS_SECTION_SPEC).
- Source: all **saved** (ended) trips in that month from Room.
- Calendar and history show the same saved trips; period picker and monthly stats use the same repository.

---

## Data persistence

- Completed trips: stored in Room (`trips` table).
- In-progress state: TripStatePersistence / TripStateManager (cleared on End or Clear trip).
- Offline: see `docs/technical/OFFLINE_PERSISTENCE.md` for future load/save of offline storage.

---

## User notes (in-app)

- End Trip dialog: explains that the trip will be saved and count in monthly stats.
- Clear Trip dialog: explains that the trip will not be saved or counted.
- Optional: short hint in Statistics section that monthly stats include only saved trips.

---

*Single source of truth for “what counts” and “what is stored.”*
