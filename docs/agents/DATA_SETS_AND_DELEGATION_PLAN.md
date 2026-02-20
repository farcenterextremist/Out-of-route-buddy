# Comprehensive plan: employee data sets and delegation

**Coordinator:** This document expands each role’s **data sets** (what they read and produce) and refines **which work is delegated to whom**.

---

## Part 1 — All employee roles (summary)

| # | Role | One-line role |
|---|------|----------------|
| 1 | **Project Design / Creative Manager** | Owns product vision, roadmap, feature prioritization, and creative direction (no code). |
| 2 | **UI/UX Specialist** | Owns screens, flows, accessibility, design system, and UI copy (no implementation). |
| 3 | **Front-end Engineer** | Implements Android UI: layouts, fragments, views, resources, ViewModels wiring (no back-end logic). |
| 4 | **Back-end Engineer** | Owns data layer, services, repositories, business logic, persistence (no UI layout). |
| 5 | **DevOps Engineer** | Owns build, CI/CD, Gradle, scripts, deployment, environments (no feature code). |
| 6 | **QA Engineer** | Owns test strategy, test cases, automation, regression (no production feature code). |
| 7 | **Security Specialist** | Owns security review, threat model, secrets, compliance (recommendations only). |
| 8 | **Email Editor / Market Guru** | Owns email copy, marketing messaging, positioning, subject lines (no sending logic). |
| 9 | **File Organizer** | Owns repo/doc structure, file naming, reorg proposals (no content authoring). |
| 10 | **Human-in-the-Loop Manager** | Owns user communication: sends/reads email, summaries, decision requests (no product decisions). |

---

## Part 2 — Expand each role’s data sets

**Data set** = the docs, code paths, templates, and artifacts the role **consumes** and **produces** so they can work consistently.

### 2.1 Project Design / Creative Manager

**Current:** Agent card only; references `app/`, `docs/` at a high level.

**Expand with:**
- [x] **Consumes:** `docs/agents/WORKER_TODOS_AND_IDEAS.md`, `docs/agents/team-parameters.md` (workdays, user preferences), `phone-emulator/EMULATOR_PERFECTION_PLAN.md` (for feature context), any `*_PLAN.md` or `ROADMAP*.md`.
- [x] **Produces:** `docs/product/ROADMAP.md` (or similar), `docs/product/FEATURE_BRIEF_<name>.md` for major features (e.g. Auto drive), prioritization notes the coordinator can use.
- [x] **Data-set file:** `docs/agents/data-sets/design-creative.md` — list of input paths and output paths/templates.

**Refine delegation to this role:** “What should we build next?” “Prioritize these three features.” “Write a one-page brief for [feature].” “Align roadmap with workdays.”

---

### 2.2 UI/UX Specialist

**Current:** Agent card; references `app/src/main/res/`, layouts, strings.

**Expand with:**
- [ ] **Consumes:** `app/src/main/res/layout/*.xml`, `res/values/strings.xml`, `phone-emulator/` (as visual spec), `docs/product/FEATURE_BRIEF_*.md` when available, accessibility guidelines link or short doc.
- [ ] **Produces:** `docs/ux/SCREENS_AND_FLOWS.md` (or per-feature), `docs/ux/ACCESSIBILITY_CHECKLIST.md`, wireframe notes or copy suggestions (can be in markdown).
- [ ] **Data-set file:** `docs/agents/data-sets/ui-ux.md` — layout/string paths, emulator path, output doc paths.

**Refine delegation:** “Design the flow for [feature].” “Improve accessibility for statistics section.” “Propose where the Auto drive button lives.” “Write UI copy for [screen].”

---

### 2.3 Front-end Engineer

**Current:** Agent card; references `app/.../presentation/`, `res/`.

**Expand with:**
- [ ] **Consumes:** `app/src/main/res/`, `app/src/main/java/.../presentation/`, `phone-emulator/` (as reference), `docs/ux/` when UI/UX has produced specs, `EMULATOR_PERFECTION_PLAN.md` for parity list.
- [ ] **Produces:** Kotlin/XML in `app/` (layouts, fragments, activities, resources); no new docs unless technical notes in code or a short `docs/technical/` note.
- [ ] **Data-set file:** `docs/agents/data-sets/frontend.md` — code paths (layout, presentation, drawable, values), emulator path, dependency on Back-end contracts.

