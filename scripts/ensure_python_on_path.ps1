# Add Python to the current user's PATH so "python" works in new terminals.
# Run once: powershell -ExecutionPolicy Bypass -File scripts\ensure_python_on_path.ps1
# Requires: Python installed in a standard location (see paths below).

$pathsToCheck = @(
    "$env:LocalAppData\Programs\Python\Python313",
    "$env:LocalAppData\Programs\Python\Python312",
    "$env:LocalAppData\Programs\Python\Python311",
    "$env:LocalAppData\Programs\Python\Python310",
    "$env:LocalAppData\Programs\Python\Python39",
    "${env:ProgramFiles}\Python312",
    "${env:ProgramFiles}\Python311"
)

$pythonDir = $null
foreach ($d in $pathsToCheck) {
    $exe = Join-Path $d "python.exe"
    if (Test-Path $exe) {
        $pythonDir = $d
        break
    }
}

if (-not $pythonDir) {
    $onPath = Get-Command python -ErrorAction SilentlyContinue
    if ($onPath) {
        Write-Host "Python is already on PATH: $($onPath.Source)" -ForegroundColor Green
        exit 0
    }
    Write-Host "Python not found in standard locations or on PATH." -ForegroundColor Red
    Write-Host "Install from https://www.python.org/downloads/ and check 'Add Python to PATH'." -ForegroundColor Yellow
    exit 1
}

$userPath = [Environment]::GetEnvironmentVariable("Path", "User")
if ($userPath -split ";" -contains $pythonDir) {
    Write-Host "Python already on user PATH: $pythonDir" -ForegroundColor Green
    exit 0
}

$newPath = "$pythonDir;$userPath"
[Environment]::SetEnvironmentVariable("Path", $newPath, "User")
Write-Host "Added to user PATH: $pythonDir" -ForegroundColor Green
Write-Host "Open a NEW terminal (or restart Cursor) for 'python' to be available." -ForegroundColor Cyan
