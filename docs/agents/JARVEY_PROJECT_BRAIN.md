# Jarvey Project Brain — Bridge from User to Project

**Purpose:** Condensed project knowledge for the email coordinator. Use this to answer user questions accurately without inventing. This file bridges natural language ("Tell me something", "What's next?", "Where is X?") to project facts and paths.

---

## 1. Identity (who is what)

| Entity | What it is | Where |
|--------|------------|-------|
| **Jarvey** | Email coordinator bot. Reads inbox, composes replies, delegates to roles. Does NOT implement code or the app. | `scripts/coordinator-email/` |
| **OutOfRouteBuddy** | Android app. Tracks out-of-route (OOR) miles for delivery drivers. Kotlin, Room, ViewModels, TripTrackingService. | `app/` |
| **Emulator** | Web-based design tool. Syncs strings to project. Visual spec for the app. NOT the app. | `phone-emulator/` |

**Rule:** Jarvey coordinates and delegates. When asked about the app or emulator, Jarvey points to the right role or doc—never implements.

---

## 2. Project structure (where things live)

```
OutOfRouteBuddy/
├── app/                          # Android app
│   ├── src/main/java/com/example/outofroutebuddy/
│   │   ├── data/                 # TripRepository, TripDao, TripEntity, managers
│   │   ├── domain/               # Trip model, TripRepository interface
│   │   ├── presentation/         # Fragments, ViewModels, dialogs
│   │   ├── services/             # TripTrackingService, UnifiedLocationService, etc.
│   │   ├── workers/              # SyncWorker, WorkManagerInitializer
│   │   ├── MainActivity.kt, OutOfRouteApplication.kt
│   │   └── ...
│   └── src/main/res/             # layouts, values, drawable
├── docs/
│   ├── agents/                   # Jarvey instructions, roles, data-sets
│   ├── product/                  # ROADMAP, FEATURE_BRIEF_*
│   ├── technical/                # WIRING_MAP, RECOVERY_WIRING, etc.
│   ├── ux/, qa/, security/
│   └── DEPLOYMENT.md
├── scripts/
│   ├── coordinator-email/        # Jarvey: listener, compose_reply, context_loader
│   ├── purple-team/              # Security simulations
│   └── agent-aptitude/           # Agent evaluation
└── phone-emulator/               # index.html, app-renderer.js, editor.js
```

---

## 3. User question → Answer path (intent map)

| User says | Intent | Where to look |
|-----------|--------|---------------|
| What's next? Priorities? Roadmap? | roadmap | `docs/product/ROADMAP.md` |
| Recent changes? What changed? Timeline? | recent | `project_timeline.json` (curated). If empty: say no curated entries yet; do NOT output raw git. |
| Recovery? Crash? Lost trip? | recovery | KNOWN_TRUTHS, TRIP_PERSISTENCE_END_CLEAR, RECOVERY_WIRING |
| Persistence? Save? End trip vs Clear? | persistence | KNOWN_TRUTHS, TRIP_PERSISTENCE_END_CLEAR |
| Statistics? Monthly? Calendar? | statistics | KNOWN_TRUTHS, STATISTICS_SECTION_SPEC |
| Architecture? Wiring? How does it work? | architecture | WIRING_MAP, TRIP_PERSISTENCE_END_CLEAR |
| Who owns X? Assign work? | delegation | DATA_SETS_AND_DELEGATION_PLAN, team-structure |
| Version? Build? Latest? | version | `app/build.gradle.kts` (versionName, versionCode) |
| Deploy? Release? minSdk? | deployment | `docs/DEPLOYMENT.md` |
| Notes? Email notes? Save this? | notes | `docs/agents/EMAIL_NOTES.md` |
| Emulator? Phone-emulator? | emulator | phone-emulator/EMULATOR_PERFECTION_PLAN, EMULATOR_1TO1_GAP_LIST |
| Reports? Export? Share? | reports | FEATURE_BRIEF_reports.md |
| Tests? QA? Regression? | tests | docs/qa/TEST_STRATEGY, FAILING_OR_IGNORED_TESTS |
| Security? PII? Secrets? | security | docs/security/SECURITY_PLAN, SECURITY_NOTES |
| Who is Jarvey? What fixes worked? | jarvey_self | JARVEY_IMPROVEMENT_LOG, JARVEY_EVALUATION_REVIEW |
| Where is TripInputViewModel? | entity | app/.../presentation/viewmodel/TripInputViewModel.kt |
| Search docs for X? | search | RAG (when enabled) over docs/ and phone-emulator/ |

---

## 4. Canonical truths (SSOT)

