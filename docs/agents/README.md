# Team Docs

This directory defines the **Master Branch Coordinator** concept and the specialist roles used to reason about OutOfRouteBuddy work.

## Quick start

- **Use the coordinator:** Ask for the “Master Branch Coordinator,” “coordinator,” or “branch coordinator” when you want cross-role planning and task breakdowns.
- **Use a specific role:** Reference the role by name and, if helpful, point to the matching file under `roles/`.

## Contents

| File | Purpose |
|------|--------|
| [KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md](./KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md) | Canonical behavior for persistence, recovery, calendar, GPS, theme, and settings |
| [team-structure.md](./team-structure.md) | Roster, responsibilities, and handoff rules |
| [roles/*.md](./roles/) | One role card per specialist role |
| [purple-team-protocol.md](./purple-team-protocol.md) | Red + Blue collaboration flow |
| [security-team-proof-of-work.md](./security-team-proof-of-work.md) | Proof-of-work log format for security exercises |
| [AGENT_APTITUDE_AND_SCORING.md](./AGENT_APTITUDE_AND_SCORING.md) | Internal scoring and role-evaluation docs |
| [BOARD_MEETING_PLAN.md](./BOARD_MEETING_PLAN.md) | Call script for structured multi-role planning sessions |

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
| Email Editor / Market Guru | [email-editor-market-guru.md](./roles/email-editor-market-guru.md) | Copy, marketing messaging, outreach wording |
| File Organizer | [file-organizer.md](./roles/file-organizer.md) | Repo and doc structure, file naming |
| **Red Team** | [red-team-agent.md](./roles/red-team-agent.md) | Attack simulation and adversarial review |
| **Blue Team** | [blue-team-agent.md](./roles/blue-team-agent.md) | Detection, response, and remediation |

## Security team

- **Red Team:** Simulates realistic attacks and pressure-tests assumptions.
- **Blue Team:** Checks whether alarms and controls would have fired, then proposes or implements fixes.
- **Purple Team:** Red and Blue collaborate in one flow using `purple-team-protocol.md`.

## Invocation examples

- *"Act as the Red Team – Technical Ninja only."*
- *"Run a Purple Team exercise: Red attacks [X], Blue checks alarms."*
- *"Act as the Master Branch Coordinator and break down the work for adding a Reports screen."*
- *"I need the UI/UX Specialist to suggest improvements for the statistics section."*
