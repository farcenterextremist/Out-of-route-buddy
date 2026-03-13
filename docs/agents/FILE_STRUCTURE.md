# File structure and documentation organization

**Purpose:** Single source of truth for where everything lives and why. Use this when adding new docs, moving files, or onboarding. Aligns with [KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md](KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md) for app behavior; this doc is for **repository and docs layout**.

**Owner:** File Organizer role. Propose changes here; move files only with coordinator/user approval (except when following an approved reorg).

**Last updated:** 2025-03-11

---

## Principles (research-backed)

1. **Predictability** — Files live where people expect them (e.g. test strategy under `qa/`, feature briefs under `product/`).
2. **Separation by audience and content type** — Not by "whatever we added last." Top-level docs folders map to who uses them: product, agents/team, qa, technical, automation, security, ops.
3. **Shallow over deep** — Prefer 2–3 folder levels. Deep nesting increases cognitive load and navigation time (studies: ~14s retrieval, success drops with depth). Keep sibling count per folder manageable (e.g. 3–8 semantic groups).
4. **One index per area** — Each major area has a README or index (e.g. `docs/README.md`, `qa/README.md`, `automation/README.md`) so "where do I find X?" is answerable in one place.
5. **No duplication** — Link to the single source; don’t copy chunks across docs. If two docs need the same list, one owns it and the other links.
6. **Don’t over-engineer** — Only create folders we actually use. Avoid "future" buckets that stay empty.

---

## Repository root (top level)

| Item | Purpose |
|------|---------|
| `app/` | Android app (Kotlin, Gradle). Structure follows Android + layer convention: `presentation/`, `domain/`, `data/`, `services/`, `di/`, etc. See CODEBASE_OVERVIEW. |
| `docs/` | All project documentation. Entry point: `docs/README.md`. |
| `scripts/` | Coordinator email, automation scripts, one-off runnables. |
| `.github/` | CI workflows (e.g. android-tests, coverage). |
| `gradle/`, `build/`, `.cursor/`, `.vscode/` | Build and editor config. |
| `README.md`, `CHANGELOG.md` | Project readme and version history. |
| `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties` | Gradle build. |
| **Root-level docs** | Only a few: `GRADLE_9_MIGRATION_NOTES.md` (build-related, often opened from root). Prefer putting new docs under `docs/` and linking from `docs/README.md`. |
| **TEST_FAILURES_DOCUMENTATION** | Lives at **`docs/qa/TEST_FAILURES_DOCUMENTATION.md`** so all QA docs are under `docs/qa/`. |

---

## docs/ structure (high level)

```
docs/
├── README.md                    # Master index; start here
├── TASKS_INDEX.md               # All TODOs/tasks (CRUCIAL, worker, in-code)
├── CRUCIAL_IMPROVEMENTS_TODO.md # Prioritized improvements
├── GOAL_AND_MISSION.md          # North star
├── SCOPE_AND_BOUNDARIES.md
├── ARCHITECTURE.md              # App layers, persistence, recovery
├── AGENTS.md                    # Agent entry (improvement loop, build)
├── DEPLOYMENT.md                # Build, run, deploy
├── SELF_IMPROVEMENT_PLAN.md
├── product/                     # Product and design
├── agents/                      # Team roles, known truths, data-sets, hub
├── qa/                          # Test strategy, plans, coverage, failing tests
├── technical/                   # Wiring, persistence, GPS, DB, code quality
├── ux/                          # UX specs, accessibility, copy
├── security/                    # Security notes, checklists
├── release/                     # Release checklists, go/no-go
├── automation/                  # Improvement loop, token loop, cyber, sandbox
├── readiness/                   # Plateau, finished state, progress bar
├── comms/                       # Subject lines, copy
├── archive/                     # Completed brainstorms, old prompts (reference only)
└── plans/                       # One-off or multi-phase plans (e.g. calendar stat cards)
```

