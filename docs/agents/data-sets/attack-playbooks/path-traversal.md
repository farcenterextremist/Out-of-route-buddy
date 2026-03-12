# path-traversal

| Field | Value |
|-------|-------|
| id | path-traversal |
| name | Path traversal in file input |
| surface | App |
| simulation_type | validation |
| expected | reject |
| blue_alarm | InputValidator.sanitizeFilePath |
| technique_id | — |

## Description

`../../etc/passwd`, `~/secret.txt`; InputValidator.sanitizeFilePath must reject.
