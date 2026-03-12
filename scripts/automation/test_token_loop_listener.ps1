# Test Token Loop Listener — Verify token loop listener is wired and functioning.
# Run from repo root: .\scripts\automation\test_token_loop_listener.ps1
# Simulates token loop events and validates output. Exit 0 = pass, 1 = fail.

param([switch]$KeepOutput)

$ErrorActionPreference = "Stop"
$RepoRoot = $PSScriptRoot
for ($i = 0; $i -lt 2; $i++) { $RepoRoot = Split-Path -Parent $RepoRoot }
Set-Location $RepoRoot

$ListenerScript = Join-Path $RepoRoot "scripts\automation\token_loop_listener.ps1"
$TestEventsFile = Join-Path $RepoRoot "docs\automation\token_loop_events_test.jsonl"
$logDir = Split-Path -Parent $TestEventsFile
if (-not (Test-Path $logDir)) { New-Item -ItemType Directory -Path $logDir -Force | Out-Null }

$RunId = "token-test-$((Get-Date).ToString('yyyyMMdd-HHmmss'))"
$failCount = 0

Write-Host "Testing Token Reduction Loop Listener..."
Write-Host ""

# 1. Listener script exists
if (-not (Test-Path $ListenerScript)) {
    Write-Host "FAIL: token_loop_listener.ps1 not found at $ListenerScript"
    $failCount++
} else {
    Write-Host "PASS: token_loop_listener.ps1 exists"
}

# 2. Emit test events (use -OutFile for test isolation)
Remove-Item $TestEventsFile -Force -ErrorAction SilentlyContinue
$events = @(
    @{ e = "token_loop_start"; s = ""; n = "simulation"; m = "{}" },
    @{ e = "step_start";       s = "1"; n = "audit rules"; m = "{}" },
    @{ e = "step_end";         s = "1"; n = ""; m = '{"always_apply_count":1,"always_apply_lines":57}' },
    @{ e = "step_start";       s = "2"; n = "check new always-apply"; m = "{}" },
    @{ e = "step_end";         s = "2"; n = ""; m = "{}" },
    @{ e = "token_loop_end";   s = ""; n = "steps=2"; m = '{"steps_completed":2}' }
)
foreach ($ev in $events) {
    $params = @{ Event = $ev.e; RunId = $RunId; OutFile = $TestEventsFile }
    if ($ev.s) { $params["Step"] = $ev.s }
    if ($ev.n) { $params["Note"] = $ev.n }
    if ($ev.m -ne "{}") { $params["Metrics"] = $ev.m }
    & $ListenerScript @params 2>&1 | Out-Null
}

# 3. Validate output: file exists, expected line count, each line valid JSON
$expectedLines = $events.Count
if (-not (Test-Path $TestEventsFile)) {
    Write-Host "FAIL: No events file created at $TestEventsFile"
    $failCount++
} else {
    $lines = Get-Content $TestEventsFile | Where-Object { $_.Trim() -ne "" }
    if ($lines.Count -lt $expectedLines) {
        Write-Host "FAIL: Expected at least $expectedLines event lines, got $($lines.Count)"
        $failCount++
    } else {
        Write-Host "PASS: $($lines.Count) events recorded"
    }
    $parseErrors = 0
    foreach ($line in $lines) {
        try {
            $obj = $line | ConvertFrom-Json
            if (-not $obj.event) { $parseErrors++ }
        } catch {
            $parseErrors++
        }
    }
    if ($parseErrors -gt 0) {
        Write-Host "FAIL: $parseErrors lines invalid or missing event"
        $failCount++
    } else {
        Write-Host "PASS: All lines valid JSON with event"
    }
}

# 4. Verify token-loop event types
$lines = Get-Content $TestEventsFile -ErrorAction SilentlyContinue | Where-Object { $_.Trim() -ne "" }
$hasStart = ($lines | ForEach-Object { ($_ | ConvertFrom-Json).event } | Where-Object { $_ -eq "token_loop_start" }).Count -gt 0
$hasEnd = ($lines | ForEach-Object { ($_ | ConvertFrom-Json).event } | Where-Object { $_ -eq "token_loop_end" }).Count -gt 0
if (-not $hasStart -or -not $hasEnd) {
    Write-Host "FAIL: Missing token_loop_start or token_loop_end event"
    $failCount++
} else {
    Write-Host "PASS: token_loop_start and token_loop_end present"
}

# 5. Verify step events have step field when expected
$stepEvents = $lines | ForEach-Object { $o = $_ | ConvertFrom-Json; if ($o.event -eq "step_start" -or $o.event -eq "step_end") { $o } }
$stepEventsWithStep = $stepEvents | Where-Object { $_.step }
if ($stepEvents.Count -gt 0 -and $stepEventsWithStep.Count -lt $stepEvents.Count) {
    Write-Host "FAIL: Some step events missing step field"
    $failCount++
} else {
    Write-Host "PASS: Step events have step field"
}

# 6. Verify step_end with metrics produces parsed metrics object (not only metrics_raw)
$stepEndWithMetrics = $lines | ForEach-Object { $o = $_ | ConvertFrom-Json; if ($o.event -eq "step_end" -and (Get-Member -InputObject $o -Name "metrics" -MemberType Properties -ErrorAction SilentlyContinue)) { $o } } | Select-Object -First 1
if (-not $stepEndWithMetrics -or -not $stepEndWithMetrics.metrics) {
    Write-Host "FAIL: step_end with metrics did not produce parsed metrics object (check for metrics_raw only)"
    $failCount++
} else {
    $m = $stepEndWithMetrics.metrics
    $hasCount = (Get-Member -InputObject $m -Name "always_apply_count" -MemberType Properties -ErrorAction SilentlyContinue)
    $hasLines = (Get-Member -InputObject $m -Name "always_apply_lines" -MemberType Properties -ErrorAction SilentlyContinue)
    if (-not $hasCount -or -not $hasLines) {
        Write-Host "FAIL: metrics object missing always_apply_count or always_apply_lines"
        $failCount++
    } else {
        Write-Host "PASS: step_end metrics parsed (always_apply_count, always_apply_lines)"
    }
}

# Cleanup
if (-not $KeepOutput) {
    Remove-Item $TestEventsFile -Force -ErrorAction SilentlyContinue
}

Write-Host ""
if ($failCount -eq 0) {
    Write-Host "All token loop listener tests passed."
    exit 0
} else {
    Write-Host "FAIL: $failCount test(s) failed."
    exit 1
}
