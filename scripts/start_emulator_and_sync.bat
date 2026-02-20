@echo off
title OutOfRouteBuddy Emulator + Sync
set "REPO_ROOT=%~dp0.."
cd /d "%REPO_ROOT%"

:: Start sync service in a new window
start "OORB Sync Service" cmd /k "call scripts\emulator-sync-service\start_sync_service.bat"

:: Start emulator server in another window (so it keeps running)
start "OORB Emulator Server" cmd /k "cd /d "%REPO_ROOT%" && python -m http.server 3000 -d phone-emulator"

:: Give servers a moment to start
timeout /t 2 /nobreak >nul

:: Open browser to the emulator
start http://localhost:3000

echo.
echo Emulator should open in your browser at http://localhost:3000
echo Sync service and emulator server are running in the other two windows.
echo Close those windows when you're done to stop them.
echo.
pause
