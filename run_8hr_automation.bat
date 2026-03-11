@echo off
REM Run 8-hour automation from repo root. Double-click or run from any folder.
cd /d "%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\automation\run_8hr_automation.ps1" %*
pause
