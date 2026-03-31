# Measure loop-design efficiency from the current automation state.
# Run from repo root: .\scripts\automation\measure_loop_efficiency.ps1

param(
    [ValidateSet("auto", "pass", "fail")]
    [string]$RunContractStatus = "auto",

    [ValidateSet("auto", "pass", "fail")]
    [string]$GateWrappersStatus = "auto",

    [ValidateSet("auto", "pass", "fail")]
    [string]$SharedStateAuditStatus = "auto",

    [ValidateSet("auto", "pass", "fail")]
    [string]$ContinuityStatus = "auto",

    [ValidateSet("auto", "pass", "fail")]
    [string]$HealthSignalsStatus = "auto",

    [ValidateSet("auto", "pass", "fail")]
    [string]$DocumentationStatus = "auto",

    [string]$StatePath = "",
    [switch]$Quiet,
    [switch]$PassThru
)

$ErrorActionPreference = "Stop"
. (Join-Path $PSScriptRoot "loop_run_contract.ps1")

function Resolve-BooleanStatus {
    param(
        [string]$Override,
        [scriptblock]$AutoResolver
    )

    switch ($Override) {
        "pass" { return $true }
        "fail" { return $false }
        default { return (& $AutoResolver) }
    }
}

function Test-AllPathsExist {
    param(
        [string]$RepoRoot,
        [string[]]$RelativePaths
    )

    foreach ($relativePath in $RelativePaths) {
        if (-not (Test-Path (Join-Path $RepoRoot $relativePath))) {
            return $false
        }
    }

    return $true
}

function Test-FileContainsAll {
    param(
        [string]$Path,
        [string[]]$Needles
    )

    if (-not (Test-Path $Path)) {
        return $false
    }

    $content = Get-Content -Path $Path -Raw
    foreach ($needle in $Needles) {
        if ($content -notmatch [Regex]::Escape($needle)) {
            return $false
        }
    }

    return $true
}

function Get-LatestPulseBlock {
    param([string]$Path)

    if (-not (Test-Path $Path)) {
        return $null
    }

    $content = Get-Content -Path $Path -Raw
    if (-not $content) {
        return $null
    }

    $blocks = $content -split "(?m)^---\r?\n"
    $nonEmpty = $blocks | Where-Object { -not [string]::IsNullOrWhiteSpace($_) }
    if (-not $nonEmpty) {
        return $null
    }

    return ($nonEmpty | Select-Object -Last 1).Trim()
}

function New-AsciiProgressBar {
    param(
        [int]$Percent,
        [int]$Width = 20
    )

    $bounded = [Math]::Max(0, [Math]::Min(100, $Percent))
    $filled = [Math]::Round(($bounded / 100.0) * $Width)
    $empty = $Width - $filled
    return "[" + ("#" * $filled) + ("-" * $empty) + "] $bounded%"
}

$RepoRoot = Get-LoopAutomationRepoRoot -ScriptRoot $PSScriptRoot
Set-Location $RepoRoot

if (-not $StatePath) {
    $StatePath = Join-Path $RepoRoot "docs\automation\loop_efficiency_state.json"
}

