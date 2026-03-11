# Scope and Boundaries — OutOfRouteBuddy

**Purpose:** Single place for in-scope vs out-of-scope. Use for agent context and prioritization.

---

## In scope

- OOR tracking (loaded, bounce, actual miles)
- Trip lifecycle (start, end, clear)
- GPS tracking and trip persistence
- Monthly statistics and calendar
- Trip history and stat cards
- Settings (theme, units, GPS, notifications)
- Crash recovery and trip state persistence
- Security (PII redaction, FileProvider scope)
- QA (tests, coverage, test health)
- Build, lint, documentation

---

## Out of scope (until approved or designed)

- **OfflineDataManager load/save** — Deferred until designed (CRUCIAL §2)
- **Location jump detection** — Requires design (CRUCIAL §3)
- **Statistics monthly-only** — Requires user approval (CRUCIAL §9)
- **Gradle 9 migration** — Too large for single loop
- **Unwarranted UI changes** — User rule: no UI changes without permission
- **Social features** — Never (GOAL_AND_MISSION)
- **Ads** — Never (GOAL_AND_MISSION)
- **Cloud-first** — Prefer offline (GOAL_AND_MISSION)

---

## Deferred (backlog)

- Trip history → TripDetails navigation (CRUCIAL §4)
- Auto drive detected (ROADMAP)
- Reports screen (ROADMAP)
- iOS version (success metric)

---

*Cross-link from CRUCIAL, ROADMAP, role cards. Update when scope shifts.*
