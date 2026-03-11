# Improvement Loop — Common Sense Parameters

**Purpose:** Non-negotiable rules and guardrails for every Improvement Loop run. Apply during the mission for safe, predictable, full autonomous execution.

**References:** [IMPROVEMENT_LOOP_ROUTINE.md](./IMPROVEMENT_LOOP_ROUTINE.md), [LOOP_TIERING.md](./LOOP_TIERING.md), [AUTONOMOUS_LOOP_SETUP.md](./AUTONOMOUS_LOOP_SETUP.md)

---

## Before Any Changes

| Parameter | Rule |
|-----------|------|
| **Pre-loop checkpoint** | Create checkpoint (git commit or tag) before making any changes. Record in summary. Enables "revert" if something breaks. |
| **Read USER_PREFERENCES first** | Read [USER_PREFERENCES_AND_DESIGN_INTENT.md](./USER_PREFERENCES_AND_DESIGN_INTENT.md) before research or changes. Do not drift from design intent. |
| **No UI changes without permission** | User rule: "DO NOT MAKE ANY UNWARRANTED CHANGES TO THE UI WITHOUT MY PERMISSION." When uncertain → suggest, don't implement. |

---

## During Execution

| Parameter | Rule |
|-----------|------|
| **Tests must pass** | If a change breaks tests → revert the change, report in summary, move on. Do not proceed with failing tests. |
| **Timebox per task** | Do not spend >10 min on any single test fix or dead-code item. If stuck → document and defer. |
| **One improvement per category** | Kaizen rule: One improvement per category per loop (Phase 1–3). Avoid overload. |
| **No secrets or PII** | Never add API keys, passwords, or PII to logs or code. Grep for coordinates/tripId before committing. |
| **Additive only (Light)** | Light tier: additive only (strings, docs, comments). No code removal, no refactor, no new logic. |
| **Small and localized (Medium)** | Medium tier: small, localized change. Low risk. Do not drift from original UI layout. |

---

## When Things Go Wrong

| Parameter | Rule |
|-----------|------|
| **Build fails** | Document in summary. Try lint anyway if possible. Do not pretend build passed. Suggest `./gradlew clean assembleDebug` in next steps. |
| **Tests fail** | Stop. Revert the change that caused it. Report in summary. Do not leave tests red. |
| **Uncertain** | When in doubt → suggest in summary, don't implement. Ask user. |
| **Heavy task** | Never implement Heavy without: (1) sandboxed in FUTURE_IDEAS, (2) visual image, (3) user says "approve 100% implement". **One by one:** Ask "Are you ready to implement this new feature?" before each Heavy feature. Never run the entire Heavy tier at once. |

---

## Full Autonomous Mode

When running **fully autonomous** (user said GO, Run Everything or allowlist configured):

| Parameter | Rule |
|-----------|------|
| **Light + Medium** | Run without stopping. No prompts. Execute all Light and Medium tasks in scope. |
| **Heavy** | Skip or document for later. Do not implement Heavy without user approval. If Heavy tasks exist, add to summary "Proposed for next run (requires approval)" and continue. |
| **No approval prompts** | Do not stop to ask "Would you like me to implement these medium tasks?" — Medium runs autonomously. |
| **Summary at end** | Always write A-grade summary with Metrics block, checkpoint, Quality Grade. |

---

## Summary

- **Checkpoint first.** Revert if broken.
- **Respect design intent.** No unwarranted UI changes.
- **Tests green.** Revert changes that break tests.
- **Timebox.** Move on if stuck.
- **Autonomous = Light + Medium run; Heavy deferred.**

---

*Integrates with IMPROVEMENT_LOOP_ROUTINE. Read at loop start.*
