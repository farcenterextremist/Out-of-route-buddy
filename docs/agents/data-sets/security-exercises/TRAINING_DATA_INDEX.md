# Security Training Data Index

This index lists all training data files produced by Purple Team simulations for use in agent training, few-shot examples, or security dashboards.

## Format

Each `*-purple-training.json` file contains:

```json
{
  "exercise_id": "YYYY-MM-DD-purple-simulations",
  "date": "YYYY-MM-DD",
  "source": "run_purple_simulations.py",
  "unit_tests_passed": true,
  "validation_simulations": [
    {
      "attack": "validate_unknown_top_level",
      "expected": "reject",
      "actual": "reject",
      "passed": true,
      "blue_visibility": "validate_design_keys in sync_service"
    }
  ],
  "synthetic_scenarios": [
    {"scenario_id": "direct_ignore_previous", "category": "prompt_injection", "expected": "flag", "automated": false}
  ],
  "http_simulations": [],
  "summary": {
    "unit_tests": "pass",
    "validation_passed": 3,
    "validation_total": 3,
    "http_passed": 0,
    "http_total": 0
  }
}
```

## Files

| File | Date | validation_passed | Notes |
|------|------|------------------|-------|
| `artifacts/2026-03-11-purple-training.json` | 2026-03-11 | 2/2 | Playbook discovery; 7 synthetic scenarios |

**Creating new files:** Run `python scripts/purple-team/run_purple_simulations.py --full` or `./gradlew :app:securitySimulations`. Use `--with-http` when sync service is running to include HTTP attack results.

## Use cases

- **Few-shot examples:** Include validation_simulations and http_simulations in prompts for Red/Blue agent context
- **Fine-tuning:** Aggregate multiple runs for security-focused model training
- **Dashboards:** Parse summary for pass/fail counts over time
- **Regression:** Compare new runs to baseline to detect control degradation
