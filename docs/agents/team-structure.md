# Master Branch Coordinator – Team Structure

This document defines the **Master Branch Coordinator** and its **employee**(sub agents) roles for the OutOfRouteBuddy application. The coordinator delegates work, resolves handoffs, and ensures the Human-in-the-Loop Manager can reach you (the user) via email.

---

## Coordinator

**Master Branch Coordinator Agent**  
- **Purpose:** Orchestrate the team, assign work to the right role(s), resolve overlaps, and escalate to the Human-in-the-Loop Manager when the user should be notified (suggestions, questions, or status updates).  
- **Instructions:** See [Coordinator Instructions](./coordinator-instructions.md).  
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
| **Human-in-the-Loop Manager** | User communication: suggestions, questions, work updates; sends emails to you | Emails (via script), summaries, decision requests |
| **Red Team** | Attack simulation: Lead (scope), Specialist (phishing/social engineering), Technical Ninja (custom code, bypass) | Red action logs, payloads, PoC scripts; proof of work in `security-team-proof-of-work.md` and `data-sets/security-exercises/` |
| **Blue Team** | Defenders: detect, respond, fix when Red attacks; in Purple mode, check if alarm went off and fix if not | Blue check logs, remediation proposals, artifact paths |

---

## Handoff and escalation

- **Coordinator → Role:** Coordinator assigns tasks to one or more roles and can reference the role’s agent card (in `roles/`) for consistent behavior.  
- **Role → Coordinator:** A role can “return” results to the coordinator or ask for re-assignment.  
- **Coordinator → Human-in-the-Loop:** When the user should be informed or asked something, the coordinator (or any role) hands off to the Human-in-the-Loop Manager, who drafts and sends an email using the configured email script.  
- **Open line of communication:** The coordinator and Human-in-the-Loop Manager are authorized to **read and write email** when they need to ask questions or consult. See **`docs/agents/OPEN_LINE_OF_COMMUNICATION.md`**.
- **Human-in-the-Loop → You:** Emails are sent to the address configured in `scripts/coordinator-email/`. To read your replies, they run `read_replies.py` and read `last_reply.txt`.

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
- [Human-in-the-Loop Manager](./roles/human-in-the-loop-manager.md)
- [Red Team](./roles/red-team-agent.md)
- [Blue Team](./roles/blue-team-agent.md)

Use these when invoking a specific “employee” or when the coordinator delegates to a role.
