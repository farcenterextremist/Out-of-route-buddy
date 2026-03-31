# Standardized loop start preflight.
# Run from repo root:
# .\scripts\automation\start_loop_run.ps1 -Loop improvement -EmitStartEvent

param(
    [Parameter(Mandatory = $true)]
    [ValidateSet("improvement", "token", "cyber", "cyber_security", "synthetic_data", "file_organizer")]
    [string]$Loop,

    [string]$RunId = "",
    [string]$Note = "",
    [switch]$EmitStartEvent,
    [string]$ContextDir = "",
    [string]$ContextFile = "",
    [string]$ListenerOutFile = ""
)

$ErrorActionPreference = "Stop"
. (Join-Path $PSScriptRoot "loop_run_contract.ps1")

function Get-LoopRunPrefix {
    param([string]$CanonicalLoop)

    switch ($CanonicalLoop) {
        "token" { return "token" }
        "improvement" { return "run" }
        default { return $CanonicalLoop }
    }
}

$RepoRoot = Get-LoopAutomationRepoRoot -ScriptRoot $PSScriptRoot
Set-Location $RepoRoot

$CanonicalLoop = Resolve-LoopCanonicalName -Loop $Loop
if (-not $RunId) {
    $RunId = New-LoopRunId -Prefix (Get-LoopRunPrefix -CanonicalLoop $CanonicalLoop)
}

$requiredDocs = @(
    "docs\automation\LOOP_GATES.md",
    "docs\automation\LOOP_DYNAMIC_SHARING.md",
    "docs\automation\LOOP_CONSISTENCY_STANDARD.md"
)

foreach ($relativePath in $requiredDocs) {
    $fullPath = Join-Path $RepoRoot $relativePath
    if (-not (Test-Path $fullPath)) {
        throw "Missing required loop contract file: $relativePath"
    }
}

$eventsFile = Join-Path $RepoRoot "docs\automation\loop_shared_events.jsonl"
$latestFile = Get-LoopLatestStatePath -RepoRoot $RepoRoot -Loop $CanonicalLoop
$latestDir = Split-Path -Parent $latestFile
if (-not (Test-Path $latestDir)) {
    New-Item -ItemType Directory -Path $latestDir -Force | Out-Null
}

if (-not $ContextDir) {
    $ContextDir = Join-Path $RepoRoot "docs\automation\_loop_run_context"
}
if (-not (Test-Path $ContextDir)) {
    New-Item -ItemType Directory -Path $ContextDir -Force | Out-Null
}
if (-not $ContextFile) {
    $ContextFile = Join-Path $ContextDir "$CanonicalLoop-$RunId.json"
}

$startedAt = (Get-Date).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ssZ")
$context = [ordered]@{
    loop = $CanonicalLoop
    run_id = $RunId
    started_at = $startedAt
    events_file = $eventsFile
    latest_file = $latestFile
    required_docs = $requiredDocs
}
if ($Note) {
    $context["note"] = $Note
}

$contextJson = $context | ConvertTo-Json -Depth 10
Set-Content -Path $ContextFile -Value $contextJson -Encoding UTF8

if ($EmitStartEvent) {
    switch ($CanonicalLoop) {
        "improvement" {
            $listenerScript = Join-Path $RepoRoot "scripts\automation\loop_listener.ps1"
            $listenerParams = @{
                Event = "loop_start"
                RunId = $RunId
                Note = $Note
            }
            if ($ListenerOutFile) {
                $listenerParams["OutFile"] = $ListenerOutFile
            }
            & $listenerScript @listenerParams
        }
        "token" {
            $listenerScript = Join-Path $RepoRoot "scripts\automation\token_loop_listener.ps1"
            $listenerParams = @{
                Event = "token_loop_start"
                RunId = $RunId
                Note = $Note
            }
            if ($ListenerOutFile) {
                $listenerParams["OutFile"] = $ListenerOutFile
            }
            & $listenerScript @listenerParams
        }
        default {
            Write-Host "INFO: No specialized start listener is defined for '$CanonicalLoop'. Context file recorded without event emission."
        }
    }
}

Write-Host "Loop start preflight ready for '$CanonicalLoop' -> RunId: $RunId"
Write-Host "Context file: $ContextFile"

[PSCustomObject]$context
