# 1-hour team brainstorm: App improvement & polish (25-point list)

**Date:** 2025-02-19  
**Purpose:** Generate a 25-point todo list for improving and polishing the OutOfRouteBuddy app, with each item assigned to an agent.  
**Next step:** Send meeting summary to user via email → wait for response → execute workday.

---

## Meeting summary (1-hour brainstorm)

**Coordinator:** We're here to align on one list of 25 improvement/polish items, each owned by one of you. Focus on the **Android app** (not the emulator for this list): stability, UX, tests, docs, security, and foundation. Go around the room.

---

**Project Design / Creative Manager (DD):**  
- Prioritize "reduce friction" and "trustworthy tracking" from the roadmap: any polish that makes start/end trip clearer or stats more trustworthy belongs on the list.  
- Add one item: **Update ROADMAP with "next 3" and a short sprint note** so the team knows what we're batching.  
- Add: **One FEATURE_BRIEF for the next feature** (e.g. Auto drive or Reports) so BE/FE/QA have a single source of truth.

**UI/UX Specialist (UX):**  
- Polish: **Trip screen touch targets** — ensure all buttons meet minimum 48dp and are easy to hit.  
- **Statistics section**: labels vs values hierarchy (secondary vs primary text) and contrast; one task to document or fix.  
- **End Trip flow**: confirm copy and button order (End Trip / Clear / Continue) match user expectations; document in a short UX note.  
- **Accessibility**: one pass for TalkBack labels and focus order on Trip and History.

**Front-end Engineer (FE):**  
- **Trip history → trip details**: if a details screen exists, wire navigation from history; if not, add a stub or backlog item.  
- **Start/End Trip button state**: ensure disabled/loading state is visible (e.g. progress or disabled style).  
- **Strings and resources**: remove any hardcoded strings that should be in `strings.xml`; one audit task.  
- **Theme/dark mode**: verify all Trip and History screens respect dark theme (no white flashes).

**Back-end Engineer (BE):**  
- **Offline persistence**: implement or document `OfflineDataManager` load/save so trips survive app restart.  
- **Location jump detection**: define and implement (or document) jump detection in `TripStateManager`.  
- **Trip state persistence**: ensure active trip survives process death; verify and document.

**DevOps Engineer (DO):**  
- **Gradle 9**: run with `--warning-mode all`, document deprecations in `GRADLE_9_MIGRATION_NOTES.md`.  
- **DEPLOYMENT.md**: align with actual minSdk 24 and JDK 17.  
- **Health check**: add a one-command or one-script check: build + unit tests (optional: .env present for email).

**QA Engineer (QA):**  
- **Failing/ignored tests**: fix or document TripInputViewModelIntegrationTest, TripHistoryByDateViewModelTest, LocationValidationServiceTest, ThemeScreenshotTest; update TEST_STRATEGY or test plan.  
- **Smoke test for coordinator email**: mock or dry-run for send_email and read_replies so we don't break the open line.  
- **Regression checklist**: one short checklist (Trip start/end, History, Statistics) for manual or automated smoke.

**Security Specialist (SEC):**  
- **Secrets**: confirm `google-services.json` and coordinator `.env` / `last_reply.txt` policy; document in SECURITY_NOTES.  
- **Location/PII**: short note on where location/trip data is stored and transmitted; recommend hardening if needed.

**Email Editor / Market Guru (EE):**  
- **Subject-line templates**: 2–3 templates for "need your decision" / "here's an update" / "please confirm" for HITL.  
- **One-sentence value prop** for OutOfRouteBuddy (for store or outreach).  
- **Sign-off**: recommend consistent sign-off for coordinator emails.

**File Organizer (FO):**  
- **Cross-links**: ensure CRUCIAL_IMPROVEMENTS_TODO and this 25-point list are linked from docs index and coordinator instructions.  
- **Single source of truth**: one task to add "Current improvement list" pointer in README or team-parameters.

**Human-in-the-Loop Manager (HITL):**  
- **Send this meeting summary and the 25-point list to the user**; ask for approval or changes.  
- **After user response**: remind coordinator to execute the workday (run through the list and mark done).

---

**Coordinator:** We'll distill this into exactly **25 tasks**, one owner per task. Execution will start **after** the user replies to the summary email.

---

## 25-point todo list (by agent)

