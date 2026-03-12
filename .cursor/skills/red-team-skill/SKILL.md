---
name: red-team-skill
description: >-
  Invokes Red Team with ATT&CK/ATLAS technique IDs; generates structured output;
  saves to proof-of-work. Use when the user says Red Team, attack simulation,
  ATT&CK technique, ATLAS technique, or requests Red Team exercises.
---

# Red Team Skill

## Invocation

When user invokes **Red Team**, **attack simulation**, or specifies **ATT&CK/ATLAS technique IDs**:

1. Read `docs/agents/roles/red-team-agent.md` and `docs/agents/security-team-proof-of-work.md`
2. Map requested techniques to attack actions (see [reference.md](reference.md))
3. Execute attack simulation per scope
4. Generate structured output with technique IDs
5. Save to proof-of-work

---

## Structured Output (with Technique IDs)

For every Red action, produce:

```markdown
### Red action – [phase]
- **Role:** Lead | Specialist | Technical Ninja
- **Technique IDs:** T1566 (Phishing), ATLAS-T-001 (Prompt Injection), etc.
- **Target:** [surface]
- **Action:** [what was simulated]
- **Result:** Success | Failed | Partial
- **Blue visibility:** Yes | No | Unclear
- **Artifacts:** [path or "see above"]
```

---

## Technique ID Usage

- **ATT&CK:** MITRE ATT&CK (e.g. T1566, T1059, T1190). Use when simulating traditional attacks.
- **ATLAS:** Adversarial Threat Landscape for AI Systems (e.g. ATLAS-T-001). Use when simulating AI/LLM attacks (prompt injection, context poisoning, etc.).

Include technique IDs in every Red action block for coverage mapping.

---

## Proof-of-Work

- Append to `docs/agents/security-team-proof-of-work.md` or create `docs/agents/data-sets/security-exercises/YYYY-MM-DD-<exercise>.md`
- Save scripts, payloads, PoC to `docs/agents/data-sets/security-exercises/artifacts/`
- Use template from `docs/agents/security-team-proof-of-work.md` § Exercise log

---

## Purple Coordination

When running with Blue (Purple): coordinate per `docs/agents/purple-team-protocol.md`. Red attacks → Blue checks → fix → re-test.

---

## Additional Resources

- Technique mappings: [reference.md](reference.md)
- Red agent: [docs/agents/roles/red-team-agent.md](../../../docs/agents/roles/red-team-agent.md)
