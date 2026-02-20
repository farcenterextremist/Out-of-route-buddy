@echo off
REM Resolve Python from common locations so "python" is available in this session.
REM Use from repo root: call scripts\set_python_env.bat
REM Then use "%PYTHON%" for commands; PATH is updated for this session when a full path is found.

set "PYTHON="

REM 1. Try python on PATH (if already available)
where python >nul 2>&1
if %errorlevel% equ 0 (
  set "PYTHON=python"
  goto :done
)

REM 2. Try common install locations (LocalAppData)
if exist "%LocalAppData%\Programs\Python\Python313\python.exe" (
  set "PYTHON=%LocalAppData%\Programs\Python\Python313\python.exe"
  goto :done
)
if exist "%LocalAppData%\Programs\Python\Python312\python.exe" (
  set "PYTHON=%LocalAppData%\Programs\Python\Python312\python.exe"
  goto :done
)
if exist "%LocalAppData%\Programs\Python\Python311\python.exe" (
  set "PYTHON=%LocalAppData%\Programs\Python\Python311\python.exe"
  goto :done
)
if exist "%LocalAppData%\Programs\Python\Python310\python.exe" (
  set "PYTHON=%LocalAppData%\Programs\Python\Python310\python.exe"
  goto :done
)
if exist "%LocalAppData%\Programs\Python\Python39\python.exe" (
  set "PYTHON=%LocalAppData%\Programs\Python\Python39\python.exe"
  goto :done
)

REM 3. Program Files (system-wide install)
if exist "%ProgramFiles%\Python312\python.exe" (
  set "PYTHON=%ProgramFiles%\Python312\python.exe"
  goto :done
)
if exist "%ProgramFiles%\Python311\python.exe" (
  set "PYTHON=%ProgramFiles%\Python311\python.exe"
  goto :done
)

REM 4. Fallback: hope "python" is on PATH later
set "PYTHON=python"

:done
REM Add Python's directory to PATH for this session so "python" works in child processes
if not "%PYTHON%"=="python" (
  for %%P in ("%PYTHON%") do set "PYTHON_DIR=%%~dpP"
  set "PYTHON_DIR=%PYTHON_DIR:~0,-1%"
  set "PATH=%PYTHON_DIR%;%PATH%"
)
exit /b 0
