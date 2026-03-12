---
name: kotlin-android-specialist
description: >-
  Specializes in Kotlin and Android for OutOfRouteBuddy: Room, Hilt, Compose,
  ViewModel, StateFlow, Coroutines. Use when editing Kotlin in app/, Room entities,
  ViewModels, services, or when the user asks for Kotlin/Android help.
---

# Kotlin/Android Specialist

## Quick Reference

Read `docs/technical/KOTLIN_BEST_PRACTICES.md` before editing Kotlin in `app/`. Follow project conventions.

---

## Project Conventions

### Architecture

- **Repository chain:** Domain interface → DomainTripRepositoryAdapter → data impl → TripDao
- **Only End trip writes to trip store.** Clear trip never inserts. See `docs/agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md`
- **UI → ViewModel → data:** No UI talks to Room or SharedPreferences directly

### Layers

| Layer | Location | Key types |
|-------|----------|-----------|
| presentation | presentation/ | Fragments, ViewModels |
| domain | domain/ | Trip model, TripRepository interface |
| data | data/ | TripRepository impl, TripDao, AppDatabase |
| services | services/ | TripTrackingService, UnifiedLocationService |

### Key Patterns

- **ViewModels:** StateFlow for UI state; viewModelScope for coroutines; inject repository, not Context
- **Room:** suspend DAO for one-shot; Flow for reactive queries; entities in data/
- **Hilt:** @Inject constructor; modules in di/
- **Coroutines:** Dispatchers.IO for IO; repeatOnLifecycle for collection

---

## Must-Follow

- No PII in logs (coordinates, trip IDs). See `docs/security/SECURITY_NOTES.md`
- Run `./gradlew detekt`; fix new issues
- Match existing code in the same layer when unclear

---

## Additional Resources

- Full practices: [docs/technical/KOTLIN_BEST_PRACTICES.md](../../../docs/technical/KOTLIN_BEST_PRACTICES.md)
- Known truths: [docs/agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md](../../../docs/agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md)
