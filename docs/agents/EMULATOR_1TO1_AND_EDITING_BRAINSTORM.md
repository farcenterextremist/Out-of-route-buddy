# Emulator brainstorm: 1:1 with the app + smooth editing (undo, save, real-time to Cursor)

**Goal of the meeting:** Let the agents figure out how to (1) make the emulator an exact 1:1 to the actual app, and (2) make sure edits are sent back to Cursor in real time with a smooth editing process (undo, save, make it make sense).

**Attendees (roles):** Project Design, UI/UX, Front-end, Back-end, DevOps, QA, Security, Email Editor, File Organizer, Human-in-the-Loop.

---

## Part 1: Agent brainstorm — 1:1 fidelity

### Project Design / Creative Manager

- **Single source of truth:** Decide whether the emulator is the design source (design in emulator → sync to app) or the app is (app resources → feed emulator). Recommendation: emulator as design source for **copy and labels**; app **layout/dimens/colors** can be the visual spec the emulator matches.
- **Scope of “1:1”:** 1:1 should mean: (a) every screen the app has that we care to design is represented in the emulator; (b) every visible string/label that exists in the app exists in the emulator and is editable; (c) look and feel (spacing, colors, typography, icons) match the app’s resources. Document which screens are “in scope” (e.g. Trip screen first, then Settings, Trip History later).
- **Asset pipeline:** If the app has `values/colors.xml`, `dimens.xml`, and drawables, the emulator could load or generate CSS/vars from them (script or build step) so one change in the app updates the emulator’s look. That keeps 1:1 without hand-duplicating.

### UI/UX Specialist

- **Pixel/dp alignment:** Pull exact values from the app: action bar 56dp, toolbar margins 16dp/8dp, input elevation 2dp, card padding 10dp. Convert dp to px (e.g. 1dp ≈ 1px at 360px width) and use CSS variables so one place controls spacing.
- **Typography:** Match `textAppearance` / font size and weight from the app (e.g. title 24sp bold, body 16sp, secondary 14sp). If the app uses a custom font, the emulator can use the same or a web-safe equivalent and document the mapping.
- **Icons:** Already using SVG; ensure each SVG matches the app’s `ic_*.xml` (gear, chevron, pause, play). Option: script that converts VectorDrawable to SVG or hand-maintain a small set. Same icon set = true 1:1.
- **States:** Match app states (idle / trip active / paused, stats collapsed/expanded, light/dark). Document a small state diagram so behavior is 1:1, not just pixels.

### Front-end Engineer

- **Layout parity:** The app may use ConstraintLayout/LinearLayout; the emulator uses flexbox/CSS. Create a mapping doc: “app element X → emulator selector Y” and list any remaining gaps (e.g. a view the app has that the emulator doesn’t).
- **Data-driven render:** Keep design state as the single source; render from it. If we add “load from app” later, it would populate the same design state so the rest of the pipeline is unchanged.
- **Responsiveness:** App has one layout at 360dp width; emulator phone frame is 360px. Lock the inner viewport to that or scale proportionally so 1:1 is unambiguous.

### Back-end Engineer

- **Strings sync direction:** Edits flow emulator → sync service → `strings.xml`. For true 1:1 we need the emulator’s default design to reflect current `strings.xml` when the project has one. Option: on first load (or “Load from project” button), GET the current strings from the sync service (new endpoint) and merge into design state so the emulator shows exactly what the app has.
- **State persistence:** Design state already in localStorage; add optional “last synced at” and “pending changes” flag so the user knows if the project is ahead or behind the emulator.

### DevOps Engineer

- **CI snapshot:** Optional job that builds the app and dumps strings/layout info (e.g. from resources) and compares to emulator default design or to a golden JSON. That would catch drift between app and emulator.
- **Version alignment:** Emulator could show “Matches app as of [git rev or date]” if we add an endpoint or file that the build writes (e.g. `app/build/emulator-baseline.json`).

### QA Engineer

- **1:1 checklist:** A test checklist: for each string in `strings.xml`, there is an editable element in the emulator; for each key screen state (idle, trip active, stats expanded), the emulator matches the app’s layout and labels. Run manually or semi-automate (e.g. compare string keys to design state keys).
- **Visual regression:** Optional: screenshot the app on an emulator and the web emulator and diff (or compare key regions). Heavy for a first pass; a simple “all string keys present” check is a good start.

### Security Specialist

- **No secrets in design state:** Design state and sync payload must stay free of credentials or API keys. Keep “do not store secrets in emulator/sync” in the checklist.
- **Sync service:** Already localhost-only; no change for 1:1.

### File Organizer

- **One 1:1 spec:** A single doc (e.g. `phone-emulator/1TO1_SPEC.md`) that links EMULATOR_PERFECTION_PLAN, the app’s layout/values references, and the “load from project” / “sync to project” flow. So 1:1 is defined and findable in one place.

