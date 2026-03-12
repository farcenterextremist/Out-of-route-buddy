# Simulation Environment — Cyber Security Loop

**Purpose:** Define what is simulated vs. agent-driven in the Cyber Security Loop. Ensures safe boundaries and clear expectations.

---

## What Is Simulated (Automated)

| Surface | How | Expected |
|---------|-----|----------|
| **Trip validation** | SecuritySimulationTest.kt calls ValidationFramework, InputValidator with NaN, negative miles, path traversal | Reject; no exception swallowed |
| **Entity validation** | ValidationFramework.validateTripEntity() with invalid data | Reject |
| **Unit test wrapper** | run_purple_simulations.py runs `./gradlew :app:testDebugUnitTest` | Pass |
| **Playbook-driven** | run_purple_simulations.py reads attack-playbooks; runs validation simulations | Output includes playbook_id, passed/failed |

---

## What Requires Agent (Manual/Agent-Driven)

| Surface | Why | How |
|---------|-----|-----|
| **Prompt injection** | Requires agent context; cannot fully automate | Harness produces test cases; Red/Blue agents verify in chat |
| **Rules backdoor** | Would require modifying .cursor/rules | Red simulates; Blue checks; no actual file change |
| **Context poisoning** | Cross-project reference in agent context | Red/Blue protocol; document in proof of work |
| **Doc injection** | Would require modifying docs | Red simulates; Blue checks; PR review as defense |

---

## Safe Boundaries

| Boundary | Rule |
|----------|------|
| **No destructive commands** | Simulations never run `rm -rf`, `del`, or similar |
| **No real PII** | Use mock coordinates, fake trip IDs only |
| **No production** | All runs are local; no external services unless explicitly scoped |
| **No auto-exec from web** | Research findings are documented; no code from web executed without review |
| **Sandbox** | Unit tests and Python script run in isolated context |

---

## Integration with Improvement Loop

When Improvement Loop has **Security focus** (Phase 1.2):

1. Run `./gradlew securitySimulations` (or `run_purple_simulations.py` directly)
2. Log results to proof of work
3. Add metrics to summary (simulations_run, passed, failed)

Agent-driven Purple exercises (Red attacks, Blue checks) can run in same session or separately.

---

*Reference for CYBER_SECURITY_LOOP_ROUTINE and run_purple_simulations.py.*
