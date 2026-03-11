@echo off
REM Run instrumented tests for Statistics and Stat Card.
REM Clears app data first, then runs connectedDebugAndroidTest.
REM Usage: scripts\run-stats-stat-card-instrumented-tests.bat

cd /d "%~dp0\.."
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0run-stats-stat-card-instrumented-tests.ps1"
exit /b %ERRORLEVEL%