### Human-in-the-Loop

- **Stakeholder check:** After 1:1 improvements, send a short email: “Emulator now matches the app’s Trip screen (strings, spacing, icons). Please try side-by-side and confirm.”

---

## Part 2: Agent brainstorm — Edits to Cursor, real-time, smooth editing (undo, save, make it make sense)

### Project Design / Creative Manager

- **Mental model:** “Edit in the emulator → Save (or auto-sync) → project files update → Cursor shows the change.” One clear sentence for users. Avoid “sometimes you click Sync, sometimes it auto-syncs” without explanation; make the rule explicit (e.g. “Save = update in emulator; Sync to project = write to repo; turn on Auto-sync to do both on Save.”).
- **Undo:** Design expects “Undo” to mean “revert the last edit (or last N edits) in the emulator” and optionally “revert the last sync” (harder). Start with “Undo in the emulator” so the editing process is smooth; “revert sync” can be a later phase.

### UI/UX Specialist

- **Save vs Sync vs Auto-sync:**
  - **Save** = persist in the emulator (localStorage + re-render). Always visible (e.g. “Save” in the property panel). After Save, user can choose to sync or rely on auto-sync.
  - **Sync to project** = send current design to the sync service → write to `strings.xml`. Clear label: “Sync to project” or “Push to Cursor.” Show feedback: “Synced N strings” or “No changes” or “Sync failed.”
  - **Auto-sync:** Optional checkbox “After each Save, push to project” so Cursor updates in real time. Default can be off so users don’t accidentally overwrite; power users turn it on.
- **Undo button:** Place in the emulator toolbar (outside the phone frame). One button: “Undo.” Tooltip: “Revert last edit.” Disabled when nothing to undo. Optional: “Redo” next to it.
- **Consistency:** One place to “confirm” an edit (Save in the panel). No hidden or duplicate save actions. If we add inline edit (double-click), on blur/Enter we still go through the same “update design + saveDesign” path so Undo and Sync see one flow.

### Front-end Engineer

- **Undo stack:** Keep a history stack (e.g. last 20 design states). On each `updateDesign` or “Save” in the panel, push a copy of `designState` (or a diff) onto the stack before applying the change. “Undo” pops the stack and restores that state, then `saveDesign()` and `render()`. Redo = second stack (push current state when we undo, pop on Redo).
- **Save button semantics:** “Save” in the property panel = update design state + saveDesign() + render(). It does not necessarily sync; Sync is separate (manual button or auto-sync if enabled). So: Save = commit in emulator; Sync = commit to disk. Clear and implementable.
- **Real-time to Cursor:** Already have: (1) Sync to project (manual), (2) Auto-sync after Save (optional). To make it “real time,” ensure: (a) sync service writes the file immediately; (b) Cursor picks up file changes (it does when the file is open or on focus). Optional: after sync, show a small “File updated; Cursor will show the change” so the user knows it’s done.
- **Smoothness:** Debounce auto-sync (e.g. 500 ms after last Save) so rapid edits don’t spam the sync service. For manual Sync, one click and disable the button until the request completes (already there). No double-submit.

### Back-end Engineer

- **Sync API:** Keep POST /sync as the only write path. Optional: GET /strings or /design that returns current `strings.xml` as JSON so the emulator can “Load from project” and achieve 1:1 with the repo. That doesn’t change the “edits to Cursor” flow but supports “start from app state.”
- **Idempotency:** Multiple syncs with the same design = same file content. No need for “version” in the first iteration; just overwrite with the current design.

### DevOps Engineer

- **No change** to the editing flow; sync service and one-click launch are enough for “edits to Cursor” and real-time file updates.

### QA Engineer

- **Editing flow test:** (1) Open emulator, (2) Edit a field, Save, (3) Confirm Undo reverts it, (4) Edit again, Save, (5) Sync to project, (6) Check that `strings.xml` changed and Cursor (if open) shows the new value. Document as a smoke test.
- **Auto-sync test:** With Auto-sync on, Save → within a few seconds check file on disk (or sync service log) to confirm POST was sent and file updated.

### Security Specialist

- **No change** for undo/save/sync; same “no secrets” rule.

### Email Editor / Market Guru

- **In-app copy:** Short line in the emulator: “Save = update here. Sync to project = update Cursor. Use Undo to revert.” So the editing process “makes sense” at a glance.

### File Organizer

- **Doc the flow:** In `phone-emulator/README.md` or a short “Editing and sync” section: (1) Edit (right-click → Edit or double-click), (2) Save (in panel), (3) Undo (toolbar) to revert last edit, (4) Sync to project (or Auto-sync) to write to repo. Link to sync service README. So the “make it make sense” is written down and findable.

