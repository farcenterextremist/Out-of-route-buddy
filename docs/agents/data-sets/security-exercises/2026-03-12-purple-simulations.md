# Purple Simulations — 2026-03-12

**exercise_id:** 2026-03-12-purple-simulations
**unit_tests_passed:** True

## Validation Simulations

- trip_validation_rejects_nan: PASS (expected reject)
- trip_validation_rejects_negative: PASS (expected reject)
- trip_validation_rejects_out_of_range: PASS (expected reject)
- input_validator_rejects_path_traversal: PASS (expected reject)

## Synthetic Scenarios (for data sets)

- trip_nan: app_validation / validation_bypass (automated)
- trip_negative: app_validation / validation_bypass (automated)
- path_traversal_parent: app_validation / path_traversal (automated)
- direct_ignore_previous: prompt_injection / direct (agent-driven)
- indirect_readme: prompt_injection / indirect (agent-driven)
- typoglycemia: prompt_injection / typoglycemia (agent-driven)
- rules_backdoor: prompt_injection / rules_tampering (agent-driven)
