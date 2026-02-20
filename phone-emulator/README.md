# OutOfRouteBuddy Virtual Phone Emulator

A Figma-like web-based phone emulator for the OutOfRouteBuddy app. Deploy it anywhere and let users **touch/click elements to edit** labels, text, and content in real time.

## Features

- **Virtual phone frame** – Displays the app in a realistic device mockup
- **Touch-to-edit** – Toggle Edit Mode, then tap any button or field to change its text
- **Properties panel** – Edit selected elements (like Figma’s inspector)
- **Persistent edits** – Changes are saved to `localStorage`
- **Copy for Cursor** – Generates Cursor-ready instructions and copies to clipboard; paste into Cursor chat to apply edits to the real project
- **Sync to project** – Pushes edits to the codebase when the **Emulator Sync Service** is running. Use the desktop shortcut **"OutOfRouteBuddy Emulator"** to open the emulator in one click (it starts the sync service and server and opens your browser). Option **Auto-sync to project** in the property panel pushes after each Save so Cursor sees updates right away.
- **Export/Import** – Save designs as JSON and load them later
- **Reset** – Restore the default design

## Quick Start

**One click:** Double-click the desktop shortcut **"OutOfRouteBuddy Emulator"** (create it once with `powershell -ExecutionPolicy Bypass -File scripts\emulator-sync-service\create_desktop_shortcut.ps1`). It opens the emulator in your browser and starts the sync service so edits can update the project; Cursor sees file changes as they’re written.

**Or from repo root:** Run `scripts\launch_emulator.bat` to start sync + server and open http://localhost:3000.

Or manually:

1. Open `index.html` in a browser (or serve the folder with any static server).
2. **Right-click** any field (toolbar, inputs, buttons, labels).
3. Click **Edit** in the context menu.
4. Change the value, add notes, and click **Save**.
5. Click **Copy for Cursor** to copy the edits as instructions, then paste into Cursor chat to apply them to the Android project.

## Deployment

The emulator is static HTML/CSS/JS. Deploy it to:

- **GitHub Pages** – Push the `phone-emulator` folder to a repo and enable Pages
- **Netlify** – Drag and drop the folder or connect a repo
- **Vercel** – Import the project and deploy
- **Any static host** – Upload the folder as-is

No build step or server required.

## File Structure

```
phone-emulator/
├── index.html         # Main page
├── styles.css         # Emulator and app styling
├── app-renderer.js    # Renders app UI from design state
├── cursor-exporter.js # Maps edits to project files, generates Cursor instructions
├── editor.js          # Touch-to-edit logic
└── README.md
```

## Design State

Edits are stored as JSON. Example:

```json
{
  "toolbar": { "title": "OOR" },
  "loadedMiles": { "hint": "Loaded Miles" },
  "startButton": { "text": "Start Trip" },
  ...
}
```

Use **Export Design** to download and **Import Design** to load a saved design.

## Editing and sync

1. **Edit** – Right-click a field (or long-press on touch) and choose **Edit**; the properties panel opens.
2. **Save** – Change the value and click **Save** to update the emulator (stored in localStorage). This does not push to the project yet.
3. **Undo** – Use the **Undo** toolbar button (or Ctrl+Z) to revert the last edit. **Redo** (or Ctrl+Y) re-applies it.
4. **Sync to project** – Click **Sync to project** to write the current design to the repo (e.g. `strings.xml`). Cursor will show file changes when the file is open or refocused. Or turn on **Auto-sync to project (after each Save)** in the property panel to push after each Save (debounced).

See [scripts/emulator-sync-service/README.md](../scripts/emulator-sync-service/README.md) for sync service setup.

**Load from project:** Click **Load from project** to fetch the current `strings.xml` from the sync service and merge it into the emulator so the phone matches the repo. Use **Sync to project** to push your edits the other way.

For 1:1 fidelity (spacing, string-key parity, icons), see [1TO1_SPEC.md](1TO1_SPEC.md).

## Customization

To add more editable elements, in `app-renderer.js`:

1. Add the field to `DEFAULT_DESIGN`.
2. Add an HTML element with `data-edit-path` and `data-edit-key`.
3. Add the `editable` class.

The editor will automatically support the new element.

## Emulator index (plans & improvements)

One place for all emulator docs:

| Doc | Purpose |
|-----|--------|
| **`EMULATOR_PERFECTION_PLAN.md`** (this folder) | 1:1 fidelity with the real app, missing UI elements, dialogs |
| **`docs/agents/EMULATOR_20_IMPROVEMENTS_FULL_TEAM_CONSULTATION.md`** | Full-team consultation and the agreed **20 improvements** checklist |
| **`docs/agents/EMULATOR_1TO1_AND_EDITING_BRAINSTORM.md`** | Brainstorm: **1:1 with the app** + **smooth editing** (undo, save, real-time to Cursor) |
| **`docs/agents/EMULATOR_PHASE_ABC_100_TODOS.md`** | **103 agent tasks** for Phase A (undo/save/sync), Phase B (1:1), Phase C (polish); execute A → B → C |
| **`docs/agents/EMULATOR_1TO1_AND_EDITING_100_PLAN.md`** | **102 agent tasks**: Phase P (1:1 perfection) + Phase E (editing tools); consult all agents, then execute |
| **`docs/agents/EMULATOR_EDITING_TEAM_CONSULTATION_AND_TODO.md`** | Right-click edit anything, add things, real-time updates |
| **`docs/agents/EMULATOR_EDITING_EXTENSIVE_AGENT_TODOS.md`** | Phase-by-phase agent tasks (including Figma-inspired editing) |
| **`docs/agents/EMULATOR_FIGMA_INSPIRED_CONSULTATION.md`** | Click-to-select, selection outline, property panel, double-click inline edit |
| **`scripts/emulator-sync-service/README.md`** | Sync service: start shortcut, Sync to project, custom icon |
| **`docs/qa/EMULATOR_SYNC_SMOKE_CHECKLIST.md`** | Smoke checklist: edit → sync → export/import → Copy for Cursor |
| **`docs/qa/EMULATOR_PHASE_ABC_CHECKLIST.md`** | Phase A/B/C: string-key parity + editing flow (Undo, Sync, Load from project) |
| **`docs/agents/EMAIL_AT_END_OF_BIG_CHANGES.md`** | Email user after major emulator (or other) milestones |

## Emulator editing improvements (plan)

**Editing scope:** Every visible text/control in the phone frame is editable (right-click → Edit). Right-click empty space or a container for **Add element here** (Heading, Label, Button, Text input, Spacer). Changes apply in real time; use Undo/Redo as needed.

For the full roadmap (Figma-inspired click-to-select, inline edit, Delete key), see **`docs/agents/EMULATOR_EDITING_TEAM_CONSULTATION_AND_TODO.md`**, **`docs/agents/EMULATOR_EDITING_EXTENSIVE_AGENT_TODOS.md`**, and **`docs/agents/EMULATOR_1TO1_AND_EDITING_100_PLAN.md`**. After completing a big emulator change, the team emails the user per **`docs/agents/EMAIL_AT_END_OF_BIG_CHANGES.md`**.
