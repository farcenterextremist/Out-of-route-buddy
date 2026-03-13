# Loop health checks — design and usage

**Purpose:** Define **health checks that run constantly during the loops** using a popular architecture: **liveness** (lightweight, fast) and **readiness** (full gates at phase boundaries). Keeps loops safe and catch environment/regression issues early.

**References:** [IMPROVEMENT_LOOP_ROUTINE.md](./IMPROVEMENT_LOOP_ROUTINE.md), [pulse_check.ps1](../scripts/automation/pulse_check.ps1), [LOOP_MASTER_ROLE.md](./LOOP_MASTER_ROLE.md).

---

## Design (popular patterns)

### 1. Liveness vs readiness

| Type | What it answers | When it runs | Cost |
|------|-----------------|--------------|------|
| **Liveness** | Is the loop environment still valid? (repo, Gradle, key paths, writable logs) | **At loop start** and **at the start of every phase** (Phase 0, 1, 2, 3, 4) | Low — no Gradle, no tests; ~1–2 s |
| **Readiness** | Is the build and test state good enough to proceed? (tests pass, lint) | **At the end of every phase** (after Phase 1, 2, 3, 4) | Higher — runs `gradlew test`, optionally lint; 1–5+ min |

This mirrors **Kubernetes-style** liveness vs readiness: liveness = "is the process alive and its world OK?"; readiness = "is it ready to serve traffic / proceed to next stage?" In our case, "proceed" = move to the next phase or finish the loop.

### 2. Where health checks run (constant during the loop)

| When | Check | Script / action |
|------|--------|-------------------|
| **Before Phase 0** (loop start) | Liveness | `.\scripts\automation\loop_health_check.ps1 -Quick` |
| **Start of Phase 1** | Liveness | `.\scripts\automation\loop_health_check.ps1 -Quick` |
| **End of Phase 1** | Readiness | `.\scripts\automation\pulse_check.ps1 -Note "Phase 1: ..."` |
| **Start of Phase 2** | Liveness | `.\scripts\automation\loop_health_check.ps1 -Quick` |
| **End of Phase 2** | Readiness | `.\scripts\automation\pulse_check.ps1 -Note "Phase 2: ..."` |
| **Start of Phase 3** | Liveness | `.\scripts\automation\loop_health_check.ps1 -Quick` |
| **End of Phase 3** | Readiness | `.\scripts\automation\pulse_check.ps1 -Note "Phase 3: ..."` |
| **Start of Phase 4** | Liveness | `.\scripts\automation\loop_health_check.ps1 -Quick` |
| **End of Phase 4** | Readiness | `.\scripts\automation\pulse_check.ps1 -Note "Phase 4: ..."` |

So **liveness runs at a constant** (every phase boundary at phase start), and **readiness runs at every phase end** via the existing pulse check.

### 3. Gate behavior (optional)

| Mode | On liveness failure | On readiness failure (tests/lint) |
|------|---------------------|-----------------------------------|
| **Warn (default)** | Log to health state file and console; exit 1. Agent can log and continue or abort. | Already handled by pulse_check (logs to pulse_log); agent can continue or revert. |
| **Gate** | Run with `-Gate`; script exits 1 and agent should **not** start the next phase until environment is fixed. | Run pulse_check; if tests fail, treat as gate: do not proceed to next phase until fixed or user says continue. |

**Recommendation:** Use **Warn** by default so one bad check (e.g. network blip) doesn’t stop the whole loop. Use **Gate** when you want strict phase gates (e.g. "do not start Phase 2 if Phase 1 readiness failed").

### 4. Heartbeat (optional)

A **heartbeat** can record "loop still progressing": e.g. write a timestamp to `docs/automation/loop_health_state.json` at each phase start/end. A separate watchdog could then check "last heartbeat &lt; 30 min" to detect a stuck run. The current design uses **liveness at phase boundaries** as the built-in heartbeat; the state file is updated by `loop_health_check.ps1 -Quick`.

---

## What liveness checks (loop_health_check.ps1 -Quick)

| Check | Purpose |
|-------|---------|
| Repo root exists and is directory | Avoid running from wrong path. |
| `gradlew.bat` (or `gradlew`) exists | Build possible. |
| `app` and `app/src` exist | App module present. |
| `docs/automation` exists | Pulse log and health state can be written. |
| `docs/automation` writable | Can append to pulse_log and write health state. |
| (Optional) Last pulse age | If pulse_log exists and is too old (e.g. &gt; 2 hr), warn "no recent pulse" — loop may have stalled. |

All checks are **deterministic and fast** (no Gradle, no tests). Exit 0 = liveness OK; exit 1 = unhealthy (and optional `-Gate` means "do not proceed").

---

## What readiness checks (pulse_check.ps1)

Already implemented:

- **Unit tests:** `.\gradlew.bat :app:testDebugUnitTest` (unless `-Quick`).
- **Lint:** `.\gradlew.bat :app:lintDebug` (when not `-Quick`).
- **Log:** Append to `docs/automation/pulse_log.txt` with timestamp, test result, lint, note.

So **readiness = existing pulse_check** at phase end. No new script; just run it at each phase end as today.

---

## Scripts and wiring

| Script | Purpose |
|--------|---------|
| **loop_health_check.ps1** | Liveness: run with `-Quick` at loop start and at start of each phase. Optional `-Gate` to exit 1 and block. Writes `docs/automation/loop_health_state.json` (timestamp, status, optional last pulse age). |
| **pulse_check.ps1** | Readiness: run at end of each phase (Phase 1–4). Tests + lint + pulse_log. |

**Paths (from repo root):**

```powershell
.\scripts\automation\loop_health_check.ps1 -Quick
.\scripts\automation\loop_health_check.ps1 -Quick -Gate
.\scripts\automation\pulse_check.ps1 -Note "Phase N: ..."
```

---

## Integration with Improvement Loop routine

- **Phase 0 (before research):** Run `loop_health_check.ps1 -Quick`. If exit code non-zero, log "Liveness check failed: [reason]; consider fixing before continuing" and optionally stop.
- **Start of Phase 1, 2, 3, 4:** Run `loop_health_check.ps1 -Quick`. Log result; if `-Gate` and fail, do not start phase tasks until fixed.
- **End of Phase 1, 2, 3, 4:** Run `pulse_check.ps1 -Note "Phase N: ..."` as today. Treat test failure as readiness failure; agent can revert or fix before continuing.

Same pattern can be used for **Token Loop** and **Cyber Security Loop**: run liveness at step/phase boundaries; run a readiness-style check (e.g. listener + snapshot) at step end.

---

## State file (loop_health_state.json)

Written by `loop_health_check.ps1` so the last liveness result and timestamp are visible:

```json
{
  "lastCheck": "2026-03-13T12:00:00Z",
  "status": "ok",
  "checks": { "repo": true, "gradle": true, "app": true, "docs": true, "writable": true },
  "note": "liveness"
}
```

If a check fails, `status` is `"degraded"` or `"fail"` and `checks` indicates which failed. Optional: `lastPulse` from pulse_log so a future watchdog can detect "no pulse in 30 min".

---

*This design follows common CI/CD and container health patterns (liveness/readiness) and keeps health checks running at a constant (every phase boundary) during the loops.*
