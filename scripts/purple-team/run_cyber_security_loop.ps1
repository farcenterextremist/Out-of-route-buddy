# Cyber Security Loop — Orchestrator
# Run from repo root: .\scripts\purple-team\run_cyber_security_loop.ps1
# Invokes loop listener at phase boundaries; runs security simulations.

$ErrorActionPreference = "Stop"
$RepoRoot = $PSScriptRoot
for ($i = 0; $i -lt 2; $i++) { $RepoRoot = Split-Path -Parent $RepoRoot }
Set-Location $RepoRoot

$ListenerScript = Join-Path $RepoRoot "scripts\automation\loop_listener.ps1"

# Phase 0: Research (optional - user can run manually)
Write-Host "Cyber Security Loop — Phase 0: Research (optional)"
Write-Host "  See docs/automation/CYBER_SECURITY_RESEARCH.md"

# Phase 1: Simulate
Write-Host "Cyber Security Loop — Phase 1: Simulate"
& $ListenerScript -Event "cyber_security_simulate" -Note "Phase 1 start"
./gradlew.bat :app:securitySimulations
$simExit = $LASTEXITCODE
$metrics = '{"phase":"1","simulations":"run"}'
& $ListenerScript -Event "cyber_security_simulate_result" -Note "Phase 1 end" -Metrics $metrics
if ($simExit -ne 0) { exit $simExit }

Write-Host "Cyber Security Loop complete. Check docs/agents/data-sets/security-exercises/artifacts/"