**What belongs at docs root:** Core indexes and north-star docs that are linked from README and AGENTS. Anything that is a **plan, prompt, or one-off** goes under `archive/`, `plans/`, or the relevant area (e.g. release plan in `release/`).

---

## Where to put new docs

| If the doc is… | Put it in… | Index it in… |
|----------------|------------|--------------|
| Product roadmap, feature brief, future ideas | `docs/product/` | docs/README.md § Product & design |
| Role card, known truths, data-set, hub artifact | `docs/agents/` (or agents/data-sets/, agents/roles/) | docs/README.md § Team docs; hub in hub/README.md |
| Test strategy, test plan, coverage, failing tests | `docs/qa/` | docs/README.md § QA; qa/README.md |
| Wiring, persistence, DB, GPS, code quality, ADR | `docs/technical/` | docs/README.md § (link technical as a group) |
| UX spec, accessibility, copy, themes | `docs/ux/` | docs/README.md § (link ux as a group) |
| Security review, checklist, threat note | `docs/security/` | docs/README.md § Security & ops |
| Release checklist, go/no-go, store packet | `docs/release/` | docs/README.md § (link release) |
| Improvement loop, token loop, cyber loop, sandbox | `docs/automation/` | docs/README.md § Automation; automation/README.md |
| Plateau, finished state, progress bar | `docs/readiness/` | docs/README.md § Readiness |
| One-off prompt used to generate a plan (completed) | `docs/archive/prompts/` | docs/archive/README.md |
| Multi-phase or one-off plan (e.g. calendar data lifecycle) | `docs/plans/` | docs/README.md or plans/README.md if added |

---

## Archive and prompts

- **`docs/archive/`** — Completed brainstorms and one-off artifacts kept for reference. Current improvement sources: CRUCIAL_IMPROVEMENTS_TODO, product/ROADMAP.
- **`docs/archive/prompts/`** — One-off *_PROMPT.md files that were used to generate a plan or run (e.g. POLISHING_PLAN_PROMPT, SHIP_TO_GOOGLE_PLAY_PLAN_PROMPT). Kept so we don’t lose the exact prompt; not part of active workflow. Listed in `docs/archive/README.md`.

---

## Cross-references

- **Tasks and TODOs:** [TASKS_INDEX.md](TASKS_INDEX.md) — single entry point; links to CRUCIAL, WORKER_TODOS, COMPREHENSIVE_AGENT_TODOS, FAILING_OR_IGNORED_TESTS.
- **App behavior and wiring:** [agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md](agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md), [technical/WIRING_MAP.md](technical/WIRING_MAP.md), [agents/CODEBASE_OVERVIEW.md](agents/CODEBASE_OVERVIEW.md).
- **Hub and Loop Master:** [agents/data-sets/hub/README.md](agents/data-sets/hub/README.md), [automation/LOOP_MASTER_ROLE.md](automation/LOOP_MASTER_ROLE.md).

---

## Suggested moves (for user approval)

*All previously suggested moves are complete.* No further moves pending.

**Done:**
- `TEST_FAILURES_DOCUMENTATION.md` — repo root → `docs/qa/TEST_FAILURES_DOCUMENTATION.md`
- `docs/*_PROMPT.md` — → `docs/archive/prompts/`
- `docs/PROJECT_AUDIT_2025_02_27.md` — → `docs/archive/PROJECT_AUDIT_2025_02_27.md`
- `docs/DATA_TIERS.md` — → `docs/technical/DATA_TIERS.md`
- `docs/Make_Python_Available.md` — content merged into `docs/DEV_SETUP.md`; DEPLOYMENT.md links to DEV_SETUP for Python. File removed.

---

## References

- File Organizer data set: [agents/data-sets/file-organizer.md](agents/data-sets/file-organizer.md)
- Research: predictability and separation of concerns (folder structure best practices); shallow structures and cognitive load (folder depth vs. retrieval time); docs by audience (e.g. GitLab docs structure).  
- Internal: docs/README.md, AGENTS.md, TASKS_INDEX.md.
