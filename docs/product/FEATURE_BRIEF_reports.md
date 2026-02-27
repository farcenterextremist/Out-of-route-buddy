# Feature brief: Reports screen

**Owner:** Project Design / Creative Manager  
**Created:** 2025-02-19  
**Status:** Brief only — hand off to UI/UX (layout), Front-end (screen), Back-end (data), QA (test cases).  
**Related:** [ROADMAP.md](ROADMAP.md), [CRUCIAL_IMPROVEMENTS_TODO.md](../CRUCIAL_IMPROVEMENTS_TODO.md), [KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md](../agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md).

---

## Problem

Users want a single place to see "where did I drive" and "how much out-of-route" over a chosen period. Today they may have to infer from Statistics and History; a dedicated Reports view would make this explicit and support export or share.

---

## Value

- **Clear view** — One screen: period selector (e.g. this month), summary (total miles, OOR miles, OOR %), and optionally a list or chart.
- **Export/share** — Option to export report (e.g. PDF or share text) for records or fleet managers.
- **Builds on existing** — Uses same data as Statistics and History; no new data model required for v1.

---

## High-level behavior

1. **Entry** — From main app (e.g. nav or Statistics), user opens "Reports" or "View report."
2. **Period** — User selects period (this week, this month, custom range); same semantics as Statistics period where possible.
3. **Content** — Summary: total miles, OOR miles, OOR % for the period; optional: list of trips in period.
4. **Actions** — Optional: Export, Share (hand off to FE/UX for format and UX).
5. **Acceptance** — QA: "User can open Reports, pick month, see correct totals; export (if implemented) produces expected output."

---

*Re-prioritize with user if needed. See ROADMAP for next 3 (Auto drive, Reports, History).*
