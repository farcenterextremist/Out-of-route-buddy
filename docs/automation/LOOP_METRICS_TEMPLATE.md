# Improvement Loop — Metrics Template

**Purpose:** Define which metrics to capture per run to close the improvement loop. Research: "Close the loop with measurable checks."

**References:** [IMPROVEMENT_LOOP_RESEARCH_2025-03.md](./IMPROVEMENT_LOOP_RESEARCH_2025-03.md), [IMPROVEMENT_LOOP_ROUTINE.md](./IMPROVEMENT_LOOP_ROUTINE.md)

---

## Required Metrics (Every Run)

| Metric | Source | How to capture |
|--------|--------|----------------|
| **Test count** | `.\gradlew.bat :app:testDebugUnitTest` | Pass/fail from output; total count |
| **Lint** | `.\gradlew.bat :app:lintDebug` | Errors / warnings from report |
| **Files changed** | Git | `git diff --stat` or `git status --short` count |
| **Focus area** | This run | Security, UI/UX, Shipability, Code Quality, File Structure, Data/Metrics |
| **Variant** | Trigger | Quick (30 min), Standard (90 min), Full (2 hr) |

---

## Optional Metrics (When Available)

| Metric | Source | How to capture |
|--------|--------|----------------|
| **Coverage %** | `.\gradlew.bat jacocoSuite` | From report `app/build/reports/jacoco/` |
| **Build time** | Gradle | `.\gradlew.bat assembleDebug --profile` |
| **Checkpoint** | Pre-loop | Commit hash or tag name for revert |
| **User metadata** | App (opt-in) | If metadata collection/display was added: note in summary. See [USER_METADATA_USAGE_GUIDE.md](./USER_METADATA_USAGE_GUIDE.md). |

---

## Summary Block Format

Copy into every summary:

```markdown
## Metrics

| Metric | Value |
|--------|-------|
| Tests | Pass / Fail (count) |
| Lint | 0 errors, X warnings |
| Files changed | N |
| Focus | [Security \| UI/UX \| Shipability \| Code Quality \| File Structure \| Data/Metrics] |
| Variant | [Quick \| Standard \| Full] |
| Checkpoint | `commit` or `tag` |
```

---

## File Organizer Use

File Organizer uses metrics to recommend next focus (e.g., "Lint warnings increased → next focus: Code Quality").
