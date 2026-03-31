# Test the readonly loop shared-state audit against isolated fixtures.
# Run from repo root: .\scripts\automation\test_loop_shared_state_audit.ps1

$ErrorActionPreference = "Stop"

$RepoRoot = $PSScriptRoot
for ($i = 0; $i -lt 2; $i++) { $RepoRoot = Split-Path -Parent $RepoRoot }
Set-Location $RepoRoot

$failCount = 0

function Fail([string]$Message) {
    Write-Host "FAIL: $Message"
    $script:failCount++
}

function Pass([string]$Message) {
    Write-Host "PASS: $Message"
}

$auditScript = Join-Path $RepoRoot "scripts\automation\audit_loop_shared_state.ps1"
$tmpRoot = Join-Path $RepoRoot "docs\automation\_continuity_test_tmp\audit"
$eventsFile = Join-Path $tmpRoot "loop_shared_events.audit.test.jsonl"
$latestDir = Join-Path $tmpRoot "loop_latest"
$reportPath = Join-Path $tmpRoot "audit_report.txt"
$summaryRelative = "docs/automation/_continuity_test_tmp/audit/improvement-summary.md"
$summaryPath = Join-Path $RepoRoot $summaryRelative

Remove-Item $tmpRoot -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Path $latestDir -Force | Out-Null
Set-Content -Path $summaryPath -Value "# audit summary`n" -Encoding UTF8

if (-not (Test-Path $auditScript)) {
    Fail "audit_loop_shared_state.ps1 not found"
}

$goodEvent = '{"ts":"2026-03-16T10:00:00Z","loop":"improvement","event":"finished","run_id":"run-audit-pass","summary_path":"docs/automation/_continuity_test_tmp/audit/improvement-summary.md","next_steps":["keep going"]}'
Set-Content -Path $eventsFile -Value $goodEvent -Encoding UTF8

Set-Content -Path (Join-Path $latestDir "improvement.json") -Value (@'
{
  "loop": "improvement",
  "last_run_ts": "2026-03-16T10:00:00Z",
  "last_run_id": "run-audit-pass",
  "run_id": "run-audit-pass",
  "summary_path": "docs/automation/_continuity_test_tmp/audit/improvement-summary.md",
  "suggested_next_steps": ["keep going"]
}
'@) -Encoding UTF8

foreach ($loop in @("token", "cyber", "synthetic_data", "file_organizer")) {
    Set-Content -Path (Join-Path $latestDir "$loop.json") -Value (@"
{
  "loop": "$loop",
  "last_run_ts": null,
  "last_run_id": null,
  "summary_path": null,
  "suggested_next_steps": []
}
"@) -Encoding UTF8
}

& $auditScript -EventsFile $eventsFile -LatestDir $latestDir -ReportPath $reportPath
if ($LASTEXITCODE -ne 0) {
    Fail "Audit should pass on matching fixtures"
} else {
    Pass "Audit passes on matching fixtures"
}

Set-Content -Path (Join-Path $latestDir "improvement.json") -Value (@'
{
  "loop": "improvement",
  "last_run_ts": "2026-03-16T10:00:00Z",
  "last_run_id": "run-audit-stale",
  "run_id": "run-audit-stale",
  "summary_path": "docs/automation/_continuity_test_tmp/audit/improvement-summary.md",
  "suggested_next_steps": ["keep going"]
}
'@) -Encoding UTF8

& $auditScript -EventsFile $eventsFile -LatestDir $latestDir -ReportPath $reportPath
if ($LASTEXITCODE -eq 0) {
    Fail "Audit should fail on stale latest-state identifiers"
} else {
    Pass "Audit fails on stale latest-state identifiers"
}

Remove-Item $tmpRoot -Recurse -Force -ErrorAction SilentlyContinue

Write-Host ""
if ($failCount -eq 0) {
    Write-Host "All shared-state audit tests passed."
    exit 0
} else {
    Write-Host "FAIL: $failCount shared-state audit test(s) failed."
    exit 1
}
