---
name: purple-orchestrator-skill
description: >-
  Runs full Purple flow: scope → Red attack → Blue check → fix → re-test. Use
  when the user says Purple Team, Purple exercise, run Purple, or requests
  Red+Blue coordinated exercise.
---

# Purple Orchestrator Skill

## Invocation

When user says **Purple Team**, **Purple exercise**, **run Purple**, or **Red+Blue**:

1. Read `docs/agents/purple-team-protocol.md`
2. Execute full flow below
3. Save all outputs to proof-of-work

---

## Flow

### 1. Scope

- **User/Lead** states: target (e.g. "trip export", "rules backdoor", "prompt injection"), environment, off-limits
- **Red Lead** confirms scope; chooses role(s): Specialist, Technical Ninja, or both
- **Output:** Scope block (target, environment, techniques in scope)

### 2. Red Attack

- **Red Team** performs attack(s) per scope
- Each action: structured block with **Technique IDs** (ATT&CK/ATLAS), Target, Action, Result, Blue visibility, Artifacts
- Use `red-team-skill` for technique mapping and output format

### 3. Blue Check

- **Blue Team** reviews each Red action
- For each: Alarm went off? If no → gap; propose mitigation
- Map detections and mitigations to technique IDs
- Use `blue-team-skill` for output format

### 4. Fix

- **Blue** proposes or implements fix (log, validation, alert, code change)
- Record remediation in proof-of-work
- Save artifacts to `docs/agents/data-sets/security-exercises/artifacts/`

### 5. Re-Test (Optional)

- **Red** re-runs same attack to verify fix
- Blue confirms alarm now fires
- Document in proof-of-work

---

## Proof-of-Work

- Create or append to `docs/agents/data-sets/security-exercises/YYYY-MM-DD-purple-<target>.md`
- Use template from `docs/agents/security-team-proof-of-work.md`
- Include exercise_id, date, target, mode: Purple
- Append Red blocks and Blue blocks in order

---

## Quick Reference

| Step | Agent | Action |
|------|-------|--------|
| 1 | User/Lead | Scope |
| 2 | Red | Attack with technique IDs |
| 3 | Blue | Check alarms; map to techniques |
| 4 | Blue | Fix if missed |
| 5 | Red | Re-test (optional) |

---

## Additional Resources

- Protocol: [docs/agents/purple-team-protocol.md](../../../docs/agents/purple-team-protocol.md)
- Proof-of-work: [docs/agents/security-team-proof-of-work.md](../../../docs/agents/security-team-proof-of-work.md)
