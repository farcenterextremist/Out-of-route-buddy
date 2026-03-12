# Purple Team Scripts — Cyber Security Loop

Scripts for running attack simulations and producing proof of work.

## run_purple_simulations.py

Runs security attack simulations and produces training JSON.

**Usage:**
```bash
# From repo root
python scripts/purple-team/run_purple_simulations.py
python scripts/purple-team/run_purple_simulations.py --full
python scripts/purple-team/run_purple_simulations.py --validation-only
```

**Options:**
- `--full` (default): Run unit tests (SecuritySimulationTest) + produce training JSON
- `--validation-only`: Skip unit tests; produce structure only
- `--with-http`: Include HTTP simulations when sync service is running (deferred)

**Output:**
- `docs/agents/data-sets/security-exercises/artifacts/YYYY-MM-DD-purple-training.json`
- `docs/agents/data-sets/security-exercises/YYYY-MM-DD-purple-simulations.md`

## Integration Points

| Integration | Command |
|-------------|---------|
| **Gradle** | `./gradlew securitySimulations` |
| **CI** | security-simulations job in android-tests.yml |
| **Improvement Loop** | Phase 1.2 when Security focus |

## Playbooks

Attack playbooks: `docs/agents/data-sets/attack-playbooks/`

Training data index: `docs/agents/data-sets/security-exercises/TRAINING_DATA_INDEX.md`
