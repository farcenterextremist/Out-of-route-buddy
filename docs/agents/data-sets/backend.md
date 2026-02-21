# Back-end Engineer — data set

## Consumes (reads / references)

### Primary
- **Known truths & SSOT:** `docs/agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md` — persistence, recovery, repository chain, Room, OfflineDataManager status; use before changing data or services.
- **Data layer:** `app/src/main/java/com/example/outofroutebuddy/data/` — repositories, DAOs, database, adapters (e.g. `AppDatabase`, `TripRepository`, `DomainTripRepositoryAdapter`).
- **Domain:** `app/src/main/java/com/example/outofroutebuddy/domain/` — models, repository interfaces, use-case boundaries; you implement these.
- **Services:** `app/src/main/java/com/example/outofroutebuddy/services/` — `TripTrackingService`, `UnifiedLocationService`, `UnifiedTripService`, `UnifiedOfflineService`, sync workers, etc. You own behavior and changes here.
- **Feature briefs (behavior):** `docs/product/FEATURE_BRIEF_*.md` — e.g. Auto drive detection rules, persistence requirements, “what the system should do”; use to implement the right behavior.

### Secondary
- **Team parameters:** `docs/agents/team-parameters.md` — if the user has specified business rules (e.g. workdays, defaults); use when implementing preferences or defaults.
- **ViewModel usage:** `app/.../presentation/viewmodel/` — see how the UI consumes repositories/services so you don’t break contracts; do not change ViewModels (Front-end owns that boundary).

## Produces (writes / owns)

### Primary
- **Kotlin in data/domain/services:** New or updated entities, DAOs, repositories, services, domain models. ViewModels and UI call into your layer; you own the implementation.
- **Repository interfaces (in domain):** When the UI needs something new, you add or extend the interface and implement it in `data/` or services.

### Secondary
- **Technical notes (optional):** `docs/technical/DATA_MODEL.md` or `docs/technical/SERVICES.md` — short overview of main entities, services, and data flow for future you or other roles. Not required for every change.

## Key boundaries

- **UI:** You expose data and behavior via repositories and services; you do not edit layouts, fragments, or ViewModel UI logic. Coordinate with **Front-end** on method names and return types if needed.
- **Build / CI:** You do not change Gradle or pipelines → **DevOps**.
- **Security:** If a feature touches location, PII, or credentials, get a review from **Security Specialist** and implement their recommendations.

## Delegation (when to assign to this role)

- “Implement detection logic for Auto drive” (movement/speed, geofence, or similar).
- “Persist trip state across app kill” / “Ensure active trip survives process death.”
- “Add smart default for bounce miles” (remember last, prefill).
- “Expose [X] to the UI” (new repository method or service API).
- “Fix [bug] in TripTrackingService / UnifiedLocationService / repository.”

## Out of scope (do not assign here)

- XML layouts, fragments, ViewModels UI logic → **Front-end Engineer**.
- Build, CI, deployment → **DevOps Engineer**.
- Test implementation strategy / test plans → **QA Engineer** (you may write unit tests for your own code; QA owns strategy and integration tests).
