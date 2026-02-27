# Jarvey Glossary — Domain Terms for OutOfRouteBuddy

Quick reference for project-specific terms. Use when the user asks "what is X" or when clarity is needed.

---

| Term | Definition |
|------|------------|
| **OOR** | Out-of-route miles; miles driven off the planned delivery route |
| **Loaded miles** | Miles traveled while the truck is loaded |
| **Bounce miles** | Miles to/from a stop with no delivery (empty run) |
| **Trip store** | Room database `trips` table; only End trip writes here |
| **End trip** | Saves trip to Room; appears in stats, calendar, history |
| **Clear trip** | Clears in-memory state; does NOT save to Room |
| **TripTrackingService** | Live GPS tracking; source for trip miles during active trip |
| **TripCrashRecoveryManager** | 30s auto-save; restores trip state after app crash |
| **TripPersistenceManager** | SharedPreferences; holds active trip state (24h) |
| **Room** | Android SQLite ORM; persistence for trips, stats |
| **SSOT** | Single source of truth; see KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md |
| **HITL** | Human-in-the-loop; user decisions via email |
| **Emulator** | Phone-emulator; visual spec for the real app |
| **Auto drive** | Feature: detect when driver is on the road; one-tap start |
| **Reports screen** | Planned feature: trip/OOR reports, export, share |
| **Deployment** | Build/release process; minSdk 24, JDK 17; see docs/DEPLOYMENT.md |
| **RAG** | Vector search over docs when user says "search docs for X"; indexes docs/ and phone-emulator/ |
