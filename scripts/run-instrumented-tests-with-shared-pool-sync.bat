@echo off
REM Run instrumented tests and then sync the shared pool.
REM Usage: scripts\run-instrumented-tests-with-shared-pool-sync.bat

cd /d "%~dp0\.."
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0run-instrumented-tests-with-shared-pool-sync.ps1"
exit /b %ERRORLEVEL%
