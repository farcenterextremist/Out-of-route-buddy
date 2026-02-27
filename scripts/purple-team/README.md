# Purple Team Simulations — OutOfRouteBuddy

Automated Red/Blue Team simulations for security validation. Runs attack scenarios and documents results for proof of work and training data.

## Quick Start

```bash
# From repo root
python scripts/purple-team/run_purple_simulations.py
```

## What It Does

1. **Unit tests** — Runs `scripts/emulator-sync-service/test_sync_service.py` (key allowlist, size limit, validation)
2. **Validation simulations** — Tests `validate_design_keys` directly (unknown key rejection, known path acceptance)
3. **HTTP simulations** (optional) — Sends attack payloads to sync service at `http://127.0.0.1:8765`

## Options

| Option | Description |
|--------|-------------|
| `--output-dir PATH` | Where to write exercise logs (default: `docs/agents/data-sets/security-exercises/`) |
| `--with-http` | Run HTTP attack simulations (requires sync service: `python scripts/emulator-sync-service/sync_service.py` or `start_sync_service.bat`) |

## HTTP Simulations (--with-http)

To run HTTP attack simulations:

1. Start the sync service: `scripts/emulator-sync-service/start_sync_service.bat` or `python scripts/emulator-sync-service/sync_service.py`
2. Run: `python scripts/purple-team/run_purple_simulations.py --with-http`

Attack scenarios:
- **valid_design** — Valid design JSON (expect 200)
- **unknown_key_injection** — Design with key not in allowlist (expect 400)
- **oversized_payload** — Body > 64KB (expect 413)

## Outputs

| File | Purpose |
|------|---------|
| `docs/agents/data-sets/security-exercises/YYYY-MM-DD-purple-simulations.md` | Exercise log (proof of work) |
| `docs/agents/data-sets/security-exercises/artifacts/YYYY-MM-DD-purple-training.json` | Training data (JSON) for agent ingestion |

## Training Data Format

The JSON training file contains:
- `exercise_id`, `date`, `source`
- `unit_tests_passed`
- `validation_simulations` — list of {attack, expected, actual, passed, blue_visibility}
- `http_simulations` — same structure (when --with-http)
- `summary` — counts for quick reference

Use for agent fine-tuning, few-shot examples, or security audit dashboards.

## References

- Purple Team protocol: `docs/agents/purple-team-protocol.md`
- Proof of work: `docs/agents/security-team-proof-of-work.md`
- Security plan: `docs/security/SECURITY_PLAN.md`