**Refine delegation:** “Implement [screen/component] from UI/UX spec.” “Match emulator behavior in the app.” “Add the Auto drive button/indicator.” “Fix [UI bug].”

---

### 2.4 Back-end Engineer

**Current:** Agent card; references `data/`, `domain/`, `services/`.

**Expand with:**
- [ ] **Consumes:** `app/src/main/java/.../data/`, `domain/`, `services/`, `docs/product/FEATURE_BRIEF_*.md` for behavior (e.g. Auto drive detection rules), `docs/agents/team-parameters.md` if business rules come from user.
- [ ] **Produces:** Kotlin in `data/`, `domain/`, `services/`; repository interfaces and implementations; optional `docs/technical/DATA_MODEL.md` or API notes.
- [ ] **Data-set file:** `docs/agents/data-sets/backend.md` — code paths, dependency on Design/UX for “what” to build.

**Refine delegation:** “Implement detection logic for Auto drive.” “Persist trip state across app kill.” “Add smart default for bounce miles.” “Expose [X] to the UI layer.”

---

### 2.5 DevOps Engineer

**Current:** Agent card; references root Gradle, `.github/`, `scripts/`.

**Expand with:**
- [ ] **Consumes:** `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`, `app/build.gradle.kts`, `.github/`, `scripts/`, `docs/DEPLOYMENT.md`, `docs/ONEDRIVE_SETUP.md` (for “where project lives”).
- [ ] **Produces:** Gradle/config changes, pipeline YAML, scripts, deployment runbooks; updates to `docs/DEPLOYMENT.md` or a small `docs/ops/` set.
- [ ] **Data-set file:** `docs/agents/data-sets/devops.md` — config paths, script paths, doc paths.

**Refine delegation:** “Fix the build.” “Add a CI step for [X].” “Document how to run tests from command line.” “Exclude build/.gradle from OneDrive if possible.”

---

### 2.6 QA Engineer

**Current:** Agent card; references `app/src/test/`, `app/src/androidTest/`.

**Expand with:**
- [x] **Consumes:** `app/src/test/`, `app/src/androidTest/`, `docs/agents/WORKER_TODOS_AND_IDEAS.md` (test-related todos), feature briefs for test scenarios, `scripts/coordinator-email/` for smoke-testing read/send.
- [x] **Produces:** New/updated tests, `docs/qa/TEST_PLAN_<feature>.md` or a master `TEST_STRATEGY.md`, bug reproduction steps.
- [x] **Data-set file:** `docs/agents/data-sets/qa.md` — test paths, how to run tests, dependency on feature briefs for scenarios.

**Refine delegation:** “Add tests for [feature].” “Fix or document the failing test in [file].” “Smoke-test the email scripts.” “Write a test plan for Auto drive.”

---

### 2.7 Security Specialist

**Current:** Agent card; references manifest, services, data, config.

**Expand with:**
- [ ] **Consumes:** `AndroidManifest.xml`, `app/src/main/java/.../services/`, `data/`, `scripts/coordinator-email/.env.example` (no .env), any doc that describes PII or location handling.
- [ ] **Produces:** `docs/security/REVIEW_<feature>.md` or `THREAT_NOTES.md`, hardening recommendations (in docs or as comments); no implementation.
- [ ] **Data-set file:** `docs/agents/data-sets/security.md` — manifest path, service paths, credential-handling rules.

**Refine delegation:** “Review Auto drive for privacy.” “Confirm .env and last_reply are never committed.” “Short threat note for lost-device scenario.”

---

### 2.8 Email Editor / Market Guru

**Current:** Agent card; references email script folder.

**Expand with:**
- [ ] **Consumes:** `scripts/coordinator-email/README.md`, `docs/agents/OPEN_LINE_OF_COMMUNICATION.md`, `docs/agents/team-parameters.md` (tone/context), past email bodies in `scripts/coordinator-email/*.txt` as examples.
- [ ] **Produces:** Email body/subject drafts (in `scripts/coordinator-email/` or `docs/comms/`), `docs/comms/SUBJECT_LINE_TEMPLATES.md`, one-sentence value prop for the app.
- [ ] **Data-set file:** `docs/agents/data-sets/email-editor.md` — comms doc paths, template paths.

**Refine delegation:** “Draft the next status email to the user.” “Write 2–3 subject-line templates for decision vs update vs confirm.” “One-sentence value prop for OutOfRouteBuddy.”

---

### 2.9 File Organizer

**Current:** Agent card; references `docs/`, `scripts/`, `app/` at a high level.

