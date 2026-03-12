---
name: attack-library-skill
description: >-
  Maintains reusable attack playbooks with ATT&CK/ATLAS technique mappings.
  Use when creating playbooks, attack library, export flow, rules backdoor,
  prompt injection playbook, or maintaining security exercise templates.
---

# Attack Library Skill

## Purpose

Maintain reusable playbooks for Red Team exercises. Each playbook maps to ATT&CK/ATLAS techniques and can be invoked by name.

---

## Playbook Structure

```markdown
## [Playbook Name]

**Technique IDs:** T1566, ATLAS-T-001, ...
**Target:** [surface, e.g. trip export, rules files]
**Role:** Specialist | Technical Ninja

### Steps
1. [Action]
2. [Action]
3. [Action]

### Expected Blue Detection
- [What should fire]

### Artifacts
- [Scripts, payloads, file paths]
```

---

## Standard Playbooks

| Playbook | Techniques | Target |
|----------|------------|--------|
| **Export flow** | T1190, T1005 | Trip export, FileProvider |
| **Rules backdoor** | ATLAS-T-002, T1562 | .cursor/rules/*.mdc |
| **Prompt injection** | ATLAS-T-001 | User input, docs, README |
| **Context poisoning** | ATLAS-T-002 | External files, cross-project |
| **Allowlist bypass** | T1078, T1562 | Command allowlist, auto-run |
| **Phishing scenario** | T1566 | Support email, credential harvest |

---

## Where to Store

- **Playbooks:** `docs/agents/data-sets/security-exercises/ATTACK_LIBRARY.md` or `playbooks/` subfolder
- **Updates:** Add new playbooks; update technique mappings when ATT&CK/ATLAS changes
- **Invocation:** "Run playbook: export flow" → load playbook, execute per Red Team skill

---

## Adding a Playbook

1. Name the playbook (e.g. "export flow", "rules backdoor")
2. Map to technique IDs (ATT&CK, ATLAS)
3. Define target surface
4. Write steps (actionable, repeatable)
5. Define expected Blue detection
6. Append to ATTACK_LIBRARY

---

## Additional Resources

- Technique reference: [red-team-skill/reference.md](../red-team-skill/reference.md)
- Proof-of-work: [docs/agents/security-team-proof-of-work.md](../../../docs/agents/security-team-proof-of-work.md)
