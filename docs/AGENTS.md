# OutOfRouteBuddy — Start Here

**Mission:** OutOfRouteBuddy gives drivers advanced analytics and tracking for out-of-route miles.

**Success:** Downloads, useful data for users, iPhone requests.

**Never:** Social features, ads, cloud-first.

---

## Quick links

| Doc | Purpose |
|-----|---------|
| [docs/README.md](docs/README.md) | Documentation index |
| [docs/GOAL_AND_MISSION.md](docs/GOAL_AND_MISSION.md) | Goal, mission, success criteria |
| [docs/agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md](docs/agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md) | Canonical behavior (persistence, recovery, calendar, GPS) |
| [docs/CRUCIAL_IMPROVEMENTS_TODO.md](docs/CRUCIAL_IMPROVEMENTS_TODO.md) | Prioritized improvements |
| [docs/SELF_IMPROVEMENT_PLAN.md](docs/SELF_IMPROVEMENT_PLAN.md) | Self-improvement pillars (run when user directs) |
| [docs/automation/CURSOR_SELF_IMPROVEMENT.md](docs/automation/CURSOR_SELF_IMPROVEMENT.md) | Safe web search, prompt-injection protections, contextualization |
| [docs/automation/SANDBOX_TESTING.md](docs/automation/SANDBOX_TESTING.md) | Feature testing before merge; sandbox phase |
| [docs/automation/AUTONOMOUS_LOOP_SETUP.md](docs/automation/AUTONOMOUS_LOOP_SETUP.md) | Full automation (zero human intervention) |

---

## Improvement Loop

When user says **GO** (or "run improvement loop"):

1. Read [docs/automation/IMPROVEMENT_LOOP_COMMON_SENSE.md](docs/automation/IMPROVEMENT_LOOP_COMMON_SENSE.md) (checkpoint first, tests green, full autonomy)
2. Follow [docs/automation/IMPROVEMENT_LOOP_ROUTINE.md](docs/automation/IMPROVEMENT_LOOP_ROUTINE.md)
3. For autonomy: [docs/automation/AUTONOMOUS_LOOP_SETUP.md](docs/automation/AUTONOMOUS_LOOP_SETUP.md)

---

## Build & test

```bash
./gradlew assembleDebug
./gradlew :app:testDebugUnitTest
```

---

*Solo drivers first; fleet management later. See docs/GOAL_AND_MISSION.md for full context.*
