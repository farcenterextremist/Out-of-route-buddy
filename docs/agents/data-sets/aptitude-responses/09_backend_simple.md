# Back-end Engineer — Simple

- **Role card:** `docs/agents/roles/backend-engineer.md`
- **Prompt type:** Simple

## Prompt (copy-paste)

```
Where is trip data persisted (e.g. which layer or component)? One sentence.
```

## Look for (scoring)

Data/domain/services paths; no UI; handoff to Front-end for UI contract.

---

## Response

Trip data is persisted in the **Room database** via `AppDatabase` and `TripRepository` (data layer in `app/.../data/repository/TripRepository.kt`); the domain layer uses `DomainTripRepositoryAdapter` which calls `TripRepository` and maps to domain `Trip`. I don’t write UI; I’d hand UI contract (e.g. "total trips this month") to the Front-end Engineer.

