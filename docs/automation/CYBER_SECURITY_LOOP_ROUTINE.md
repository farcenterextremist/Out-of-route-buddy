# Cyber Security Loop — Routine

**Purpose:** Repeatable security exercise that researches attacks, runs simulations in a safe environment, and integrates with Red/Blue/Purple proof of work.

**Trigger:** User says **"Run Cyber Security Loop"** or **"GO security"** — or runs automatically when Improvement Loop has Security focus (Phase 1.2).

**Upon initiation:** Check todos — [CRUCIAL_IMPROVEMENTS_TODO.md](../CRUCIAL_IMPROVEMENTS_TODO.md), [TOKEN_LOOP_NEXT_TASKS.md](./TOKEN_LOOP_NEXT_TASKS.md) (latest run), [CYBER_SECURITY_LOOP_AUDIT.md](./CYBER_SECURITY_LOOP_AUDIT.md) (blind spots), and any in-progress todos. Prioritize from these lists.

**References:** [CYBER_SECURITY_LOOP_COMMON_SENSE.md](./CYBER_SECURITY_LOOP_COMMON_SENSE.md), [SIMULATION_ENVIRONMENT.md](./SIMULATION_ENVIRONMENT.md), [IMPROVEMENT_LOOP_ROUTINE.md](./IMPROVEMENT_LOOP_ROUTINE.md), [CYBER_SECURITY_LOOP_AUDIT.md](./CYBER_SECURITY_LOOP_AUDIT.md), [SYNTHETIC_ATTACK_SCENARIOS](../agents/data-sets/SYNTHETIC_ATTACK_SCENARIOS.md)

---

## Phases

| Phase | Goal | Actions |
|-------|------|---------|
| **0 Research** | Discover new attacks, defenses, techniques | Web search; update ATTACK_LIBRARY; read CYBER_SECURITY_RESEARCH; check CYBER_SECURITY_LOOP_AUDIT for blind spots |
| **1 Simulate** | Run attacks in safe environment | `./gradlew securitySimulations`; SecuritySimulationTest; run_purple_simulations.py |
| **2 Purple** | Red attacks, Blue checks, fix if missed | Agent-driven or review simulation results; log to proof of work |
| **3 Improve** | Update defenses, summary | Implement Blue fixes; write CYBER_SECURITY_LOOP_SUMMARY_YYYY-MM-DD.md |

---

## Integration Points

| Integration | When | What |
|-------------|------|------|
| **Gradle** | `./gradlew securitySimulations` | Runs SecuritySimulationTest + run_purple_simulations.py |
| **CI** | Push/PR to main/master/develop | security-simulations job in android-tests.yml |
| **Improvement Loop** | Phase 1.2 when Security focus | Runs securitySimulations; logs to proof of work |
| **Loop listener** | run_cyber_security_loop.ps1 | Events: cyber_security_simulate, cyber_security_simulate_result |

---

## Pre-loop Checkpoint

Before Phase 1 (Simulate): Create git tag or commit. Enables revert if simulations cause issues. See [CYBER_SECURITY_LOOP_COMMON_SENSE.md](./CYBER_SECURITY_LOOP_COMMON_SENSE.md).

---

## Tiering

| Tier | Action |
|------|--------|
| **Light** | Run simulations; log results; update proof of work |
| **Medium** | Add 1–2 new playbooks; run full loop |
| **Heavy** | New attack types; new simulation harness; requires user approval |

---

## Output

- **Proof of work:** `docs/agents/data-sets/security-exercises/YYYY-MM-DD-purple-simulations.md`
- **Training JSON:** `docs/agents/data-sets/security-exercises/artifacts/YYYY-MM-DD-purple-training.json` (includes `synthetic_scenarios` for data sets)
- **Summary:** `docs/automation/CYBER_SECURITY_LOOP_SUMMARY_YYYY-MM-DD.md`
- **Run ledger:** `docs/automation/CYBER_SECURITY_LOOP_RUN_LEDGER.md` — record pre/during/post for rollback and improvement

---

## Recording (per run)

| When | Record |
|------|--------|
| **Pre-loop** | Checkpoint (commit/tag), git status, timestamp in ledger |
| **During** | Phase 0–3 status in ledger |
| **Post-loop** | Proof of work, training JSON, summary paths; validation passed; next steps |

---

*Integrates with Improvement Loop. Run every 1–2 weeks or pre-release when Security focus.*
