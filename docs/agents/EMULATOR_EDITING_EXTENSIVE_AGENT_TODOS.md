# Emulator editing – extensive agent todo plan

**Purpose:** Granular, executable task list for the emulator editing improvements (right-click edit anything, add element on empty space, real-time enactment).  
**Source:** `docs/agents/EMULATOR_EDITING_TEAM_CONSULTATION_AND_TODO.md`  
**Use:** Assign by task ID; check off when done; respect dependencies and phases.

---

## Phase 0: Prerequisites (no dependency)

| ID | Role | Task | File(s) | Acceptance criteria |
|----|------|------|---------|---------------------|
| **FO-1** | File Organizer | Ensure `docs/agents/EMULATOR_EDITING_TEAM_CONSULTATION_AND_TODO.md` is linked from `phone-emulator/README.md`. | `phone-emulator/README.md` | README contains a "See also" or "Emulator editing improvements" link to the consultation doc. |
| **FO-2** | File Organizer | Add this extensive todo doc to docs index. | `docs/README.md` | Index has a row for `agents/EMULATOR_EDITING_EXTENSIVE_AGENT_TODOS.md`. |
| **DD-1** | Project Design / Creative Manager | Add one line to ROADMAP or backlog: "Emulator editing: edit anything, add element here, real time." | `docs/product/ROADMAP.md` | ROADMAP or a "Backlog" section mentions emulator editing. |
| **DD-2** | Project Design / Creative Manager | Document decision: custom elements export for Copy-for-Cursor — "strings only" / "layout snippet" / "manual for now". | `docs/product/` or `phone-emulator/README.md` | One sentence or short note so Front-end knows how to handle custom element export. |

---

## Phase 1: UI/UX specs (unblocks Front-end)

