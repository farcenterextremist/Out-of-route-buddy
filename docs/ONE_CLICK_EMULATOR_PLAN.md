# One-Click Desktop Icon → Emulator + Real-Time Sync to Cursor

**Goal:** Double-click one desktop icon → phone emulator/editing tool opens in the browser; edits sync to the project so Cursor sees updates in (near) real time.

**Timebox:** 2 hours brainstorming/consulting/research → 1 hour execution.

---

## Part 1: Problem statement and success criteria

### What “working” means

1. **One click:** User double-clicks the desktop icon (“OutOfRouteBuddy Emulator” or similar).
2. **Emulator opens:** The phone emulator/editing UI opens in the default browser (no manual navigation, no opening a second app first).
3. **Edits reach the project:** When the user edits in the emulator and syncs (button or auto), `app/src/main/res/values/strings.xml` (and any other mapped files) are updated on disk.
4. **Cursor sees changes:** Cursor IDE shows the updated file (either automatically on file change or after focus/refresh).

### What’s failing today

- **Desktop icon** currently runs only **start_sync_service.bat** (sync service on port 8765). It does **not** start the emulator server or open the browser, so the user never “gets to” the emulator from that icon.
- **start_emulator_and_sync.bat** does the full flow (sync + emulator server + open browser) but is **not** what the desktop shortcut points to, and it may still have environment issues when run from a shortcut (e.g. Python not found in the child `cmd` that runs the HTTP server).
- **Real-time:** Today the user must click “Sync to project” for changes to hit the repo; Cursor already picks up file changes once the file is written.

---

## Part 2: Root-cause analysis

| Issue | Cause |
|-------|--------|
| Desktop icon doesn’t open emulator | Shortcut Target = `start_sync_service.bat` only. No step opens the emulator UI. |
| “Start in” / working directory | Shortcut WorkingDirectory = `scripts\emulator-sync-service`. When we run `start_emulator_and_sync.bat` from a shortcut, we must set WorkingDirectory to **repo root** so `scripts\` and `phone-emulator\` resolve. |
| Python not found in child windows | When a shortcut runs a .bat, PATH is often minimal. Child `cmd /k "python -m http.server ..."` may not see Python. Use the **same** full path to `python.exe` for both sync and emulator server. |
| Browser opens before server is ready | If we `start http://localhost:3000` before the HTTP server is listening, the first load can fail. Start both servers first, short delay, then open browser. |
| Real-time to Cursor | Files are written by the sync service; Cursor detects changes on disk. “Real time” = reduce delay from “edit” to “file on disk” (e.g. auto-sync with debounce so user doesn’t have to click Sync every time). |

---

## Part 3: Research and options

### 3.1 How to open the emulator

- **Option A – HTTP server (current):** Run `python -m http.server 3000 -d phone-emulator`, then open `http://localhost:3000`. Pro: works from any origin; sync service (localhost:8765) is same-origin for fetch. Con: need Python and a second process.
- **Option B – file://:** Open `phone-emulator\index.html` via `start "" "path\to\index.html"`. Pro: no server. Con: many browsers block or restrict `fetch()` from `file://` to `localhost` (CORS / mixed content). Sync would likely fail.
- **Conclusion:** Keep serving the emulator over HTTP (localhost:3000) so “Sync to project” works reliably.

### 3.2 One script vs two

- **One script (recommended):** One batch file (e.g. `launch_emulator.bat` at repo root or in `scripts/`) that: (1) starts sync service in a visible/minimized window, (2) starts emulator HTTP server in another window, (3) waits 2–3 s, (4) opens `http://localhost:3000`. Desktop shortcut points at this script with “Start in” = repo root.
- **Two shortcuts:** One for “Start sync”, one for “Open emulator in browser” — more clicks and confusion. Rejected.

### 3.3 Where the script lives and shortcut target

