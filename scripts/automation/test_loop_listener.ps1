# Test Loop Listener — Verify listener is wired and functioning.
# Run from repo root: .\scripts\automation\test_loop_listener.ps1
# Simulates loop events and validates output. Exit 0 = pass, 1 = fail.

param([switch]$KeepOutput)

$ErrorActionPreference = "Stop"
$RepoRoot = $PSScriptRoot
for ($i = 0; $i -lt 2; $i++) { $RepoRoot = Split-Path -Parent $RepoRoot }
Set-Location $RepoRoot

$ListenerScript = Join-Path $RepoRoot "scripts\automation\loop_listener.ps1"
$TestEventsFile = Join-Path $RepoRoot "docs\automation\loop_events_test.jsonl"
$logDir = Split-Path -Parent $TestEventsFile
if (-not (Test-Path $logDir)) { New-Item -ItemType Directory -Path $logDir -Force | Out-Null }

$RunId = "test-$((Get-Date).ToString('yyyyMMdd-HHmmss'))"
$failCount = 0

Write-Host "Testing Improvement Loop Listener..."
Write-Host ""

# 1. Listener script exists
if (-not (Test-Path $ListenerScript)) {
    Write-Host "FAIL: loop_listener.ps1 not found at $ListenerScript"
    $failCount++
} else {
    Write-Host "PASS: loop_listener.ps1 exists"
}

# 2. Emit test events (use -OutFile for test isolation)
Remove-Item $TestEventsFile -Force -ErrorAction SilentlyContinue
$events = @(
    @{ e = "loop_start"; p = ""; n = "simulation"; m = "{}" },
    @{ e = "phase_start"; p = "0"; n = "research"; m = "{}" },
    @{ e = "phase_end"; p = "0"; n = ""; m = '{"tasks":1}' },
    @{ e = "pulse"; p = ""; n = "Phase 1"; m = '{"tests":"simulated"}' },
    @{ e = "loop_end"; p = ""; n = "Pulses=1"; m = "{}" }
)
foreach ($ev in $events) {
    $params = @{ Event = $ev.e; RunId = $RunId; OutFile = $TestEventsFile }
    if ($ev.p) { $params["Phase"] = $ev.p }
    if ($ev.n) { $params["Note"] = $ev.n }
    if ($ev.m -ne "{}") { $params["Metrics"] = $ev.m }
    & $ListenerScript @params 2>&1 | Out-Null
}

# 3. Validate output: file exists, has 5 lines, each is valid JSON
if (-not (Test-Path $TestEventsFile)) {
    Write-Host "FAIL: No events file created at $TestEventsFile"
    $failCount++
} else {
    $lines = Get-Content $TestEventsFile | Where-Object { $_.Trim() -ne "" }
    if ($lines.Count -lt 5) {
        Write-Host "FAIL: Expected at least 5 event lines, got $($lines.Count)"
        $failCount++
    } else {
        Write-Host "PASS: $($lines.Count) events recorded"
    }
    $parseErrors = 0
    foreach ($line in $lines) {
        try {
            $null = $line | ConvertFrom-Json
        } catch {
            $parseErrors++
        }
    }
    if ($parseErrors -gt 0) {
        Write-Host "FAIL: $parseErrors lines are not valid JSON"
        $failCount++
    } else {
        Write-Host "PASS: All lines valid JSON"
    }
}

# 4. Verify event types recorded
$lines = Get-Content $TestEventsFile -ErrorAction SilentlyContinue | Where-Object { $_.Trim() -ne "" }
$hasStart = ($lines | ForEach-Object { ($_ | ConvertFrom-Json).event } | Where-Object { $_ -eq "loop_start" }).Count -gt 0
$hasEnd = ($lines | ForEach-Object { ($_ | ConvertFrom-Json).event } | Where-Object { $_ -eq "loop_end" }).Count -gt 0
if (-not $hasStart -or -not $hasEnd) {
    Write-Host "FAIL: Missing loop_start or loop_end event"
    $failCount++
} else {
    Write-Host "PASS: loop_start and loop_end present"
}

# Cleanup
if (-not $KeepOutput) {
    Remove-Item $TestEventsFile -Force -ErrorAction SilentlyContinue
}

Write-Host ""
if ($failCount -eq 0) {
    Write-Host "All listener tests passed."
    exit 0
} else {
    Write-Host "FAIL: $failCount test(s) failed."
    exit 1
}
