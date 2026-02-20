# Emulator Sync Service

Pushes emulator design edits to the project codebase so you don’t have to copy-paste. When the service is running, use **Sync to project** in the emulator (or enable **Auto-sync to project** in the property panel) to write changes to `app/src/main/res/values/strings.xml`. Cursor sees file changes as soon as they’re written.

## Quick start (one-click)

**Desktop shortcut:** Double-click **"OutOfRouteBuddy Emulator"** on your desktop. It starts the sync service, the emulator server, and opens your browser to http://localhost:3000. Two console windows will stay open (sync + server); close them when you’re done.

To create or refresh the shortcut, run from the repo root:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\emulator-sync-service\create_desktop_shortcut.ps1
```

The shortcut uses a **stylized icon** (custom `icon.ico` in this folder if present, else a system icon). To generate a custom icon: `pip install Pillow`, then `python scripts/emulator-sync-service/create_icon.py`, then re-run the shortcut script above.

## Manual start (sync service only)

If you only want to start the sync service (e.g. you already have the emulator open):

```bat
scripts\emulator-sync-service\start_sync_service.bat
```

Leave the window open. You should see: `Listening on http://127.0.0.1:8765/sync`. In the emulator, make your edits and click **Sync to project** (or enable Auto-sync in the property panel).

## How it works

- The service runs a small HTTP server on **port 8765** (configurable via `OORB_SYNC_PORT`).
- The emulator sends a POST to `http://127.0.0.1:8765/sync` with the current design JSON.
- The service uses the same mapping as **Copy for Cursor** and updates (or creates) `app/src/main/res/values/strings.xml` under the project root.
- Project root is set automatically when you run `start_sync_service.bat` (parent of `scripts/`). You can override with `OORB_PROJECT_ROOT`.

## Requirements

- Python 3.6+ (stdlib only; no extra packages).
- Project root must contain (or will get) `app/src/main/res/values/strings.xml`.

## Optional: custom sync URL

In the emulator, you can set a different sync URL (e.g. different port) in the browser console:

```js
localStorage.setItem('oorb-sync-url', 'http://127.0.0.1:8765/sync');
```

Then reload the emulator page.
