# Cyber Security — Data Summary & Utilization

**Purpose:** What we collected, what it means, how to use it. For all agents consuming hub data.

---

## Data Collected

| Type | Location | Contents |
|------|----------|----------|
| **Run ledger** | `docs/automation/CYBER_SECURITY_LOOP_RUN_LEDGER.md` | Checkpoint, phase status, proof of work path, validation passed, next steps |
| **Training JSON** | `security-exercises/artifacts/*-purple-training.json` | validation_simulations, synthetic_scenarios, summary |
| **Proof of work** | `security-exercises/*-purple-simulations.md` | Human-readable validation results per playbook |
| **Attack library** | `ATTACK_LIBRARY.md`, `attack-playbooks/*.md` | Playbook index and definitions |

---

## What It Means

- **Validation pass** = app rejected malicious input (NaN, negative, path traversal) → Blue team working
- **Synthetic scenarios** = 7 total: 3 automated (SecuritySimulationTest), 4 agent-driven (Red/Blue manual)
- **Run ledger** = checkpoint for rollback; phase status for debugging

---

## How to Utilize

| Goal | Action |
|------|--------|
| **Regression** | `python scripts/purple-team/diff_training_runs.py <baseline> <current>` |
| **Few-shot** | Use validation_simulations + synthetic_scenarios from training JSON in Red/Blue prompts |
| **Metrics** | Parse summary.validation_passed, validation_total from training JSON |
| **Rollback** | Use Checkpoint from ledger |
| **Run loop** | `./gradlew :app:securitySimulations` |
