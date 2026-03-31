# Heavy tier — refined todo list

**Purpose:** Single refined list of all **Heavy**-tier tasks (human approval required before implementation). Use for prioritization and "what's next" when you want to implement a Heavy feature.  
**References:** [LOOP_TIERING.md](./LOOP_TIERING.md), [FUTURE_IDEAS.md](../product/FUTURE_IDEAS.md), [HEAVY_IDEAS_FAVORITES.md](./HEAVY_IDEAS_FAVORITES.md).

**Rule:** Heavy = one-by-one implementation; question lock + visual approval ("approve 100% implement") before implementation. See LOOP_TIERING § Question Lock and Visual Approval Clause.

---

## Refined Heavy list (by category)

### Social / data sharing
| # | Idea | FUTURE_IDEAS | Notes |
|---|------|--------------|--------|
| 1 | Multi-user data sharing | § 1.1 | Backend API, auth, privacy; fleet dashboard. |
| 2 | Driver ranking | § 1.2 | Depends on 1.1; Statistics or Rankings screen. |
| 3 | Ranking chart | § 1.3 | Depends on 1.2; chart over time vs peers. |

### Engagement / updates
| # | Idea | FUTURE_IDEAS | Notes |
|---|------|--------------|--------|
| 4 | Optional email signup for updates | § 2.1 | Settings / "Stay updated"; opt-in only. |

### Visualization
| # | Idea | FUTURE_IDEAS | Notes |
|---|------|--------------|--------|
| 5 | Route deviation map (instant replay) | § 3.1 | Red-line map on stat card; expected vs actual path. |

### Simulation / synthetic data
| # | Idea | FUTURE_IDEAS | Notes |
|---|------|--------------|--------|
| 6 | Sandboxed virtual fleet | § 4.1 | Approved for implementation: synthetic fleet service + shared-pool export, fully separated from GOLD Room data. |

### UI polish / icons
| # | Idea | FUTURE_IDEAS | Notes |
|---|------|--------------|--------|
| (moved) | Trash can icon beautification | § 5.1 | Reclassified to Medium execution queue (approved-from-Heavy). |

### Navigation / app chrome
| # | Idea | FUTURE_IDEAS | Notes |
|---|------|--------------|--------|
| (moved) | Scrolling top toolbar / taskbar | § 6.1 | Reclassified to Medium execution queue (approved-from-Heavy). |
| (moved) | Hamburger menu left of "Out of route" title | § 6.2 | Reclassified to Medium execution queue (approved-from-Heavy). |

### Branding
| # | Idea | FUTURE_IDEAS | Notes |
|---|------|--------------|--------|
| 10 | Possible app name change | § 7.1 | Ideas later; app title, launcher, store. |

### Goals & progress
| # | Idea | FUTURE_IDEAS | Notes |
|---|------|--------------|--------|
| 11 | OOR percentage goal / target | § 8.1 | User sets target %; progress vs goal in stats. |

### Notifications
| # | Idea | FUTURE_IDEAS | Notes |
|---|------|--------------|--------|
| 12 | End-of-day trip summary notification (opt-in) | § 9.1 | Daily summary push; opt-in; WorkManager/AlarmManager. |

### OOR display
| # | Idea | FUTURE_IDEAS | Notes |
|---|------|--------------|--------|
| 13 | OOR over/under view with color semantics | § 10.1 | Over/under + green/blue/red; calendar, cards, stats. |

### Edge cases
| # | Idea | FUTURE_IDEAS | Notes |
|---|------|--------------|--------|
| 14 | Load cancelled mid-trip / new load (get future context) | § 11.1 | Partial trip + new load; design workflow first. |

### Agent hub & in-app
| # | Idea | FUTURE_IDEAS | Notes |
|---|------|--------------|--------|
| 15 | Text command: "Send to hub" (in-app) | § 12.1 | Agent prompt done; in-app text command/export = Heavy. |

### Sandbox tooling
| # | Idea | FUTURE_IDEAS | Notes |
|---|------|--------------|--------|
| 21 | Lightweight feature preview container | § 13.1 | Preview fully sandboxed features before implementation; minimal container only. |
| 51 | Loop Council timed sandbox | N/A | Six-instance timer/council architecture with hybrid device + durability judging; sandbox/design only until separately approved for scaffolding or live execution. |

