# Board Meeting Summary — 2025-03-15

## Alignment

All roles confirmed they operate from **Known Truths and Single Source of Truth** for persistence, recovery, calendar, and settings. No contradictions raised.

## Recommendations by role

| Role | Key recommendations |
|------|---------------------|
| **Design/Creative** | Keep roadmap and crucial improvements in sync; briefs reference SSOT; user decisions via Human-in-the-Loop with clear summary. |
| **UI/UX** | One accessibility pass (checklist → Front-end); stats/period copy clarity; Help & Info placement spec. |
| **Front-end** | Single source for calendar/stats from ViewModel/Room; Export button/ViewModel handoff to Back-end for contract; Settings via existing managers. |
| **Back-end** | No new persistence paths; OfflineDataManager/SyncWorker must not bypass Clear semantics; period boundaries stay in UnifiedTripService/PeriodCalculationService. |
| **DevOps** | CI runs correct tests; two-way email program: document how user sends and agents read; no secrets in repo. |
| **QA** | Test SSOT explicitly; test plan per major feature; smoke-test email scripts when changed. |
| **Security** | Review Export (FileProvider, share scope) when it lands; confirm .env/last_reply not committed; short lost-device threat note. |
| **Email Editor** | Subject templates (decision/update/confirm); one-sentence value prop; clear, short tone. |
| **File Organizer** | Board Meeting output in board-meeting/; index key plans in docs; security-exercises convention. |
| **Red Team** | Next Purple exercise when Export ships; document for Blue what to check; no scope creep. |
| **Blue Team** | Define “alarm” per surface; auditable remediation; re-test after fix. |

## Handoffs

- No blockers.
- **DevOps + Human-in-the-Loop:** Continue two-way email program (user can email team anytime; agents read replies).
- **Security:** Review when Export is in scope.
- **Red/Blue:** Run Purple exercise when we have a target (e.g. Export flow).

## What the user should be told

1. Board Meeting ran; full team contributed with SSOT-aligned recommendations.
2. No urgent blockers. Optional: weekly Board digest by email.
3. When Export or a big feature lands: Security review and optional Purple exercise; user emailed when done.

## Transcript

Full transcript: **board-meeting-2025-03-15.md** in this folder.