**Expand with:**
- [ ] **Consumes:** Full repo tree (no deep code), `docs/`, `scripts/`, `phone-emulator/`, naming patterns in existing files.
- [ ] **Produces:** `docs/agents/FILE_STRUCTURE.md` or similar, reorg proposals (in a doc), naming-convention notes; no moving files without coordinator/user approval.
- [ ] **Data-set file:** `docs/agents/data-sets/file-organizer.md` — top-level folders, doc index location, rules for proposing moves.

**Refine delegation:** “Propose a docs/ structure (e.g. product vs agents vs ops).” “Add a docs index and link key plans.” “Decide where WORKER_TODOS and future plans live.”

---

### 2.10 Human-in-the-Loop Manager

**Current:** Agent card; references email script, last_reply.txt, team-parameters.

**Expand with:**
- [ ] **Consumes:** `scripts/coordinator-email/send_email.py`, `read_replies.py`, `last_reply.txt`, `docs/agents/OPEN_LINE_OF_COMMUNICATION.md`, `docs/agents/team-parameters.md`, `docs/comms/SUBJECT_LINE_TEMPLATES.md` (when Email Editor has created it).
- [ ] **Produces:** Sent emails (via script), optional `docs/agents/REPLY_LOG.md` or one-line “last reply summarized” in team-parameters; no product decisions.
- [ ] **Data-set file:** `docs/agents/data-sets/human-in-the-loop.md` — script paths, when to send vs read, how to update team-parameters from replies.

**Refine delegation:** “Email the user that [X] is done and ask [Y].” “Run read_replies and update team-parameters from their reply.” “Send weekly one-liner.” “Reply with a number: 1=… 2=… 3=….”

---

## Part 3 — Delegation matrix (who gets what)

| Request / work type | Primary role(s) | Secondary / handoff |
|---------------------|-----------------|----------------------|
| “What should we build? Prioritize.” | Design/Creative | → Human-in-the-Loop if user must decide |
| “Design the flow for [feature].” | UI/UX | Design if scope unclear |
| “Implement [screen] from spec.” | Front-end | Back-end if data/API needed |
| “Implement detection / persistence / service.” | Back-end | Front-end for UI contract |
| “Fix build / add CI step / deployment.” | DevOps | QA for test pipeline |
| “Add tests for [feature].” | QA | Front-end/Back-end for implementation bugs |
| “Review [feature] for security/privacy.” | Security | Back-end/DevOps for implementation |
| “Draft email to user / subject templates.” | Email Editor | Human-in-the-Loop to send |
| “Reorg docs / propose structure.” | File Organizer | Coordinator or user to approve |
| “Email user / read reply / update params.” | Human-in-the-Loop | Email Editor for copy |
| “New feature end-to-end.” | Design → UI/UX → Front-end + Back-end → QA | Security if data-sensitive; Human-in-the-Loop for user approval |
| “User said they replied.” | Human-in-the-Loop (read_replies, last_reply) | Coordinator to act on content |
| “Emulator vs app parity.” | UI/UX (spec) + Front-end (implement) | EMULATOR_PERFECTION_PLAN as data set |

---

## Part 4 — Implementation checklist

- [ ] Create `docs/agents/data-sets/` and one `*.md` per role (design-creative.md, ui-ux.md, frontend.md, backend.md, devops.md, qa.md, security.md, email-editor.md, file-organizer.md, human-in-the-loop.md) with “Consumes” and “Produces” and paths.
- [ ] Create optional folders: `docs/product/` (briefs, roadmap), `docs/ux/` (flows, a11y), `docs/technical/` (data model, API), `docs/qa/`, `docs/security/`, `docs/comms/`, `docs/ops/` as needed when roles produce.
- [ ] Update each role’s agent card in `docs/agents/roles/` to reference its data-set file and the delegation matrix (or link to this plan).
- [ ] Add “Delegation” section to coordinator instructions: “Use DATA_SETS_AND_DELEGATION_PLAN.md for who gets what and what data each role uses.”

---

## Part 5 — Quick reference for the coordinator

- **Data sets:** See `docs/agents/data-sets/<role>.md` (after creation) and Part 2 above.
- **Who does what:** See Part 3 delegation matrix.
- **Expanding a role:** Add paths and artifacts to their data-set file and, if needed, to their agent card.
- **New work type:** Add a row to the delegation matrix and assign primary/secondary roles.

— Master Branch Coordinator
