# Virtual Fleet & Rankings System Design

**Created:** 2026-03-31
**Status:** Implemented (temporary rankings until real driver adoption)

---

## Overview

OutOfRouteBuddy now includes a **virtual fleet** of 8 realistic driver archetypes and a **rankings engine** that lets the real user see where they'd rank against a simulated fleet. This is a bridge feature — it provides value and context *now* while the app builds its real user base.

When enough real drivers are using the app, the virtual fleet can be phased out or kept as benchmarks.

---

## Real-World Research (Sources)

| Data Point | Value | Source |
|-----------|-------|--------|
| Average OOR deviation | ~4.34% | Per Diem Plus / ATRI |
| OOR cost (company driver) | ~$39/week, $1,956/year | Per Diem Plus |
| OOR cost (owner-operator) | ~$107/week, $5,350/year | Per Diem Plus |
| Amazon DSP route length | 120-220 miles, 4-5 hours | Alibaba/industry reports |
| Long-haul miles | 180-740 per trip | ATRI operational costs |
| Gig driver (DoorDash) earnings | $23.42/hr gross, $0.92/mile | EarnifyHub 2026 |
| Gig deliveries per hour | 1.8 (DoorDash), 2.1 (UberEats) | EarnifyHub 2026 |
| Deadhead miles industry avg | 15-25% | OTrucking / ATRI |
| IRS standard mileage rate 2026 | $0.67/mile | IRS |
| Fleet scoring weights | On-time 30-35%, Completion 35% | Dispatch Driver Score |
| Best practice: simplicity | Explainable in 30 seconds | Joy.so driver loyalty research |
| Gamification retention boost | ~15% when well-designed | Trophy.so |

---

## Virtual Fleet: 8 Driver Archetypes

Each archetype has distinct behavioral parameters calibrated against the research above.

| # | Archetype | OOR Range | Miles/Trip | Trips/Day | Personality |
|---|-----------|-----------|------------|-----------|-------------|
| 1 | **The Veteran** | 1-3% | 400-650 | 1.5 | 15yr experience, rock-steady |
| 2 | **The Rookie** | 6-12% | 200-400 | 1.2 | Learning, improving over time |
| 3 | **The Efficient** | 0.5-2% | 300-550 | 1.8 | GPS-obsessed, lowest OOR |
| 4 | **The Detour King** | 8-20% | 250-500 | 1.0 | "Knows a shortcut", frequent stops |
| 5 | **The Night Owl** | 3-6% | 300-500 | 1.3 | Night shifts, construction zones |
| 6 | **The Local** | 4-8% | 80-200 | 3.0 | Short urban runs |
| 7 | **The Highway Warrior** | 1-4% | 500-800 | 0.8 | Interstate long-haul |
| 8 | **The Gig Runner** | 5-15% | 50-150 | 5.0 | Multi-stop delivery |

### Data Generation Features

- **30 days** of trip history per driver (configurable)
- **Deterministic seeding** — same seed = same fleet, for reproducibility
- **Weekday/weekend variation** — 60% trip volume on weekends
- **Improvement trends** — Rookie OOR decreases over the 30-day window
- **Night run probability** — Night Owl has 85% chance per trip
- **GPS quality ranges** — Efficient has best GPS, Gig Runner has worst
- **Natural noise** — Gaussian distribution on OOR%, random breaks, variable speeds
- **All PLATINUM tier** — never contaminates GOLD human data

---

## Rankings System

### Route Efficiency Score (0-100)

| Factor | Weight | How It's Calculated |
|--------|--------|-------------------|
| **OOR Efficiency** | 40% | `max(0, 100 - (avgOOR% / 25 * 100))` — 0% OOR = 100, 25%+ = 0 |
| **Consistency** | 25% | `max(0, 100 - (stdDev * 10))` — low variability = high score |
| **Trip Volume** | 15% | `min(100, trips/30 * 100)` — 30+ trips = full marks |
| **GPS Quality** | 10% | Average GPS quality % across trips |
| **Route Discipline** | 10% | % of trips where OOR < 5% threshold |

### Tier System

