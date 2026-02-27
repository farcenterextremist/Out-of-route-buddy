# Back-end Engineer — Semi Simple

- **Role card:** `docs/agents/roles/backend-engineer.md`
- **Prompt type:** Semi Simple

## Prompt (copy-paste)

```
We need to expose 'total trips this month' for the statistics screen. What would you add or change (repository, service, or domain) and which file(s)?
```

## Look for (scoring)

Data/domain/services paths; no UI; handoff to Front-end for UI contract.

---

## Response

**Add/change:** (1) **Domain:** Ensure the domain `TripRepository` (or a use case) exposes something like `getTripCountForMonth(yearMonth)` or reuse `getTripStatistics(start, end)` and derive count. (2) **Adapter:** In `DomainTripRepositoryAdapter`, call the data layer’s `getTripsForDateRange(firstDayOfMonth, lastDayOfMonth)` and return count or a small stats object. (3) **Files:** `app/.../domain/repository/TripRepository.kt` (interface), `app/.../data/repository/DomainTripRepositoryAdapter.kt` (implementation), and optionally `UnifiedTripService.getTripStatistics()` if that’s the API the ViewModel uses. The Front-end Engineer will need the UI contract (e.g. a StateFlow or suspend that returns the number). I don’t touch layouts or views.

