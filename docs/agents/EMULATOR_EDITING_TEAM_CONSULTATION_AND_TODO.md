# Emulator editing tool – team consultation & comprehensive todo

**Date:** 2025-02-19  
**Duration:** ~20 minutes (team consultation)  
**Goal:** Improve the phone emulator so we can **right-click and edit anything**, **right-click empty space to add things**, and have the **emulator enact changes in real time**.  
**Scope:** `phone-emulator/` (HTML, CSS, JS) and related docs.

---

## 1. Team convened

Roles that can help improve the emulator editing tool:

| Role | Why they’re involved | Ownership |
|------|----------------------|-----------|
| **UI/UX Specialist** | Flows for “add element here”, which element types to offer, where empty-space zones make sense; accessibility (long-press, keyboard). | Specs, flows, element palette. |
| **Front-end Engineer** | Owns emulator implementation (`phone-emulator/`). | editor.js, app-renderer.js, index.html, styles.css, cursor-exporter.js. |
| **Project Design / Creative Manager** | Scope of “edit anything” and “add anything” (e.g. label, button, input, spacer); priority vs other work. | Prioritization, scope. |
| **QA Engineer** | Smoke-test editing flows, add-element, export/import, Copy for Cursor after changes. | Test checklist, regression. |
| **File Organizer** | Keep emulator docs (EMULATOR_PERFECTION_PLAN, this doc) discoverable and linked. | Links, docs index. |

**Not primary for this feature:** Back-end (no server), DevOps (static deploy only), Security (no new credentials), Email Editor, Human-in-the-Loop (unless we email the user a summary).

---

## 2. 20-minute consultation summary

### 2.1 Current behavior (recap)

- **Right-click:** Only elements with class `.editable` and `data-edit-path` / `data-edit-key` show the context menu; one “Edit” option opens the properties panel (Value + Notes + Save).
- **Empty space:** Right-click on non-editable areas (e.g. empty padding, container divs) does nothing.
- **Real time:** On “Save”, `updateDesign()` runs → `saveDesign(); render();` so the screen updates immediately after save. There is no live preview while typing in the panel.

### 2.2 “Edit anything”

- **Design:** Agreed that “edit anything” means any visible text or control in the phone frame can be edited (toolbar, inputs, buttons, labels, section titles, stat blocks). Today some inner spans (e.g. stat rows) are not individually editable.
- **Front-end approach:** (1) Ensure every meaningful text/control has `data-edit-path` and `data-edit-key` (and `.editable`), or (2) add a fallback: right-click on an element without a path opens “Edit” with a generated path (e.g. `custom.byId[elementId]`) and allow editing innerText/value. Prefer (1) for predictable Copy-for-Cursor mapping; use (2) for truly “any” element with optional export later.
- **UI/UX:** Suggest a short “Editable regions” note in the emulator README so designers know what is editable and what “add element” does.

### 2.3 “Right-click empty space to add things”

- **Design:** “Add things” = add new UI elements (e.g. heading, label, button, input, spacer) at a chosen place. Empty space = right-click on `#app-content` or on a container (e.g. `.app-body`, `.app-card`) when the exact target is not an existing editable.
- **UI/UX:** Define a small **element palette**: Heading, Label, Button, Text input, Spacer. “Add element here” opens a small dialog: type (dropdown), optional label/text, then “Add”. Placement: append to the container’s “slot” (e.g. `customElements[]` with a `containerId` and optional `index`).
- **Front-end:** On right-click with no `.editable` target, show context menu with “Add element here”. Store new elements in design state (e.g. `designState.customElements = [{ type, text, containerId, index }]`). Renderer must inject these into the right container during `render()` so the emulator enacts changes in real time (next section).

### 2.4 “Emulator enacts changes in real time”

- **Current:** Save → `updateDesign()` → `saveDesign(); render()` → DOM updated. So “after save” is already real time.
- **Enhancement:** “Real time” also interpreted as (1) **live preview** while typing in the properties panel (optional): on `input`/`change`, update the design state and call `render()` (or update only the selected node’s text to avoid focus loss), and (2) **immediate feedback** when adding an element: after “Add”, re-render once so the new element appears without reload.
- **Front-end:** Ensure no full-page reload; single source of truth is `designState`; every mutation goes through `updateDesign()` or a new `addCustomElement()` then `render()`.

### 2.5 Cross-cutting

