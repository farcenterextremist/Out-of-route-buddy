# Attack Library — Reusable Playbooks

**Purpose:** Reusable Red Team playbooks with ATT&CK/ATLAS technique mappings. Invoke by name: "Run playbook: export flow".

---

## Export Flow

**Technique IDs:** T1190 (Exploit Public-Facing Application), T1005 (Data from Local System)  
**Target:** Trip export, FileProvider, CSV share  
**Role:** Technical Ninja

### Steps
1. Review code path for export without ownership/context check
2. Simulate access to export without proper app context
3. Check FileProvider scope and path validation

### Expected Blue Detection
- TripExportAudit or equivalent log when export is generated
- Validation of trip ownership before export

### Artifacts
- `artifacts/export-flow-review.md`

---

## Rules Backdoor

**Technique IDs:** ATLAS-T-002 (Context Poisoning), T1562 (Impair Defenses)  
**Target:** `.cursor/rules/*.mdc`  
**Role:** Technical Ninja

### Steps
1. Review rules files for injectable content
2. Simulate addition of malicious instructions to rules
3. Check if rules are audited before execution

### Expected Blue Detection
- Audit of rules file changes; cross-check against KNOWN_TRUTHS
- SECURITY_NOTES §13 (rules backdoor)

### Artifacts
- `artifacts/rules-backdoor-review.md`

---

## Prompt Injection

**Technique IDs:** ATLAS-T-001 (Prompt Injection)  
**Target:** User input, README, docs  
**Role:** Technical Ninja

### Steps
1. Identify user-controlled inputs (prompts, docs, configs)
2. Simulate injection payload (e.g. "Ignore previous instructions...")
3. Check input validation and output sanitization

### Expected Blue Detection
- Input validation; allowlist for trusted sources
- CURSOR_SELF_IMPROVEMENT: doc injection mitigation

### Artifacts
- `artifacts/prompt-injection-payloads.md`

---

## Context Poisoning

**Technique IDs:** ATLAS-T-002 (Context Poisoning)  
**Target:** External files, cross-project contamination  
**Role:** Technical Ninja

### Steps
1. Simulate external file with hidden instructions
2. Check if README/docs are treated as potentially injectable
3. Verify cross-check against KNOWN_TRUTHS before acting

### Expected Blue Detection
- CURSOR_SELF_IMPROVEMENT: context scope, single project
- Flag conflicts with security phases

### Artifacts
- `artifacts/context-poisoning-scenario.md`

---

## Allowlist Bypass

**Technique IDs:** T1078 (Valid Accounts), T1562 (Impair Defenses)  
**Target:** Command allowlist, auto-run settings  
**Role:** Technical Ninja

### Steps
1. Review allowlist configuration
2. Simulate command execution outside allowlist
3. Check if Run Everything is used for untrusted sessions

### Expected Blue Detection
- Allowlist over Run Everything
- `cd c:\Users\brand\OutofRoutebuddy` prefix for loop commands

### Artifacts
- `artifacts/allowlist-bypass-review.md`

---

## Phishing Scenario

**Technique IDs:** T1566 (Phishing)  
**Target:** Support email, credential harvest  
**Role:** Specialist

### Steps
1. Draft fake "OutOfRouteBuddy support" email
2. Propose pretext (e.g. "IT needs you to re-validate")
3. Identify indicators (URLs, senders, wording)

### Expected Blue Detection
- User training; suspicious link/sender alerts
- No real emails sent without approval

### Artifacts
- `artifacts/phishing-draft.md`

---

*Add new playbooks below. Include technique IDs, target, role, steps, expected detection, artifacts.*
