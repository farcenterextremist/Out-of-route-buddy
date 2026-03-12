# Master Branch Coordinator – Team Structure

This document defines the **Master Branch Coordinator** concept and the specialist roles used for OutOfRouteBuddy planning and execution.

---

## Coordinator

**Master Branch Coordinator Agent**  
- **Purpose:** Orchestrate the team, assign work to the right role(s), and resolve overlaps.  
- **Skills:** coordinator-delegation (handoffs, role assignment), improvement-loop-wizard (when running GO). Use `.cursor/skills/coordinator-delegation/SKILL.md`, `.cursor/skills/improvement-loop-wizard/SKILL.md`.
- **Invocation:** Use the coordinator when you want high-level planning, cross-role decisions, or a single point of contact for “who should do what.”

---

## Employees (Roles)

| Role | Scope | Primary artifacts |
|------|--------|-------------------|
| **Project Design / Creative Manager** | Vision, product design, feature prioritization, creative direction | Roadmaps, PRDs, design briefs |
| **UI/UX Specialist** | Interfaces, flows, accessibility, usability | Wireframes, flows, UI specs, design tokens |
| **Front-end Engineer** | Android UI (XML, Compose), fragments, views, resources | Kotlin/XML, layouts, themes |
| **Back-end Engineer** | Data, APIs, services, business logic, persistence | Kotlin services, repositories, DB/API design |
| **DevOps Engineer** | Build, CI/CD, environments, deployment, observability | Gradle, scripts, pipelines, configs |
| **QA Engineer** | Test strategy, test cases, automation, regression | Tests, test plans, quality gates |
| **Security Specialist** | Security review, threat model, secrets, compliance | Security notes, hardening, recommendations |
| **Email Editor / Market Guru** | Email copy, marketing messaging, positioning, outreach | Email drafts, messaging guidelines, subject lines |
| **File Organizer** | Repo and doc structure, file naming, keeping things tidy | Structure proposals, naming conventions, reorg plans |
| **Red Team** | Attack simulation: Lead (scope), Specialist (phishing/social engineering), Technical Ninja (custom code, bypass) | Red action logs, payloads, PoC scripts; proof of work in `security-team-proof-of-work.md` and `data-sets/security-exercises/` |
| **Blue Team** | Defenders: detect, respond, fix when Red attacks; in Purple mode, check if alarm went off and fix if not | Blue check logs, remediation proposals, artifact paths |

---

## Handoff and escalation

- **Coordinator → Role:** Coordinator assigns tasks to one or more roles and can reference the role’s agent card (in `roles/`) for consistent behavior.  
- **Role → Coordinator:** A role can “return” results to the coordinator or ask for re-assignment.  
- **Coordinator → Role:** When a task becomes concrete, the coordinator assigns it to the role that owns that surface.
- **User decisions:** When the user should approve scope, priorities, or risk, summarize the decision clearly in chat or documentation before implementation.

---

## Role agent cards

Each role has a short agent card under `docs/agents/roles/`:

- [Design / Creative Manager](./roles/design-creative-manager.md)  
- [UI/UX Specialist](./roles/ui-ux-specialist.md)  
- [Front-end Engineer](./roles/frontend-engineer.md)  
- [Back-end Engineer](./roles/backend-engineer.md)  
- [DevOps Engineer](./roles/devops-engineer.md)  
- [QA Engineer](./roles/qa-engineer.md)  
- [Security Specialist](./roles/security-specialist.md)  
- [Email Editor / Market Guru](./roles/email-editor-market-guru.md)  
- [File Organizer](./roles/file-organizer.md)  
- [Red Team](./roles/red-team-agent.md)
- [Blue Team](./roles/blue-team-agent.md)

Use these when invoking a specific “employee” or when the coordinator delegates to a role.
