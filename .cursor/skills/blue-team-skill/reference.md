# Blue Team Skill — Mitigation Reference

## ATT&CK Mitigations (MITRE)

| Technique | Mitigation |
|-----------|------------|
| T1566 Phishing | M1041 (Segment), M1021 (Restrict Web), M1017 (User Training) |
| T1190 Exploit Public-Facing App | M1046 (Validate Inputs), M1054 (Restrict Library) |
| T1059 Command/Scripting | M1040 (Behavior Prevention), M1038 (Execution Prevention) |
| T1078 Valid Accounts | M1032 (MFA), M1026 (Privileged Account Management) |

## ATLAS Mitigations (AI/LLM)

| Technique | Mitigation |
|-----------|------------|
| ATLAS-T-001 Prompt Injection | Input validation, allowlist, output sanitization |
| ATLAS-T-002 Context Poisoning | Audit external files, cross-check KNOWN_TRUTHS |
| ATLAS-T-003 Jailbreaking | Safety guardrails, output filtering |
| ATLAS-T-004 Data Exfiltration | Rate limits, output filtering, no secrets in context |

## OutOfRouteBuddy Controls

- TripExportAudit, TripDeleteAudit (SECURITY_NOTES)
- CURSOR_SELF_IMPROVEMENT: doc injection, allowlist, rules audit
- FileProvider scope, sync API key allowlist
