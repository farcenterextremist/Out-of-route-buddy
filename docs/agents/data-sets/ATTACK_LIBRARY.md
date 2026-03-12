# Attack Library — Cyber Security Loop

**Purpose:** Index of attack playbooks for Red/Blue/Purple exercises. Each playbook defines simulation_type, expected behavior, and Blue alarm.

**Location:** Playbooks live in `docs/agents/data-sets/attack-playbooks/`

---

## Playbook Format

Each playbook file includes:

| Field | Description |
|-------|-------------|
| `id` | Unique identifier (e.g. trip-validation-bypass) |
| `name` | Human-readable name |
| `surface` | Target (app, Cursor, sync) |
| `simulation_type` | validation \| unit_test \| prompt_injection |
| `technique_id` | MITRE ATT&CK or ATLAS (optional) |
| `expected` | reject \| pass \| flag |
| `blue_alarm` | What should detect it |

---

## Playbook Index

| ID | Name | Surface | simulation_type | expected | blue_alarm |
|----|------|---------|-----------------|----------|------------|
| trip-validation-bypass | Trip validation bypass (NaN, negative) | App | validation | reject | ValidationFramework, InputValidator |
| path-traversal | Path traversal in file input | App | validation | reject | InputValidator.sanitizeFilePath |
| export-path-traversal | FileProvider path traversal | App | unit_test | reject | FileProvider scope, SECURITY_NOTES §5 |
| rules-backdoor | Cursor rules backdoor | Cursor | prompt_injection | flag | PR review, SECURITY_NOTES §13 |
| prompt-injection-readme | README prompt injection | Cursor | prompt_injection | flag | Self-improvement rules, audit |

---

## Categories

### App (Validation / Unit Test)

- **trip-validation-bypass** — NaN, negative miles, out-of-range; ValidationFramework and InputValidator must reject
- **path-traversal** — `../../etc/passwd`, `~/secret.txt`; InputValidator.sanitizeFilePath must reject
- **export-path-traversal** — User-controlled paths in FileProvider; fixed patterns only

### Cursor (Agent-Driven / Prompt Injection)

- **rules-backdoor** — Hidden instruction in .mdc; requires PR review
- **prompt-injection-readme** — Hidden instruction in README; agent must flag

---

## Adding Playbooks

1. Create `docs/agents/data-sets/attack-playbooks/<id>.md`
2. Add row to this index
3. Ensure `run_purple_simulations.py` can read it (if simulation_type is validation or unit_test)

---

## Synthetic Data Sets

Scenarios for synthetic data generation: [SYNTHETIC_ATTACK_SCENARIOS.md](./SYNTHETIC_ATTACK_SCENARIOS.md). Training JSON includes `synthetic_scenarios` block with scenario_id, category, technique, payload_sample, expected, automated.

---

*Used by run_purple_simulations.py and Red/Blue agents.*
