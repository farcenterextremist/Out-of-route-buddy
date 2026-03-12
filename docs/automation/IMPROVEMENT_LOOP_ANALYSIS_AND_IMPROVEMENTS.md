# Improvement Loop — Analysis, Improvements, and Priming

**Purpose:** Analyze the current Improvement Loop, suggest concrete improvements, and prime the next run.  
**Created:** 2025-03-11  
**References:** [IMPROVEMENT_LOOP_ROUTINE.md](./IMPROVEMENT_LOOP_ROUTINE.md), [2-hour-loop.mdc](../.cursor/rules/2-hour-loop.mdc), [improvement-loop-wizard SKILL](../.cursor/skills/improvement-loop-wizard/SKILL.md), [120_MINUTE_LOOP_SUMMARY_2025-03-11.md](./120_MINUTE_LOOP_SUMMARY_2025-03-11.md)

---

## 1. Current Loop Anatomy

| Layer | Doc / Trigger | Role |
|-------|----------------|------|
| **Trigger** | User says **GO** / "run improvement loop" | 2-hour-loop.mdc, AGENTS.md, improvement-loop-wizard SKILL |
| **Common sense** | IMPROVEMENT_LOOP_COMMON_SENSE.md | Checkpoint first, no UI drift, tests green, timebox |
| **Reasoning** | IMPROVEMENT_LOOP_REASONING.md | Checkpoints before research, task selection, each change, summary |
| **Routine** | IMPROVEMENT_LOOP_ROUTINE.md | Phases 0–4; tiering; listener; subagents |
| **Tiering** | LOOP_TIERING.md | Light (auto), Medium (auto), Heavy (approval + question lock + visual) |
| **Self-improvement** | CURSOR_SELF_IMPROVEMENT.md | Safe web, context refresh, research 1 CRUCIAL, drastic → suggest |
| **Focus** | LOOP_FOCUS_ROTATION.md | Rotate: Security → UI/UX → Shipability → Code Quality → File Structure → Data/Metrics |
| **Plan variant** | 120_MINUTE_IMPROVEMENT_LOOP.md | Concrete 2‑hr plan with subagent spawn reference |

**Last run (2025-03-11):** Shipability/Code Quality focus. BuildConfig alignment, version in Settings, stat card accessibility. Build had pre-existing AAPT issues; checkpoint was not recorded.

---

## 2. Analysis — What Works Well

- **Single entry point:** "GO" is clear; wizard + rule + AGENTS.md align.
- **Tiering and autonomy:** Light + Medium without stopping; Heavy gated (question lock, visual approval) is well specified.
- **Reasoning checkpoints:** Explicit "think before you act" and summary Reasoning table improve traceability.
- **Research-first:** Phase 0 reads CRUCIAL, REDUNDANT_DEAD_CODE, FAILING_OR_IGNORED_TESTS, security, tiering before any change.
- **Safety:** Revert from checkpoint, no unwarranted UI changes, unit tests only in loop.
- **Summary format:** A-grade template (metrics, next steps, Quality Grade) is defined and was followed in 2025-03-11.

---

## 3. Gaps and Risks

| Gap | Impact | Recommendation |
|-----|--------|----------------|
| **USER_PREFERENCES_AND_DESIGN_INTENT.md missing** | Routine and wizard say "read first"; audit says "Yes". Agent may skip or assume. | Create stub (see §5 Priming). |
| **Phase 0.5 vs 0.3 naming** | CURSOR_SELF_IMPROVEMENT says "Phase 0.5"; ROUTINE says "0.3 Cursor self-improvement". | Align on one number in both docs (e.g. keep 0.3 in ROUTINE, reference 0.5 in CURSOR_SELF_IMPROVEMENT as "optional Phase 0.5" for clarity). |
| **Checkpoint not recorded** | Last summary: "Checkpoint: Not recorded". Revert not possible. | Make checkpoint **required** in common sense; add to wizard Step 0 as mandatory; add to summary template as required field. |
| **Build before loop** | AAPT errors prevented lint. Loop didn’t run clean/build first. | Add "Optional: run `./gradlew clean assembleDebug` before Phase 1; if build fails, document and still run unit tests (often pass despite assemble issues)." in routine or common sense. |
| **Design & UX research (0.4)** | CURSOR_SELF_IMPROVEMENT checklist has "Phase 0.4 Design & UX research" but ROUTINE Phase 0.4 is "extend when UI/UX focus". | Keep as-is; 0.4 is optional/deeper when UI/UX focus. Ensure summary always has "Design research" line (even if "Skipped — not UI/UX focus"). |
| **Suggested next steps vs CRUCIAL** | Overlap between "Suggested next steps" in summary and CRUCIAL_IMPROVEMENTS_TODO. | Next run: pull 1–2 items from CRUCIAL into "Suggested next steps" explicitly so they’re in one place for the agent. |
| **REDUNDANT_DEAD_CODE §1** | BuildConfig alignment was done (TripTrackingService uses BuildConfig). §1 still says "Not used" and "refactor TripTrackingService to use BuildConfig". | Update §1 to mark ACTION_START_TRIP/ACTION_END_TRIP as resolved; leave NOTIFICATION_CHANNEL_ID if still unused. |

