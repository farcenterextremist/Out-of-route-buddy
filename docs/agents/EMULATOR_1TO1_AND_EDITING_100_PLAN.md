# Emulator 1:1 perfection + editing tools – 100+ agent plan

**Purpose:** Fix two gaps: (1) **1:1 representation** of the emulator vs the real app is not yet perfected; (2) **editing tools** are not functioning properly. This plan assigns 100+ granular tasks to the full team and should be executed with agent consultation.

**Source:** [EMULATOR_PERFECTION_PLAN.md](../../phone-emulator/EMULATOR_PERFECTION_PLAN.md), [EMULATOR_EDITING_EXTENSIVE_AGENT_TODOS.md](EMULATOR_EDITING_EXTENSIVE_AGENT_TODOS.md), [1TO1_SPEC.md](../../phone-emulator/1TO1_SPEC.md).

**Role codes:** DD = Design, UX = UI/UX, FE = Front-end, BE = Back-end, DO = DevOps, QA = QA, SEC = Security, EE = Email Editor, FO = File Organizer, HITL = Human-in-the-Loop.

**Execution order:** Phase P (1:1 perfection) then Phase E (editing tools), or consult with all agents and parallelize where no dependencies exist.

---

## Phase P – 1:1 representation perfection

| ID | Role | Task | File(s) | Acceptance criteria |
|----|------|------|---------|---------------------|
| P-FO-1 | FO | Create gap list doc: diff 1TO1_SPEC vs EMULATOR_PERFECTION_PLAN and list every unchecked 1:1 item. | `docs/agents/` or `phone-emulator/` | Single checklist of 1:1 gaps. |
| P-FO-2 | FO | Ensure EMULATOR_PERFECTION_PLAN sections 1–8 are cross-referenced from this plan. | This doc | Cross-refs present. |
| P-UX-1 | UX | Specify toolbar: 56dp height, 16dp start/end margin, 8dp gap; confirm vs custom_toolbar.xml. | `phone-emulator/1TO1_SPEC.md` or styles | Values in spec/CSS. |
| P-UX-2 | UX | Specify settings icon: SVG matching ic_settings (gear); no emoji. | `phone-emulator/1TO1_SPEC.md` | Icon spec. |
| P-UX-3 | UX | Specify statistics button: arrow icon (ic_arrow_up/down), position textEnd; down when collapsed, up when expanded. | `phone-emulator/1TO1_SPEC.md` | Arrow spec. |
| P-UX-4 | UX | Specify inputs: numberDecimal, no stroke on light (card_white_rounded), 2dp elevation. | `phone-emulator/1TO1_SPEC.md` | Input spec. |
| P-UX-5 | UX | Specify Start/Pause/End: Pause 48dp right of Start when trip active; Start vs End text; progress bar below. | `phone-emulator/1TO1_SPEC.md` | Button/state spec. |
| P-UX-6 | UX | Specify Today's Info card: 10dp padding; row layout label left, value right. | `phone-emulator/1TO1_SPEC.md` | Card spec. |
| P-UX-7 | UX | Specify Statistics: divider light_gray_background; labels secondary, values primary. | `phone-emulator/1TO1_SPEC.md` | Stats spec. |
| P-UX-8 | UX | Specify dark mode: card #2C2C2C, gradient #1E1E1E→#121212, toolbar #1E1E1E. | `phone-emulator/1TO1_SPEC.md` | Dark spec. |
| P-FE-1 | FE | Toolbar: enforce --toolbar-height 56px, --toolbar-margin 16px, --toolbar-gap 8px in CSS and use in .app-toolbar. | `phone-emulator/styles.css` | Toolbar matches spec. |
| P-FE-2 | FE | Replace toolbar settings emoji with inline SVG gear matching ic_settings. | `phone-emulator/app-renderer.js` | Gear SVG in toolbar. |
| P-FE-3 | FE | Statistics: use SVG chevron down when collapsed, up when expanded; position on textEnd (right of text). | `phone-emulator/app-renderer.js`, styles.css | Arrow icon and position. |
| P-FE-4 | FE | Inputs: inputmode="decimal", pattern for digits; no border on light; box-shadow 2dp (--input-elevation). | `phone-emulator/app-renderer.js`, styles.css | Inputs match app. |
| P-FE-5 | FE | Add Pause/Resume button (48dp, ic_pause/ic_play SVG) to right of Start button; visible only when trip active. | `phone-emulator/app-renderer.js`, styles.css | Pause button present. |
| P-FE-6 | FE | Trip state: idle / active / paused; Start Trip vs End Trip button text; toggle on click. | `phone-emulator/app-renderer.js` | State and text switch. |
| P-FE-7 | FE | Progress bar below Start button; show when "starting" (e.g. 1s); use existing or add .app-progress. | `phone-emulator/app-renderer.js`, styles.css | Progress bar visible on start. |
| P-FE-8 | FE | Today's Info card: padding 10dp (--card-padding); row structure label left, value right. | `phone-emulator/styles.css`, app-renderer.js | Card layout matches. |
| P-FE-9 | FE | Statistics section: divider color light_gray_background (#F5F5F5 light / #2C2C2C dark). | `phone-emulator/styles.css` | Divider matches. |
| P-FE-10 | FE | Statistics row: label secondary text color, value primary; apply to all stat rows. | `phone-emulator/styles.css` | Typography matches. |
| P-FE-11 | FE | Dark theme: card bg #2C2C2C, toolbar/appbar gradient #1E1E1E→#121212, toolbar #1E1E1E. | `phone-emulator/styles.css` [data-theme="dark"] | Dark colors match app. |
| P-FE-12 | FE | Input validation: show error state (red border) when loaded/bounce miles invalid; use input_error_background equivalent. | `phone-emulator/app-renderer.js`, styles.css | Error state visible. |
| P-FE-13 | FE | Settings button: open Settings modal (Mode, Templates, Help & Info); Mode syncs with theme toggle. | `phone-emulator/app-renderer.js`, index.html | Settings modal works. |
| P-FE-14 | FE | End Trip: when active and End Trip clicked, show confirmation modal (End Trip / Clear Trip / Continue Trip). | `phone-emulator/app-renderer.js`, index.html | End Trip dialog. |
| P-FE-15 | FE | Statistics "View" button: open period picker or simple selector (This Week / Month / Custom). | `phone-emulator/app-renderer.js`, index.html | Period selector. |
| P-FE-16 | FE | Fix re-render duplicate listeners: use event delegation or single attach so stats/buttons don't double-fire. | `phone-emulator/app-renderer.js`, editor.js | No duplicate handlers. |
| P-FE-17 | FE | Statistics expand/collapse: arrow down when collapsed, up when expanded; smooth max-height transition. | `phone-emulator/app-renderer.js`, styles.css | Arrow direction and animation. |
| P-BE-1 | BE | Sync service: if any new string keys added for 1:1, add to EMULATOR_TO_PROJECT in sync_service.py. | `scripts/emulator-sync-service/sync_service.py` | Mapping complete. |
| P-QA-1 | QA | Verify toolbar height and margins match spec (measure in dev tools). | — | Pass. |
| P-QA-2 | QA | Verify settings icon is SVG gear, not emoji. | — | Pass. |
| P-QA-3 | QA | Verify statistics arrow position and direction (down collapsed, up expanded). | — | Pass. |
| P-QA-4 | QA | Verify inputs: decimal, no stroke light, 2dp shadow. | — | Pass. |
| P-QA-5 | QA | Verify Pause button appears when trip active; toggles pause/play. | — | Pass. |
| P-QA-6 | QA | Verify Start → End Trip text and progress bar on start. | — | Pass. |
| P-QA-7 | QA | Verify Today's Info padding and row layout. | — | Pass. |
| P-QA-8 | QA | Verify Statistics divider and label/value colors. | — | Pass. |
| P-QA-9 | QA | Verify dark theme colors (card, toolbar, gradient). | — | Pass. |
| P-QA-10 | QA | Verify Settings modal opens; Mode toggles theme. | — | Pass. |
| P-QA-11 | QA | Verify End Trip confirmation and period picker. | — | Pass. |
| P-QA-12 | QA | Document 1:1 verification steps in docs/qa/EMULATOR_1TO1_VERIFICATION.md. | `docs/qa/EMULATOR_1TO1_VERIFICATION.md` | Doc exists. |
| P-SEC-1 | SEC | Confirm no new secrets in design state or sync payload for 1:1 changes. | 1TO1_SPEC or checklist | No secrets. |
| P-DD-1 | DD | Add one line to ROADMAP: "Emulator 1:1 perfection (toolbar, stats, inputs, trip state, modals)." | `docs/product/ROADMAP.md` | ROADMAP updated. |
| P-HITL-1 | HITL | Draft email for user after Phase P: "Emulator now matches app 1:1 (toolbar, icons, trip state, modals). Please compare side-by-side." | Draft in docs/agents/ or script | Draft ready. |
| P-QA-13 | QA | Verify toolbar region 1:1 (height, margins, icon). | — | Pass. |
| P-QA-14 | QA | Verify trip inputs + Start/Pause region 1:1. | — | Pass. |
| P-QA-15 | QA | Verify Today's Info + Statistics region 1:1. | — | Pass. |
| P-FE-18 | FE | Cursor exporter: add any new string keys from 1:1 to EMULATOR_TO_PROJECT (and sync_service.py). | `phone-emulator/cursor-exporter.js`, sync_service.py | Mappings complete. |

---

## Phase E – Editing tools (functioning properly)

| ID | Role | Task | File(s) | Acceptance criteria |
|----|------|------|---------|---------------------|
| E-FO-1 | FO | Link this plan from phone-emulator/README.md (emulator index table). | `phone-emulator/README.md` | Link present. |
| E-FO-2 | FO | Add this plan to docs/README.md index. | `docs/README.md` | Index row added. |
| E-DD-1 | DD | Document decision: editing scope = "every visible text/control editable" + "Add element here" for empty space. | `phone-emulator/README.md` or docs | One sentence. |
| E-UX-1 | UX | Spec: right-click on .editable → Edit; right-click on empty/container → "Add element here". | `docs/ux/` or EMULATOR_ADD_ELEMENT_SPEC | Flow documented. |
| E-UX-2 | UX | Spec: long-press 500–600 ms on touch opens same context menu as right-click. | docs/ux or perfection plan | Duration and behavior. |
| E-UX-3 | UX | Spec: properties panel shows Value + Notes; Save applies updateDesign and re-renders; optional Live preview. | docs/ux or README | Panel behavior. |
| E-UX-4 | UX | Spec: element palette for Add (Heading, Label, Button, Text input, Spacer) and target containers. | `docs/ux/EMULATOR_ADD_ELEMENT_SPEC.md` | Palette and containers. |
| E-FE-1 | FE | Ensure every visible text/control in phone frame has data-edit-path, data-edit-key, class editable. | `phone-emulator/app-renderer.js` | No gaps; audit stat rows, period label/value. |
| E-FE-2 | FE | Context menu: when right-click target has no .editable ancestor but is inside #app-content, show menu with "Add element here" (and optionally disable Edit). | `phone-emulator/editor.js` | Right-click empty shows Add. |
| E-FE-3 | FE | Record right-click container for Add: closest .app-body, .app-card, or #app-content; store in pendingAddContainer. | `phone-emulator/editor.js` | Container captured. |
| E-FE-4 | FE | Add "Add element here" menu item to context menu; show when target is non-editable or container. | `phone-emulator/index.html`, editor.js | Menu item present and shown. |
| E-FE-5 | FE | Add modal/dialog "Add element here": type (Heading/Label/Button/Text input/Spacer), text input, [Add][Cancel]. | `phone-emulator/index.html` | Dialog markup. |
| E-FE-6 | FE | Style add-element dialog in styles.css (overlay, panel, dropdown, buttons). | `phone-emulator/styles.css` | Dialog styled. |
| E-FE-7 | FE | Wire Add element: [Add] calls AppRenderer.addCustomElement(containerId, index, {type, text}), render(), hide dialog. | `phone-emulator/editor.js` | Add inserts and re-renders. |
| E-FE-8 | FE | designState.customElements: array; loadDesign/resetDesign init/clear it; addCustomElement() pushes item, saveDesign(), render(). | `phone-emulator/app-renderer.js` | customElements state. |
| E-FE-9 | FE | addCustomElement(containerId, index, payload): unique id, push to customElements, saveDesign, render; expose on AppRenderer. | `phone-emulator/app-renderer.js` | addCustomElement works. |
| E-FE-10 | FE | In render(), after main template, inject custom elements into container by containerId; each with data-edit-path/key, editable, stable id. | `phone-emulator/app-renderer.js` | Custom elements render. |
| E-FE-11 | FE | Long-press: touchstart/touchend 500–600 ms without movement → show context menu at touch position; reuse menu logic. | `phone-emulator/editor.js` | Long-press opens menu. |
| E-FE-12 | FE | Properties panel: for input fields support both hint (placeholder) and value; show correct field per data-edit-type. | `phone-emulator/editor.js` | Placeholder vs value editable. |
| E-FE-13 | FE | Input value sync: when user types in Loaded/Bounce miles input, persist to designState (loadedMiles.value, bounceMiles.value) on change/blur. | `phone-emulator/app-renderer.js` or editor.js | Input values survive re-render. |
| E-FE-14 | FE | Selection outline: left-click on .editable sets selectedElement, apply .selected (accent outline); clear previous. | `phone-emulator/editor.js`, styles.css | Click selects; outline visible. |
| E-FE-15 | FE | On selection change open/update properties panel with selected path/key/value/notes. | `phone-emulator/editor.js` | Panel updates on select. |
| E-FE-16 | FE | Double-click on editable text: inline edit (contenteditable or input); on blur/Enter call updateDesign, saveDesign, render. | `phone-emulator/editor.js` | Double-click inline edit. |
| E-FE-17 | FE | Delete key: when custom element selected, Delete/Backspace removes from customElements, saveDesign, render, clear selection. | `phone-emulator/editor.js`, app-renderer.js | Delete removes custom. |
| E-FE-18 | FE | After render(), re-apply selection outline to node matching selectedMeta if still in DOM; re-attach click handler. | `phone-emulator/editor.js` | Selection survives re-render. |
| E-FE-19 | FE | Context menu and panel: ensure no duplicate listeners after render; use delegation or single attach on #app-content. | `phone-emulator/editor.js` | No double-firing. |
| E-FE-20 | FE | Save button in panel: call updateDesign(pathKey, value) and setNote; pathKey format "path.key" (e.g. toolbar.title). | `phone-emulator/editor.js` | Save updates design. |
| E-FE-21 | FE | updateDesign in app-renderer: accept dotted path, push undo, clear redo, set value at path, saveDesign, render, updateUndoRedoButtons. | `phone-emulator/app-renderer.js` | Single save path. |
| E-FE-22 | FE | Optional: "Remove element" in context menu or panel for custom elements; remove from customElements, save, render. | `phone-emulator/editor.js` | Remove custom element. |
| E-FE-23 | FE | Accessibility: selection outline contrast; :focus-visible; aria-labels on panel inputs and Save. | `phone-emulator/editor.js`, styles.css | A11y OK. |
| E-QA-1 | QA | Smoke: right-click editable → Edit → change value → Save → phone frame updates. | — | Pass. |
| E-QA-2 | QA | Smoke: right-click empty area → Add element here → add Heading → element appears. | — | Pass. |
| E-QA-3 | QA | Smoke: long-press on editable opens context menu. | — | Pass. |
| E-QA-4 | QA | Smoke: select element → panel shows value → change → Save → re-render correct. | — | Pass. |
| E-QA-5 | QA | Smoke: Undo after edit reverts; Redo re-applies. | — | Pass. |
| E-QA-6 | QA | Smoke: custom element → Delete key removes it. | — | Pass. |
| E-QA-7 | QA | Smoke: Loaded/Bounce input value persists after re-render. | — | Pass. |
| E-QA-8 | QA | Document editing flow verification in docs/qa/EMULATOR_EDITING_VERIFICATION.md. | `docs/qa/EMULATOR_EDITING_VERIFICATION.md` | Doc exists. |
| E-EE-1 | EE | Review panel labels (Value, Notes, Save, Live preview, Auto-sync) for clarity. | — | Copy approved. |
| E-HITL-1 | HITL | Draft email for user after Phase E: "Emulator editing fixed: right-click Edit, Add element here, long-press, selection. Please try." | Draft | Draft ready. |
| E-QA-9 | QA | Verify context menu on editable and on empty. | — | Pass. |
| E-QA-10 | QA | Verify custom element add and remove. | — | Pass. |
| E-QA-11 | QA | Verify selection and panel sync after re-render. | — | Pass. |
| E-FE-24 | FE | Update phone-emulator/README: Add element here, long-press, selection; link to this plan. | `phone-emulator/README.md` | README updated. |

---

## Dependency summary

- **Phase P (1:1):** P-FO-1, P-FO-2, P-UX-* can run first. P-FE-* implement in order (toolbar → icons → inputs → trip state → modals → styles). P-QA-* after FE. P-BE-1 if new strings. P-DD-1, P-SEC-1, P-HITL-1 at end.
- **Phase E (editing):** E-FO-*, E-DD-1, E-UX-* first. E-FE-1 (audit editable) then E-FE-2–7 (context menu + Add dialog), E-FE-8–10 (customElements state + render), E-FE-11–23 (long-press, panel, selection, inline edit, Delete, persistence). E-QA-* after FE. E-EE-1, E-HITL-1 at end.
- **Cross-phase:** Phase E can start in parallel with Phase P for doc/UX tasks; FE implementation of E may assume P visual structure is stable.

---

## Task index by role

| Role | Task IDs |
|------|----------|
| DD | P-DD-1, E-DD-1 |
| UX | P-UX-1–P-UX-8, E-UX-1–E-UX-4 |
| FE | P-FE-1–P-FE-18, E-FE-1–E-FE-24 |
| BE | P-BE-1 |
| QA | P-QA-1–P-QA-15, E-QA-1–E-QA-11 |
| SEC | P-SEC-1 |
| EE | E-EE-1 |
| FO | P-FO-1, P-FO-2, E-FO-1, E-FO-2 |
| HITL | P-HITL-1, E-HITL-1 |

**Total:** Phase P 56 tasks, Phase E 46 tasks → **102 tasks**.

---

## Cross-references

- [EMULATOR_PERFECTION_PLAN.md](../../phone-emulator/EMULATOR_PERFECTION_PLAN.md) – 1:1 gaps and priorities.
- [1TO1_SPEC.md](../../phone-emulator/1TO1_SPEC.md) – Current 1:1 spec.
- [EMULATOR_EDITING_EXTENSIVE_AGENT_TODOS.md](EMULATOR_EDITING_EXTENSIVE_AGENT_TODOS.md) – Edit anything, Add element, Figma-inspired.
- [EMULATOR_EDITING_TEAM_CONSULTATION_AND_TODO.md](EMULATOR_EDITING_TEAM_CONSULTATION_AND_TODO.md) – Team consultation.
- [EMULATOR_PHASE_ABC_100_TODOS.md](EMULATOR_PHASE_ABC_100_TODOS.md) – Phase A/B/C (undo, save, sync, 1:1 base).
- [scripts/emulator-sync-service/README.md](../../scripts/emulator-sync-service/README.md) – Sync service.

---

*After completing Phase P or Phase E, send the summary email per [EMAIL_AT_END_OF_BIG_CHANGES.md](EMAIL_AT_END_OF_BIG_CHANGES.md).*
