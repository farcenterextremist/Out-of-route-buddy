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

**Sandbox status:** Ideas documented in this file are **100% sandboxed**. You can add Heavy-tier ideas here whenever you choose. Run **Medium tier** Improvement Loop to apply sandbox improvements (index, cross-links, new ideas).

---

## Index by Category

| Category | Ideas |
|----------|-------|
| [Social / Data Sharing](#1-social--data-sharing) | Multi-user data sharing, driver ranking |
| [Engagement / Updates](#2-engagement--updates) | Optional email signup for new updates and features |
| [Visualization](#3-visualization) | Ranking chart, route deviation map (instant replay) |
| [Simulation / Synthetic Data](#4-simulation--synthetic-data) | Sandboxed virtual fleet |

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

**Validation checklist (40%):** (1) Define expected-path source (loaded miles route? bounce waypoints?); (2) Map SDK choice (Google Maps vs Mapbox); (3) Red-line rendering from GPS track vs expected; (4) Stat card placement (expandable block); (5) Performance with long trips.

**Status:** Sandboxed 100%. Not in ROADMAP yet.

---

## 4. Simulation / Synthetic Data

### 4.1 Sandboxed virtual fleet

**Description:** A sandboxed virtual fleet of fleet managers and drivers that we can use to collect data on. Simulated personas (managers, drivers) generate trip data, out-of-route behavior, and usage patterns in a controlled environment.

**Use cases:**
- **Synthetic data** — Generate realistic trip data for testing, analytics, and model training
- **Future ideas** — Validate new features (driver ranking, route deviation map, etc.) against simulated behavior before real users
- **Future improvements** — Stress-test backend, UI, and reporting with high volume or edge cases
- **Anything** — Flexible sandbox for experimentation, demos, or research

**Placement:** Separate simulation layer or service; could feed into a test/dev environment or a synthetic-data pipeline.

**Dependencies:** Simulation engine or scripted personas, data schema alignment with real app, privacy-safe (no real PII).

**Status:** Sandboxed 100%. Heavy tier. Not in ROADMAP yet.

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
