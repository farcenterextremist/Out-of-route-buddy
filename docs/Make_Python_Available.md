# Making Python available

Several project scripts need Python (sync service, coordinator email, emulator server). Use one of the options below so `python` works in your environment.

---

## Option 1: Add Python to PATH permanently (recommended)

Run this **once** from the repo root (PowerShell):

```powershell
powershell -ExecutionPolicy Bypass -File scripts\ensure_python_on_path.ps1
```

The script looks for Python in standard install locations (e.g. `%LocalAppData%\Programs\Python\Python312`) and adds that folder to your **user** PATH. Open a **new** terminal (or restart Cursor) afterward so `python` is available everywhere.

---

## Option 2: Install Python and check “Add to PATH”

1. Download [Python from python.org](https://www.python.org/downloads/).
2. Run the installer and **check “Add Python to PATH”** at the bottom.
3. Finish installation and open a new terminal.

Then `python` (and `pip`) work in any new command prompt or PowerShell window.

---

## Option 3: Use the project’s Python resolver (current session only)

Batch files in this repo already resolve Python from common locations (e.g. `scripts\emulator-sync-service\start_sync_service.bat`, `scripts\start_emulator_server.bat`). They try:

- `%LocalAppData%\Programs\Python\Python312\python.exe` (and 311, 310, 39)
- `%ProgramFiles%\Python312\python.exe` (and 311)
- then `python` on PATH

So the **desktop shortcut** and **launch_emulator.bat** should find Python without PATH if it’s installed in one of those folders.

To make `python` available in **your** current session from repo root:

```bat
call scripts\set_python_env.bat
```

Then `%PYTHON%` is set and Python’s directory is prepended to PATH for that command window. Use `"%PYTHON%"` in your own batch files if you call them from that window.

---

## Verify

In a **new** terminal (after Option 1 or 2):

```bat
python --version
```

You should see something like `Python 3.12.x`. Then you can run e.g.:

```bat
python scripts/coordinator-email/send_email.py "Test" "Body"
```

---

## If Python is in a different folder

Edit the path in:

- **scripts\emulator-sync-service\start_sync_service.bat** — set `PYTHON=` to your `python.exe` path.
- **scripts\start_emulator_server.bat** — same.

Or add that folder to your user PATH (Option 1 or 2) so no script edits are needed.
