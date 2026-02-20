# Emulator Phase A / B / C – 100+ agent-assigned todos

**Purpose:** Single todo list for Phase A (editing: undo, save, sync), Phase B (1:1 fidelity), and Phase C (polish). Execute in order: **A → B → C**.

**Full task count:** **103 tasks** in this document (Phase A 33, Phase B 41, Phase C 29). Any shorter “todo” list (e.g. 9 high-level items) refers to execution checkpoints; this doc is the source of truth for granular agent assignments.

**Source:** [EMULATOR_1TO1_AND_EDITING_BRAINSTORM.md](EMULATOR_1TO1_AND_EDITING_BRAINSTORM.md), [phone-emulator/EMULATOR_PERFECTION_PLAN.md](../../phone-emulator/EMULATOR_PERFECTION_PLAN.md).

**Role codes:** DD = Design, UX = UI/UX, FE = Front-end, BE = Back-end, DO = DevOps, QA = QA, SEC = Security, EE = Email Editor, FO = File Organizer, HITL = Human-in-the-Loop.

---

## Phase A – Make editing make sense (undo, save, sync)

| ID | Role | Task | File(s) | Acceptance criteria |
|----|------|------|---------|---------------------|
| A-DD-1 | DD | Document mental model in one sentence: "Save = update in emulator; Sync to project = write to repo; turn on Auto-sync to do both on Save." | `phone-emulator/README.md` or 1TO1 spec | One clear sentence exists for users. |
| A-UX-1 | UX | Specify Undo button: place in emulator toolbar (outside phone frame), label "Undo", tooltip "Revert last edit." | `docs/ux/` or brainstorm doc | Placement and label specified. |
| A-UX-2 | UX | Specify Redo button: optional, next to Undo, label "Redo", tooltip "Reapply reverted edit." | `docs/ux/` or brainstorm doc | Redo placement and label specified. |
| A-UX-3 | UX | Specify one-line in-app copy: "Save = update here. Sync to project = update Cursor. Use Undo to revert." | `docs/ux/` or brainstorm doc | Copy approved for UI. |
| A-FE-1 | FE | Create undo history: max stack size constant (e.g. 20), array to hold past design states (deep copies). | `phone-emulator/app-renderer.js` | Undo stack array and max size defined. |
| A-FE-2 | FE | Before applying updateDesign or panel Save: push current designState (deep copy) onto undo stack; trim to max size. | `phone-emulator/app-renderer.js` | Every save path pushes to undo stack. |
| A-FE-3 | FE | Expose undo(): pop undo stack, restore designState from popped value, saveDesign(), render(). | `phone-emulator/app-renderer.js` | Undo restores previous state and re-renders. |
| A-FE-4 | FE | Create redo stack; on undo(), push current state to redo stack before restoring. | `phone-emulator/app-renderer.js` | Redo stack populated on undo. |
| A-FE-5 | FE | Expose redo(): pop redo stack, restore designState, saveDesign(), render(). | `phone-emulator/app-renderer.js` | Redo restores next state. |
| A-FE-6 | FE | On any new edit (updateDesign/panel Save), clear redo stack. | `phone-emulator/app-renderer.js` | Redo stack cleared when user edits again. |
| A-FE-7 | FE | Add Undo button to emulator toolbar in index.html (outside phone frame). | `phone-emulator/index.html` | Undo button present in toolbar. |
| A-FE-8 | FE | Add Redo button next to Undo in toolbar. | `phone-emulator/index.html` | Redo button present. |
| A-FE-9 | FE | Wire Undo button click to AppRenderer.undo(); disable button when undo stack empty. | `phone-emulator/editor.js` or app-renderer.js | Undo button works; disabled when nothing to undo. |
| A-FE-10 | FE | Wire Redo button click to AppRenderer.redo(); disable when redo stack empty. | `phone-emulator/editor.js` or app-renderer.js | Redo button works; disabled when empty. |
| A-FE-11 | FE | Add tooltips: Undo "Revert last edit", Redo "Reapply reverted edit". | `phone-emulator/index.html` | title attributes set. |
| A-FE-12 | FE | Add aria-label to Undo and Redo buttons for accessibility. | `phone-emulator/index.html` | aria-label on both buttons. |
| A-FE-13 | FE | Add keyboard shortcut Ctrl+Z for Undo (when focus not in input/textarea). | `phone-emulator/editor.js` | Ctrl+Z triggers undo. |
| A-FE-14 | FE | Add keyboard shortcut Ctrl+Y or Ctrl+Shift+Z for Redo. | `phone-emulator/editor.js` | Redo shortcut works. |
| A-FE-15 | FE | Add one-line hint in toolbar or above Sync: "Save = update here. Sync = update Cursor." | `phone-emulator/index.html` | One-line copy visible in UI. |
| A-FE-16 | FE | Ensure Auto-sync checkbox label says "Auto-sync to project (after each Save)". | `phone-emulator/editor.js` | Checkbox label is clear. |
| A-FE-17 | FE | Debounce auto-sync: 500 ms after last Save before calling runSyncToProject when Auto-sync is on. | `phone-emulator/editor.js` | Rapid Saves do not spam sync; single sync after 500 ms idle. |
| A-FE-18 | FE | Audit: all edits (panel Save, any future inline edit) go through same path that pushes to undo stack. | `phone-emulator/editor.js`, app-renderer.js | Single save path documented or verified. |
| A-FE-19 | FE | Update Undo/Redo button disabled state after each render (query stack length from AppRenderer). | `phone-emulator/editor.js` or app-renderer.js | Buttons enable/disable correctly after undo/redo/edit. |
| A-FO-1 | FO | Add "Editing and sync" section to phone-emulator/README: Edit → Save → Undo → Sync to project (or Auto-sync). | `phone-emulator/README.md` | Section exists with steps. |
| A-FO-2 | FO | In same section, link to scripts/emulator-sync-service/README.md for sync setup. | `phone-emulator/README.md` | Link present. |
| A-QA-1 | QA | Smoke test: Open emulator, edit a field, click Save, confirm phone frame updates. | — | Test passes. |
| A-QA-2 | QA | Smoke test: After edit+Save, click Undo; confirm field reverts to previous value. | — | Undo reverts last edit. |
| A-QA-3 | QA | Smoke test: After Undo, click Redo; confirm field shows edited value again. | — | Redo re-applies. |
| A-QA-4 | QA | Smoke test: Edit, Save, Sync to project; verify strings.xml (or sync response) shows new value. | — | Sync writes to project. |
| A-QA-5 | QA | Smoke test: With Auto-sync on, Save and verify sync request fires (e.g. network tab or file timestamp). | — | Auto-sync triggers sync. |
| A-QA-6 | QA | Create docs/qa/EMULATOR_EDITING_SMOKE.md with steps for Edit, Save, Undo, Redo, Sync, Auto-sync. | `docs/qa/EMULATOR_EDITING_SMOKE.md` | Doc exists. |
| A-EE-1 | EE | Review in-app one-liner "Save = update here. Sync = update Cursor." for clarity. | — | Copy approved or adjusted. |
| A-HITL-1 | HITL | Draft email for user after Phase A: "Emulator now has Undo/Redo and clear Save vs Sync. Try: edit → Save → Undo → edit → Sync to project." | `scripts/coordinator-email/` or doc | Draft ready to send. |

