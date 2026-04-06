# Future Ideas — Sandboxed

**Purpose:** Well-organized backlog of future features. **All items are sandboxed** — not in scope for implementation without explicit user approval. See [SANDBOX_TESTING.md](../automation/SANDBOX_TESTING.md) for how future ideas are validated before merge.

**Owner:** Project Design / Creative Manager  
**Created:** 2025-03-11  
**References:** [ROADMAP.md](./ROADMAP.md), [CRUCIAL_IMPROVEMENTS_TODO.md](../CRUCIAL_IMPROVEMENTS_TODO.md), [WORKER_TODOS_AND_IDEAS.md](../agents/WORKER_TODOS_AND_IDEAS.md), [IMPROVEMENT_LOOP_ROUTINE.md](../automation/IMPROVEMENT_LOOP_ROUTINE.md)

---

## Sandbox Notice

> **These ideas are NOT in scope for the Improvement Loop or autonomous work.**  
> Implementation requires user approval. Document here; prioritize in ROADMAP when ready.  
> The Improvement Loop may improve the sandbox system (index, cross-links, organization) but will not implement these features.

**Sandbox status:** Ideas documented in this file are **100% sandboxed**. You can add Heavy-tier ideas here whenever you choose. Run **Medium tier** Improvement Loop to apply sandbox improvements (index, cross-links, new ideas). To prioritize which Heavy ideas to implement first, use [HEAVY_IDEAS_FAVORITES.md](../automation/HEAVY_IDEAS_FAVORITES.md).

---

## Index by Category

