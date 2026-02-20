# Regression checklist — Trip, History, Statistics

**Owner:** QA Engineer  
**Purpose:** Short manual (or automated) smoke checklist for core app flows.  
**Related:** 25-point #19, `docs/agents/APP_IMPROVEMENT_25_POINT_BRAINSTORM.md`.

---

## Trip start/end

- [ ] **Start trip:** Enter loaded miles and bounce miles → tap Start Trip → trip becomes active; UI shows active state.
- [ ] **End trip:** With active trip → tap End Trip → confirmation (End Trip / Clear / Continue) → tap End Trip → trip ends and is saved; appears in history.
- [ ] **Clear/discard:** With active trip → End Trip → tap Clear (or Discard) → trip is abandoned; not in history.
- [ ] **Continue:** With active trip → End Trip → tap Continue → dialog dismisses; trip remains active.
- [ ] **Persistence:** Start trip → force-stop app → reopen → active trip is restored (or document if not yet implemented).

---

## History

- [ ] **Open history:** From main nav/screen → History opens; list shows (e.g. by date).
- [ ] **By date:** Select a date → list of trips for that date (or empty state).
- [ ] **Delete trip:** From history → delete a trip → it is removed from list.
- [ ] **Trip details (when implemented):** Tapping a trip opens details screen; backlog until then (see CRUCIAL #4).

---

## Statistics

- [ ] **Monthly only:** Statistics section shows only monthly data; no weekly/yearly tabs or options.
- [ ] **Values correct:** After completing trips, monthly totals (miles, OOR, etc.) match expectations.
- [ ] **After trip end:** New trip appears in history and is reflected in statistics for the current month.

---

*Run after major changes or before release. For automated smoke, see test suite and consider adding UI tests for these flows.*
