@echo off
REM Send a preset "big change" email. Tries PowerShell first (no Python needed), then Python.
REM Usage: send_phase_completion_email.bat PRESET
REM Example: send_phase_completion_email.bat phase_abc
set "SCRIPT_DIR=%~dp0"
cd /d "%SCRIPT_DIR%"
if "%~1"=="" (
  echo Usage: %~nx0 PRESET
  echo Example: %~nx0 phase_abc
  exit /b 1
)
REM 1. Try PowerShell (built into Windows, same .env)
powershell -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT_DIR%send_phase_completion_email.ps1" -Preset "%~1"
if %errorlevel% equ 0 exit /b 0
REM 2. Fall back to Python
set "REPO_ROOT=%SCRIPT_DIR%..\.."
call "%REPO_ROOT%\scripts\set_python_env.bat"
"%PYTHON%" "%SCRIPT_DIR%send_phase_completion_email.py" %*
if errorlevel 1 pause
exit /b %errorlevel%