### Human-in-the-Loop

- **After shipping Undo + clear Save/Sync:** Email: “Emulator now has Undo and a clear Save vs Sync flow. Please try: edit → Save → Undo → edit again → Sync to project, and confirm it matches what you expect.”

---

## Part 3: Consolidated recommendations

### 1:1 with the app

| Priority | Recommendation | Owner |
|----------|----------------|-------|
| 1 | Create **`phone-emulator/1TO1_SPEC.md`** (or extend EMULATOR_PERFECTION_PLAN) that lists: (a) screens in scope, (b) source of spacing/colors (app values or emulator CSS), (c) string-key parity (every strings.xml key has an editable in the emulator). | File Organizer + UI/UX |
| 2 | **Align spacing and typography** with the app: 56dp toolbar, 16dp/8dp margins, 10dp card padding, font sizes. Use CSS variables so one place drives the look. | Front-end + UI/UX |
| 3 | **Icons:** Keep SVG; ensure they match app drawables (gear, chevron, pause, play). Add a short “icon source” note in the spec. | Front-end |
| 4 | **Optional “Load from project”:** Sync service endpoint GET /design or /strings that returns current strings as JSON; emulator merges into design state so the emulator “starts from the app.” | Back-end + Front-end |
| 5 | **QA checklist:** “Every string key in strings.xml has an editable in the emulator”; run when adding new app strings. | QA |

### Edits to Cursor, real-time, smooth editing

| Priority | Recommendation | Owner |
|----------|----------------|-------|
| 1 | **Undo (and optional Redo):** Add an undo stack (e.g. last 20 states). On Save/updateDesign, push previous state; “Undo” in toolbar restores and re-renders. Redo stack optional. | Front-end |
| 2 | **Clear labels:** Keep “Save” = commit in emulator; “Sync to project” = write to repo. Add one line in the UI: “Save = update here. Sync = update Cursor.” Optional: “Auto-sync: push to project after each Save.” | UI/UX + Front-end |
| 3 | **Single Save path:** All edits (panel Save, future inline edit) go through the same updateDesign + saveDesign path so Undo and Auto-sync see one flow. | Front-end |
| 4 | **Debounce Auto-sync:** If Auto-sync is on, debounce (e.g. 500 ms after last Save) before calling runSyncToProject so rapid edits don’t spam the service. | Front-end |
| 5 | **Document the flow:** In README or “Editing and sync”: Edit → Save → Undo (optional) → Sync to project (or rely on Auto-sync). Link to sync service. | File Organizer |
| 6 | **Smoke test:** Edit → Save → Undo → Edit → Save → Sync → verify file and Cursor. Add to QA checklist. | QA |

---

## Part 4: Suggested implementation order

**Phase A – Make editing make sense (undo, save, sync)**

1. Add **Undo** (and optionally **Redo**) in the emulator toolbar with a history stack.
2. Clarify **Save** vs **Sync** in the UI (one line of copy + optional debounced Auto-sync).
3. Document the flow in README and add a QA smoke test.

**Phase B – 1:1 fidelity**

1. Write **1TO1_SPEC.md** (or extend the perfection plan) with screens in scope, spacing/typography source, and string-key parity.
2. Align CSS (spacing, typography, colors) with app values/dimens.
3. Optionally add “Load from project” (GET from sync service) so the emulator can start from current app strings.

**Phase C – Polish**

1. QA checklist for string-key parity and editing flow.
2. Human-in-the-Loop email after each phase so the user can confirm.

---

## Part 5: Cross-references

- **Phase A/B/C execution:** **`docs/agents/EMULATOR_PHASE_ABC_100_TODOS.md`** — 103 agent tasks for Phase A (undo, save, sync), Phase B (1:1), Phase C (polish). Execute A → B → C.
- **1:1 perfection + editing tools:** **`docs/agents/EMULATOR_1TO1_AND_EDITING_100_PLAN.md`** — 102 agent tasks: Phase P (1:1 representation perfection), Phase E (editing tools functioning properly). Consult all agents, then execute P then E.
- **Existing plans:** `phone-emulator/EMULATOR_PERFECTION_PLAN.md`, `docs/agents/EMULATOR_EDITING_EXTENSIVE_AGENT_TODOS.md`, `docs/agents/EMULATOR_FIGMA_INSPIRED_CONSULTATION.md`.
- **Sync and launch:** `scripts/emulator-sync-service/README.md`, `docs/ONE_CLICK_EMULATOR_PLAN.md`.
- **This doc:** Use as the single brainstorm output for “1:1” and “smooth editing (undo, save, real-time to Cursor).” Assign tasks from Part 3 and 4 to the roles above and track in EMULATOR_PHASE_ABC_100_TODOS.md.
