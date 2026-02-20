@echo off
title OORB Emulator Server
set "REPO=%~dp0.."
cd /d "%REPO%"
call "%REPO%\scripts\set_python_env.bat"
echo Serving emulator at http://localhost:3000
echo Close this window to stop the server.
echo.
"%PYTHON%" -m http.server 3000 -d phone-emulator
pause