- **Shortcut Target:** Full path to a single launch script, e.g. `…\Out-of-route-buddy\scripts\launch_emulator.bat` (or `start_emulator_and_sync.bat`).
- **Shortcut “Start in”:** Repo root, e.g. `…\Out-of-route-buddy`. All script paths (e.g. `scripts\emulator-sync-service\start_sync_service.bat`, `phone-emulator`) are then relative to repo root.

### 3.4 Python in child processes

- When the launch script does `start cmd /k "cd /d "%REPO_ROOT%" && python -m http.server 3000 -d phone-emulator"`, the child `cmd` inherits the **parent’s** environment. If the parent was started from a shortcut, `python` may not be in PATH.
- **Fix:** Resolve Python once in the launch script (e.g. `%LocalAppData%\Programs\Python\Python312\python.exe` or fallback `python`), set a `PYTHON=` variable, and use `"%PYTHON%"` in **both** the sync-service call and the emulator-server command (e.g. pass it into the child or use a small helper .bat that uses the same logic).

### 3.5 Real-time sync to Cursor

- **Current:** User clicks “Sync to project” → POST to 8765 → sync service writes `strings.xml` → Cursor sees the change when the file is updated (and possibly refocused).
- **Improvement:** Add optional **auto-sync** in the emulator: on design-state change (e.g. after save in property panel or after debounced input), POST to the sync service so the file is updated without a manual “Sync to project” click. Keep the button for on-demand sync. Cursor doesn’t need special handling; it already reacts to file changes.

---

## Part 4: Recommended architecture

### 4.1 Single launch script (e.g. `scripts/launch_emulator.bat`)

1. **Resolve repo root:** `%~dp0..` if script is in `scripts/`, then `cd /d "%REPO_ROOT%"`.
2. **Resolve Python:** `set "PYTHON=%LocalAppData%\Programs\Python\Python312\python.exe"`, then `if not exist "%PYTHON%" set "PYTHON=python"`.
3. **Start sync service:** `start "OORB Sync" cmd /k "call scripts\emulator-sync-service\start_sync_service.bat"`. (That batch already uses its own Python resolution; no change needed there if it works.)
4. **Start emulator server:** Use the **same** `PYTHON` so the child has a working Python. E.g. `start "OORB Emulator" cmd /k "cd /d "%REPO_ROOT%" && "%PYTHON%" -m http.server 3000 -d phone-emulator"`. Ensure quoting is correct for paths with spaces.
5. **Wait:** `timeout /t 3 /nobreak >nul`.
6. **Open browser:** `start http://localhost:3000`.
7. **Message:** Echo “Emulator at http://localhost:3000. Close the other two windows to stop.”

### 4.2 Desktop shortcut (updated)

- **Target:** `…\Out-of-route-buddy\scripts\launch_emulator.bat` (or `start_emulator_and_sync.bat` if we keep that name).
- **Start in:** `…\Out-of-route-buddy` (repo root).
- **Icon:** Keep current (custom or shell32).
- **Create/update:** PowerShell script that creates this shortcut (e.g. `create_desktop_shortcut.ps1`) so “one click” means “run launch_emulator.bat from repo root.”

### 4.3 Real-time sync (optional, in-emulator)

- In the emulator JS, after `updateDesign()` or after a “Save” in the property panel (and optionally on a debounced timer when the user types), call the same POST logic as “Sync to project” (with a short debounce, e.g. 1–2 s). Option: make it configurable (e.g. “Auto-sync to project” checkbox) so the user can choose manual vs automatic. Cursor will see changes as soon as the sync service writes the file.

---

## Part 5: Execution plan (1 hour)

### Step 1 – Single launch script (25 min)

- Create or rename to **`scripts/launch_emulator.bat`**.
- Implement: resolve repo root from `%~dp0`, resolve Python (full path + fallback), start sync in new window, start emulator server in new window using same Python, 3 s delay, open browser, echo instructions.
- Test by double-clicking the .bat from File Explorer (from repo root and from another folder) and confirm: two windows (sync + server), browser opens to emulator, “Sync to project” works.