| Tier | Percentile | What It Means |
|------|-----------|---------------|
| **Diamond** | Top 5% | Elite route adherence |
| **Platinum** | Top 20% | Excellent performance |
| **Gold** | Top 40% | Above average |
| **Silver** | Top 60% | Average fleet performance |
| **Bronze** | Bottom 40% | Room for improvement |

### Cohort System (Fair Ranking)

Drivers are ranked **only against others with similar trip counts** so new users
never feel permanently behind. The engine assigns a cohort based on completed trips:

| Cohort | Trip Count | Subtitle | Volume Full Marks |
|--------|-----------|----------|-------------------|
| **Newcomer** | 1-10 | "Just Getting Started" | 10 trips |
| **Regular** | 11-30 | "Finding Your Groove" | 30 trips |
| **Experienced** | 31-75 | "Road Tested" | 75 trips |
| **Legend** | 76+ | "Fleet Legend" | 120 trips |

**How it works:**
- Trip Volume score is **relative to the cohort ceiling** — a Newcomer with 8 trips
  gets 80% volume, while a Legend needs 96 trips for the same percentage
- The leaderboard only shows drivers in the user's cohort
- The UI shows a "next cohort" badge: *"3 more trips to Regular!"*
- Virtual fleet has 14 drivers spread across all 4 cohorts (3-4 per bracket)

### Key Design Decisions

1. **Personal, not social** — Rankings are "where would you rank in a fleet?" not competitive social features (per mission doc)
2. **Cohort-based fairness** — New users compete against similarly-experienced drivers, not lifetime veterans
3. **Habitual patterns > isolated incidents** — Consistency weight rewards steady performance, not one good day
4. **Explainable in 30 seconds** — Five clear factors, simple tier names, four intuitive cohorts
5. **Promotion motivation** — "X more trips to reach the next tier" drives engagement
6. **PLATINUM data only** — Virtual fleet never touches Room database or GOLD data
7. **In-memory fleet** — Generated on demand, cached per session, no DB persistence needed

---

## Architecture

```
┌─────────────────────────────┐
│    RankingsViewModel        │  ← UI state management
│    (presentation layer)     │
└──────────┬──────────────────┘
           │
┌──────────▼──────────────────┐
│  FleetRankingsRepository    │  ← Coordinates user data + virtual fleet
│  (domain/ranking)           │
└──────┬───────────┬──────────┘
       │           │
┌──────▼───┐  ┌────▼─────────────┐
│ Rankings │  │ VirtualFleet     │
│ Engine   │  │ DataGenerator    │
│ (scoring)│  │ (archetypes)     │
└──────────┘  └──────────────────┘
       │
┌──────▼───────────────────────┐
│  TripRepository (domain)     │  ← User's real GOLD trips
└──────────────────────────────┘
```

### New Files

| File | Purpose |
|------|---------|
| `domain/models/DriverArchetype.kt` | 8 archetype enum with generation params |
| `domain/models/RankingModels.kt` | RankingCohort, RankingTier, RankingBreakdown, RankingScore, FleetLeaderboard |
| `domain/ranking/RankingsEngine.kt` | Core scoring algorithm |
| `domain/ranking/FleetRankingsRepository.kt` | Coordinates fleet generation + scoring |
| `services/VirtualFleetDataGenerator.kt` | Archetype-based trip generation |
| `presentation/viewmodel/RankingsViewModel.kt` | UI state for rankings screen |

### Modified Files

| File | Change |
|------|--------|
| `di/RepositoryModule.kt` | Added DI bindings for RankingsEngine, VirtualFleetDataGenerator, FleetRankingsRepository |
| `services/VirtualFleetSandboxService.kt` | Added archetype fleet export + injected VirtualFleetDataGenerator |

---

## What's Next

- [ ] **UI**: Wire `nav_rankings` to a real Rankings Fragment (need user approval for UI changes)
- [ ] **Polish**: Add visual tier badges, score animations
- [ ] **Streaks**: Daily/weekly streak tracking for engagement
- [ ] **History**: Track score changes over time (trending up/down)
- [ ] **Real drivers**: When user base grows, transition from virtual fleet to anonymized real benchmarks
- [ ] **Challenges**: Weekly OOR challenges ("Stay under 3% this week")
