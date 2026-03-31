# Test Shared State Schema + Deduplication Guard
# Run from repo root: .\scripts\automation\test_shared_state_schema.ps1

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

function Test-HasProp($Obj, [string]$Name) {
    return $Obj -and ($Obj.PSObject.Properties.Name -contains $Name)
}

function Test-StringProp($Obj, [string]$Name) {
    if (-not (Test-HasProp $Obj $Name)) { return $false }
    return $Obj.$Name -is [string] -and -not [string]::IsNullOrWhiteSpace($Obj.$Name)
}

function Test-StringOrNullProp($Obj, [string]$Name) {
    if (-not (Test-HasProp $Obj $Name)) { return $false }
    if ($null -eq $Obj.$Name) { return $true }
    return $Obj.$Name -is [string] -and -not [string]::IsNullOrWhiteSpace($Obj.$Name)
}

Write-Host "Testing shared-state schema + dedupe guard..."
Write-Host ""

$eventsPath = Join-Path $RepoRoot "docs\automation\loop_shared_events.jsonl"
if (-not (Test-Path $eventsPath)) {
    Fail "Missing events file: $eventsPath"
} else {
    $lines = Get-Content $eventsPath | Where-Object { $_.Trim() -ne "" }
    if ($lines.Count -eq 0) {
        Fail "Events file is empty"
    } else {
        Pass "Events file has $($lines.Count) non-empty lines"

        # Validate only recent window to avoid blocking on unrelated historical debt.
        $windowSize = [Math]::Min(200, $lines.Count)
        $windowLines = $lines[($lines.Count - $windowSize)..($lines.Count - 1)]

        $eventObjs = @()
        $jsonFail = 0
        foreach ($line in $windowLines) {
            try {
                $obj = $line | ConvertFrom-Json
                $eventObjs += $obj
            } catch {
                $jsonFail++
            }
        }
        if ($jsonFail -gt 0) {
            Fail "$jsonFail recent event line(s) are not valid JSON"
        } else {
            Pass "Recent event window parses as valid JSON"
        }

        $schemaFail = 0
        foreach ($ev in $eventObjs) {
            if (-not (Test-StringProp $ev "ts")) { $schemaFail++ }
            if (-not (Test-StringProp $ev "loop")) { $schemaFail++ }
            if (-not (Test-StringProp $ev "event")) { $schemaFail++ }

            if ((Test-HasProp $ev "next_steps") -and -not ($ev.next_steps -is [System.Array])) {
                $schemaFail++
            }

            if ((Test-HasProp $ev "event") -and $ev.event -eq "finished") {
                if (-not (Test-StringProp $ev "run_id")) { $schemaFail++ }
                if (-not (Test-StringProp $ev "summary_path")) { $schemaFail++ }
            }
        }
        if ($schemaFail -gt 0) {
            Fail "Recent event schema validation failed ($schemaFail issue(s))"
        } else {
            Pass "Recent event schema validation passed"
        }

        # Deduplication guard in recent window for finished events.
        $seen = @{}
        $dupes = @()
        foreach ($ev in $eventObjs) {
            if ((Test-HasProp $ev "event") -and $ev.event -eq "finished" -and (Test-StringProp $ev "loop") -and (Test-StringProp $ev "run_id")) {
                $key = "$($ev.loop)|$($ev.run_id)"
                if ($seen.ContainsKey($key)) {
                    $dupes += $key
                } else {
                    $seen[$key] = $true
                }
            }
        }

        if ($dupes.Count -gt 0) {
            $uniqueDupes = $dupes | Sort-Object -Unique
            $sample = ($uniqueDupes | Select-Object -First 5) -join ", "
            Fail "Duplicate finished run_id key(s) in recent window: $sample"
        } else {
            Pass "No duplicate finished run_id keys in recent window"
        }
    }
}

$latestDir = Join-Path $RepoRoot "docs\automation\loop_latest"
$requiredLoops = @("improvement", "token", "cyber", "synthetic_data")
if (-not (Test-Path $latestDir)) {
    Fail "Missing loop_latest directory: $latestDir"
} else {
    foreach ($loop in $requiredLoops) {
        $latestPath = Join-Path $latestDir "$loop.json"
        if (-not (Test-Path $latestPath)) {
            Fail "Missing latest state file: $latestPath"
            continue
        }

        try {
            $obj = Get-Content $latestPath -Raw | ConvertFrom-Json
        } catch {
            Fail "$loop latest state is not valid JSON"
            continue
        }

        $localFail = 0
        # Backward compatible strictness: allow null placeholders for legacy loops.
        if (-not (Test-StringOrNullProp $obj "last_run_ts")) { $localFail++ }
        if (-not (Test-StringOrNullProp $obj "summary_path")) { $localFail++ }
        if (-not (Test-HasProp $obj "suggested_next_steps")) {
            $localFail++
        } elseif (-not ($obj.suggested_next_steps -is [System.Array])) {
            $localFail++
        }

        # Optional for backward compatibility: if present, run_id must be a non-empty string.
        if ((Test-HasProp $obj "run_id") -and -not (Test-StringProp $obj "run_id")) {
            $localFail++
        }

        if ($localFail -gt 0) {
            Fail "$loop latest state schema validation failed ($localFail issue(s))"
        } else {
            Pass "$loop latest state schema validation passed"
            if ((Test-HasProp $obj "last_run_ts") -and $null -eq $obj.last_run_ts) {
                Write-Host "WARN: $loop has null last_run_ts placeholder; consider refreshing from next loop run"
            }
            if ((Test-HasProp $obj "summary_path") -and $null -eq $obj.summary_path) {
                Write-Host "WARN: $loop has null summary_path placeholder; consider refreshing from next loop run"
            }
        }
    }
}

Write-Host ""
if ($failCount -eq 0) {
    Write-Host "All shared-state schema tests passed."
    exit 0
} else {
    Write-Host "FAIL: $failCount shared-state schema check(s) failed."
    exit 1
}