### Step 2 – Desktop shortcut (15 min)

- Update **`scripts/emulator-sync-service/create_desktop_shortcut.ps1`** (or equivalent):
  - Shortcut **TargetPath** = full path to `scripts\launch_emulator.bat`.
  - Shortcut **WorkingDirectory** = repo root (parent of `scripts`).
  - Keep icon and description.
- Run the script to create/update the desktop shortcut.
- Test: double-click the **desktop icon** only; confirm emulator opens and sync works.

### Step 3 – Robustness (10 min)

- In `launch_emulator.bat`, if the emulator server window fails (e.g. Python not found), add a one-line hint: “Edit PYTHON= in this script if Python is elsewhere.”
- Optionally: in the sync-service batch, ensure it does not depend on “Start in” being repo root (it already uses `%~dp0` to find itself and then `cd ..\..` to repo root). No change if already correct.

### Step 4 – Real-time sync (optional, 10 min)

- In the emulator, add optional auto-sync: after “Save” in the property panel (and optionally on a debounced timer for live preview), call the sync URL. Add a small “Auto-sync” toggle and store preference in localStorage. Document in README or in-app hint.

### Step 5 – Documentation (5 min)

- Update **phone-emulator/README.md** and **scripts/emulator-sync-service/README.md**: “To open the emulator with one click, use the desktop shortcut (run create_desktop_shortcut.ps1 once to create it). It starts the sync service, the emulator server, and opens your browser to http://localhost:3000.”
- Add a one-line note in **docs/ONE_CLICK_EMULATOR_PLAN.md**: “Implemented: [date]. Shortcut target = launch_emulator.bat; Start in = repo root.”

---

## Part 6: Implementation summary

**Implemented:** Plan executed so one desktop icon opens the emulator and edits can sync to Cursor.

- **`scripts/launch_emulator.bat`** – Single entry point: starts sync service window, starts emulator server window, waits 3 s, opens http://localhost:3000. Uses `scripts/start_emulator_server.bat` so Python path is resolved in a child batch (no quoting issues).
- **`scripts/start_emulator_server.bat`** – Helper: cd to repo root, resolve Python, run `python -m http.server 3000 -d phone-emulator`.
- **Desktop shortcut** – Created by `scripts/emulator-sync-service/create_desktop_shortcut.ps1`: **Target** = `scripts\launch_emulator.bat`, **Start in** = repo root. Shortcut name: **"OutOfRouteBuddy Emulator"**. Re-run the PowerShell script to create or refresh the shortcut.
- **Auto-sync** – In the emulator property panel: "Auto-sync to project (after Save, update Cursor files)". When checked, each Save also POSTs to the sync service so `strings.xml` is updated and Cursor sees the change without clicking "Sync to project".

**How to use:** Double-click the desktop icon **"OutOfRouteBuddy Emulator"**. Two console windows (sync + server) and the browser will open. Keep the two windows open while editing. Enable "Auto-sync to project" in the property panel if you want edits to push to the project after every Save.

---

## Part 7: Out of scope (for this plan)

- Changing Cursor itself (no plugin required; file-on-disk is enough).
- Supporting non-Windows (same architecture can be adapted with shell scripts later).
- File watching / hot reload inside the Android app (this plan is emulator → strings.xml → Cursor only).

---

## Summary

- **Why it wasn’t working:** The desktop icon ran only the sync service and never started the emulator or opened the browser; the “full” script wasn’t wired to the shortcut and had environment risks (Python path, working directory).
- **Fix:** One launch script that starts sync + emulator server (with reliable Python) and opens the browser; desktop shortcut points at that script with “Start in” = repo root. Optional: auto-sync from emulator so Cursor sees edits in near real time without clicking “Sync to project” every time.
