---
name: feature-brief-writer
description: >-
  Writes feature briefs per OutOfRouteBuddy template: problem, value, behavior,
  handoffs. Use when creating FEATURE_BRIEF_*.md, product specs, or when the user
  asks for a feature brief.
---

# Feature Brief Writer

## Quick Reference

Read `docs/product/FEATURE_BRIEF_auto_drive.md` as template. All briefs reference SSOT: `docs/agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md`.

---

## Brief Structure

```markdown
# Feature brief: [Name]

**Owner:** Project Design / Creative Manager
**Created:** YYYY-MM-DD
**Status:** Brief only — hand off to [roles]
**Related:** ROADMAP.md, CRUCIAL_IMPROVEMENTS_TODO.md, KNOWN_TRUTHS

---

## Problem
[What pain does this solve?]

## Value
[Benefits; user impact]

## When to use it
[Ideal scenario; overrides; not for]

## High-level behavior
1. [Step]
2. [Step]
3. [Step]

## Out of scope (for this brief)
[What goes to other roles: Back-end, UI/UX, QA, Security]

---

*Next: [Handoff chain]*
```

---

## Handoff Roles

| Role | Receives |
|------|----------|
| UI/UX | Wireframes, flows, placement, defaults |
| Back-end | Trigger logic, persistence, APIs |
| Front-end | Implementation of UI |
| QA | Test cases, test plan |
| Security | Privacy review, PII handling |

---

## SSOT Alignment

- **Persistence:** Follow KNOWN_TRUTHS (Room, TripRepository, only End writes)
- **Recovery:** Crash recovery, TripPersistenceManager
- **Calendar/Stats:** Room via getMonthlyTripStatistics, getTripsByDateRange
- **Security:** No PII in logs; on-device only unless documented

---

## Location

Save to `docs/product/FEATURE_BRIEF_<feature>.md`. Update `docs/product/ROADMAP.md` when adding.

---

## Additional Resources

- Template: [docs/product/FEATURE_BRIEF_auto_drive.md](../../../docs/product/FEATURE_BRIEF_auto_drive.md)
- SSOT: [docs/agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md](../../../docs/agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md)
- Roadmap: [docs/product/ROADMAP.md](../../../docs/product/ROADMAP.md)
