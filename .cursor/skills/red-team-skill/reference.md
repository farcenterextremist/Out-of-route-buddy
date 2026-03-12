# Red Team Skill — Technique Reference

## ATT&CK (MITRE) — Common Techniques

| ID | Name | Use |
|----|------|-----|
| T1566 | Phishing | Specialist: credential harvesting, malicious links |
| T1059 | Command and Scripting Interpreter | Technical Ninja: script execution |
| T1190 | Exploit Public-Facing Application | API, web app exploitation |
| T1078 | Valid Accounts | Abuse of legitimate credentials |
| T1087 | Account Discovery | Enumerate users, accounts |
| T1003 | OS Credential Dumping | Extract credentials from storage |
| T1040 | Network Sniffing | Intercept traffic |
| T1562 | Impair Defenses | Disable logging, evade detection |

## ATLAS (AI/LLM) — Common Techniques

| ID | Name | Use |
|----|------|-----|
| ATLAS-T-001 | Prompt Injection | Inject instructions via user input |
| ATLAS-T-002 | Context Poisoning | Corrupt model context via external files |
| ATLAS-T-003 | Jailbreaking | Bypass safety guardrails |
| ATLAS-T-004 | Data Exfiltration | Extract training data or secrets via prompts |
| ATLAS-T-005 | Malicious Output | Coerce harmful or misleading output |

## OutOfRouteBuddy Surfaces

- Trip export, FileProvider, sync API
- Rules files (.cursor/rules/*.mdc), README, docs
- Cursor MCP, allowlist, auto-run