| ID | Role | Task | File(s) | Acceptance criteria |
|----|------|------|---------|---------------------|
| **UX-1** | UI/UX Specialist | Create `docs/ux/EMULATOR_ADD_ELEMENT_SPEC.md`: user flow for "Add element here". Steps: right-click empty or container → context menu shows "Add element here" → click → dialog opens. | `docs/ux/EMULATOR_ADD_ELEMENT_SPEC.md` | Doc exists; describes trigger (right-click non-editable), dialog open, and where "here" is (container id or zone). |
| **UX-2** | UI/UX Specialist | In the same spec or a table: define **element palette** — types and default label/text. Types: `Heading`, `Label`, `Button`, `Text input`, `Spacer`. Defaults: e.g. "New Heading", "New Label", "New Button", placeholder "Enter text", spacer (no text). | `docs/ux/EMULATOR_ADD_ELEMENT_SPEC.md` | Table or list: type → default text/placeholder. |
| **UX-3** | UI/UX Specialist | Define **placement**: which containers accept new elements. E.g. `app-body` (main scroll area), `app-card` (Today's Info card), or a single "Custom block" container. Specify at least one container id for Front-end. | `docs/ux/EMULATOR_ADD_ELEMENT_SPEC.md` | Spec lists container ids (e.g. `app-body`) and whether "Add" appends or inserts at index. |
| **UX-4** | UI/UX Specialist | Add short **Editable regions** note: what is editable (any node with `.editable` and `data-edit-path`/`data-edit-key`), and that "Add element here" inserts into the right-clicked container. | `docs/ux/EMULATOR_ADD_ELEMENT_SPEC.md` or `phone-emulator/README.md` | Note exists so designers know what can be edited and where add-element inserts. |
| **UX-5** | UI/UX Specialist | Document **long-press**: 500–600 ms on touch to open same context menu as right-click. Document optional **keyboard**: e.g. Enter when an editable is focused to open properties panel. | `docs/ux/EMULATOR_ADD_ELEMENT_SPEC.md` or EMULATOR_PERFECTION_PLAN | Long-press duration and keyboard shortcut (if any) are specified for Front-end. |

---

## Phase 2: Front-end — Edit anything

| ID | Role | Task | File(s) | Acceptance criteria |
|----|------|------|---------|---------------------|
| **FE-1** | Front-end Engineer | In `app-renderer.js`, add `data-edit-path` and `data-edit-key` (and class `editable`) to every remaining visible text/control that does not yet have them. Current gaps (from render template): `app-stats-period-label`, `app-stats-period-value`, and each stat row's `.stat-label` / `.stat-value` for weekly/monthly/yearly (so users can edit e.g. "0.0" in a stat row). | `phone-emulator/app-renderer.js` | No visible text inside the phone frame is non-editable; each has path/key and .editable. |
| **FE-2** | Front-end Engineer | Ensure toolbar lines (`.app-toolbar-line`) are either editable or explicitly excluded from "edit anything" in README. If excluded, document in README. | `phone-emulator/app-renderer.js` or `phone-emulator/README.md` | Decision made and consistent (either editable or documented as decorative). |
| **FE-3** | Front-end Engineer | Optional fallback: in `editor.js`, if right-click target has no `.editable` ancestor but is inside `#app-content`, show context menu with "Edit" and create a synthetic path (e.g. `custom.fallback` + unique id); on Edit, allow editing innerText or value and persist to `designState`; re-render. Document in README that fallback edits may not export to Cursor. | `phone-emulator/editor.js`, `phone-emulator/app-renderer.js` | Unmarked elements still get Edit; behavior documented. |

---

## Phase 2b: Figma-inspired editability (consultation: `docs/agents/EMULATOR_FIGMA_INSPIRED_CONSULTATION.md`)

| ID | Role | Task | File(s) | Acceptance criteria |
|----|------|------|---------|---------------------|
| **UX-F1** | UI/UX Specialist | In `docs/ux/EMULATOR_ADD_ELEMENT_SPEC.md` (or addendum): specify **selection state** (outline: 2px accent), **double-click** on text = inline edit, **Delete** = remove selected custom element. Panel opens/updates on select. | `docs/ux/` or consultation doc | Selection and inline-edit behavior specified. |
| **FE-F1** | Front-end Engineer | **Click-to-select:** Left-click on editable (or ancestor) sets `selectedElement` and `selectedMeta`; apply **selection outline** (accent color) to selected node; clear previous selection. | `editor.js`, `styles.css` | Click selects and shows outline. |
| **FE-F2** | Front-end Engineer | **Properties panel on select:** When selection changes, open/update panel with selected element's path, value, notes (reuse showPropertiesPanel). | `editor.js` | Selecting updates Edit panel. |
| **FE-F3** | Front-end Engineer | **Double-click inline edit:** dblclick on editable text → replace with input/contenteditable, focus; on blur/Enter call updateDesign, saveDesign, render. | `editor.js` | Double-click edits text in place. |
| **FE-F4** | Front-end Engineer | **Delete key:** keydown Delete/Backspace + selected custom element → remove from customElements, saveDesign, render, clear selection. Optional confirm. | `editor.js`, `app-renderer.js` | Delete removes selected custom element. |
| **FE-F5** | Front-end Engineer | **Selection survives re-render:** After render(), re-apply outline to node matching selectedMeta; clear if node gone. Re-attach click handler. | `editor.js` | Selection restored after re-render. |
| **FE-F6** | Front-end Engineer | **Accessibility:** Selection outline contrast; :focus-visible for keyboard; Tab/Enter for panel and inline edit. | `editor.js`, `styles.css` | Keyboard and contrast OK. |

---

## Phase 3: Front-end — Add element here (context menu + dialog)

| ID | Role | Task | File(s) | Acceptance criteria |
|----|------|------|---------|---------------------|
| **FE-4** | Front-end Engineer | In `editor.js`, extend the `contextmenu` listener on `#app-content`: when `e.target.closest('.editable')` is null, still `preventDefault()` and `stopPropagation()`, and show the context menu. Add a second menu item: **"Add element here"** (in addition to "Edit" which may be disabled or hidden when target is not .editable). | `phone-emulator/editor.js` | Right-click on empty area or container shows context menu with "Add element here". |
| **FE-5** | Front-end Engineer | When showing "Add element here", record the **container** that was right-clicked: e.g. `e.target.closest('.app-body') || e.target.closest('.app-card') || document.getElementById('app-content')`. Store in a variable (e.g. `pendingAddContainer`) and pass container id or element to the add-element dialog. | `phone-emulator/editor.js` | Container (or default) is known when user clicks "Add element here". |
| **FE-6** | Front-end Engineer | In `index.html`, add a **modal/dialog** for adding an element: heading "Add element here", dropdown or buttons for type (Heading, Label, Button, Text input, Spacer), optional text input for label/text, buttons [Add] [Cancel]. Give the dialog an id (e.g. `add-element-dialog`) and keep it hidden by default. | `phone-emulator/index.html` | Dialog markup exists; can be shown/hidden via JS. |
| **FE-7** | Front-end Engineer | In `styles.css`, add styles for the add-element dialog: overlay, panel, dropdown, text input, buttons. Match existing emulator toolbar/panel look. | `phone-emulator/styles.css` | Dialog is readable and usable. |
| **FE-8** | Front-end Engineer | In `editor.js`, when user clicks "Add element here" in the context menu, show the add-element dialog (e.g. `add-element-dialog.classList.add('visible')`). Wire [Cancel] to hide the dialog. Wire [Add] to: read type and text from form, call `AppRenderer.addCustomElement(containerId, index, { type, text })`, then `AppRenderer.render()`, then hide dialog and deselect. | `phone-emulator/editor.js` | Flow: right-click empty → Add element here → dialog → Add → element appears in phone frame. |

---

## Phase 4: Front-end — Design state and rendering for custom elements

| ID | Role | Task | File(s) | Acceptance criteria |
|----|------|------|---------|---------------------|
| **FE-9** | Front-end Engineer | In `app-renderer.js`, ensure `designState` has a key `customElements`: an array. On `loadDesign()`, initialize `designState.customElements = designState.customElements || []`. In `resetDesign()`, set `designState.customElements = []`. | `phone-emulator/app-renderer.js` | customElements is always an array; survives load/reset. |
| **FE-10** | Front-end Engineer | Implement `addCustomElement(containerId, index, payload)` in app-renderer.js: push `{ id: uniqueId(), type: payload.type, text: payload.text || '', containerId, index }` to `designState.customElements`, then `saveDesign()` and `render()`. Expose on `window.AppRenderer`. | `phone-emulator/app-renderer.js` | addCustomElement exists; persists and triggers render. |
| **FE-11** | Front-end Engineer | In `render()`, after the main `container.innerHTML = ...` template, **inject custom elements**. For each item in `designState.customElements`, build a DOM node (or HTML string) based on `type`: Heading → h3, Label → span, Button → button, Text input → input, Spacer → div with min-height. Set `data-edit-path="customElements.i"` and `data-edit-key="text"` (or appropriate) and class `editable`. Append (or insert at index) into the container matching `containerId` (e.g. `.app-body`). | `phone-emulator/app-renderer.js` | Custom elements appear in the phone frame in the correct container; each is editable. |
| **FE-12** | Front-end Engineer | Give each custom element a stable `id` attribute (e.g. `custom-el-${item.id}`) so it can be targeted for "Remove element" later. Ensure re-render does not duplicate or lose custom elements (render from designState only). | `phone-emulator/app-renderer.js` | Ids are stable; no duplicate/missing custom elements after render. |

---

## Phase 5: Front-end — Real time and polish

| ID | Role | Task | File(s) | Acceptance criteria |
|----|------|------|---------|---------------------|
| **FE-13** | Front-end Engineer | Confirm and document: on Save in properties panel, `updateDesign()` → `saveDesign(); render()` (already in place). No full-page reload. Add a one-line comment in editor.js or app-renderer.js if helpful. | `phone-emulator/editor.js` or `app-renderer.js` | Real-time update on Save is explicit and working. |
| **FE-14** | Front-end Engineer | Optional: **Live preview** — in the properties panel, on `input` or `change` on the value field, debounce (e.g. 300 ms) and call `updateDesign(pathKey, newVal)` and `render()`. If focus is lost on re-render, consider updating only the selected element's text content instead of full render, or add a "Live preview" checkbox to toggle. | `phone-emulator/editor.js` | Either live preview works without breaking focus, or it's documented as deferred. |
| **FE-15** | Front-end Engineer | **Long-press for touch**: on `#app-content`, listen for `touchstart` and `touchend` (or `touchcancel`). If touch lasts 500–600 ms without movement, prevent default and show the same context menu at touch position (use clientX/clientY from touch event). Reuse existing context menu logic. | `phone-emulator/editor.js` | Long-press on phone frame opens context menu (Edit / Add element here). |
| **FE-16** | Front-end Engineer | Optional: **Remove element** — for nodes with `data-edit-path` matching `customElements.N`, add "Remove element" to the context menu or to the properties panel. On confirm, remove that item from `designState.customElements`, `saveDesign()`, `render()`. | `phone-emulator/editor.js`, `app-renderer.js` | Custom elements can be removed; state and DOM stay in sync. |

---

## Phase 6: Cursor exporter and README

| ID | Role | Task | File(s) | Acceptance criteria |
|----|------|------|---------|---------------------|
| **FE-17** | Front-end Engineer | Per Design decision (DD-2): either (a) extend `cursor-exporter.js` to include custom elements (e.g. as "Add these strings" or "Layout snippet"), or (b) add a short note in README/cursor-exporter: "Custom elements are not yet exported to Cursor; export design JSON and apply manually if needed." | `phone-emulator/cursor-exporter.js` or `phone-emulator/README.md` | Custom elements are either exported or explicitly documented as not exported. |
| **FE-18** | Front-end Engineer | Update `phone-emulator/README.md`: under Quick Start or Features, add "Right-click empty space (or a container) and choose **Add element here** to insert a new Heading, Label, Button, Text input, or Spacer. Changes apply in real time." Link to `docs/ux/EMULATOR_ADD_ELEMENT_SPEC.md` if it exists. | `phone-emulator/README.md` | README describes add-element and real-time behavior. |

---

## Phase 7: QA

| ID | Role | Task | File(s) | Acceptance criteria |
|----|------|------|---------|---------------------|
| **QA-1** | QA Engineer | **Smoke: Edit anything** — Right-click toolbar title, toolbar icon, both inputs, Start button, Today's Info title, each card row label/value, Statistics button, period label/button, each stat block title, and each stat row value. Confirm "Edit" opens, Save updates the screen immediately (no reload). | — | All listed regions are editable and update on Save. |
| **QA-2** | QA Engineer | **Smoke: Add element** — Right-click empty area in the phone frame. Click "Add element here". Add one of each type (Heading, Label, Button, Text input, Spacer) with test text. Confirm each appears in the frame in real time. Export design, reload page, Import design; confirm custom elements are still present. | — | Add-element flow works; export/import preserves custom elements. |
| **QA-3** | QA Engineer | **Smoke: Real time** — Change a value in the properties panel and click Save; confirm the phone frame updates without page reload. If live preview is implemented, confirm typing (with debounce) updates the frame. | — | Save and optional live preview behave as specified. |
| **QA-4** | QA Engineer | **Regression** — Export Design (JSON), Import Design, Copy for Cursor. Confirm existing editable fields still export to the expected strings.xml mappings; no console errors during normal use. | — | Export/import and Copy for Cursor still work. |
| **QA-5** | QA Engineer | **Touch** — If long-press is implemented, test on a touch device or Chrome DevTools device emulation: long-press on the phone frame opens the context menu. | — | Long-press opens menu when implemented. |
| **QA-6** | QA Engineer | Create a short **test checklist** in `docs/qa/TEST_CHECKLIST_emulator_editing.md` (or append to an existing QA doc) listing the above scenarios so future runs are consistent. | `docs/qa/TEST_CHECKLIST_emulator_editing.md` or similar | Checklist exists and is linked or listed in docs index. |

---

## Phase 8: File Organizer (final links)

| ID | Role | Task | File(s) | Acceptance criteria |
|----|------|------|---------|---------------------|
| **FO-3** | File Organizer | Link `docs/agents/EMULATOR_EDITING_EXTENSIVE_AGENT_TODOS.md` from `docs/agents/EMULATOR_EDITING_TEAM_CONSULTATION_AND_TODO.md` (e.g. "For a granular task list with IDs and phases, see EMULATOR_EDITING_EXTENSIVE_AGENT_TODOS.md"). | `docs/agents/EMULATOR_EDITING_TEAM_CONSULTATION_AND_TODO.md` | Consultation doc points to extensive todo. |
| **FO-4** | File Organizer | In `phone-emulator/EMULATOR_PERFECTION_PLAN.md`, under Editor / UX or Implementation Priority, add a line: "Execution plan: see docs/agents/EMULATOR_EDITING_EXTENSIVE_AGENT_TODOS.md." | `phone-emulator/EMULATOR_PERFECTION_PLAN.md` | EMULATOR_PERFECTION_PLAN links to extensive todo. |

---

## Dependency summary

```
Phase 0: FO-1, FO-2, DD-1, DD-2 (no deps)
Phase 1: UX-1 → UX-2, UX-3, UX-4, UX-5 (UX-1 first; rest can be same doc)
Phase 2: FE-1, FE-2, FE-3 (after UX-4 for "editable" definition; can start after Phase 0)
Phase 2b: UX-F1, FE-F1–FE-F6 (Figma-inspired: click-to-select, outline, panel on select, double-click inline edit, Delete key; after Phase 1 for UX-F1)
Phase 3: FE-4, FE-5 (after FE-1); FE-6, FE-7, FE-8 (FE-6/FE-7 before FE-8)
Phase 4: FE-9, FE-10, FE-11, FE-12 (FE-9 before FE-10/FE-11; FE-11 uses FE-10)
Phase 5: FE-13, FE-14, FE-15, FE-16 (after Phase 4)
Phase 6: FE-17 (after DD-2), FE-18 (after Phase 5)
Phase 7: QA-1–QA-6 (after Phase 6)
Phase 8: FO-3, FO-4 (any time after this doc exists)
```

**Suggested execution order:**  
0 → 1 → 2 → 2b → 3 → 4 → 5 → 6 → 7 → 8.  
Design (DD-1, DD-2) and File Organizer (FO-1, FO-2) can run in parallel with Phase 1. After completing a **big change** (e.g. full emulator phase, major feature), **email the user** per `docs/agents/EMAIL_AT_END_OF_BIG_CHANGES.md`.

---

## Task index by role

| Role | Task IDs |
|------|----------|
| **File Organizer** | FO-1, FO-2, FO-3, FO-4 |
| **Project Design / Creative Manager** | DD-1, DD-2 |
| **UI/UX Specialist** | UX-1, UX-2, UX-3, UX-4, UX-5, UX-F1 |
| **Front-end Engineer** | FE-1–FE-18, FE-F1–FE-F6 (Figma-inspired) |
| **QA Engineer** | QA-1, QA-2, QA-3, QA-4, QA-5, QA-6 |

---

*Total: 4 FO, 2 DD, 6 UX (incl. UX-F1), 24 FE (incl. FE-F1–FE-F6), 6 QA. Use the IDs in standup or when assigning; check off in this doc when done. **Email the user at the end of big changes** (see docs/agents/EMAIL_AT_END_OF_BIG_CHANGES.md).*
