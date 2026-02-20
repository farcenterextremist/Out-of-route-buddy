# Emulator editing – smoke test (Phase A)

Quick steps to verify Edit, Save, Undo, Redo, Sync, and Auto-sync.

## Prerequisites

- Emulator open (desktop shortcut or http://localhost:3000).
- Optional: Sync service running for Sync to project and Auto-sync steps.

---

## Steps

1. **Edit and Save** – Right-click a field (e.g. toolbar title "OOR" or "Start Trip"). Click **Edit**. Change the value, click **Save**. Confirm the phone frame updates immediately (no reload).

2. **Undo** – Click **Undo** in the toolbar (or press Ctrl+Z). Confirm the field reverts to the previous value.

3. **Redo** – Click **Redo** (or Ctrl+Y). Confirm the field shows the edited value again.

4. **Sync to project** – With sync service running, click **Sync to project**. Confirm a toast like "Synced N string(s)" or "No changes to apply." If the service is not running, an error message should appear.

5. **Auto-sync** – In the property panel, check **Auto-sync to project (after each Save)**. Edit a field and click **Save**. Within a few seconds, a sync should run (toast or check file/network). Uncheck Auto-sync when done if you prefer manual sync.

6. **Keyboard** – With focus outside any input/textarea, press Ctrl+Z (undo) and Ctrl+Y (redo). Confirm they work. Undo and Redo buttons should enable/disable correctly.

---

## Pass criteria

- Save updates the phone frame without reload.
- Undo reverts the last edit; Redo re-applies it.
- Sync to project shows success or clear error.
- Auto-sync (when on) triggers sync after Save (debounced).
- Undo/Redo buttons and shortcuts behave as above.

---

*Source: Phase A (docs/agents/EMULATOR_PHASE_ABC_100_TODOS.md).*
