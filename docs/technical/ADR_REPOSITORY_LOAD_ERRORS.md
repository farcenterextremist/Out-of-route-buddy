# ADR: Repository load error visibility (D1)

**Status:** Accepted  
**Context:** QUALITY_AND_ROBUSTNESS_PLAN D1, PROJECT_AUDIT D1. DomainTripRepositoryAdapter currently logs and emits null/emptyList on failure; UI never sees errors.

**Decision:** Add an **additive** error channel on the adapter (no change to domain `TripRepository` interface). Adapter emits to a `SharedFlow<String>` on catch; ViewModels that need load errors receive this flow via DI and expose a `StateFlow<String?>` or one-shot event to UI. UI shows snackbar when non-null.

**Call-sites:** TripHistoryByDateViewModel (getTripsOverlappingDay), TripInputViewModel (getTripsByDateRange), TripDetailsViewModel (getTripById), DataManagementViewModel (getTripsByDateRange). Each can optionally collect the repository load error flow and expose to UI.

**Implementation:** DomainTripRepositoryAdapter holds `MutableSharedFlow<String>(extraBufferCapacity=1)`, emits `e.message ?: "Load failed"` in each `.catch { }` for getTripById, getTripsByDateRange, getTripsOverlappingDay. Module provides adapter as singleton and provides `adapter.loadErrors` for injection. ViewModels inject `@RepositoryLoadErrors Flow<String>` and expose loadError state; fragments observe and show snackbar.

**References:** docs/QUALITY_AND_ROBUSTNESS_PLAN.md (D1), docs/PROJECT_AUDIT_2025_02_27.md.
