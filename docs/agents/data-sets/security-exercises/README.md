# Security exercises – Red, Blue, Purple

This folder holds **run logs** and **artifacts** from Red Team, Blue Team, and Purple Team exercises so we have proof of work and can reuse data in future runs.

## Contents

- **Logs:** Dated or themed exercise logs (e.g. `2025-02-22-purple-outofroutebuddy.md`, `YYYY-MM-DD-purple-simulations.md`) with Red actions and Blue checks in the format defined in `docs/agents/security-team-proof-of-work.md`.
- **artifacts/:** Scripts, payloads, PoC code, config snippets, and **training data** (`*-purple-training.json`) produced during exercises. Reference these from the run logs.

## Automated Purple simulations

Run `python scripts/purple-team/run_purple_simulations.py` to generate new exercise logs and training data. See `scripts/purple-team/README.md` for options (`--with-http` for HTTP attack simulations).

## Training data

Files `artifacts/YYYY-MM-DD-purple-training.json` contain structured results for agent training:
- Unit test pass/fail
- Validation simulation results (attack, expected, actual, passed, blue_visibility)
- HTTP simulation results (when run with `--with-http`)

Use for few-shot examples, fine-tuning, or security dashboards.

## Usage

- When Red or Blue (or the user) saves proof of work, write logs here or in `docs/agents/security-team-proof-of-work.md` and put files in `artifacts/`.
- To import for a new session: point the agent at this folder and at `docs/agents/security-team-proof-of-work.md` to continue or re-run exercises.
