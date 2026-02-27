# Front-end Engineer — Simple

- **Role card:** `docs/agents/roles/frontend-engineer.md`
- **Prompt type:** Simple

## Prompt (copy-paste)

```
Name the main layout file for the trip input screen and one Kotlin fragment that uses it.
```

## Look for (scoring)

Concrete paths (res/layout, presentation/); no repository or service logic; handoff to Back-end for data.

---

## Response

**Layout:** `app/src/main/res/layout/fragment_trip_input.xml` is the main layout for the trip input screen. **Fragment:** `app/src/main/java/com/example/outofroutebuddy/presentation/ui/trip/TripInputFragment.kt` uses it (via ViewBinding / `FragmentTripInputBinding`). I don’t touch repository or service logic; data contracts come from the Back-end.

