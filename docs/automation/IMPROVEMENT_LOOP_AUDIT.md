# Improvement Loop — Audit (Blind Spots & Loose Ends)

**Purpose:** Lock down the Improvement Loop by documenting blind spots, loose ends, and fixes. Re-run this audit after major changes.

**Last audit:** 2025-03-11  
**References:** [IMPROVEMENT_LOOP_ROUTINE.md](./IMPROVEMENT_LOOP_ROUTINE.md), [IMPROVEMENT_LOOP_COMMON_SENSE.md](./IMPROVEMENT_LOOP_COMMON_SENSE.md), [LOOP_TIERING.md](./LOOP_TIERING.md)

---

## Resolved (Fixed)

| Item | Was | Fix |
|------|-----|-----|
| Phase ordering | 0.2 Autonomy appeared after 0.4 | Moved 0.2 to "Setup" section; clarified one-time |
| IMPROVEMENT_LOOP_TEAMS Heavy clause | Missing question lock | Added question lock to Heavy approval flow |
| Missing docs | META_RESEARCH, SECURITY_LOOP, etc. | Created stubs or made optional; documented fallback |
| Revert edge case | No guidance if no checkpoint | Added "If no checkpoint: inform user" |
| Listener at phases | Easy to forget | Added explicit phase_start/phase_end reminders |
| Unit tests only | Not explicit in all contexts | Added to Common Sense, Out of Scope, FAILING_OR_IGNORED_TESTS |

---

## Blind Spots (Watch For)

| Blind spot | Risk | Mitigation |
|------------|------|------------|
| **run_120min_loop.ps1 vs agent** | Script pulses; agent runs phases. Confusion about who does what. | Script is timer only. Agent follows IMPROVEMENT_LOOP_ROUTINE and executes phases. Document in routine. |
| **Quick variant + sandbox** | Quick skips most phases. "Every loop" sandbox improvement—does Quick do it? | Quick: optional. If time permits, add one brainstorm/sandbox note. Not required. |
| **Subagents and Common Sense** | Subagents may not read COMMON_SENSE; could violate rules. | Delegation rules: "Give subagent constraints: no UI changes, timebox 10 min, tests must pass." |
| **File Organizer timing** | "After summary" — does File Organizer amend the summary or is it written in one pass? | Summary author includes "Recommended new ideas" (item 10). File Organizer can run as subagent to propose; author incorporates. |
| **HEAVY_TIER_IDEAS vs FUTURE_IDEAS** | Both exist; flow between them can be unclear. | FUTURE_IDEAS = product ideas (sandboxed). HEAVY_TIER_IDEAS = loop backlog with state/%. Promote: FUTURE_IDEAS → validate → HEAVY_TIER_IDEAS. |

---

## Referenced Docs — Existence Check

| Doc | Exists? | Fallback if missing |
|-----|---------|---------------------|
| USER_PREFERENCES_AND_DESIGN_INTENT | Yes | Required; create stub if missing |
| META_RESEARCH_CHECKLIST | **No** | Skip 0.0b or use inline 5 questions from IMPROVEMENT_LOOP_TEAMS |
| SECURITY_LOOP_CHECKLIST | **No** | Use SECURITY_NOTES + SECURITY_CHECKLIST; grep PII, FileProvider |
| USER_METADATA_USAGE_GUIDE | **No** | Skip when Data/Metrics focus; document "create when needed" |
| DESIGN_AND_UX_RESEARCH | **No** | Use docs/ux/UI_CONSISTENCY + web search; create stub when needed |
| SHIPABILITY_CHECKLIST | **No** | Use STORE_CHECKLIST; create when Shipability focus |
| SECURITY_NOTES | Yes | — |
| SECURITY_CHECKLIST | Yes | — |
| TERMINOLOGY_AND_COPY | Yes (docs/ux/) | — |
| UI_CONSISTENCY | Yes (docs/ux/) | — |

---

## Loose Ends — Tied Up

| Loose end | Resolution |
|-----------|------------|
| **Revert with no checkpoint** | If user says "revert" and no checkpoint exists: "No checkpoint found. Create one next run with git commit or tag." |
| **Listener at phase boundaries** | Agent invokes `loop_listener.ps1 -Event phase_start -Phase N` at start of each phase; `phase_end` at end. Added to Phase 1–4 headers. |
| **"Light only" and checkpoint** | Checkpoint (0.0) runs before classification. User saying "Light only" skips Medium/Heavy tasks, not checkpoint. |
| **Dead code in Quick** | Quick says "one quick win (dead code or doc link)." Dead code = Medium. Doc link = Light. Both valid for Quick. |
| **Gradle commands (Windows)** | Routine uses `.\gradlew.bat`; LOOP_MASTER_ALLOWLIST uses `gradlew.bat`. Consistent. |

---

## Scope Summary (Locked Down)

**In scope:** Light + Medium (autonomous). Unit tests only. Checkpoint first. Reasoning at checkpoints. Question lock for Heavy.

**Out of scope:** Heavy (without approval). Instrumented tests. OfflineDataManager load/save. Location jump. Trip history→details. Statistics monthly-only. Gradle 9. New features. Major refactors.

**Required reads at loop start:** IMPROVEMENT_LOOP_COMMON_SENSE, IMPROVEMENT_LOOP_REASONING, USER_PREFERENCES_AND_DESIGN_INTENT.

---

*Audit complete. Update when new blind spots or loose ends are discovered.*
