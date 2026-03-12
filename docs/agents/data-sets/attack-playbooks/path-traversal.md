# Playbook: Path Traversal

**id:** path-traversal  
**name:** Path traversal in file input  
**surface:** App — InputValidator  
**simulation_type:** validation  
**technique_id:** (custom)  
**expected:** reject  
**blue_alarm:** InputValidator.sanitizeFilePath rejects traversal patterns

---

## Red Action

Send path traversal inputs:

- `../../etc/passwd`
- `~/secret.txt`
- `/etc/passwd`
- `C:\Windows\system32`

## Expected Behavior

- InputValidator.sanitizeFilePath returns null for all above
- No file access outside app-controlled paths

## Simulation

SecuritySimulationTest.kt calls InputValidator.sanitizeFilePath with these inputs; asserts null.