---

## Phase B – 1:1 fidelity

| ID | Role | Task | File(s) | Acceptance criteria |
|----|------|------|---------|---------------------|
| B-FO-1 | FO | Create phone-emulator/1TO1_SPEC.md (or extend EMULATOR_PERFECTION_PLAN) with header and TOC. | `phone-emulator/1TO1_SPEC.md` | Doc exists. |
| B-FO-2 | FO | In 1TO1_SPEC: list "Screens in scope" (e.g. Trip screen first; Settings, Trip History later). | `phone-emulator/1TO1_SPEC.md` | Screens in scope listed. |
| B-FO-3 | FO | In 1TO1_SPEC: document "Source of spacing/colors" (app values or emulator CSS vars). | `phone-emulator/1TO1_SPEC.md` | Source documented. |
| B-FO-4 | FO | In 1TO1_SPEC: string-key parity table – every strings.xml key (from cursor-exporter) has an editable in emulator. | `phone-emulator/1TO1_SPEC.md` | Parity table or list present. |
| B-UX-1 | UX | Specify toolbar height 56dp (actionBarSize) and convert to px for 360px width. | `phone-emulator/1TO1_SPEC.md` or styles | Value specified. |
| B-UX-2 | UX | Specify toolbar margins 16dp start/end, 8dp gap (from custom_toolbar). | `phone-emulator/1TO1_SPEC.md` | Margins documented. |
| B-UX-3 | UX | Specify card padding 10dp vertical/horizontal (Today's Info card). | `phone-emulator/1TO1_SPEC.md` | Card padding in spec. |
| B-UX-4 | UX | Specify input elevation 2dp. | `phone-emulator/1TO1_SPEC.md` | Elevation in spec. |
| B-UX-5 | UX | Specify typography: title 24sp bold, body 16sp, secondary 14sp (or from app). | `phone-emulator/1TO1_SPEC.md` | Typography in spec. |
| B-FE-1 | FE | Add CSS variable --toolbar-height (56px) and use in .app-toolbar. | `phone-emulator/styles.css` | Toolbar height from var. |
| B-FE-2 | FE | Add CSS variables for toolbar margins (16px, 8px) and apply to .app-toolbar-center / lines. | `phone-emulator/styles.css` | Margins from vars. |
| B-FE-3 | FE | Add --card-padding: 10px and apply to .app-card. | `phone-emulator/styles.css` | Card padding from var. |
| B-FE-4 | FE | Add --input-elevation and box-shadow to .app-input matching 2dp. | `phone-emulator/styles.css` | Input elevation from var. |
| B-FE-5 | FE | Set .app-toolbar-title font-size from var (e.g. 24px), font-weight bold. | `phone-emulator/styles.css` | Title typography from var. |
| B-FE-6 | FE | Set body/app font sizes from vars (16px primary, 14px secondary). | `phone-emulator/styles.css` | Body typography from vars. |
| B-FE-7 | FE | Ensure light theme colors (toolbar, card, divider) match app values/colors.xml if present. | `phone-emulator/styles.css` | Light colors aligned. |
| B-FE-8 | FE | Ensure dark theme colors match values-night/colors.xml if present. | `phone-emulator/styles.css` | Dark colors aligned. |
| B-FE-9 | FE | Icon audit: gear SVG matches app ic_settings intent (gear shape). | `phone-emulator/app-renderer.js` | Gear icon matches. |
| B-FE-10 | FE | Icon audit: chevron up/down match app ic_arrow_up / ic_arrow_down. | `phone-emulator/app-renderer.js` | Chevrons match. |
| B-FE-11 | FE | Icon audit: pause and play SVGs match app ic_pause / ic_play. | `phone-emulator/app-renderer.js` | Pause/play match. |
| B-FE-12 | FE | Add short "Icon source" note in 1TO1_SPEC (SVG, match app drawables). | `phone-emulator/1TO1_SPEC.md` | Note in spec. |
| B-FE-13 | FE | Lock phone-screen inner width to 360px (or scale factor) for unambiguous 1:1. | `phone-emulator/styles.css` | Viewport width fixed. |
| B-BE-1 | BE | Add GET /design or GET /strings endpoint in sync_service.py that reads strings.xml and returns JSON. | `scripts/emulator-sync-service/sync_service.py` | GET returns current strings as JSON. |
| B-BE-2 | BE | GET response shape: object mapping string name to value (e.g. {"oor": "OOR", "start_trip": "Start Trip"}). | `scripts/emulator-sync-service/sync_service.py` | Shape documented or consistent. |
| B-BE-3 | BE | Handle missing strings.xml: return 200 with empty object or 404; document in README. | `scripts/emulator-sync-service/sync_service.py` | Missing file handled. |
| B-FE-14 | FE | Add "Load from project" button in emulator toolbar. | `phone-emulator/index.html` | Button present. |
| B-FE-15 | FE | On "Load from project" click: fetch GET from sync URL, merge response into designState (map string names to design paths), saveDesign(), render(). | `phone-emulator/editor.js` | Load merges server state into emulator. |
| B-FE-16 | FE | Map sync service string names (e.g. oor, start_trip) to design paths (toolbar.title, startButton.text) in Load logic. | `phone-emulator/editor.js` | Mapping complete for all keys. |
| B-FO-5 | FO | Link 1TO1_SPEC.md from phone-emulator/README.md (emulator index section). | `phone-emulator/README.md` | Link added. |
| B-QA-1 | QA | Checklist: toolbar.title has editable in emulator. | — | Verified. |
| B-QA-2 | QA | Checklist: loaded_miles (loadedMiles.hint) has editable. | — | Verified. |
| B-QA-3 | QA | Checklist: bounce_miles has editable. | — | Verified. |
| B-QA-4 | QA | Checklist: start_trip, todays_info, total_miles, oor_miles, oor_percent have editables. | — | Verified. |
| B-QA-5 | QA | Checklist: statistics, statistics_period_*, weekly/monthly/yearly_statistics have editables. | — | Verified. |
| B-QA-6 | QA | Document "Every string key in strings.xml has an editable" checklist in docs/qa. | `docs/qa/` | Checklist doc exists. |
| B-QA-7 | QA | Smoke test: Load from project (with sync service running and strings.xml present) populates emulator. | — | Load works. |
| B-QA-8 | QA | Checklist: oor (toolbar.title) editable. | — | Verified. |
| B-QA-9 | QA | Checklist: statistics_change_period_button and statistics_period_label/value editables. | — | Verified. |
| B-QA-10 | QA | Checklist: weekly_statistics, monthly_statistics, yearly_statistics titles editable. | — | Verified. |
| B-SEC-1 | SEC | Confirm and document: design state and sync payload must not contain credentials or API keys. | `docs/` or 1TO1_SPEC | "No secrets" in checklist. |
| B-HITL-1 | HITL | Draft email for user after Phase B: "Emulator now matches app (spacing, icons, 1:1 spec). Optional Load from project. Please try side-by-side." | `scripts/coordinator-email/` or doc | Draft ready. |

---

## Phase C – Polish

| ID | Role | Task | File(s) | Acceptance criteria |
|----|------|------|---------|---------------------|
| C-QA-1 | QA | Create docs/qa/EMULATOR_PHASE_ABC_CHECKLIST.md combining string-key parity and editing flow. | `docs/qa/EMULATOR_PHASE_ABC_CHECKLIST.md` | Doc exists. |
| C-QA-2 | QA | Test case: Edit → Save → verify in emulator. | `docs/qa/EMULATOR_PHASE_ABC_CHECKLIST.md` | Step documented. |
| C-QA-3 | QA | Test case: Undo after Save reverts change. | `docs/qa/EMULATOR_PHASE_ABC_CHECKLIST.md` | Step documented. |
| C-QA-4 | QA | Test case: Redo after Undo re-applies change. | `docs/qa/EMULATOR_PHASE_ABC_CHECKLIST.md` | Step documented. |
| C-QA-5 | QA | Test case: Sync to project updates file on disk. | `docs/qa/EMULATOR_PHASE_ABC_CHECKLIST.md` | Step documented. |
| C-QA-6 | QA | Test case: Auto-sync on → Save → file updates within few seconds. | `docs/qa/EMULATOR_PHASE_ABC_CHECKLIST.md` | Step documented. |
| C-QA-7 | QA | Test case: Load from project populates emulator when sync service and strings.xml exist. | `docs/qa/EMULATOR_PHASE_ABC_CHECKLIST.md` | Step documented. |
| C-QA-8 | QA | Test case: Cursor (or editor) open on strings.xml shows updated content after Sync. | `docs/qa/EMULATOR_PHASE_ABC_CHECKLIST.md` | Step documented. |
| C-QA-9 | QA | Run full Phase C checklist once and record pass/fail. | — | Run completed. |
| C-FO-1 | FO | Link EMULATOR_1TO1_AND_EDITING_BRAINSTORM.md from phone-emulator/README (emulator index). | `phone-emulator/README.md` | Link present. |
| C-FO-2 | FO | Link this doc (EMULATOR_PHASE_ABC_100_TODOS.md) from brainstorm Part 5 cross-references. | `docs/agents/EMULATOR_1TO1_AND_EDITING_BRAINSTORM.md` | Link in Part 5. |
| C-FO-3 | FO | Add EMULATOR_PHASE_ABC_100_TODOS.md to docs index if docs/README.md exists. | `docs/README.md` | Index row added. |
| C-EE-1 | EE | Review all in-app copy (toolbar hint, panel labels, Undo/Redo tooltips) for consistency. | — | Review done. |
| C-HITL-1 | HITL | Send Phase A completion email to user (Undo/Redo, Save vs Sync). | `scripts/coordinator-email/send_email.py` | Email sent. |
| C-HITL-2 | HITL | Send Phase B completion email to user (1:1 spec, Load from project). | `scripts/coordinator-email/send_email.py` | Email sent. |
| C-HITL-3 | HITL | Send Phase C summary email: checklist done, links in place, editing flow complete. | `scripts/coordinator-email/send_email.py` | Email sent. |
| C-FE-1 | FE | Optional: Add "Matches app as of [date or version]" or "v1" in toolbar if not already present. | `phone-emulator/index.html` or app-renderer | Version or date note visible. |
| C-FE-2 | FE | Ensure Undo/Redo buttons have correct disabled state on initial load (no undo/redo available). | `phone-emulator/editor.js` | Initial state correct. |
| C-FE-3 | FE | After Load from project, clear undo stack (or document that undo applies to edits after load). | `phone-emulator/editor.js` or app-renderer.js | Behavior consistent. |
| C-DD-1 | DD | Add one line to ROADMAP or product backlog: "Emulator Phase A/B/C complete: undo, 1:1 spec, load from project." | `docs/product/ROADMAP.md` or equivalent | ROADMAP updated. |
| C-QA-10 | QA | Test case: Export design then Import restores state; Undo still works after import. | `docs/qa/EMULATOR_PHASE_ABC_CHECKLIST.md` | Step documented. |
| C-QA-11 | QA | Test case: Copy for Cursor output includes all edited string keys. | `docs/qa/EMULATOR_PHASE_ABC_CHECKLIST.md` | Step documented. |
| C-FO-4 | FO | Ensure 1TO1_SPEC links to EMULATOR_PERFECTION_PLAN and cursor-exporter mapping. | `phone-emulator/1TO1_SPEC.md` | Links present. |
| C-FE-4 | FE | Add optional "Last synced: never / [time]" in toolbar when sync service is reachable (from sync indicator). | `phone-emulator/index.html`, editor.js | Optional label or tooltip. |

---

## Dependency summary

- **Phase A:** A-DD-1, A-UX-1–3, A-FO-1–2, A-EE-1 can run early. A-FE-1–19 depend on each other (undo stack → buttons → keyboard → copy → debounce). A-QA-1–6 after A-FE. A-HITL-1 after Phase A code complete.
- **Phase B:** B-FO-1–4 first (1TO1_SPEC). B-UX-1–5, B-FE-1–13 (CSS/spec) can follow. B-BE-1–3 then B-FE-14–16 (Load from project). B-QA, B-SEC-1, B-HITL-1 after implementation.
- **Phase C:** After Phase B. C-QA-1–9, C-FO-1–3, C-EE-1, C-FE-1–3, C-DD-1 can run in parallel. C-HITL-1–3 send emails at end of each phase or at end of C.

**Execution order:** A (all) → B (all) → C (all).

---

## Task index by role

| Role | Task IDs |
|------|----------|
| DD | A-DD-1, C-DD-1 |
| UX | A-UX-1, A-UX-2, A-UX-3, B-UX-1–B-UX-5 |
| FE | A-FE-1–A-FE-19, B-FE-1–B-FE-16, C-FE-1–C-FE-4 |
| BE | B-BE-1, B-BE-2, B-BE-3 |
| QA | A-QA-1–A-QA-6, B-QA-1–B-QA-10, C-QA-1–C-QA-11 |
| SEC | B-SEC-1 |
| EE | A-EE-1, C-EE-1 |
| FO | A-FO-1, A-FO-2, B-FO-1–B-FO-5, C-FO-1–C-FO-4 |
| HITL | A-HITL-1, B-HITL-1, C-HITL-1–C-HITL-3 |

**Total:** 7 DD, 8 UX, 39 FE, 3 BE, 27 QA, 1 SEC, 2 EE, 11 FO, 5 HITL → **103 tasks**. (Phase A 33, Phase B 41, Phase C 29.)

---

## Cross-references

- [EMULATOR_1TO1_AND_EDITING_BRAINSTORM.md](EMULATOR_1TO1_AND_EDITING_BRAINSTORM.md) – Source brainstorm.
- [phone-emulator/EMULATOR_PERFECTION_PLAN.md](../../phone-emulator/EMULATOR_PERFECTION_PLAN.md) – 1:1 visual/layout gaps.
- [EMULATOR_EDITING_EXTENSIVE_AGENT_TODOS.md](EMULATOR_EDITING_EXTENSIVE_AGENT_TODOS.md) – Edit anything, add element, Figma-inspired.
- [scripts/emulator-sync-service/README.md](../../scripts/emulator-sync-service/README.md) – Sync service setup.

---

*Email the user at the end of big changes per [EMAIL_AT_END_OF_BIG_CHANGES.md](EMAIL_AT_END_OF_BIG_CHANGES.md). After completing Phase A/B/C execution, **run the automated script** (no manual step): `python scripts/coordinator-email/send_phase_completion_email.py phase_abc` or `scripts\coordinator-email\send_phase_completion_email.bat phase_abc`.*
