# Emulator: Figma-inspired editability – team consultation

**Date:** 2025-02-19  
**Context:** User asked for "Figma-inspired editability" in the phone emulator. The team consulted to define scope and add tasks to the execution plan.

---

## 1. What “Figma-inspired” means (team agreement)

We are not building Figma; we are taking **inspiration** from Figma’s feel so the emulator is direct and predictable:

| Figma behavior | Our scope (emulator) |
|----------------|----------------------|
| **Click to select** | Single click on an editable (or any element in the phone frame) selects it and shows a clear **selection state** (outline/highlight). No need to right-click first to “see” what you’re editing. |
| **Selection outline** | Selected element has a visible outline (e.g. 2px accent color, or dashed border). Optional: small “selection frame” around the element so it’s obvious what’s selected. |
| **Properties panel follows selection** | When you select an element, the **properties panel** (Edit panel) automatically shows that element’s path, value, and notes. Right-click → Edit still works; click-to-select can also open or update the panel. |
| **Double-click to edit text inline** | Double-click on a text node (label, title, button text) enters **inline edit mode**: the text becomes an input or contenteditable in place. On blur or Enter, save to design state and re-render. Feels like Figma’s double-click to edit text. |
| **Delete key** | When an element is selected (especially a custom element), **Delete** or **Backspace** removes it (with optional confirm for custom elements) and re-renders. |
| **Right-click context menu** | Keep existing: right-click → Edit, Add element here. Optionally show “Remove element” for custom elements. |
| **No drag-handles or resize (v1)** | We do **not** scope in move/resize handles or layers panel for this phase; that can be a later enhancement. |

---

## 2. Roles consulted

- **UI/UX:** Define selection state (outline style, focus ring for a11y), double-click target (which elements are “text” and get inline edit), and when the properties panel opens (on select vs only on Edit click).
- **Front-end:** Implement click-to-select, selection outline, panel-on-select, double-click inline edit, Delete key handler, and keep right-click flow.
- **Design:** Agreed that v1 is “selection + panel + inline edit + Delete”; drag/resize/layers are backlog.

---

## 3. Implementation notes (for Front-end)

- **Click vs right-click:** Left-click on `.editable` (or any node inside `#app-content`) selects that element and updates the properties panel. Right-click continues to show the context menu (Edit / Add element here). If the user has already selected via click, right-click → Edit can just focus the panel.
- **Selection storage:** Store `selectedElement` and `selectedMeta` (path, key) in editor.js; on each `render()`, re-apply the selection outline to the node that matches the current path (or clear if not found).
- **Double-click:** On `dblclick`, if target is an editable text node (or has data-edit-path), switch to inline edit: replace content with an input, focus it, on blur/Enter call `updateDesign()` and `render()`. Avoid double-firing with click (e.g. ignore second click of a double-click for selection).
- **Delete key:** `document` or `#app-content` keydown: if key is Delete or Backspace and selected element is a custom element, call remove and re-render; optionally confirm. For non-custom elements, Delete can be no-op or open “clear value” in panel.
- **Accessibility:** Selection outline should meet contrast; consider `:focus-visible` for keyboard users. Panel should be reachable via keyboard (Tab, Enter to open Edit).

---

## 4. Where this lives in the task list

New tasks are added to **`docs/agents/EMULATOR_EDITING_EXTENSIVE_AGENT_TODOS.md`** as **Phase 2b: Figma-inspired editability** (after “Edit anything” and before or alongside “Add element here”). Task IDs: **UX-F1**, **FE-F1** through **FE-F6**. See that doc for the granular list.

---

*This consultation defines the agreed Figma-inspired scope so implementation stays consistent. If the user asks for more (e.g. drag to reorder), add a new consultation or backlog item.*