| # | Role | Task | File(s) / artifact |
|---|------|------|--------------------|
| 1 | DD | Update ROADMAP with "next 3" features and a short sprint note (e.g. biweekly batch). | `docs/product/ROADMAP.md` |
| 2 | DD | Create one FEATURE_BRIEF for the next feature (Auto drive or Reports) with value and acceptance criteria. | `docs/product/FEATURE_BRIEF_*.md` |
| 3 | UX | Document or fix Trip screen touch targets (min 48dp) and one quick win. | `docs/ux/` or layout |
| 4 | UX | Statistics section: ensure label/value hierarchy and contrast; document or fix. | `docs/ux/` or styles |
| 5 | UX | End Trip flow: confirm copy and button order; document UX note. | `docs/ux/` or strings |
| 6 | UX | One accessibility pass: TalkBack labels and focus order (Trip + History). | layouts / contentDescription |
| 7 | FE | Trip history: wire navigation to trip details if screen exists; else add backlog note. | TripHistoryByDateDialog / nav |
| 8 | FE | Start/End Trip button: visible disabled or loading state. | Trip input fragment / layout |
| 9 | FE | Audit Trip/History for hardcoded strings; move to strings.xml. | app strings.xml, Kotlin |
| 10 | FE | Verify Trip and History screens respect dark theme (no white flashes). | themes, layouts |
| 11 | BE | OfflineDataManager: implement or document load/save for offline trips. | OfflineDataManager.kt, optional brief |
| 12 | BE | TripStateManager: define and implement or document location jump detection. | TripStateManager.kt |
| 13 | BE | Verify and document trip state persistence across process death. | docs/technical/ or code |
| 14 | DO | Gradle: run --warning-mode all; document deprecations in GRADLE_9_MIGRATION_NOTES. | GRADLE_9_MIGRATION_NOTES.md |
| 15 | DO | Update DEPLOYMENT.md to match minSdk 24 and JDK 17. | docs/DEPLOYMENT.md |
| 16 | DO | Add one health script/target: build + unit tests (optional .env check). | scripts/ or Gradle |
| 17 | QA | Fix or document failing/ignored tests; update TEST_STRATEGY or test plan. | docs/qa/, test files |
| 18 | QA | Smoke test for coordinator email (send/read dry-run or mock). | docs/qa/ or script |
| 19 | QA | Add regression checklist: Trip start/end, History, Statistics. | docs/qa/ |
| 20 | SEC | Document secrets policy (google-services.json, .env, last_reply.txt) in SECURITY_NOTES. | docs/security/SECURITY_NOTES.md |
| 21 | SEC | Short note: where location/trip data is stored and transmitted; hardening recommendations. | SECURITY_NOTES or equivalent |
| 22 | EE | Draft 2–3 subject-line templates for HITL (decision / update / confirm). | docs/comms/ or agents |
| 23 | EE | One-sentence value prop for OutOfRouteBuddy; recommend email sign-off. | docs/ or team-parameters |
| 24 | FO | Link CRUCIAL_IMPROVEMENTS_TODO and this 25-point list from docs index and coordinator instructions. | docs/README.md, coordinator-instructions |
| 25 | FO | Add "Current improvement list" pointer in README or team-parameters. | README or team-parameters.md |

---

## Execution (after user response)

When the user replies approving (or with changes), the coordinator will run the **workday**: assign each of the 25 tasks to the appropriate role and execute in dependency order. DD and FO items can run first; then UX/FE/BE/DO/QA/SEC/EE in parallel where possible. HITL will send the summary email and, after reply, remind coordinator to execute.

**Workday executed (2025-02-19):** DD (1–2), FO (24–25), UX (3–6), FE (7–10 docs/backlog), BE (11–13 docs), DO (14–16), QA (17–19), SEC/EE (20–23 already in SECURITY_NOTES and SUBJECT_LINE_TEMPLATES). Artifacts: ROADMAP sprint note, FEATURE_BRIEF_reports, docs/ux/*, docs/technical/*, docs/qa/*, coordinator-instructions, team-parameters, DEPLOYMENT.md, GRADLE_9_MIGRATION_NOTES.md, scripts/health_check.ps1, TripHistoryByDateDialog backlog note.

---

*Reference: ROADMAP, CRUCIAL_IMPROVEMENTS_TODO, WORKER_TODOS_AND_IDEAS, team-structure.*

*Archived: completed one-off brainstorm; current improvement list is CRUCIAL_IMPROVEMENTS_TODO and ROADMAP.*
