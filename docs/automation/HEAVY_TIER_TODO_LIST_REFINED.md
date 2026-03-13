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
| 6 | Sandboxed virtual fleet | § 4.1 | Simulated fleet/drivers for data and testing. |

### UI polish / icons
| # | Idea | FUTURE_IDEAS | Notes |
|---|------|--------------|--------|
| 7 | Trash can icon beautification | § 5.1 | Material Design 3–style delete icon; visual approval. |

### Navigation / app chrome
| # | Idea | FUTURE_IDEAS | Notes |
|---|------|--------------|--------|
| 8 | Scrolling top toolbar / taskbar | § 6.1 | Persistent top bar; scroll or adapt. |
| 9 | Hamburger menu left of "Out of route" title | § 6.2 | Drawer/menu; Settings, History, Help. |

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

---

## Other Heavy-tier items (from CRUCIAL / tiering)

| # | Item | Source | Notes |
|---|------|--------|--------|
| 16 | Statistics: monthly only | CRUCIAL §9 | Remove weekly/yearly from UI; ViewModel + repo + QA. High impact; deferred until UI approved. |
| 17 | Drastic loop improvements | LOOP_TIERING | Any change to routine, phases, tier definitions, or new loops = Heavy; document only, human approval. |
| 18 | ROADMAP features (after sandbox) | LOOP_TIERING | Auto drive, Reports screen, address input — only after sandbox validation. |
| 19 | Large refactors | LOOP_TIERING | Repository interface changes; multi-file; cross-module. |
| 20 | Architecture (major toolchain) | LOOP_TIERING | e.g. AGP 9 migration (Gradle 9 done); schema changes; new persistence paths. |

---

## Summary counts

| Type | Count |
|------|--------|
| **FUTURE_IDEAS Heavy ideas** | 15 |
| **Other Heavy (CRUCIAL / tiering)** | 5 |
| **Total refined Heavy items** | 20 |

---

## How to use this list

- **Prioritize:** Mark favorites in [HEAVY_IDEAS_FAVORITES.md](./HEAVY_IDEAS_FAVORITES.md); loop surfaces those first.
- **Implement one-by-one:** For each Heavy item, ask "Are you ready to implement this?" → question lock (image/layout/merge sim) → user says **"approve 100% implement"** before implementation.
- **Cap:** When Heavy list in FUTURE_IDEAS / Favorites reaches ~50, loop switches to judge/critique mode instead of adding new Heavy ideas.

---

*Generated from FUTURE_IDEAS, HEAVY_IDEAS_FAVORITES, LOOP_TIERING, and CRUCIAL_IMPROVEMENTS_TODO. Update when new Heavy ideas are added or completed.*
