# Playbook: Export Path Traversal

**id:** export-path-traversal  
**name:** FileProvider path traversal  
**surface:** App — TripExporter, FileProvider  
**simulation_type:** unit_test  
**technique_id:** (custom)  
**expected:** reject  
**blue_alarm:** FileProvider uses fixed patterns; no user input in paths (SECURITY_NOTES §5)

---

## Red Action

Attempt to use user-controlled paths when building FileProvider URIs for export.

## Expected Behavior

- TripExporter uses fixed patterns (`trips_export_*.csv`, `trips_report_*.txt`)
- Paths derived from context.cacheDir only
- No user input in file paths

## Simulation

Code review / unit test verifies export flow does not accept user paths. Purple exercise 2025-02-22 documented in security-exercises.
