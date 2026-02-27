# Board Meeting — 2025-03-15

**Purpose:** Full team sync — opinions, recommendations, and progress aligned with Known Truths and Single Source of Truth.

---

## 0. Coordinator (chair)

**Coordinator:** Board Meeting is open. Today we’re gathering every role’s voice: short updates, recommendations based on best practice and SSOT, and any handoffs or questions for the user. I’ll keep the order clear and hand off to Human-in-the-Loop at the end so we can summarize and optionally email the user.

**Order of speakers:** Design/Creative → UI/UX → Front-end → Back-end → DevOps → QA → Security → Email Editor → File Organizer → Red Team → Blue Team → Human-in-the-Loop.

I don’t speak for other roles; I only chair. Over to Design/Creative.

---

## 1. Project Design / Creative Manager

**Design/Creative:** I own vision, roadmap, and feature prioritization. Scope: product direction and briefs; I hand off to UI/UX for flows and to the Coordinator for implementation assignment.

**Update:** Roadmap and feature briefs in `docs/product/` should stay aligned with Known Truths so we don’t promise behavior that contradicts End vs Clear, recovery, or calendar/stats.

**Recommendations:**
1. **Prioritize from one source.** Keep `docs/product/ROADMAP.md` and `docs/CRUCIAL_IMPROVEMENTS_TODO.md` (and any 25-point brainstorm) in sync; when the user picks “next,” we update the roadmap and hand off in order (Design → UI/UX → Eng → QA).
2. **Briefs reference SSOT.** Every feature brief should say “persistence/recovery/calendar per Known Truths” so UI/UX and Eng don’t invent alternate flows.
3. **User decisions go through HITL.** When we have “Option A vs B vs defer,” I hand off to Human-in-the-Loop with a one-paragraph summary so the user gets a clear email, not jargon.

**Handoff:** Nothing blocking. Next: UI/UX.

---

## 2. UI/UX Specialist

**UI/UX:** I’m the bridge between product and implementation. I own interfaces, flows, accessibility, and layout guidance; Front-end implements.

**Update:** Flows and copy should match actual behavior—End vs Clear, what drives calendar and stats (Room only, per SSOT). I reference `res/layout/`, `strings.xml`, and the emulator so recommendations are implementable.

**Recommendations:**
1. **One accessibility pass.** Document one consolidated pass (touch targets, labels, contrast) in `docs/ux/ACCESSIBILITY_CHECKLIST.md` and hand off to Front-end as a single batch so we don’t scatter fixes.
2. **Statistics section clarity.** Ensure the stats row and period selector clearly reflect “current month” vs “custom period” (Thursday–Friday) so users don’t wonder where numbers come from; copy in strings and any tooltips should say so.
3. **Help & Info placement.** If we add or move Help/Info, keep it discoverable and consistent with existing settings/dialog patterns; I’ll spec, Front-end implements.

**Handoff:** None this week. Over to Front-end.

---

## 3. Front-end Engineer

**Front-end:** I implement the Android UI: layouts, fragments, view binding, themes. I live in `app/src/main/res/` and `presentation/`; data contracts come from Back-end.

**Update:** UI is wired to ViewModels and repositories per Known Truths. No UI talks to Room or SharedPreferences directly; TripInputFragment → TripInputViewModel → TripRepository, etc. Theme and distance units from SettingsManager.

**Recommendations:**
1. **Calendar/stats single source.** CustomCalendarDialog and the stats row should only consume what ViewModel gets from Room (getTripsByDateRange, getMonthlyTripStatistics). No duplicate logic in the UI layer.
2. **Export/PDF handoff.** If we add Export to PDF/CSV, I own the button and ViewModel call; Back-end owns the file/URI and repository API. I’ll hand off for the contract if it doesn’t exist yet.
3. **Settings dialog consistency.** Theme and distance units are in SettingsManager; any new settings should follow the same pattern (read/write via existing manager, no direct prefs in fragments).

**Handoff:** None. Back-end next.

---

## 4. Back-end Engineer

**Back-end:** I own data, persistence, services, and business logic. Room, TripRepository, TripTrackingService, period calculation, recovery—all behind the UI.

**Update:** Repository chain is clear: Domain TripRepository ← DomainTripRepositoryAdapter ← data TripRepository ← TripDao. Only End trip writes to Room; Clear never inserts. Recovery: Application.recoveredTripState then TripPersistenceManager then inactive. Live miles from TripTrackingService only.

**Recommendations:**
1. **No new persistence paths.** Any new feature that “saves” trip-related data should go through the existing repository chain and respect End vs Clear; no side stores for monthly stats or calendar.
2. **OfflineDataManager/SyncWorker.** These are stubs per Known Truths; when we implement, they must not bypass Clear semantics or create a second source for trip data.
3. **Period boundaries.** UnifiedTripService + PeriodCalculationService own STANDARD vs CUSTOM; any change to period logic stays here and is documented in Known Truths.

