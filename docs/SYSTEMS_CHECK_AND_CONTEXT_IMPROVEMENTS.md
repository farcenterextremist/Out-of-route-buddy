# Systems Check — Context, Scope, and Improvement Opportunities

**Date:** 2025-03-11  
**Purpose:** Overall systems check; identify ways to improve agent context and project scope clarity.

---

## 1. Current State — What’s Working

| Area | Status | Evidence |
|------|--------|----------|
| **Documentation index** | Strong | `docs/README.md` — clear quick links to ROADMAP, CRUCIAL, Known Truths, ARCHITECTURE |
| **Single source of truth** | Strong | `KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md` — canonical behavior for persistence, recovery, calendar, GPS |
| **Codebase overview** | Good | `CODEBASE_OVERVIEW.md` — one-page summary for “how does X work?” |
| **Technical wiring** | Good | `WIRING_MAP.md`, `TRIP_PERSISTENCE_END_CLEAR.md`, `GPS_AND_LOCATION_WIRING.md` |
| **Role structure** | Strong | 12+ roles with cards, data-sets, handoff rules; Red/Blue/Purple team |
| **Automation** | Good | 2-hour loop routine, autonomous setup, master allowlist |
| **Security** | Documented | SECURITY_NOTES, SECURITY_CHECKLIST, SECURITY_PLAN |
| **QA** | Documented | TEST_STRATEGY, FAILING_OR_IGNORED_TESTS, coverage docs |

---

## 2. Context Gaps — What Agents May Miss

| Gap | Impact | Recommendation |
|-----|--------|----------------|
| **No root AGENTS.md** | New agents/sessions don’t know where to start | Add `AGENTS.md` at repo root: “Start here. Read docs/README.md, docs/agents/KNOWN_TRUTHS. For Improvement Loop: docs/automation/IMPROVEMENT_LOOP_ROUTINE.md.” |
| **CODEBASE_OVERVIEW is “for Jarvey”** | Generic agents may skip it | Rename or add a short “For all agents” intro; keep Jarvey reference as secondary |
| **164+ docs in docs/** | Hard to know what’s current vs archived | Add `docs/archive/README.md` note; consider a “START_HERE.md” that lists the 5–7 must-read docs |
| **Scope scattered** | “Out of scope” lives in many files (CRUCIAL, QUALITY_PLAN, FEATURE_BRIEFs, role cards) | Create `docs/SCOPE_AND_BOUNDARIES.md` — single place for in-scope vs out-of-scope |
| **Cursor rules are sparse** | Only 4 rules: kotlin-best-practices, red-team, blue-team, 2-hour-loop | Add `project-context.mdc` that always injects: “OutOfRouteBuddy: OOR miles app. SSOT: docs/agents/KNOWN_TRUTHS. Improvements: docs/CRUCIAL_IMPROVEMENTS_TODO.” |
| **No .cursorignore** | Agent may read build artifacts, .gradle, etc. | Add `.cursorignore` to exclude `build/`, `.gradle/`, `*.apk`, large generated dirs |

---

## 3. Scope Gaps — Boundaries Are Fragmented

| Issue | Where it lives | Recommendation |
|-------|----------------|----------------|
| **In-scope for loop** | IMPROVEMENT_LOOP_ROUTINE “Out of Scope” | Good; keep. Add cross-link to SCOPE_AND_BOUNDARIES |
| **In-scope for CRUCIAL** | CRUCIAL_IMPROVEMENTS_TODO | Good |
| **Role boundaries** | Each role card has “out of scope” | Good; consolidate into SCOPE_AND_BOUNDARIES as reference |
| **UI change policy** | User rule: “DO NOT MAKE UNWARRANTED UI CHANGES” | Not in docs; add to SCOPE_AND_BOUNDARIES or team-parameters |
| **Statistics monthly-only** | CRUCIAL §9; needs user approval | Explicit in SCOPE: “Deferred until user approves” |

---

## 4. Recommended Improvements (Prioritized)

### High impact, low effort

1. **Add `AGENTS.md` at repo root**
   - 10–15 lines: project one-liner, “Start here” links, loop trigger
   - Ensures new sessions get context fast

2. **Add `docs/SCOPE_AND_BOUNDARIES.md`**
   - In-scope: OOR tracking, trip lifecycle, stats, history, settings, security, QA
   - Out-of-scope: OfflineDataManager impl (until designed), Gradle 9 migration, statistics monthly-only (until user approves), unwarranted UI changes
   - Cross-link from CRUCIAL, ROADMAP, role cards

3. **Add `.cursorignore`**
   - Exclude: `build/`, `.gradle/`, `**/build/`, `*.apk`, `*.aab`, `app/schemas/` (generated), `captures/`
   - Reduces noise and token use

### Medium impact

4. **Add `docs/START_HERE.md`**
   - “New to this project? Read these 5–7 docs in order.”
   - Links: README → KNOWN_TRUTHS → ARCHITECTURE → CRUCIAL → ROADMAP → SCOPE_AND_BOUNDARIES

5. **Add `.cursor/rules/project-context.mdc`**
   - `alwaysApply: true` (or appropriate glob)
   - One paragraph: project goal, SSOT path, improvement list path, “no unwarranted UI changes”

6. **Refresh CODEBASE_OVERVIEW**
   - Add “For all agents” at top; keep Jarvey as “also used by”
   - Add line: “TripEndedDetector, TripEndedOverlayService” if those are now central

### Lower priority

7. **Archive audit**
   - `docs/archive/` — ensure README says “archived; current work in CRUCIAL + ROADMAP”
   - Consider moving older prompts (IRON_OUT, HEALTH_AND_CODE_QUALITY, etc.) to archive if superseded

8. **Team parameters update**
   - team-parameters.md has “Primary block: Sunday night” — ensure ROADMAP and automation docs reference this for sprint planning

---

## 5. Summary

| Dimension | Grade | Notes |
|-----------|-------|-------|
| **Documentation structure** | A- | Strong index, SSOT, wiring; could use START_HERE and SCOPE consolidation |
| **Agent context** | B+ | Good role/SSOT; missing root AGENTS.md and always-on project rule |
| **Scope clarity** | B | Scattered across many docs; needs SCOPE_AND_BOUNDARIES |
| **Automation** | A | 2-hour loop, autonomous setup, allowlist well documented |
| **Technical depth** | A | Wiring, persistence, GPS, recovery all documented |

---

## 6. Quick Wins (Next 2-Hour Loop)

- [ ] Create `AGENTS.md` (5 min)
- [ ] Create `docs/SCOPE_AND_BOUNDARIES.md` (15 min)
- [ ] Add `.cursorignore` (5 min)
- [ ] Add `docs/START_HERE.md` (10 min)

---

*Systems check complete. Implement high-impact items first.*