### Extended future features (sandboxed)
| # | Idea | FUTURE_IDEAS | Notes |
|---|------|--------------|--------|
| 22 | Predictive OOR risk scoring | § 14.1 | Forecast likely off-route risk. |
| 23 | Fuel cost overlay per deviation | § 14.2 | Estimate fuel impact by detour. |
| 24 | Driver coaching timeline | § 14.3 | Time-ordered coaching hints. |
| 25 | Geofence hotspot atlas | § 14.4 | Frequent deviation zones. |
| 26 | Weather and traffic context enrich | § 14.5 | Contextualize route deviations. |
| 27 | Load type impact analyzer | § 14.6 | Compare OOR by load type. |
| 28 | Alternative route recommendation sandbox | § 14.7 | Post-trip route alternatives. |
| 29 | Shift fatigue signal estimator | § 14.8 | Heuristic fatigue indicators. |
| 30 | Anomaly replay debugger | § 14.9 | Replay anomalous route moments. |
| 31 | Policy rules engine (sandbox) | § 14.10 | Configurable OOR policy checks. |
| 32 | Team benchmark cohorts | § 14.11 | Cohort-based comparisons. |
| 33 | Privacy-preserving telemetry exporter | § 14.12 | Aggregate-safe telemetry sharing. |
| 34 | Offline conflict visualizer | § 14.13 | Show sync conflicts clearly. |
| 35 | Trip correction wizard | § 14.14 | Guided trip correction flow. |
| 36 | Smart notification scheduler | § 14.15 | Best-time notifications. |
| 37 | Voice note to trip metadata | § 14.16 | Attach short notes to trips. |
| 38 | Incident tagging workflow | § 14.17 | Standardized incident tags. |
| 39 | Route compliance heatmap calendar | § 14.18 | Calendar trend heatmap. |
| 40 | AI trip summary draft assistant | § 14.19 | Draft summary for review. |
| 41 | Custom KPI builder (advanced) | § 14.20 | User-defined KPI formulas. |
| 42 | Multi-stop segment planner | § 14.21 | Segment-aware planning/analysis. |
| 43 | Dispatch handoff package export | § 14.22 | Structured dispatch export bundle. |
| 44 | Attachment evidence locker | § 14.23 | Attach media/docs to trips. |
| 45 | Webhook integration sandbox | § 14.24 | Outbound event integration. |
| 46 | Enterprise SSO groundwork | § 14.25 | SSO-ready architecture prep. |
| 47 | Data retention policy simulator | § 14.26 | Simulate retention before apply. |
| 48 | Audit trail explorer | § 14.27 | Explore audit history quickly. |
| 49 | Synthetic-to-human parity checker | § 14.28 | Compare synthetic vs real behavior. |
| 50 | Documentation lint remediation automation | § 14.29 | Automated docs lint normalization workflow. |

---

## Other Heavy-tier items (from CRUCIAL / tiering)

| # | Item | Source | Notes |
|---|------|--------|--------|
| 16 | Statistics: monthly only | CRUCIAL §9 | Remove weekly/yearly from UI; ViewModel + repo + QA. High impact; deferred until UI approved. |
| (moved) | Drastic loop improvements | LOOP_TIERING | Reclassified to Medium execution queue (approved-from-Heavy). |
| 18 | ROADMAP features (after sandbox) | LOOP_TIERING | Auto drive, Reports screen, address input — only after sandbox validation. |
| 19 | Large refactors | LOOP_TIERING | Repository interface changes; multi-file; cross-module. |
| (moved) | Architecture (major toolchain) | LOOP_TIERING | Reclassified to Medium execution queue (approved-from-Heavy). |

---

## Summary counts

| Type | Count |
|------|--------|
| **Active FUTURE_IDEAS Heavy ideas** | 42 |
| **Active other Heavy (CRUCIAL / tiering)** | 3 |
| **Active Heavy backlog total** | 46 |
| **Moved to Medium execution queue (approved-from-Heavy)** | 5 |
| **Catalog total (active Heavy + moved queue)** | 51 |

---

## Production stage — moved to Medium execution queue (2026-03-13)

User approved these for implementation; they are now Medium execution queue items and should execute on next loop runs. See [HEAVY_IDEAS_FAVORITES.md](./HEAVY_IDEAS_FAVORITES.md) § Production stage and [ROADMAP.md](../product/ROADMAP.md).

| # | Item | Status |
|---|------|--------|
| 7 | Trash can icon beautification | 100% approved |
| 8 | Scrolling top toolbar / taskbar | 100% approved |
| 9 | Hamburger menu left of "Out of route" title | 100% approved |
| 17 | Drastic loop improvements | 100% approved |
| 20 | Architecture (major toolchain) | 100% approved |

---

## How to use this list

- **Prioritize:** Mark favorites in [HEAVY_IDEAS_FAVORITES.md](./HEAVY_IDEAS_FAVORITES.md); loop surfaces those first.
- **Production stage:** Items 7, 8, 9, 17, 20 are approved and moved to Medium execution queue; execute first on next loop runs.
- **Implement one-by-one:** For each Heavy item, ask "Are you ready to implement this?" → question lock (image/layout/merge sim) → user says **"approve 100% implement"** before implementation.
- **Cap:** When Heavy list in FUTURE_IDEAS / Favorites reaches ~50, loop switches to judge/critique mode instead of adding new Heavy ideas.

---

*Generated from FUTURE_IDEAS, HEAVY_IDEAS_FAVORITES, LOOP_TIERING, and CRUCIAL_IMPROVEMENTS_TODO. Update when new Heavy ideas are added or completed.*
