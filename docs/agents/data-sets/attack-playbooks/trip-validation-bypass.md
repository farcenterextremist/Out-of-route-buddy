# Playbook: Trip Validation Bypass

**id:** trip-validation-bypass  
**name:** Trip validation bypass (NaN, negative, out-of-range)  
**surface:** App — ValidationFramework, InputValidator  
**simulation_type:** validation  
**technique_id:** (custom)  
**expected:** reject  
**blue_alarm:** ValidationFramework.validateMiles, InputValidator.sanitizeMiles reject invalid values

---

## Red Action

Send malicious inputs to validation layer:

- `Double.NaN` or string "NaN"
- Negative miles (e.g. -10)
- Out-of-range (e.g. 20000)
- Empty or blank string

## Expected Behavior

- ValidationFramework.validateMiles returns ValidationResult(isValid=false)
- InputValidator.sanitizeMiles returns null
- Trip domain model throws IllegalArgumentException for invalid values

## Simulation

SecuritySimulationTest.kt calls ValidationFramework and InputValidator with these inputs; asserts reject.
