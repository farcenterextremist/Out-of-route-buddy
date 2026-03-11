@echo off
REM Run 30-minute pulse check from repo root.
cd /d "%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\automation\pulse_check.ps1" %*
pause
