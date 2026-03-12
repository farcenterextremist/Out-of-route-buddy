# export-path-traversal

| Field | Value |
|-------|-------|
| id | export-path-traversal |
| name | FileProvider path traversal |
| surface | App |
| simulation_type | unit_test |
| expected | reject |
| blue_alarm | FileProvider scope, SECURITY_NOTES §5 |
| technique_id | — |

## Description

User-controlled paths in FileProvider; fixed patterns only. TripExporter must only write to context.cacheDir.