$metrics = @(
    @{
        name = "Run contract wiring"
        weight = 20
        status = Resolve-BooleanStatus -Override $RunContractStatus -AutoResolver {
            $requiredFiles = @(
                "scripts\automation\loop_run_contract.ps1",
                "scripts\automation\loop_listener.ps1",
                "scripts\automation\token_loop_listener.ps1",
                "scripts\automation\pulse_check.ps1",
                "scripts\automation\run_token_loop.ps1",
                "scripts\automation\run_120min_loop.ps1"
            )
            if (-not (Test-AllPathsExist -RepoRoot $RepoRoot -RelativePaths $requiredFiles)) {
                return $false
            }

            $wiredScripts = @(
                @{ path = "scripts\automation\loop_listener.ps1"; needles = @("loop_run_contract.ps1", "New-LoopRunId") },
                @{ path = "scripts\automation\token_loop_listener.ps1"; needles = @("loop_run_contract.ps1", "New-LoopRunId") },
                @{ path = "scripts\automation\pulse_check.ps1"; needles = @("RunId", "loop_run_contract.ps1") },
                @{ path = "scripts\automation\run_token_loop.ps1"; needles = @("loop_run_contract.ps1", "RunId") },
                @{ path = "scripts\automation\run_120min_loop.ps1"; needles = @("loop_run_contract.ps1", "RunId") }
            )

            foreach ($scriptCheck in $wiredScripts) {
                if (-not (Test-FileContainsAll -Path (Join-Path $RepoRoot $scriptCheck.path) -Needles $scriptCheck.needles)) {
                    return $false
                }
            }

            return $true
        }
    },
    @{
        name = "Gate wrappers"
        weight = 15
        status = Resolve-BooleanStatus -Override $GateWrappersStatus -AutoResolver {
            $requiredFiles = @(
                "scripts\automation\start_loop_run.ps1",
                "scripts\automation\finish_loop_run.ps1"
            )
            if (-not (Test-AllPathsExist -RepoRoot $RepoRoot -RelativePaths $requiredFiles)) {
                return $false
            }

            $docsPath = Join-Path $RepoRoot "docs\automation\LOOP_GATES.md"
            $tokenRunner = Join-Path $RepoRoot "scripts\automation\run_token_loop.ps1"
            $improvementRunner = Join-Path $RepoRoot "scripts\automation\run_120min_loop.ps1"

            return (Test-FileContainsAll -Path $docsPath -Needles @("start_loop_run.ps1", "finish_loop_run.ps1", "audit_loop_shared_state.ps1")) -and
                (Test-FileContainsAll -Path $tokenRunner -Needles @("start_loop_run.ps1", "finish_loop_run.ps1")) -and
                (Test-FileContainsAll -Path $improvementRunner -Needles @("start_loop_run.ps1", "finish_loop_run.ps1"))
        }
    },
    @{
        name = "Shared-state audit"
        weight = 20
        status = Resolve-BooleanStatus -Override $SharedStateAuditStatus -AutoResolver {
            $auditScript = Join-Path $RepoRoot "scripts\automation\audit_loop_shared_state.ps1"
            if (-not (Test-Path $auditScript)) {
                return $false
            }

            & $auditScript *> $null
            return ($LASTEXITCODE -eq 0)
        }
    },
    @{
        name = "Continuity protection"
        weight = 20
        status = Resolve-BooleanStatus -Override $ContinuityStatus -AutoResolver {
            $stateFile = Join-Path $RepoRoot "docs\automation\loop_continuity_state.json"
            if (-not (Test-Path $stateFile)) {
                return $false
            }

            try {
                $state = Get-Content -Path $stateFile -Raw | ConvertFrom-Json
                return (($state.PSObject.Properties.Name -contains "status") -and $state.status -eq "passed")
            } catch {
                return $false
            }
        }
    },
    @{
        name = "Health signals"
        weight = 15
        status = Resolve-BooleanStatus -Override $HealthSignalsStatus -AutoResolver {
            $healthStatePath = Join-Path $RepoRoot "docs\automation\loop_health_state.json"
            $pulseLogPath = Join-Path $RepoRoot "docs\automation\pulse_log.txt"
            if (-not (Test-Path $healthStatePath) -or -not (Test-Path $pulseLogPath)) {
                return $false
            }

            try {
                $healthState = Get-Content -Path $healthStatePath -Raw | ConvertFrom-Json
                if ($healthState.status -ne "ok") {
                    return $false
                }
            } catch {
                return $false
            }

            $latestPulse = Get-LatestPulseBlock -Path $pulseLogPath
            if (-not $latestPulse) {
                return $false
            }

            return ($latestPulse -match "(?m)^\s*tests:\s*" -and $latestPulse -match "(?m)^\s*lint:\s*")
        }
    },
    @{
        name = "Documentation alignment"
        weight = 10
        status = Resolve-BooleanStatus -Override $DocumentationStatus -AutoResolver {
            $gatesPath = Join-Path $RepoRoot "docs\automation\LOOP_GATES.md"
            $blueprintPath = Join-Path $RepoRoot "docs\automation\LOOP_GATE_ARCHITECTURE_BLUEPRINT.md"
            $metricsPath = Join-Path $RepoRoot "docs\automation\LOOP_METRICS_TEMPLATE.md"

            return (Test-FileContainsAll -Path $gatesPath -Needles @("start_loop_run.ps1", "finish_loop_run.ps1", "audit_loop_shared_state.ps1")) -and
                (Test-FileContainsAll -Path $blueprintPath -Needles @("Approval gate:", "wrapper/audit path")) -and
                (Test-FileContainsAll -Path $metricsPath -Needles @("Loop Effectiveness"))
        }
    }
)

$score = 0
$metricResults = @()
foreach ($metric in $metrics) {
    $earned = if ($metric.status) { [int]$metric.weight } else { 0 }
    $score += $earned
    $metricResults += [PSCustomObject]@{
        name = $metric.name
        weight = [int]$metric.weight
        passed = [bool]$metric.status
        earned = $earned
    }
}

$progressBar = New-AsciiProgressBar -Percent $score
$grade = if ($score -ge 90) {
    "A"
} elseif ($score -ge 75) {
    "B"
} else {
    "C"
}

$result = [ordered]@{
    measured_at = (Get-Date).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ssZ")
    score = $score
    max_score = 100
    grade = $grade
    progress_bar = $progressBar
    metrics = $metricResults
}

$stateDir = Split-Path -Parent $StatePath
if ($stateDir -and -not (Test-Path $stateDir)) {
    New-Item -ItemType Directory -Path $stateDir -Force | Out-Null
}
$resultJson = $result | ConvertTo-Json -Depth 10
Set-Content -Path $StatePath -Value $resultJson -Encoding UTF8

if (-not $Quiet) {
    Write-Host "Loop Efficiency Score: $score/100 (Grade $grade)"
    Write-Host "Progress: $progressBar"
    foreach ($metric in $metricResults) {
        $statusText = if ($metric.passed) { "pass" } else { "fail" }
        Write-Host "- $($metric.name): $statusText ($($metric.earned)/$($metric.weight))"
    }
}

if ($PassThru) {
    [PSCustomObject]$result
}
