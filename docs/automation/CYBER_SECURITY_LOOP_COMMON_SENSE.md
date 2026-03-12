# Cyber Security Loop — Common Sense Parameters

**Purpose:** Non-negotiable rules for every Cyber Security Loop run. Ensures safe, scoped simulations with no destructive actions.

**References:** [CYBER_SECURITY_LOOP_ROUTINE.md](./CYBER_SECURITY_LOOP_ROUTINE.md), [SIMULATION_ENVIRONMENT.md](./SIMULATION_ENVIRONMENT.md)

---

## Before Any Changes

| Parameter | Rule |
|-----------|------|
| **Pre-loop checkpoint** | Create checkpoint (git commit or tag) before Phase 1 (Simulate). Record in summary. Enables "revert" if something breaks. |
| **Scope lock** | Attack only agreed targets (this codebase, validation layer, export flow, Cursor rules). Do not touch production or real user data. |
| **No destructive actions** | Simulations must not run destructive commands, delete data, or modify production. Produce code/steps for review first. |

---

## During Execution

| Parameter | Rule |
|-----------|------|
| **Sandbox only** | All simulations run in unit test context or Python script. No shell execution of untrusted payloads. |
| **No PII** | Never use real coordinates, trip IDs, or user data in simulations. Use mock/fake data only. |
| **Validation inputs only** | Validation simulations send malicious inputs (NaN, negative, path traversal) to ValidationFramework/InputValidator. Assert reject. |
| **Tests must pass** | If SecuritySimulationTest fails → fix or revert. Do not leave tests red. |

---

## Simulation Boundaries

| Allowed | Not Allowed |
|---------|-------------|
| Unit tests with mock data | Real network requests (unless --with-http and sync service running) |
| ValidationFramework with invalid inputs | Destructive shell commands |
| Python script producing test cases | Modifying .cursor/rules with malicious content |
| Agent-driven Purple review | Auto-executing code from web content |

---

## When Things Go Wrong

| Parameter | Rule |
|-----------|------|
| **Simulation fails** | Document in proof of work; fix or defer. Do not pretend pass. |
| **Uncertain** | When in doubt → suggest in summary, don't implement. Ask user. |
| **Heavy task** | New attack types or harness changes require user approval. |

---

*Read at Cyber Security Loop start. Integrates with IMPROVEMENT_LOOP_COMMON_SENSE.*
