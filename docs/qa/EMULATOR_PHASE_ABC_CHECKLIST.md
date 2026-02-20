# Emulator Phase A/B/C – QA checklist

Combines **string-key parity** (every strings.xml key has an editable in the emulator) and **editing flow** test cases. Run after Phase A, B, and C implementation.

**Reference:** [EMULATOR_PHASE_ABC_100_TODOS.md](../agents/EMULATOR_PHASE_ABC_100_TODOS.md), [EMULATOR_EDITING_SMOKE.md](EMULATOR_EDITING_SMOKE.md).

**In-app copy review (C-EE-1):** Toolbar hint ("Save = update here. Sync = update Cursor. Use Undo to revert."), panel labels (Save, Auto-sync to project), Undo/Redo tooltips and aria-labels reviewed for consistency.

---

## 1. String-key parity

Every string key in `app/src/main/res/values/strings.xml` that is mapped in the emulator must have an editable element.

| stringName | Design path | Editable? | Pass/Fail |
|------------|-------------|-----------|-----------|
| oor | toolbar.title | Toolbar title | ☐ |
| loaded_miles | loadedMiles.hint | Loaded miles input placeholder | ☐ |
| bounce_miles | bounceMiles.hint | Bounce miles input placeholder | ☐ |
| start_trip | startButton.text | Start Trip button | ☐ |
| todays_info | todaysInfo.title | Today's Info title | ☐ |
| total_miles | totalMiles.label | Total miles label | ☐ |
| oor_miles | oorMiles.label | OOR miles label | ☐ |
| oor_percent | oorPercent.label | OOR percent label | ☐ |
| statistics | statisticsButton.text | Statistics button | ☐ |
| statistics_period_label | statisticsPeriod.label | Period label | ☐ |
| statistics_change_period_button | statisticsPeriod.button | View button | ☐ |
| statistics_period_value | statisticsPeriod.value | Period value | ☐ |
| weekly_statistics | weeklyStats.title | Weekly stats title | ☐ |
| monthly_statistics | monthlyStats.title | Monthly stats title | ☐ |
| yearly_statistics | yearlyStats.title | Yearly stats title | ☐ |

**Acceptance:** Every row verified; no string key used by the emulator is missing an editable.

---

## 2. Editing flow – test cases

### 2.1 Edit → Save → verify in emulator

1. Open emulator.
2. Right-click a field (e.g. toolbar title), choose **Edit**.
3. Change the value in the properties panel, click **Save**.
4. **Expected:** Phone frame updates immediately with the new value.

**Pass / Fail:** ☐

---

### 2.2 Undo after Save reverts change

1. After an edit + Save, click **Undo** (toolbar or Ctrl+Z).
2. **Expected:** Field reverts to the previous value.

**Pass / Fail:** ☐

---

### 2.3 Redo after Undo re-applies change

1. After Undo, click **Redo** (toolbar or Ctrl+Y / Ctrl+Shift+Z).
2. **Expected:** Field shows the edited value again.

**Pass / Fail:** ☐

---

### 2.4 Sync to project updates file on disk

1. Edit a field and Save.
2. Start the sync service (e.g. `scripts\launch_emulator.bat` or `start_sync_service.bat`).
3. Click **Sync to project**.
4. **Expected:** `app/src/main/res/values/strings.xml` contains the new value (or sync response reports count).

**Pass / Fail:** ☐

---

### 2.5 Auto-sync on → Save → file updates within few seconds

1. Turn on **Auto-sync to project (after each Save)** in the property panel.
2. Edit a field and click **Save**.
3. Wait ~1–2 seconds.
4. **Expected:** strings.xml (or sync response) shows the update; no duplicate rapid requests (debounced).

**Pass / Fail:** ☐

---

### 2.6 Load from project populates emulator

1. Ensure sync service is running and `strings.xml` exists.
2. Click **Load from project**.
3. **Expected:** Emulator content updates to match current strings.xml (merged into design state).

**Pass / Fail:** ☐

---

### 2.7 Cursor (or editor) open on strings.xml shows updated content after Sync

1. Open `app/src/main/res/values/strings.xml` in Cursor (or another editor).
2. In emulator: edit, Save, **Sync to project**.
3. **Expected:** File on disk updates; editor shows new content when refocused or reloaded.

**Pass / Fail:** ☐

---

### 2.8 Export design then Import restores state; Undo still works after import

1. Edit a few fields and Save.
2. Click **Export Design**, save the JSON file.
3. Change or reset some fields.
4. Click **Import Design** and select the exported file.
5. **Expected:** Emulator shows the exported state.
6. Click **Undo** (if applicable after import).
7. **Expected:** Undo behavior is consistent (e.g. no undo if import replaced state; or one undo step if import was applied as one action).

**Pass / Fail:** ☐

---

### 2.9 Copy for Cursor output includes all edited string keys

1. Edit several fields (e.g. toolbar title, Start Trip, Today's Info).
2. Click **Copy for Cursor**.
3. Paste into a text editor.
4. **Expected:** Output includes instructions or diffs for each edited string key (oor, start_trip, todays_info, etc.).

**Pass / Fail:** ☐

---

## 3. Run record

| Date | Run by | Overall pass/fail | Notes |
|------|--------|--------------------|--------|
| | | ☐ Pass ☐ Fail | |

---

*After running, update the pass/fail checkboxes and the run record. For detailed smoke steps see [EMULATOR_EDITING_SMOKE.md](EMULATOR_EDITING_SMOKE.md).*
