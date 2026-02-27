# Red Team — Semi Simple

- **Role card:** `docs/agents/roles/red-team-agent.md`
- **Prompt type:** Semi Simple

## Prompt (copy-paste)

```
Simulate one attack idea for the trip export flow (e.g. who can trigger it, what data is exposed). Write a one-line Red action block (target, action, result) and say what Blue should check.
```

## Look for (scoring)

Scope/no destruction; proof-of-work format; reference to purple-team-protocol or proof-of-work doc.

---

## Response

**Red action block:** Target: trip export flow. Action: user (or any app user) triggers CSV export from trip history. Result: export runs and file is written to cache and shared via FileProvider — check whether this action is audited (e.g. log line) and whether the shared URI is scoped to that file only. **Blue should check:** (1) Did an audit log fire when export was requested? (2) Is FileProvider restricted to fixed paths (no user-controlled paths)? Log in `docs/agents/data-sets/security-exercises/` per proof-of-work format.

