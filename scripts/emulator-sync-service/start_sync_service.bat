@echo off
title OutOfRouteBuddy Emulator Sync Service
set "SCRIPT_DIR=%~dp0"
cd /d "%SCRIPT_DIR%..\.."
set "OORB_PROJECT_ROOT=%CD%"
echo Project root: %OORB_PROJECT_ROOT%
echo.

REM Resolve Python (shared logic: scripts\set_python_env.bat)
call "%OORB_PROJECT_ROOT%\scripts\set_python_env.bat"
"%PYTHON%" "%SCRIPT_DIR%sync_service.py"
if errorlevel 1 (
  echo.
  echo Python did not run. If you use a different Python path, edit this batch file and set PYTHON= to your python.exe path.
)
pause