**Handoff:** None. DevOps next.

---

## 5. DevOps Engineer

**DevOps:** I own build, CI/CD, Gradle, and deployment. I don’t own app feature code or test strategy—I make the project buildable and the pipeline reliable.

**Update:** Gradle and `.github/` workflows should run tests and build cleanly. Two-way email program with Human-in-the-Loop: scripts in `scripts/coordinator-email/`; we need a clear path for the user to send mail and for agents to read replies (scheduled or on-demand).

**Recommendations:**
1. **CI runs the right tests.** Ensure `./gradlew test` (and any androidTest we rely on) run in CI; QA defines what “right” means, I own the YAML and Gradle wiring.
2. **Two-way email UX.** Document how the user initiates (e.g. reply to same inbox) and how agents pick up (read_replies, on-demand or wrapper); short user-facing instructions so it’s not a mystery.
3. **No secrets in repo.** local.properties, .env, and credentials stay out of version control; Security can review pipeline config if we add secrets handling.

**Handoff:** Test strategy and coverage expectations to QA. Over to QA.

---

## 6. QA Engineer

**QA:** I own test strategy, test cases, and regression—not feature implementation. I consume Known Truths so test scenarios match End vs Clear, recovery precedence, and calendar/stats source.

**Update:** Unit tests in `app/src/test/`, instrumented in `app/src/androidTest/`. Tests should assert the right behavior: e.g. Clear never inserts, recovery order, monthly stats from Room only.

**Recommendations:**
1. **Test the SSOT.** Add or extend tests that explicitly verify Known Truths (e.g. Clear doesn’t insert; recovery order; TripTrackingService is source for live miles). Prevents regressions when someone “optimizes” and breaks the contract.
2. **Test plan per major feature.** For Auto drive or Export, maintain a short TEST_PLAN_<feature>.md with scenarios and how to run; hand off failures to Front-end or Back-end with steps.
3. **Email script smoke.** When we change coordinator-email scripts, smoke-test send/read (or dry-run) so we don’t break the open line; mock/stub where needed.

**Handoff:** None. Security next.

---

## 7. Security Specialist

**Security:** I do security review and recommendations—no implementation. I point at SECURITY_NOTES, manifest, and data handling; Red/Blue do attack/defense.

**Update:** Trip/location data on-device only; no PII in logs per Known Truths. FileProvider and export paths (when we add them) need a review for path traversal and share scope.

**Recommendations:**
1. **Export feature review.** When Export to PDF/CSV lands, review: FileProvider paths, what’s in the file, share scope, and document in SECURITY_NOTES or REVIEW_export.md.
2. **.env and last_reply.** Confirm .env and last_reply are never committed; scripts/coordinator-email should be in .gitignore or documented as “local only.”
3. **Lost-device scenario.** Short threat note: if device is lost, what’s on it (trips, location) and what we recommend (e.g. remote wipe, user guidance); no implementation, just doc for the user.

**Handoff:** Red/Blue for any new attack-surface work. Email Editor next.

---

## 8. Email Editor / Market Guru

**Email Editor:** I draft email copy and messaging only; Human-in-the-Loop sends. I don’t make product or technical decisions.

**Update:** When we mention features (trip save, calendar, stats) in email, copy should match Known Truths so we don’t promise behavior we don’t have (e.g. “sync to cloud” if it’s deferred).

**Recommendations:**
1. **Subject templates.** Keep 2–3 templates: decision (e.g. “Quick decision: …”), update (“… complete — please review”), confirm (“… — reply Yes/No”). Human-in-the-Loop can pick so the user sees consistent, scannable subjects.
2. **One-sentence value prop.** One clear line for OutOfRouteBuddy (e.g. “Track out-of-route miles accurately in one place”) for outreach or stakeholder emails; I’ll drop it in docs/comms or SUBJECT_LINE_TEMPLATES.
3. **Tone.** Professional, short, one clear ask per email so the user can reply without decoding jargon.

**Handoff:** All sending to Human-in-the-Loop. File Organizer next.

---

## 9. File Organizer

**File Organizer:** I own repo and doc structure and file naming. I propose where things live; Coordinator or user approves before big moves.

**Update:** docs/agents/, data-sets/, security-exercises/, board-meeting/ follow clear naming (e.g. YYYY-MM-DD-short-name.md). Known Truths and wiring docs stay linked when we add or move files.

