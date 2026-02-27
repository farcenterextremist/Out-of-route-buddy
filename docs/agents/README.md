# Master Branch Coordinator & Team

This directory defines the **Master Branch Coordinator** agent and the **employee** roles it manages for the OutOfRouteBuddy application.

## Open line of communication

The coordinator and Human-in-the-Loop Manager (and the AI when helping in Cursor) are authorized to **read and write email** when they need to ask you questions or consult—no extra permission step. See **[OPEN_LINE_OF_COMMUNICATION.md](./OPEN_LINE_OF_COMMUNICATION.md)** for how it works and when we use it.

## Quick start

- **Use the coordinator:** In Cursor (or any AI chat), ask for the “Master Branch Coordinator,” “coordinator,” or “branch coordinator.” The coordinator will assign work to the right roles and can email you or read your replies when needed.
- **Use a specific role:** Reference the role by name (e.g. “act as the Front-end Engineer”) and, if helpful, point to the corresponding file under `roles/` (e.g. `docs/agents/roles/frontend-engineer.md`).

## Contents

| File | Purpose |
|------|--------|
| [KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md](./KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md) | **Agent training:** Known truths and SSOT for persistence, recovery, calendar, GPS, theme, etc. |
| [OPEN_LINE_OF_COMMUNICATION.md](./OPEN_LINE_OF_COMMUNICATION.md) | How we read/write email to ask questions or consult |
| [team-structure.md](./team-structure.md) | Roster, responsibilities, and handoff rules |
| [coordinator-instructions.md](./coordinator-instructions.md) | Master Branch Coordinator agent instructions |
| [roles/*.md](./roles/) | One agent card per employee role |
| [purple-team-protocol.md](./purple-team-protocol.md) | Red + Blue collaboration (attack, check alarm, fix) |
| [security-team-proof-of-work.md](./security-team-proof-of-work.md) | Log and save Red/Blue/Purple exercises for proof of work |
| [AGENT_APTITUDE_AND_SCORING.md](./AGENT_APTITUDE_AND_SCORING.md) | Tests and scoring to find which agents need more training or data injection |
| [SUBAGENTS_SPAWN_AND_TRAINING.md](./SUBAGENTS_SPAWN_AND_TRAINING.md) | **Subagents:** How spawning works and how to inject role cards, data sets, Known Truths, and roundtable into spawned subagents |
| [BOARD_MEETING_PLAN.md](./BOARD_MEETING_PLAN.md) | **Board Meeting:** Call script to summon every agent in sequence for opinions, recommendations, and weekly progress; transcript + summary output |
| [COORDINATOR_EMAIL_LISTENER_RUNBOOK.md](./COORDINATOR_EMAIL_LISTENER_RUNBOOK.md) | **Coordinator email listener:** Open line while Cursor is online—runbook (Cursor loop) or `coordinator_listener.py` (LLM) |

## Roles

| Role | File | Responsibility |
|------|------|----------------|
| Project Design / Creative Manager | [design-creative-manager.md](./roles/design-creative-manager.md) | Vision, roadmap, feature prioritization |
| UI/UX Specialist | [ui-ux-specialist.md](./roles/ui-ux-specialist.md) | Screens, flows, accessibility, design consistency |
| Front-end Engineer | [frontend-engineer.md](./roles/frontend-engineer.md) | Android UI (Kotlin, XML, resources) |
| Back-end Engineer | [backend-engineer.md](./roles/backend-engineer.md) | Data, services, repositories, business logic |
| DevOps Engineer | [devops-engineer.md](./roles/devops-engineer.md) | Build, CI/CD, deployment, environments |
| QA Engineer | [qa-engineer.md](./roles/qa-engineer.md) | Test strategy, test cases, automation |
| Security Specialist | [security-specialist.md](./roles/security-specialist.md) | Security review, threat model, hardening |
| Email Editor / Market Guru | [email-editor-market-guru.md](./roles/email-editor-market-guru.md) | Email copy, marketing messaging, outreach |
| File Organizer | [file-organizer.md](./roles/file-organizer.md) | Repo and doc structure, file naming |
| Human-in-the-Loop Manager | [human-in-the-loop-manager.md](./roles/human-in-the-loop-manager.md) | Sends you emails (suggestions, questions, updates) |
| **Red Team** | [red-team-agent.md](./roles/red-team-agent.md) | Attack simulation: Lead, Specialist (phishing), Technical Ninja |
| **Blue Team** | [blue-team-agent.md](./roles/blue-team-agent.md) | Defenders: detect, respond, fix when Red attacks |

## Security team (Red, Blue, Purple)

- **Red Team:** Simulates full-scale attacks (not just bugs). Invoke: "Red Team" or "act as the Red Team" (optionally "Technical Ninja only" or "Specialist only"). See [red-team-agent.md](./roles/red-team-agent.md).
- **Blue Team:** Checks whether alarms went off after Red attacks; proposes or implements fixes if not. Invoke: "Blue Team" or "act as the Blue Team." See [blue-team-agent.md](./roles/blue-team-agent.md).
- **Purple:** Red and Blue work together in one flow. See [purple-team-protocol.md](./purple-team-protocol.md). Proof of work and artifact storage: [security-team-proof-of-work.md](./security-team-proof-of-work.md) and `data-sets/security-exercises/`.

## Human-in-the-Loop email

The Human-in-the-Loop Manager sends you emails via the script in **`scripts/coordinator-email/`**. To receive those emails:

1. Copy `scripts/coordinator-email/.env.example` to `scripts/coordinator-email/.env`.
2. Set your email address and SMTP settings in `.env` (see that folder’s README).
3. When the coordinator (or any role) asks the Human-in-the-Loop Manager to contact you, the manager will use that script to send the message.

## Invocation examples

- Red / Blue / Purple: *"Act as the Red Team – Technical Ninja only"*, *"Run a Purple Team exercise: Red attacks [X], Blue checks alarms"*, *"Blue Team: review the last Red action and propose a fix if we missed it."*
- *“Act as the Master Branch Coordinator and break down the work for adding a new ‘Reports’ screen.”*
- *“I need the UI/UX Specialist to suggest improvements for the statistics section.”* (Optionally: *“Use docs/agents/roles/ui-ux-specialist.md.”*)
- *“Coordinator: we’re done with the calendar refactor; have the Human-in-the-Loop Manager email me a short summary and ask if I want to prioritize anything next.”*