| Category | Ideas |
|----------|-------|
| [Social / Data Sharing](#1-social--data-sharing) | Multi-user data sharing, driver ranking |
| [Engagement / Updates](#2-engagement--updates) | Optional email signup for new updates and features |
| [Visualization](#3-visualization) | Ranking chart, route deviation map (instant replay) |
| [Simulation / Synthetic Data](#4-simulation--synthetic-data) | Sandboxed virtual fleet |
| [UI polish / icons](#5-ui-polish--icons) | Trash can icon beautification |
| [Navigation / app chrome](#6-navigation--app-chrome) | Scrolling top toolbar/taskbar, hamburger menu left of title |
| [Branding](#7-branding) | Possible app name change |
| [Goals & progress](#8-goals--progress) | OOR percentage goal / target |
| [Notifications](#9-notifications) | End-of-day trip summary (opt-in) |
| [OOR display](#10-oor-display) | OOR over/under view with color semantics (green/blue/red) |
| [Edge cases](#11-edge-cases) | Load cancelled mid-trip, new load; get future context |
| [Agent hub & text command](#12-agent-hub--text-command) | Text command "send to hub" |
| [Sandbox tooling](#13-sandbox-tooling) | Lightweight feature preview container for fully sandboxed features |
| [Extended heavy backlog](#14-extended-heavy-backlog) | Additional sandboxed Heavy features to reach list cap |

---

## 1. Social / Data Sharing

### 1.1 Multi-user data sharing

**Description:** System to share data across users. Enables fleet-level insights, benchmarking, and peer comparison.

**Placement:** Backend + new screens (e.g. fleet dashboard, shared reports).

**Dependencies:** Backend API, auth/identity, privacy controls, data aggregation.

**Status:** Sandboxed 100%. Not in ROADMAP yet.

---

### 1.2 Driver ranking

**Description:** Users can see where they rank against other drivers in the system. E.g. "You're in the top 15% for on-route efficiency this month."

**Placement:** Could live in:
- Statistics section (period summary)
- New "Rankings" or "Leaderboard" screen
- Metadata section of stat card

**Dependencies:** Multi-user data sharing (1.1), anonymized/aggregated metrics, ranking algorithm.

**Status:** Sandboxed 100%. Not in ROADMAP yet.

---

### 1.3 Ranking chart

**Description:** Visual chart showing driver rank over time or vs. peers. Possible ranking chart in the future.

**Placement:** Statistics section, or dedicated Rankings screen.

**Dependencies:** Driver ranking (1.2), charting library.

**Status:** Sandboxed 100%. Not in ROADMAP yet.

---

## 2. Engagement / Updates

### 2.1 Optional email signup for new updates and features

**Description:** Give users the option to sign up with their email to receive information about new updates and cool new features.

**Placement:** Settings screen or a dedicated "Stay updated" section. Opt-in only.

**Dependencies:** Email collection backend; privacy policy update; consent handling.

**Status:** Sandboxed 100%. Heavy tier. Not in ROADMAP yet.

---

## 3. Visualization

### 3.1 Route deviation map (instant replay)

**Description:** A two-dimensional map — like an instant replay — at the bottom of the stat card or metadata section. Shows where the truck driver deviated from normal/expected paths.

**Details:**
- **Red lines** indicate where the driver went off-route
- **Example:** Truck driver drove a mile off the road to stop at Walmart — the map would show the detour in red
- Gives the driver instant visual feedback on deviations for that trip

**Placement options:**
- Stat card section — at the very bottom, below metadata
- Metadata section — as an expandable "Route replay" or "Deviation map" block

**Dependencies:** Route/path data (expected vs. actual), map rendering (e.g. Google Maps, Mapbox), deviation detection logic.

**Sandbox artifact (active):** [ROUTE_DEVIATION_MAP_SANDBOX.md](sandbox/ROUTE_DEVIATION_MAP_SANDBOX.md) — gap analysis (no full GPS polyline persisted today), phased plan (trail capture → reference path → deviation logic → map UI), expected-path matrix, SDK shortlist, privacy checklist, open questions.

**Validation checklist (~55%):** (1) Trail persistence model + retention (see sandbox §3, §8); (2) Expected-path v1 decision (sandbox §4); (3) Deviation algorithm spec (sandbox §3 phase C); (4) Map SDK choice + build/licensing notes (sandbox §5); (5) Expandable UI placement + a11y (sandbox §8); (6) Performance / decimation for long trips; (7) Privacy toggle + Help copy; (8) Wireframe + visual approval + **"approve 100% implement"** before code.

**Status:** Sandboxed 100%. Sandbox brief started; not in ROADMAP yet.

---

## 4. Simulation / Synthetic Data

### 4.1 Sandboxed virtual fleet

**Description:** A sandboxed virtual fleet of fleet managers and drivers that we can use to collect data on. Simulated personas (managers, drivers) generate trip data, out-of-route behavior, and usage patterns in a controlled environment.

**Use cases:**
- **Synthetic data** — Generate realistic trip data for testing, analytics, and model training
- **Future ideas** — Validate new features (driver ranking, route deviation map, etc.) against simulated behavior before real users
- **Future improvements** — Stress-test backend, UI, and reporting with high volume or edge cases
- **Anything** — Flexible sandbox for experimentation, demos, or research

**Placement:** Separate simulation layer or service; could feed into a test/dev environment, a synthetic-data pipeline, or the shared local reporting pool.

**Dependencies:** Simulation engine or scripted personas, data schema alignment with real app, privacy-safe (no real PII), and additive export into the shared local pool.

**Shared-pool contract:**
- **Synthetic only** — Virtual-fleet output must remain **PLATINUM** or **SILVER**; never **GOLD**
- **No Room contamination** — Virtual-fleet rows must not be inserted into the app's production `trips` table
- **Export path** — Synthetic batches may be exported to the shared local reporting pool, where they stay queryable and removable by batch id
- **Reference data** — External fleet/trucking datasets may improve realism, but must stay tagged as reference-only, not firsthand trip truth

**Status:** Approved for implementation. Production path uses a separate sandbox service plus shared-pool export; OutOfRouteBuddy human Room data remains the source of truth.

---

## 5. UI polish / icons

### 5.1 Trash can icon beautification (Heavy)

**Description:** Beautify the trash-can (delete) icons used in the app and make them look more professional. **Take great care in selecting the most aesthetic icon** — consider Material Design 3 delete/trash variants, outline vs filled, stroke weight, and consistency with the rest of the app’s icon set.

**Current usage:** `app:icon="@drawable/ic_delete_trash"` on the delete button in the trip history stat card (`item_trip_history_stat_card.xml`). Replace or refine the drawable so the icon is professional and on-brand.

**Placement:** Delete button(s) in trip history stat card; any other delete/trash actions that share the same or similar icon.

**Acceptance:** Icon chosen is visually consistent with the app; professional look; user approves via **visual approval** (see LOOP_TIERING § Visual Approval Clause — generate image/mockup, user says "approve 100% implement" before implementation).

**Status:** Sandboxed 100%. Heavy tier. Requires visual approval before implementation.

---

## 6. Navigation / app chrome

### 6.1 Scrolling top toolbar / taskbar (Heavy)

**Description:** Add a scrolling top toolbar or taskbar — a persistent top bar that can scroll or adapt (e.g. with content) to keep key actions or navigation visible while using the app.

**Placement:** Top of the main app screen(s). Exact behavior (scroll-with-content vs sticky, height, contents) to be decided with visual approval.

**Dependencies:** Layout changes to main activity/fragments; coordination with existing title/header; visual approval before implementation.

**Status:** Sandboxed 100%. Heavy tier. Requires visual approval before implementation.

---

### 6.2 Hamburger menu to the left of the "Out of route" title (Heavy)

**Description:** Add a hamburger menu icon to the **left** of the "Out of route" title in the app bar. Tapping it opens a navigation drawer or menu (e.g. Settings, History, Help).

**Placement:** App bar / toolbar: `[ hamburger icon ]  Out of route  [ optional other actions ]`. Left side, before the title.

**Dependencies:** Navigation drawer or slide-out menu; wiring to existing destinations (e.g. Settings, Trip History); visual approval before implementation.

**Status:** Sandboxed 100%. Heavy tier. Requires visual approval before implementation.

---

## 7. Branding

### 7.1 Possible app name change (Heavy)

**Description:** Consider renaming the app. New name ideas and criteria can be brainstormed later (e.g. branding, clarity, store discoverability). No change until user approves direction and final name.

**Placement:** Affects app title, launcher name, store listing, and any in-app references to the app name.

**Dependencies:** Name ideas (to be explored later); user approval; string/resources and store asset updates.

**Status:** Sandboxed 100%. Heavy tier. Ideas and options to be thought through later; no implementation without user approval.

---

## 8. Goals & progress

### 8.1 OOR percentage goal / target (Heavy)

**Description:** User sets a target out-of-route percentage (e.g. 10%); the app shows progress toward that goal (e.g. "This month: 12% OOR vs goal 10%" or a simple progress indicator). Supports mission: "report and improve OOR performance."

**Placement:** Statistics section (period summary) or Settings (goal setting). Single screen or row; no new data model beyond one stored preference and existing monthly stats.

**Dependencies:** One preference (target OOR %); read-only use of existing getMonthlyTripStatistics(); visual approval before implementation.

**Status:** Sandboxed 100%. Heavy tier. Requires visual approval before implementation.

---

## 9. Notifications

### 9.1 End-of-day trip summary notification (opt-in) (Heavy)

**Description:** Optional push notification when the day ends, summarizing that day’s trips (e.g. "3 trips today, 8% OOR"). Opt-in only; off by default. Supports solo drivers gaining useful data without requiring them to open the app.

**Placement:** Settings (opt-in toggle); system notification (e.g. after midnight or at a user-chosen "end of day" time). Uses existing trip data; no server required (local scheduling).

**Dependencies:** WorkManager or AlarmManager for daily trigger; notification channel; SettingsManager for opt-in preference; visual approval before implementation.

**Status:** Sandboxed 100%. Heavy tier. Requires visual approval before implementation.

---

## 10. OOR display

### 10.1 OOR over/under view with color semantics (Heavy)

**Description:** Show OOR as **over/under** (e.g. "Under by 2.5%" or "Over by 10%") and use color to signal quality: **negative OOR** (under route) = bright matrix green; **zero / low positive** = glowey blue; **excess OOR** (high positive) = red. Applies to day indicators, trip cards, or statistics row. Thresholds can align with existing efficiency rating (e.g. Excellent/Good/Fair/Poor). See [OOR_VIEW_AND_EDGE_CASES_STUDY.md](./OOR_VIEW_AND_EDGE_CASES_STUDY.md).

**Placement:** Calendar dots, trip history cards, statistics row, or period summary. Accessibility: do not rely on color alone (e.g. "Under"/"Over" label or icon).

**Dependencies:** Design approval for thresholds and exact colors; contrast/accessibility check; visual approval before implementation.

**Status:** Sandboxed 100%. Heavy tier. Requires visual approval. Study session doc created; implementation deferred.

---

## 11. Edge cases

### 11.1 Load cancelled mid-trip / new load given (Heavy — get future context)

**Description:** Edge case: user has a load **cancelled halfway through** and is given **another load**. Need to simulate and design how the app handles this — e.g. save first segment as partial trip and start new trip for new load, or replace load in place, or support multi-segment trips. **Get future context:** Research or user input on how drivers/dispatch handle cancelled-load workflow before locking data model.

**Placement:** Trip start/end flow; possibly new "Load cancelled" or "Change load" action. Back-end (data model), Design (workflow), UI/UX (flows).

**Dependencies:** Future context (driver/dispatch semantics); design decision: split vs replace vs multi-segment; schema/UX if multi-segment. See [OOR_VIEW_AND_EDGE_CASES_STUDY.md](./OOR_VIEW_AND_EDGE_CASES_STUDY.md) § 4.

**Status:** Sandboxed 100%. Heavy tier. Study session doc created; get-future-context todo added; implementation deferred until design and context are clear.

---

## 12. Agent hub & text command

### 12.1 Text command: "Send to hub" (Heavy)

**Description:** When the user says **"send to hub"** (in chat to an agent, or in-app as a text command), the meaning is: put the completed, precious data **here** — `docs/agents/data-sets/hub/`. **Not GitHub.** The hub is our shared data folder for agent outputs. In-app: a future feature could let the user type "send to hub" to export current trip/stats to a file that lands in or syncs to the hub.

**Placement:** Agent behavior: respond to "send to hub" by writing to `docs/agents/data-sets/hub/` (see [SEND_TO_HUB_PROMPT.md](../../agents/data-sets/hub/SEND_TO_HUB_PROMPT.md)). In-app: optional text field or command UI; export flow to hub path or user-accessible file.

**Dependencies:** Agent prompt is live. In-app text command would need export format and UX; hub path is `docs/agents/data-sets/hub/`.

**Status:** Sandboxed 100% for in-app UI. Agent instruction doc and prompt in place. **Do not add in-app UI without explicit approval.**

---

## 13. Sandbox tooling

### 13.1 Lightweight feature preview container (Heavy)

**Description:** Build a minimal "feature preview container" so a user can preview any fully sandboxed feature before implementation. The container should be lightweight, isolated, and reversible.

**Intent:** Validate concept and UX early without merging full production logic. This is a preview-only shell for sandboxed features.

**Placement:** Sandbox build variant or isolated preview screen/container (non-production path) with one feature mounted at a time.

**Minimum scope:**
- Bare-minimum container UI shell
- Feature selector for sandboxed entries
- Preview mode label/watermark
- No production data mutation by default

**Dependencies:** Sandbox gating docs, feature-flag or build-variant strategy, visual approval before implementation.

**Validation checklist (30% starter):**
1. Pick container strategy (build variant vs isolated host screen)
2. Define preview data source (mock/synthetic only)
3. Define "safe off switch" (flag or route guard)
4. Define one pilot feature for preview mount

**Status:** Sandboxed 100%. Heavy tier. Use question lock + visual approval before implementation.

---

## 14. Extended heavy backlog

All items below are sandboxed Heavy ideas (future features), documented for staged evaluation.

| ID | Idea | Placement | Notes | Status |
| --- | --- | --- | --- | --- |
| 14.1 | Predictive OOR risk scoring | Statistics / trip details | Forecast likely off-route risk before trip close. | Sandboxed 100% |
| 14.2 | Fuel cost overlay per deviation | Trip history / stats | Estimate fuel impact of route deviation. | Sandboxed 100% |
| 14.3 | Driver coaching timeline | History detail view | Time-ordered coaching hints per trip. | Sandboxed 100% |
| 14.4 | Geofence hotspot atlas | Map analytics module | Aggregate frequent deviation hotspots. | Sandboxed 100% |
| 14.5 | Weather and traffic context enrich | Trip metadata pipeline | Attach weather/traffic context to deviations. | Sandboxed 100% |
| 14.6 | Load type impact analyzer | Reports/statistics | Compare OOR by load type/category. | Sandboxed 100% |
| 14.7 | Alternative route recommendation sandbox | Preview container | Show candidate safer routes post-trip. | Sandboxed 100% |
| 14.8 | Shift fatigue signal estimator | Trip analytics | Heuristic fatigue flags from trip patterns. | Sandboxed 100% |
| 14.9 | Anomaly replay debugger | Internal diagnostics screen | Replay anomalous route moments for QA. | Sandboxed 100% |
| 14.10 | Policy rules engine (sandbox) | Backend rules module | Configurable OOR policy checks. | Sandboxed 100% |
| 14.11 | Team benchmark cohorts | Multi-user analytics | Compare against similar route cohorts. | Sandboxed 100% |
| 14.12 | Privacy-preserving telemetry exporter | Export pipeline | Share aggregate-only telemetry safely. | Sandboxed 100% |
| 14.13 | Offline conflict visualizer | Sync/debug UI | Show pending/merged sync conflicts clearly. | Sandboxed 100% |
| 14.14 | Trip correction wizard | Trip edit workflow | Guided correction for partially wrong trips. | Sandboxed 100% |
| 14.15 | Smart notification scheduler | Notification settings | Best-time daily summaries and nudges. | Sandboxed 100% |
| 14.16 | Voice note to trip metadata | Trip-end flow | Attach short notes to trip events. | Sandboxed 100% |
| 14.17 | Incident tagging workflow | Trip details / exports | Standardized tags for incidents/deviations. | Sandboxed 100% |
| 14.18 | Route compliance heatmap calendar | Calendar analytics | Calendar heatmap for compliance trends. | Sandboxed 100% |
| 14.19 | AI trip summary draft assistant | Summary export tool | Draft concise trip summaries for review. | Sandboxed 100% |
| 14.20 | Custom KPI builder (advanced) | Statistics settings | User-defined KPI formulas and thresholds. | Sandboxed 100% |
| 14.21 | Multi-stop segment planner | Trip planning sandbox | Segment-aware planning and analysis. | Sandboxed 100% |
| 14.22 | Dispatch handoff package export | Export/share | Structured package for dispatcher handoff. | Sandboxed 100% |
| 14.23 | Attachment evidence locker | Trip details | Attach photos/docs to trip records. | Sandboxed 100% |
| 14.24 | Webhook integration sandbox | Integration layer | Outbound events to external systems. | Sandboxed 100% |
| 14.25 | Enterprise SSO groundwork | Auth platform | SSO-ready architecture preparation. | Sandboxed 100% |
| 14.26 | Data retention policy simulator | Settings/admin tools | Simulate retention rules before applying. | Sandboxed 100% |
| 14.27 | Audit trail explorer | Diagnostics/audit view | Readable history of key app actions. | Sandboxed 100% |
| 14.28 | Synthetic-to-human parity checker | Data quality tooling | Compare synthetic behavior to real-world patterns. | Sandboxed 100% |
| 14.29 | Documentation lint remediation automation | Tooling/process | Automated docs lint normalization workflow. | Sandboxed 100% |

---

## How to Promote an Idea

1. **User approval** — User explicitly approves moving idea from sandbox to ROADMAP.
2. **Feature brief** — Create `docs/product/FEATURE_BRIEF_<name>.md` per design process.
3. **Visual approval** — Before implementation: generate a simple image showing where the feature goes and what it looks like. User must say **"approve 100% implement"** before implementation. See [LOOP_TIERING.md](../automation/LOOP_TIERING.md) § Visual Approval Clause.
4. **Prioritize** — Add to ROADMAP "What's next" or "Prioritization" section.
5. **Help & Info** — Optionally add to `help_future_features` in strings.xml for user visibility.

---

## Maintenance

- **Improvement Loop:** May improve this doc (organization, cross-links, index) but will not implement ideas.
- **When adding ideas:** Use the category structure; add to index; keep sandbox notice visible.
- **When promoting:** Remove from this doc or mark "Promoted to ROADMAP" and add date.

---

*Integrates with [SANDBOX_TESTING.md](../automation/SANDBOX_TESTING.md) and [ROADMAP.md](./ROADMAP.md).*