- **Long-press:** UI/UX and EMULATOR_PERFECTION_PLAN already call out long-press for touch devices; keep in this todo.
- **Cursor exporter:** New custom elements need a strategy for Copy-for-Cursor (e.g. “Add to strings.xml with a new key” or “Export as layout snippet”). Front-end to extend cursor-exporter or document “custom elements export” as a follow-up.
- **QA:** After implementation, smoke-test: right-click every region, add element in empty space, Save, Export/Import, Copy for Cursor.

---

## 3. Comprehensive todo list (by role)

### 3.1 UI/UX Specialist

- [ ] **Spec: “Add element here” flow** — One-page spec or addition to `docs/ux/`: right-click empty/container → “Add element here” → dialog (type: Heading / Label / Button / Text input / Spacer, optional text) → Add. Where “here” is (which container) and how it maps to layout (e.g. append to body vs card).
- [ ] **Element palette** — Document the list of addable element types and default labels (e.g. “New Heading”, “New Button”) for Front-end to implement.
- [ ] **Editable regions note** — Short note for README or docs/ux: what is editable today (all elements with .editable) and that “add element” inserts into the selected container.
- [ ] **Long-press and keyboard** — Reconfirm long-press (touch) and optional keyboard shortcut (e.g. Enter to open edit when focused) in EMULATOR_PERFECTION_PLAN or this doc; hand off to Front-end.

**Artifacts:** `docs/ux/EMULATOR_ADD_ELEMENT_SPEC.md` (or section in existing UX doc), README or docs/ux note, any update to EMULATOR_PERFECTION_PLAN.

---

### 3.2 Front-end Engineer (emulator implementation)

**Right-click edit anything**

- [ ] **Coverage of editables** — Ensure every visible text/control in the phone frame has `data-edit-path`, `data-edit-key`, and class `editable` in `app-renderer.js` (e.g. stat row labels/values, toolbar lines if desired). So “right-click anything” works for all current UI.
- [ ] **Fallback for unmarked elements** — Optional: if right-click hits an element without `data-edit-path`, show context menu “Edit” and on Edit create a synthetic path (e.g. `custom.domId[el.id]` or `custom.byIndex[n]`) and allow editing text/value; persist in `designState` and re-render. Document behavior for Cursor export.

**Right-click empty space to add things**

- [ ] **Context menu on empty/container** — In `editor.js`, extend contextmenu handler on `#app-content`: when `e.target.closest('.editable')` is null, still preventDefault and show context menu with “Add element here” (and optionally “Edit” if target is a container with an id). Store which container was right-clicked (e.g. `app-body`, `app-card`, or `app-content`).
- [ ] **Add-element dialog** — Add a small modal/dialog: element type (Heading, Label, Button, Text input, Spacer), optional text/label, [Add] [Cancel]. On Add, call new `AppRenderer.addCustomElement(containerId, index, { type, text })` and then `render()`.
- [ ] **Design state for custom elements** — In `app-renderer.js`, add `designState.customElements = []` (array of `{ id, type, text, containerId, index }`). Implement `addCustomElement(containerId, index, payload)`, persist, and in `render()` inject these nodes into the right container so the emulator enacts changes in real time.
- [ ] **Rendering custom elements** — In `render()`, after building the main template, append or insert custom elements into the appropriate container (e.g. `.app-body` or a dedicated “custom” zone). Give each a stable id and `data-edit-path` (e.g. `customElements.0`) so they are editable and deletable later.

**Real time**

- [ ] **Immediate re-render on Save** — Already in place; confirm `updateDesign()` → `saveDesign(); render()` and that no reload is needed.
- [ ] **Live preview (optional)** — In the properties panel, on `input` or `change` on the value field, optionally call `updateDesign(pathKey, newVal)` and `render()` (or update only the selected element’s text to avoid focus issues). If full re-render steals focus, consider debounce or “Live preview” checkbox.
- [ ] **Immediate re-render on Add element** — After adding a custom element, call `render()` once so the new element appears without leaving the page.

**Other**

- [ ] **Long-press for touch** — In `editor.js`, on `#app-content`, add long-press (e.g. 500–600 ms) to open the same context menu as right-click, so touch devices can edit and add elements.
- [ ] **Delete / “Remove element”** — Optional: for custom elements, add “Remove element” in context menu or properties panel so users can delete added items; remove from `designState.customElements` and re-render.

**Artifacts:** Changes in `phone-emulator/editor.js`, `app-renderer.js`, `index.html` (dialog markup), `styles.css` (dialog, new elements). Optional: `cursor-exporter.js` extension for custom elements.

---

### 3.3 Project Design / Creative Manager

