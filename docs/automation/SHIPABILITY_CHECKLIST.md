# Shipability Checklist — 10-Minute Go/No-Go

**Purpose:** Evidence-based release readiness check. Research: "10-minute go/no-go framework." Run before pre-release loops or when user says "shipability focus."

**Timebox:** 10 minutes.

**References:** [IMPROVEMENT_LOOP_RESEARCH_2025-03.md](./IMPROVEMENT_LOOP_RESEARCH_2025-03.md), [STORE_CHECKLIST.md](../STORE_CHECKLIST.md), [OUTOFROUTEBUDDY_SHIP_INSTRUCTIONS.txt](C:\Users\brand\Desktop\OUTOFROUTEBUDDY_SHIP_INSTRUCTIONS.txt)

---

## Seven Signals

| # | Signal | Check | Pass? |
|---|--------|-------|-------|
| 1 | **Test** | Regression tests pass; adequate coverage for critical paths; no flaky tests | |
| 2 | **Code quality** | Lint zero errors; code reviews complete; no unresolved discussions | |
| 3 | **Requirements** | Features validated against acceptance criteria; PM sign-off if applicable | |
| 4 | **Risk** | High-risk changes have load testing, backward compatibility plans; DB migration safe | |
| 5 | **Environment** | Staging matches production; smoke tests pass | |
| 6 | **Team readiness** | Rollback plan ready; monitoring configured; on-call scheduled | |
| 7 | **Rollout safety** | Canary deployments, feature flags, or gradual rollout planned | |

---

## When to Run

- **Pre-release loop** — Before a loop intended to prepare for ship.
- **Shipability focus** — When this run's focus is Shipability (see [LOOP_FOCUS_ROTATION.md](./LOOP_FOCUS_ROTATION.md)).
- **On demand** — User says "run shipability check."

---

## Integration

- Add as Phase 4.1b in [IMPROVEMENT_LOOP_ROUTINE.md](./IMPROVEMENT_LOOP_ROUTINE.md) when Shipability focus.
- Reference from [STORE_CHECKLIST.md](../STORE_CHECKLIST.md).
- Reference from Desktop `OUTOFROUTEBUDDY_SHIP_INSTRUCTIONS.txt`.
