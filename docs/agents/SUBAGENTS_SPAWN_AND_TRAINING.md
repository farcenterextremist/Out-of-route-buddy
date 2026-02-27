# Subagents: how spawning works and how to inject training data

This doc explains how **subagents** (separate agent processes spawned by the host session) work and how to feed our **training and employee data** into them so each spawned role behaves consistently.

---

## How subagent spawning works

**Subagents** are separate agent processes (e.g. launched via Cursor’s task/subagent mechanism or an MCP server). They do **not** automatically see your full chat or workspace. They only see:

1. **Task description (prompt)** — What they are asked to do.
2. **Attachments (if supported)** — Files or text you explicitly pass in.

So “spawning” a subagent means:

- The **host** (you or the main AI session) decides to delegate work to a **subagent**.
- The host sends a **prompt** that describes the task and, critically, **who the subagent is** (e.g. “You are the Front-end Engineer for OutOfRouteBuddy”).
- The host can **attach** files (e.g. the role card, data set, Known Truths, roundtable transcript) so the subagent has the same training context as our “employee” roles.

The subagent runs in isolation, does the task, and returns a result. It will only follow our roles and handoffs if we **give it** the right docs in that prompt/attachment step.

---

## Implementing training and employee data into spawned subagents

**Yes — we can implement our training and employee data into subagents.** Do it by treating each spawn as “invoke this role with this context.”

### What to pass when spawning an employee subagent

| What to pass | Purpose |
|--------------|---------|
| **Role identity and task** | In the prompt: “You are the [Role]. [Task]. Follow your role card and data set; hand off as in the roundtable.” |
| **Role card** | `docs/agents/roles/<role>.md` (or `coordinator-instructions.md` for Coordinator). Attach this file so the subagent has scope, out-of-scope, handoffs, and codebase paths. |
| **Role data set** | `docs/agents/data-sets/<role>.md`. What this role consumes and produces. |
| **Known Truths and SSOT** | `docs/agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md`. So the subagent uses the same facts for persistence, recovery, calendar, GPS, settings. |
| **Employee roundtable transcript** | `docs/agents/data-sets/employee-roundtable-transcript.md` (or the section for this role). So the subagent’s handoffs and self-description match the rest of the team. |

### Minimal spawn (subagent)

- **Prompt:** “You are the [Role]. [Concrete task]. Use the attached role card.”
- **Attachments:** Role card only.

### Full training spawn (subagent)

- **Prompt:** “You are the [Role] for OutOfRouteBuddy. [Task]. Follow the attached role card and data set. Use Known Truths for any persistence, recovery, calendar, or settings. Hand off to other roles as in the roundtable transcript.”
- **Attachments:** Role card + role data set + Known Truths + employee roundtable transcript (or at least the “Handoff summary” section).

### Role → files quick reference (for spawning)

| Role | Role card | Data set |
|------|-----------|----------|
| Coordinator | `docs/agents/coordinator-instructions.md` | — |
| Design/Creative | `docs/agents/roles/design-creative-manager.md` | `docs/agents/data-sets/design-creative.md` |
| UI/UX | `docs/agents/roles/ui-ux-specialist.md` | `docs/agents/data-sets/ui-ux.md` |
| Front-end | `docs/agents/roles/frontend-engineer.md` | `docs/agents/data-sets/frontend.md` |
| Back-end | `docs/agents/roles/backend-engineer.md` | `docs/agents/data-sets/backend.md` |
| DevOps | `docs/agents/roles/devops-engineer.md` | `docs/agents/data-sets/devops.md` |
| QA | `docs/agents/roles/qa-engineer.md` | `docs/agents/data-sets/qa.md` |
| Security | `docs/agents/roles/security-specialist.md` | `docs/agents/data-sets/security.md` |
| Email Editor | `docs/agents/roles/email-editor-market-guru.md` | `docs/agents/data-sets/email-editor.md` |
| File Organizer | `docs/agents/roles/file-organizer.md` | `docs/agents/data-sets/file-organizer.md` |
| Human-in-the-Loop | `docs/agents/roles/human-in-the-loop-manager.md` | `docs/agents/data-sets/human-in-the-loop.md` |
| Red Team | `docs/agents/roles/red-team-agent.md` | — |
| Blue Team | `docs/agents/roles/blue-team-agent.md` | — |

**Shared for all:** `docs/agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md`, `docs/agents/data-sets/employee-roundtable-transcript.md`.

---

## If you use an MCP subagent server

If you use an MCP server that defines subagents as markdown files (e.g. one agent per file in an `agents/` folder):

1. **Define one “agent” per employee** (e.g. `frontend-engineer.md`, `backend-engineer.md`).
2. In each agent file, **embed or reference** our training data:
   - “You are the Front-end Engineer. Follow the role card at `docs/agents/roles/frontend-engineer.md` and the data set at `docs/agents/data-sets/frontend.md`. Use `docs/agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md` for persistence, recovery, calendar, settings. Hand off as in `docs/agents/data-sets/employee-roundtable-transcript.md`.”
3. Or keep the agent file short and **configure the server** to always inject Known Truths + roundtable when spawning any of our role agents.

Either way, the subagent process only gets what you pass in; our training and employee data **must** be part of that payload or referenced where the subagent can read them.

---

## Summary

- **Spawning** = start a subagent with a task description and optional attachments. The subagent sees only that prompt and those files.
- **Training and employee data** = role cards, data sets, Known Truths, roundtable transcript. To get them into subagents, **pass them explicitly** in the spawn (prompt + attachments or agent definition that references/embeds them).
- Use the **full training spawn** (role card + data set + Known Truths + roundtable) when you want subagent behavior to match our documented roles and handoffs.