**Recommendations:**
1. **Board Meeting home.** Transcripts and summaries live in `docs/agents/data-sets/board-meeting/` with naming board-meeting-YYYY-MM-DD.md; README there explains it. No change needed—just reinforcing.
2. **Index key plans.** Ensure docs/ or docs/agents/ has a single place that links ROADMAP, CRUCIAL_IMPROVEMENTS, Known Truths, and BOARD_MEETING_PLAN so new contributors (or agents) can find them.
3. **Security exercises.** Keep security-exercises/ and artifacts under one convention (date, short name, artifacts in subfolder) so Red/Blue and Security can find proof of work.

**Handoff:** None. Red Team next.

---

## 10. Red Team

**Red Team:** We simulate attacks within scope; we don’t fix defenses—that’s Blue. We log to security-exercises/ and proof-of-work.

**Update:** For Board context: we’d scope an exercise (e.g. export flow, or in-app data exposure) and run Lead/Specialist/Technical Ninja as needed. Every action documented for Blue to check “did the alarm go off?”

**Recommendations:**
1. **Next exercise target.** When Export or a new sensitive flow ships, run a short Purple exercise: Red targets that flow (e.g. path traversal, share scope), Blue checks detection and remediates if not.
2. **No scope creep.** We only attack what’s agreed (this app, this codebase, no production user data) so we don’t create risk.
3. **Handoff to Blue.** After each action we document what Blue should check; Blue then answers “alarm?” and fixes gaps.

**Handoff:** Blue Team to respond to any open Red actions. Blue next.

---

## 11. Blue Team

**Blue Team:** We defend: after Red acts, we ask “did the alarm go off?” If not, we document the gap and propose or implement a fix.

**Update:** We use the same proof-of-work and security-exercises docs. In Purple mode we’re in the same flow as Red so we can fix and re-test quickly.

**Recommendations:**
1. **Define “alarm” per surface.** For export, trip data, and sharing: what should we log or enforce? Document so we can say “yes/no” clearly when Red runs an action.
2. **Remediation is auditable.** Every fix (new log, new check, FileProvider scope) should be in a file or config we can point to; SECURITY_NOTES when we add a control.
3. **Re-test after fix.** Where possible, Red re-runs the same attack to verify the fix; we close the loop in the same Board/Purple cycle.

**Handoff:** None. Human-in-the-Loop to close.

---

## 12. Human-in-the-Loop Manager

**Human-in-the-Loop:** I’m the team’s link to the user. I send emails via scripts/coordinator-email/ and read replies when the user responds. I don’t make product or technical decisions—I communicate what the team needs and relay replies.

**Summary of this Board Meeting:**
- **Alignment:** All roles confirmed they operate from Known Truths and SSOT for persistence, recovery, calendar, and settings. No contradictions raised.
- **Recommendations captured:** Design (roadmap/briefs + HITL for user decisions); UI/UX (accessibility pass, stats clarity, Help placement); Front-end (single source for calendar/stats, export handoff to Back-end); Back-end (no new persistence paths, OfflineDataManager when implemented); DevOps (CI tests, two-way email UX); QA (test SSOT, test plans per feature, email script smoke); Security (export review, .env/last_reply, lost-device note); Email Editor (subject templates, value prop, tone); File Organizer (board-meeting home, index key plans, security-exercises convention); Red (next exercise when Export ships, handoff to Blue); Blue (define alarm per surface, auditable remediation, re-test).
- **Handoffs:** No blockers. DevOps and I continue on two-way email program; Security will review when Export is in scope; Red/Blue will run Purple when we have a target.

**What the user should be told:**
1. Board Meeting ran successfully; full team weighed in with SSOT-aligned recommendations.
2. No urgent blockers. Optional: ask user if they want a weekly Board summary by email (I can send a short digest).
3. When Export or a big feature lands, we’ll run Security review and optionally a Purple exercise; user will be emailed when that’s done.

**Optional email draft (for sending via scripts/coordinator-email/):**

**Subject:** OutOfRouteBuddy: Board Meeting complete — team sync and recommendations

**Body:**

Hi,

We ran a full Board Meeting today: every role (Design, UI/UX, Front-end, Back-end, DevOps, QA, Security, Email Editor, File Organizer, Red, Blue, and me) gave a short update and recommendations aligned with our Known Truths and single source of truth. No contradictions or blockers.

Summary: we’re aligned on persistence, recovery, calendar, and stats. Recommendations are logged in the transcript (docs/agents/data-sets/board-meeting/board-meeting-2025-03-15.md). When we add Export or another sensitive feature, Security will review and we can run a short Purple exercise.

If you’d like a weekly Board digest by email, reply and we’ll set it up. Otherwise we’ll email you after big changes or when we need a decision.

— Your team

---

*End of Board Meeting — 2025-03-15*