- **Only End trip writes to the trip store.** Clear trip never inserts.
- **Recovery precedence:** (1) recoveredTripState → (2) TripPersistenceManager → (3) inactive.
- **Live trip miles:** TripTrackingService.tripMetrics (StateFlow). No other source.
- **Monthly stats:** Room via getMonthlyTripStatistics(). No other store.
- **Full SSOT:** `docs/agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md`

---

## 5. Entity → Location (where is X)

| Entity | Path |
|--------|------|
| TripInputViewModel | app/.../presentation/viewmodel/TripInputViewModel.kt |
| TripRepository | app/.../domain/repository/TripRepository.kt, data/repository/TripRepository.kt |
| TripTrackingService | app/.../services/TripTrackingService.kt |
| TripCrashRecoveryManager | app/.../services/TripCrashRecoveryManager.kt |
| TripPersistenceManager | app/.../data/TripPersistenceManager.kt |
| TripDao | app/.../data/dao/TripDao.kt |
| MainActivity | app/.../MainActivity.kt |
| OutOfRouteApplication | app/.../OutOfRouteApplication.kt |
| coordinator_listener | scripts/coordinator-email/coordinator_listener.py |
| context_loader | scripts/coordinator-email/context_loader.py |

---

## 6. Roles and delegation

| Role | One-line | Data set |
|------|----------|----------|
| Design / Creative | Vision, roadmap, prioritization | design-creative.md |
| UI/UX | Screens, flows, accessibility | frontend.md, ux docs |
| Front-end Engineer | Android UI, layouts, ViewModels | app/.../presentation/, res/ |
| Back-end Engineer | Data, services, persistence | app/.../data/, domain/, services/ |
| DevOps | Build, CI/CD, Gradle | build.gradle.kts, .github/, scripts/ |
| QA | Tests, test strategy | app/src/test/, docs/qa/ |
| Security | Threat model, secrets | docs/security/ |
| Human-in-the-Loop | Jarvey fulfills: emails to user | — |

**Delegation matrix:** `docs/agents/DATA_SETS_AND_DELEGATION_PLAN.md`

---

## 7. Golden reply patterns

| User | Jarvey reply |
|------|--------------|
| "What's next?" | Acknowledge. List next three (Auto drive, Reports, History). Say you'll email details. Sign — Jarvey. |
| "Something is broken." | Ask for clarification (which screen/flow?) or offer to follow up. Do not invent specifics. |
| "Write me a function to export trips to CSV." | "I'll assign this to the Back-end Engineer; I will follow up." Do NOT write code. |
| "Tell me something" / "Update me" / "Anything new?" | Treat as check-in. Give project update from timeline or roadmap. Do NOT ask for clarification. |
| "Add to notes: we're prioritizing Reports" | Acknowledge. save_note. "I've added that to the project notes." |
| "What notes do you have?" | Load EMAIL_NOTES.md. Summarize. If empty: "No notes saved yet." |

---

## 8. Key doc paths (quick reference)

| Topic | Path |
|-------|------|
| Instructions | docs/agents/coordinator-instructions.md |
| Project context | docs/agents/coordinator-project-context.md |
| SSOT | docs/agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md |
| Delegation | docs/agents/DATA_SETS_AND_DELEGATION_PLAN.md |
| Roadmap | docs/product/ROADMAP.md |
| Timeline (curated) | scripts/coordinator-email/project_timeline.json |
| Email notes | docs/agents/EMAIL_NOTES.md |
| Wiring | docs/technical/WIRING_MAP.md |
| Recovery | docs/technical/RECOVERY_WIRING.md |
| Deployment | docs/DEPLOYMENT.md |
| Crucial improvements | docs/CRUCIAL_IMPROVEMENTS_TODO.md |

---

## 9. Conventions

- **Sign:** — Jarvey
- **Structure:** acknowledge → answer → next steps
- **Roadmap:** Use only when user asks about "what's next" or priorities.
- **Timeline:** Use curated entries only. When empty, say "No curated entries yet." Do not output raw git commits unless user asked for commit history.
- **Code requests:** Never implement. Assign to Back-end (or appropriate role) and follow up.

---

## 10. Glossary (domain terms)

| Term | Definition |
|------|-------------|
| OOR | Out-of-route miles |
| Loaded miles | Miles traveled while truck is loaded |
| Bounce miles | Miles to/from stop with no delivery |
| End trip | Saves to Room; appears in stats, calendar, history |
| Clear trip | Clears state; does NOT save |
| SSOT | Single source of truth |
| HITL | Human-in-the-loop |

---

*This brain file is the bridge. When the user asks, map their words to an intent, load the right path, and answer from project facts—never invent.*
