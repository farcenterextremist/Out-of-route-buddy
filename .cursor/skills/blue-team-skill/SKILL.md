---
name: blue-team-skill
description: >-
  Invokes Blue Team with technique IDs; maps detections to ATT&CK/ATLAS;
  proposes mitigations. Use when the user says Blue Team, defenders, detection
  mapping, ATT&CK mitigation, or requests Blue Team review.
---

# Blue Team Skill

## Invocation

When user invokes **Blue Team**, **defenders**, or requests **detection mapping**:

1. Read `docs/agents/roles/blue-team-agent.md` and `docs/agents/security-team-proof-of-work.md`
2. Review Red action(s); map to ATT&CK/ATLAS technique IDs
3. Check: Did our alarm go off?
4. If no: propose mitigation with technique mapping
5. Save to proof-of-work

---

## Structured Output (with Technique IDs)

For every Blue check, produce:

```markdown
### Blue check – [phase]
- **Red action reviewed:** [brief description]
- **Technique IDs:** T1566, ATLAS-T-001 (from Red block)
- **Alarm went off?** Yes | No
- **If yes:** What detected it? [log, alert, rule]
- **If no (gap):** What should have detected it?
- **Mitigation:** [proposed or implemented fix]
- **ATT&CK/ATLAS mapping:** [detection → technique; mitigation → technique]
- **Artifacts:** [paths]
```

---

## Detection Mapping

- **ATT&CK:** Map detection (log, alert, validation) to MITRE technique. Document which sub-technique if applicable.
- **ATLAS:** Map AI-specific mitigations (input validation, output sanitization, context audit) to ATLAS techniques.

Include technique IDs so coverage reports can build heat maps.

---

## Mitigation Proposals

| Technique type | Mitigation examples |
|----------------|---------------------|
| Phishing (T1566) | User training, link validation, suspicious sender alerts |
| Injection (T1190, ATLAS-T-001) | Input validation, allowlist, output encoding |
| Context poisoning (ATLAS-T-002) | Audit external files, cross-check against KNOWN_TRUTHS |
| Credential abuse (T1078) | MFA, session limits, anomaly detection |

---

## Proof-of-Work

- Append to `docs/agents/security-team-proof-of-work.md` or exercise file
- Link Blue block to Red block (same exercise_id)
- Save remediation artifacts to `docs/agents/data-sets/security-exercises/artifacts/`

---

## Purple Coordination

When running with Red (Purple): per `docs/agents/purple-team-protocol.md`. Red attacks → Blue checks → fix → re-test.

---

## Additional Resources

- Blue agent: [docs/agents/roles/blue-team-agent.md](../../../docs/agents/roles/blue-team-agent.md)
- Mitigation mappings: [reference.md](reference.md)
