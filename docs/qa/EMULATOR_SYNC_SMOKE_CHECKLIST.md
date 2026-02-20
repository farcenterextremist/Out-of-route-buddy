# Emulator + Sync – smoke checklist (Improvement #16)

Use this checklist to verify the emulator and sync pipeline in a few minutes.

## Prerequisites

- Python 3.6+ (for sync service and optional one-command run).
- Optional: run **one command** from repo root:  
  `scripts\start_emulator_and_sync.bat`  
  (starts sync service in one window, emulator on http://localhost:3000 in another).

Or manually:

- Start sync service: double-click desktop shortcut **"OutOfRouteBuddy Emulator Sync"** or run `scripts\emulator-sync-service\start_sync_service.bat`.
- Open emulator: open `phone-emulator/index.html` in a browser, or run `python -m http.server 3000 -d phone-emulator` and go to http://localhost:3000.

---

## Steps

1. **Open emulator**  
   - [ ] Page loads; phone frame and toolbar are visible.  
   - [ ] Toolbar shows "OutOfRouteBuddy Emulator", hint text, and buttons (Copy for Cursor, Sync to project, etc.).

2. **Edit a field**  
   - [ ] Right-click a label or button inside the phone (e.g. "Start Trip" or "OOR").  
   - [ ] Context menu appears with "Edit".  
   - [ ] Click "Edit"; properties panel opens with Value and Notes.  
   - [ ] Change the value, click Save; phone content updates.

3. **Sync to project**  
   - [ ] With sync service running, click **Sync to project**.  
   - [ ] Toast or message indicates success (e.g. "Synced N string(s) to project") or "No changes to apply".  
   - [ ] If sync service is not running: error message suggests starting the service.

4. **Export / Import**  
   - [ ] Click **Export Design**; a JSON file downloads.  
   - [ ] Click **Import Design** and select that file (or another valid design JSON).  
   - [ ] Emulator content updates to match the imported design.

5. **Copy for Cursor**  
   - [ ] Click **Copy for Cursor**.  
   - [ ] Paste into a text editor; output contains "Apply these string changes" and/or XML snippets for `strings.xml`.

6. **Settings and End Trip**  
   - [ ] Click the **gear (Settings)** in the app toolbar; Settings modal opens (Mode, Templates, Help & Info).  
   - [ ] Start a trip (enter Loaded/Bounce miles, click Start Trip).  
   - [ ] Click **End Trip**; confirmation modal opens (End Trip / Clear Trip / Continue Trip).  
   - [ ] Choose an option; modal closes and trip state updates as expected.

7. **Statistics period**  
   - [ ] Expand Statistics, click **View**; period picker modal opens.  
   - [ ] Choose "This month" or "Custom"; modal closes and (for This month) label/value update.

---

## If something fails

- **Sync fails:** Ensure sync service is running (green dot in toolbar when reachable).  
- **Context menu or Edit not working:** Hard refresh (Ctrl+F5) and try again.  
- **Import fails:** Ensure the JSON file is a valid emulator design export.

---

*Source: Improvement #16 (QA) from EMULATOR_20_IMPROVEMENTS_FULL_TEAM_CONSULTATION.md*
