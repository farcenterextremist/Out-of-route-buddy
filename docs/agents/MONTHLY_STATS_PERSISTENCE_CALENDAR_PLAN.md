# Monthly Statistics, Persistence, Calendar & End/Clear Trip — Full Team Consultation Plan

**Created:** 2026-02-20  
**Coordinator:** Master Branch Coordinator  
**Purpose:** Single plan from all agents for wiring monthly statistics, data persistence, calendar storage, user-facing notes (End trip vs Clear trip), metadata storage, and security. Execute over ~5 hours of work; email user when finished.

---

## 1. Executive summary (Coordinator)

- **Monthly statistics:** Already wired (ViewModel `refreshAggregateStatistics()` → `getMonthlyTripStatistics()`; fragment binds `monthlyStatistics` to `monthlyStats`). Plan: verify flow, ensure **End trip** triggers save and immediate refresh of monthly stats; add explicit user-facing copy so users know "End trip" saves and counts toward monthly stats.
- **Persistence & storage:** Trips are persisted via Room and trip-state persistence. Plan: confirm End trip writes completed trip to Room; Clear trip does not. Document and optionally harden OfflineDataManager for full offline persistence (see CRUCIAL #2).
- **Calendar:** CustomCalendarDialog and period selection exist; history-by-date is available. Plan: ensure completed trips appear in calendar/history and are included in monthly aggregates; add notes so user understands calendar reflects saved trips only.
- **User notes:** Add in-app and doc notes: **End trip** = save, store, count in monthly stats; **Clear trip** = discard, do not save or count.
- **Metadata & security:** Document and implement recommendations from Security and Back-end (metadata schema, encryption-at-rest options, PII handling).

---

## 2. Design / Creative Manager

- **Product intent:** Monthly stats are the single source of truth for "how did I do this month?" Only **ended** (saved) trips should count. Clear trip is an escape hatch for bad or test trips.
- **Priorities:** (1) User clarity: End trip = save & count; Clear trip = discard. (2) Calendar and history show only saved trips. (3) One clear monthly view; no weekly/yearly in UI (per STATISTICS_SECTION_SPEC).
- **Handoff:** UI/UX for copy and flows; Back-end for persistence contract.

---

## 3. UI/UX Specialist

- **End trip vs Clear trip (already in place):** Dialog order is correct: End Trip (primary), Clear Trip (secondary), Continue Trip (dismiss). See `docs/ux/END_TRIP_FLOW_UX.md`.
- **User notes to add:**
  - **In the End Trip confirmation dialog:** Add one line: "End Trip will **save** this trip and **count it in your monthly statistics**."
  - **In the Clear Trip confirmation dialog:** Add: "This trip will **not** be saved and will **not** count toward monthly stats."
  - **Statistics section:** Optional short hint: "Monthly stats include all trips you ended (saved). Cleared trips are not included."
- **Accessibility:** Ensure new copy has contentDescription/accessibilityLabel where applicable.
- **Handoff:** Front-end to implement string and layout changes; QA to verify.

---

## 4. Front-end Engineer

- **Monthly statistics wiring (current):** `TripInputFragment` observes `state.monthlyStatistics` and calls `updateStatisticsRow(binding.monthlyStats, state.monthlyStatistics)`. ViewModel loads via `refreshAggregateStatistics()` → `tripRepository.getMonthlyTripStatistics()`. On **End trip**, ViewModel should call a refresh of aggregate statistics after persisting the trip (confirm `endTrip()` triggers repository insert + then `refreshAggregateStatistics()` or equivalent).
- **Tasks:**
  1. Add string resources for the new user notes (End trip saves and counts; Clear trip does not).
  2. Update `showEndTripConfirmation()` message to include: "End Trip will save this trip and count it in your monthly statistics."
  3. Update `showClearTripConfirmation()` message to: "This trip will not be saved and will not count toward monthly stats. Use this only for bad or test trips."
  4. Optionally add a short hint in the Statistics expandable section (e.g. "Includes trips you ended (saved). Cleared trips are not included.").
  5. Ensure after `viewModel.endTrip()` the fragment/ViewModel refreshes monthly stats (ViewModel should already do this if endTrip() inserts and then refreshes; verify and add refresh call if missing).
- **Calendar:** No structural change; ensure CustomCalendarDialog and history-by-date use the same repository so saved trips appear. If period picker is used for "current month," monthly stats and calendar stay in sync.

---

## 5. Back-end Engineer

- **Persistence today:** Completed trips are stored in Room (`TripEntity` / `trips` table). Trip state (in-progress, recovery) is in TripStatePersistence / TripStateManager. End trip flow should: (1) persist completed trip to Room, (2) clear in-progress state. Clear trip should: (1) clear in-progress state and any in-memory draft, (2) **not** insert a row into `trips`.
- **Tasks:** *(Implemented; see `docs/technical/TRIP_PERSISTENCE_END_CLEAR.md`.)*
  1. Confirm `endTrip()` path results in a single insert of the completed trip into Room and that `getMonthlyTripStatistics()` and `getTripsByDateRange()` include that row. **Done:** Doc confirms ViewModel → insertTrip → Room; refreshAggregateStatistics() after save.
  2. Confirm `clearTrip()` does not write a completed trip to Room; only clears state. **Done:** Doc confirms no insert; state and persistence cleared only.
  3. **Metadata:** Document or extend trip entity metadata (e.g. trip type, notes, device info) in a structured way. **Done:** TRIP_PERSISTENCE_END_CLEAR.md documents TripEntity structured fields; SECURITY_NOTES.md adds trip-metadata recommendation.
  4. **OfflineDataManager:** Documented in `docs/technical/OFFLINE_PERSISTENCE.md`; TRIP_PERSISTENCE_END_CLEAR.md references it for future load/save when offline persistence across restart is in scope.
- **Calendar:** `getTripsByDateRange()` and trip history by date read from the same Room source; no separate "calendar" store. See TRIP_PERSISTENCE_END_CLEAR.md.

---

## 6. DevOps Engineer

- **Build & deploy:** No change required for this feature set. If Back-end adds new migrations for metadata, ensure schema version bump and migration path.
- **Observability:** Optional: add a simple metric or log (non-PII) when a trip is saved (End trip) vs cleared (Clear trip) for aggregate quality metrics. Do not log PII (coordinates, user identifiers).

---

## 7. QA Engineer

- **Test plan:**
  1. **End trip:** Start trip → End trip → confirm one row in DB for that trip; monthly stats update to include it; calendar/history shows the trip for that date.
  2. **Clear trip:** Start trip → Clear trip → confirm no new row in DB; monthly stats unchanged; calendar/history does not show a trip for that session.
  3. **Persistence:** End trip → kill app → reopen → trip still in history and in monthly stats.
  4. **Copy:** Verify dialog strings state "save" and "count" for End trip and "not saved" / "not count" for Clear trip.
- **Regression:** Run existing instrumentation and unit tests; ensure no breakage in TripInputViewModel or repository.

---

## 8. Security Specialist

- **PII and metadata:** Trip data (distance, OOR %, dates) is stored locally in Room. Per `docs/security/SECURITY_NOTES.md`: do not log PII; app-private storage is acceptable; consider EncryptedSharedPreferences or SQLCipher only if handling higher-sensitivity data later.
- **Metadata storage:** If new metadata (e.g. trip type, notes) is added, keep it in Room with the same access controls; avoid storing credentials or tokens in trip records. Prefer structured fields over free-text if it could contain sensitive info.
- **Clear trip:** Ensures bad or test data is not persisted; good for both accuracy and privacy (no accidental save of wrong trip).

---

## 9. Email Editor / Market Guru

- **Messaging for user:** Emphasize clarity and control: "You're in control: End trip = save and count in monthly stats; Clear trip = discard so it never counts."
- **Subject line suggestion for the completion email:** "OutOfRouteBuddy: Monthly stats & trip saving plan ready"

---

## 10. File Organizer

- **Docs:** This plan lives at `docs/agents/MONTHLY_STATS_PERSISTENCE_CALENDAR_PLAN.md`. User-facing notes are in `docs/ux/END_TRIP_FLOW_UX.md` and in-app strings. Optional: add `docs/product/FEATURE_BRIEF_monthly_stats_and_persistence.md` as a single reference for "what counts, what doesn't."

---

## 11. Human-in-the-Loop Manager

- **Email after completion:** Send the user (Brandon) an email summarizing: (1) what was planned, (2) End trip vs Clear trip and where the notes appear, (3) metadata and security bullets, (4) next steps (e.g. run tests, deploy). Use the coordinator email script; see `docs/agents/EMAIL_AT_END_OF_BIG_CHANGES.md`.

---

## 12. Implementation checklist (5-hour pacing)

| # | Task | Owner | Est. |
|---|------|--------|------|
| 1 | Add string resources for End trip / Clear trip notes | Front-end | 20 min |
| 2 | Update End Trip dialog message (save + count in monthly stats) | Front-end | 15 min |
| 3 | Update Clear Trip dialog message (not saved, not counted) | Front-end | 15 min |
| 4 | Optional: Statistics section hint re saved vs cleared | Front-end | 15 min |
| 5 | Verify ViewModel refreshes monthly stats after endTrip() | Front-end / Back-end | 30 min |
| 6 | Confirm Room insert on End trip and no insert on Clear trip | Back-end | 30 min |
| 7 | Document or implement OfflineDataManager load/save (or defer) | Back-end | 45 min |
| 8 | Document metadata schema / extension points | Back-end | 30 min |
| 9 | Update END_TRIP_FLOW_UX.md and optional FEATURE_BRIEF | File Organizer / Design | 20 min |
| 10 | QA: test End trip → DB + stats; Clear trip → no DB; persistence | QA | 45 min |
| 11 | Security pass: no PII in logs; metadata note | Security | 15 min |
| 12 | Draft and send completion email to user | Human-in-the-Loop | 20 min |

Total ~5 hours. Order: 1–4 (strings/dialogs), 5–6 (verify persistence), 7–8 (backend/docs), 9 (docs), 10–11 (QA + security), 12 (email).

---

*When done, Human-in-the-Loop sends the summary email to the user.*