---

## 4. Suggested Improvements (Non-Drastic)

- **Checkpoint mandatory:** In IMPROVEMENT_LOOP_COMMON_SENSE and wizard Step 0: checkpoint is required; summary must include checkpoint (commit or tag). If working tree dirty, allow `git stash` then commit, or tag HEAD.
- **Pre-loop build note:** In IMPROVEMENT_LOOP_ROUTINE Phase 0 or common sense: "If you have time, run `./gradlew clean assembleDebug` before Phase 1. If build fails, note in summary and continue; run unit tests anyway."
- **Single focus doc for next run:** Add to each summary a "Next run focus" line (already in LOOP_FOCUS_ROTATION). In priming, set "Next run focus: Security" (next in rotation after Shipability/Code Quality).
- **Phase 0.3 / 0.5 alignment:** In CURSOR_SELF_IMPROVEMENT.md, add one line: "This is Phase 0.5 in some docs; in IMPROVEMENT_LOOP_ROUTINE it is Phase 0.3. Same content."
- **Reasoning in summary:** Last summary had no "Reasoning (this run)" table. Remind in LOOP_METRICS_TEMPLATE or ROUTINE 4.3: include Reasoning table per IMPROVEMENT_LOOP_REASONING.md.
- **File Organizer / Heavy ideas:** ROUTINE 4.3 item 10 says "Add at least 1–2 Heavy ideas per run when HEAVY_TIER_IDEAS below 50." Confirm HEAVY_TIER_IDEAS.md exists and is referenced; if not, add to priming.

---

## 5. Priming for Next Run

Done as part of this analysis:

1. **USER_PREFERENCES_AND_DESIGN_INTENT.md** — Stub created so "read first" succeeds. You can fill in design intent and must-not-change list.
2. **120_MINUTE_LOOP_SUMMARY_2025-03-11.md** — Updated with explicit "Next run focus" and priming note.
3. **REDUNDANT_DEAD_CODE_REPORT.md** — §1 updated to reflect BuildConfig alignment done; NOTIFICATION_CHANNEL_ID left as optional next step.
4. **This document** — Serves as the "analyze and prime" artifact; next run can read it in Phase 0 for context.

**Next run focus (recommended):** **Security** (next in LOOP_FOCUS_ROTATION after Shipability/Code Quality).

**Quick priming checklist for you (optional):**

- [ ] Run `./gradlew clean assembleDebug` once to see if AAPT issues persist; if they do, note in summary again.
- [ ] Add any design intent or "must not change" items to USER_PREFERENCES_AND_DESIGN_INTENT.md.
- [ ] When you say **GO**, the agent will create a checkpoint first (commit or tag), then read research docs, then execute Light + Medium.

---

## 6. Summary

| Aspect | Status |
|--------|--------|
| **Loop structure** | Solid; trigger, tiering, reasoning, and routine are aligned. |
| **Risks** | Missing USER_PREFERENCES doc (fixed with stub), checkpoint not mandatory (improvement suggested), phase numbering 0.3 vs 0.5 (cosmetic). |
| **Improvements** | Mandatory checkpoint + record in summary; pre-loop build note; Reasoning table in every summary; REDUNDANT_DEAD_CODE §1 updated. |
| **Priming** | Stub created; last summary updated; REDUNDANT_DEAD_CODE updated; next run focus set to Security. |

You can run the next loop by saying **GO**; the agent will follow the routine, create a checkpoint first, and use the primed inputs above.
