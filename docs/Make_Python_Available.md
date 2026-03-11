# Making Python available

Some utility scripts in this repo use Python. Use one of the options below so `python` works in your environment.

---

## Option 1: Add Python to PATH permanently

Run this once from the repo root:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\ensure_python_on_path.ps1
```

Open a new terminal afterward so the updated PATH is picked up.

---

## Option 2: Install Python and check "Add Python to PATH"

1. Download [Python from python.org](https://www.python.org/downloads/).
2. Run the installer and check **Add Python to PATH**.
3. Open a new terminal when installation finishes.

---

## Option 3: Set Python for the current shell only

From the repo root:

```bat
call scripts\set_python_env.bat
```

This sets `%PYTHON%` and prepends Python's directory to PATH for the current command window.

---

## Verify

```bat
python --version
```

You should see something like `Python 3.12.x`.
