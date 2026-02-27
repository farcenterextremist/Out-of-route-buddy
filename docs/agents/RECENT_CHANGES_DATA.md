# Recent Changes Data — Single Source of Truth

**Purpose:** Document all data sources for "what recent changes" so Jarvey can answer accurately. When the user asks "What recent changes have been done to the project?" or similar, Jarvey uses these sources.

---

## Current summary (last updated 2026-02-26)

Recent work includes: **App v1.0.2** — Toolbar redesign (stretched layout, cracked-road background, "Out of Route" text with black outline); Settings menu polish (rounded corners, Help & Info visibility, X close buttons); Calendar updates (current date highlight, bigger month name); Statistics simplified (weekly/yearly removed, Monthly focus); period onboarding and refresh flow; app icon iterations. **Docs/tests:** Known truths & SSOT, two-way email, wiring/recovery/persistence; TripStatisticsWiringTest for datesWithTripsInPeriod and period stats. **Planned:** Monthly Statistics wiring and Stat Card on calendar.

---

## Two Types of "Recent Changes"

| User asks | Intent | Primary source | Fallback |
|-----------|--------|----------------|----------|
| "What recent changes to the **project**?" | `recent` | `project_timeline.json` (curated) | "No curated entries yet; I'll summarize once the team adds phase completions." |
| "What recent changes to **Jarvey**?" | `jarvey_self` | `JARVEY_IMPROVEMENT_LOG.md` | JARVEY_EVALUATION_REVIEW, SCENARIO_RUN_RESULTS |

---

## Project Recent Changes (OutOfRouteBuddy)

### Primary: project_timeline.json

- **Path:** `scripts/coordinator-email/project_timeline.json`
- **Content:** Curated entries (phase completions, manual additions). Format: `[{date, type, title, detail}, ...]`
- **Loaded by:** `context_loader.get_project_timeline_curated()` when intent is `recent`
- **Intent keywords:** "recent", "last commit", "what changed", "timeline", "recent changes", "project history", "what's new"

### When timeline is empty

Jarvey must say: "No curated timeline entries yet; I'll summarize once the team adds phase completions." Do **not** output raw git commit hashes unless the user explicitly asked for commit history.

### How entries are added

1. **Phase completion emails:** `send_phase_completion_email.py` calls `append_to_timeline()` when a phase is completed
2. **Manual:** Call `context_loader.append_to_timeline(date, etype, title, detail)` or edit `project_timeline.json` directly
3. **Max entries:** `PROJECT_TIMELINE_MAX_ENTRIES` (50); oldest pruned

### Related docs

- [coordinator-project-context.md](coordinator-project-context.md) — Golden example for "Tell me recent project changes"
- [JARVEY_FAQ.md](data-sets/JARVEY_FAQ.md) — "What are recent updates / what changed recently?"

---

## Jarvey Recent Changes (Model / Bot Updates)

### Primary: JARVEY_IMPROVEMENT_LOG.md

- **Path:** `docs/agents/JARVEY_IMPROVEMENT_LOG.md`
- **Content:** Fixes, before/after params, what worked and why
- **Loaded by:** `jarvey_self` intent
- **Intent keywords:** "jarvey fix", "what fixes worked", "jarvey improvement", "jarvey changes", "recent jarvey updates"

### Key entries (for training / future use)

- Same-inbox mode fix (2026-02-25)
- UNSEEN-only and case-sensitive subject fix (2026-02-25)
- Prompt fix: "Send me recent updates" clarity (2026-02-25)
- Anti-hallucination and HITL distillation
- Re:/forwarded email support
- Context plan, response patterns, clarification flow, structured output, circuit breaker, observability

### Related docs

- [JARVEY_IMPROVEMENT_LOG.md](JARVEY_IMPROVEMENT_LOG.md) — Full change log
- [JARVEY_FAQ.md](data-sets/JARVEY_FAQ.md) — "What fixes worked for Jarvey?"
- [JARVEY_CONTEXT_PLAN.md](JARVEY_CONTEXT_PLAN.md) — How Jarvey responds to scenarios

---

## Data Flow (Jarvey prompt)

```
User: "What recent changes have been done to the project?"
  → detect_intents() → ["recent"]
  → load_context_for_user_message() loads project_timeline_curated (800 chars)
  → get_project_timeline_curated() returns entries or "No curated entries yet..."
  → LLM composes reply from that context
```

```
User: "What recent changes to Jarvey?"
  → detect_intents() → ["jarvey_self"]
  → load_context_for_user_message() loads JARVEY_IMPROVEMENT_LOG, JARVEY_EVALUATION_REVIEW, etc.
  → LLM composes reply from that context
```

---

## Maintenance

- **When adding project changes:** Use `append_to_timeline()` or run `send_phase_completion_email.py` for phase completions
- **When adding Jarvey fixes:** Update `JARVEY_IMPROVEMENT_LOG.md` with date, change, before/after, notes
- **When user reports wrong answer:** Check intent keywords and source caps; add to FAQ if recurring
