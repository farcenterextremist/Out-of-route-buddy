@echo off
title OutOfRouteBuddy Emulator Launcher
set "REPO_ROOT=%~dp0.."
cd /d "%REPO_ROOT%"

echo Starting OutOfRouteBuddy Emulator...
echo.

REM 1. Start sync service (writes edits to strings.xml)
start "OORB Sync Service" cmd /k "call scripts\emulator-sync-service\start_sync_service.bat"

REM 2. Start emulator HTTP server
start "OORB Emulator Server" cmd /k "call scripts\start_emulator_server.bat"

REM 3. Wait for servers to bind
timeout /t 3 /nobreak >nul

REM 4. Open browser to emulator
start http://localhost:3000

echo.
echo Emulator opened at http://localhost:3000
echo Sync service and server run in the other two windows. Close them when done.
echo.
pause
