# Emulator editing flow verification

Smoke tests for right-click Edit, Add element here, long-press, and custom elements. See [EMULATOR_1TO1_AND_EDITING_100_PLAN.md](../agents/EMULATOR_1TO1_AND_EDITING_100_PLAN.md) Phase E.

## Steps

1. **Right-click editable → Edit:** Right-click a label/button, choose Edit, change value, Save → phone frame updates.
2. **Right-click empty → Add element here:** Right-click empty area in phone, choose "Add element here", pick type (e.g. Heading) and text, Add → element appears.
3. **Long-press (touch):** Long-press ~550 ms on editable → context menu opens.
4. **Custom element edit:** Right-click a custom-added element → Edit → change text → Save.
5. **Undo/Redo:** After edit, Undo reverts; Redo re-applies.
6. **Input value persists:** Type in Loaded/Bounce miles, trigger re-render (e.g. toggle stats) → value still there.

Pass/fail: record after each run.
