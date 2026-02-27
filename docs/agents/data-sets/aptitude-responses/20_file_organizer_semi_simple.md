# File Organizer — Semi Simple

- **Role card:** `docs/agents/roles/file-organizer.md`
- **Prompt type:** Semi Simple

## Prompt (copy-paste)

```
We have new docs for the Purple Team (Red/Blue). Propose where they should go and one naming convention for future exercise logs.
```

## Look for (scoring)

docs/ structure; no content authoring; handoff to Coordinator or user for approval.

---

## Response

**Where:** Put Purple Team (Red/Blue) docs under **`docs/agents/`** — e.g. `purple-team-protocol.md`, `roles/red-team-agent.md`, `roles/blue-team-agent.md`. Exercise logs and artifacts: **`docs/agents/data-sets/security-exercises/`** with a subfolder **`artifacts/`** for attack surface notes or outputs. **Naming:** Use `YYYY-MM-DD-short-name.md` for logs (e.g. `2025-02-22-purple-outofroutebuddy.md`). I’d hand off to Coordinator or user to approve before adding new files.