- [ ] **Scope and priority** — Confirm scope: “edit anything” + “add element here” + “real time” as above. Prioritize vs other roadmap items (Auto drive, Reports, History) and add to `docs/product/ROADMAP.md` or a short “Emulator editing” line.
- [ ] **Copy-for-Cursor for custom elements** — Decide whether custom elements are exported as “add these strings” / “add this layout snippet” or documented for manual apply; hand off to Front-end for implementation or doc.

**Artifacts:** ROADMAP or backlog note; decision on custom-element export.

---

### 3.4 QA Engineer

- [ ] **Smoke test: edit anything** — After Front-end changes: right-click every visible text/control in the phone frame and confirm “Edit” opens and Save updates the screen immediately.
- [ ] **Smoke test: add element** — Right-click empty space (and optionally a container), choose “Add element here”, add one of each type (Heading, Label, Button, Input, Spacer), confirm they appear in real time and persist after Export/Import.
- [ ] **Smoke test: real time** — Confirm Save updates UI without reload; optional live preview works if implemented.
- [ ] **Regression** — Export Design, Import Design, Copy for Cursor still work; existing editable fields still export correctly.
- [ ] **Touch** — If long-press is implemented, smoke-test on a touch device or emulated touch.

**Artifacts:** Short test checklist in `docs/qa/` or in this doc (e.g. “Emulator editing checklist”); optional TEST_PLAN_emulator_editing.md.

---

### 3.5 File Organizer

- [ ] **Link this doc** — Ensure `docs/agents/EMULATOR_EDITING_TEAM_CONSULTATION_AND_TODO.md` is linked from `phone-emulator/README.md` and from `docs/README.md` or docs index (and optionally from EMULATOR_PERFECTION_PLAN).
- [ ] **EMULATOR_PERFECTION_PLAN** — Add a line under “Editor / UX Improvements” or “Implementation Priority” that points to this consultation and todo (right-click edit anything, add element, real time).

**Artifacts:** One or two cross-links in `phone-emulator/README.md`, `docs/README.md`, and optionally `EMULATOR_PERFECTION_PLAN.md`.

---

## 4. Implementation order (suggested)

1. **UI/UX** — Publish short “Add element here” spec and element palette (so Front-end has a clear target).
2. **Front-end** — (a) Extend editables so all visible elements are right-click editable; (b) context menu on empty/container + “Add element here” + design state + render custom elements (real time); (c) optional live preview and long-press.
3. **Design** — Scope/priority and custom-element export decision.
4. **QA** — Run smoke tests and document checklist.
5. **File Organizer** — Add links so the team and future sessions can find this plan.

---

## 5. Quick reference: files to touch

| File / area | Changes |
|-------------|---------|
| `phone-emulator/editor.js` | Context menu on non-editable (empty/container), “Add element here”, long-press, optional live preview. |
| `phone-emulator/app-renderer.js` | `customElements` in design state, `addCustomElement()`, render custom elements into containers; ensure all desired nodes have .editable. |
| `phone-emulator/index.html` | Modal/dialog for “Add element” (type + text + Add/Cancel). |
| `phone-emulator/styles.css` | Dialog styles, custom element styles. |
| `phone-emulator/cursor-exporter.js` | Optional: export custom elements (or document limitation). |
| `phone-emulator/README.md` | Update “Right-click” and “Add element” behavior; link to this doc. |
| `docs/ux/` | Add-element spec, element palette, editable-regions note. |
| `docs/qa/` | Emulator editing smoke checklist. |
| `docs/product/ROADMAP.md` | Optional line for emulator editing. |
| `EMULATOR_PERFECTION_PLAN.md` | Link to this doc; long-press / real time. |

**Figma-inspired:** See **`docs/agents/EMULATOR_FIGMA_INSPIRED_CONSULTATION.md`** (click-to-select, selection outline, panel on select, double-click inline edit, Delete key). Tasks in extensive todo: UX-F1, FE-F1–FE-F6.  
**Email at end of big changes:** See **`docs/agents/EMAIL_AT_END_OF_BIG_CHANGES.md`**.

---

*Summary: The team agreed on “edit anything” (full editable coverage + optional fallback), “add element here” (right-click empty/container → dialog → customElements + render in real time), and “real time” (immediate render on Save and on Add; optional live preview). For a granular, executable task list with task IDs, phases, dependencies, and acceptance criteria, see **`docs/agents/EMULATOR_EDITING_EXTENSIVE_AGENT_TODOS.md`**.*
