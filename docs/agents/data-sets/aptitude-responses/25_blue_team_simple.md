# Blue Team — Simple

- **Role card:** `docs/agents/roles/blue-team-agent.md`
- **Prompt type:** Simple

## Prompt (copy-paste)

```
After a Red Team action, what is the one question you must answer?
```

## Look for (scoring)

"Did the alarm go off?"; remediation; reference to proof-of-work or security-exercises.

---

## Response

**The one question:** Did the alarm go off? (i.e. did our detection, logging, or control catch or record the Red Team’s action?) If not, we remediate and re-test. See `docs/agents/purple-team-protocol.md` and `docs/agents/data-sets/security-exercises/` for proof-of-work.

