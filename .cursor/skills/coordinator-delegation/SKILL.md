---
name: coordinator-delegation
description: >-
  Orchestrates team roles, assigns work, and manages handoffs for OutOfRouteBuddy.
  Use when coordinating tasks, delegating to roles, or when the user asks who
  should do what.
---

# Coordinator / Delegation Skill

## Quick Reference

Read `docs/agents/team-structure.md`. Assign work to the role that owns the surface.

---

## Role → Scope

| Role | Scope | Artifacts |
|------|-------|-----------|
| Project Design / Creative Manager | Vision, prioritization, feature briefs | ROADMAP, FEATURE_BRIEF_* |
| UI/UX Specialist | Interfaces, flows, accessibility | Wireframes, UI specs |
| Front-end Engineer | Android UI (XML, Compose) | Kotlin, layouts, themes |
| Back-end Engineer | Data, services, persistence | Repositories, DAOs, services |
| DevOps Engineer | Build, CI/CD | Gradle, pipelines |
| QA Engineer | Tests, quality gates | Test plans, TEST_STRATEGY |
| Security Specialist | Threat model, secrets | SECURITY_NOTES |
| File Organizer | Repo structure, naming | Reorg plans |
| Red/Blue Team | Attack/defense | security-team-proof-of-work |

---

## Delegation Rules

1. **One surface, one owner** — Assign to the role that owns that surface
2. **Handoff with brief** — When delegating, reference role card and any FEATURE_BRIEF or doc
3. **User decisions** — When scope, priority, or risk needs approval: summarize clearly; wait for user before implementing
4. **Out of scope** — Role says "out of scope" → Coordinator re-assigns or escalates to user

---

## Role Cards

Agent cards in `docs/agents/roles/`:
- design-creative-manager.md, ui-ux-specialist.md, frontend-engineer.md
- backend-engineer.md, devops-engineer.md, qa-engineer.md
- security-specialist.md, file-organizer.md
- red-team-agent.md, blue-team-agent.md

---

## Example Delegation

**User:** "Add export to PDF on trip history"

**Coordinator:** Assign to:
- UI/UX: Where does the button go? Accessibility?
- Back-end: Export logic, data shape
- Front-end: Implement button + call
- QA: Test plan for export flow
- Security: PII in export? FileProvider scope?

---

## Escalation

- **Ambiguous:** Ask user to clarify scope or priority
- **Overlap:** Resolve which role owns; avoid duplicate work
- **Drastic change:** Per Improvement Loop — suggest, don't implement; "Should I implement X, or document for later?"

---

## Additional Resources

- Team structure: [docs/agents/team-structure.md](../../../docs/agents/team-structure.md)
- Role cards: [docs/agents/roles/](../../../docs/agents/roles/)
- Known truths: [docs/agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md](../../../docs/agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md)
