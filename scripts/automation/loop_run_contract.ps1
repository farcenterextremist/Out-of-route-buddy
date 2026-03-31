function Get-LoopAutomationRepoRoot {
    param(
        [Parameter(Mandatory = $true)]
        [string]$ScriptRoot
    )

    $repoRoot = $ScriptRoot
    for ($i = 0; $i -lt 2; $i++) {
        $repoRoot = Split-Path -Parent $repoRoot
    }

    return $repoRoot
}

function Resolve-LoopCanonicalName {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Loop
    )

    switch ($Loop.ToLowerInvariant()) {
        "cyber_security" { return "cyber" }
        default { return $Loop.ToLowerInvariant() }
    }
}

function Get-LoopLatestStatePath {
    param(
        [Parameter(Mandatory = $true)]
        [string]$RepoRoot,

        [Parameter(Mandatory = $true)]
        [string]$Loop
    )

    $canonicalLoop = Resolve-LoopCanonicalName -Loop $Loop
    return Join-Path $RepoRoot "docs\automation\loop_latest\$canonicalLoop.json"
}

function New-LoopRunId {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Prefix
    )

    $normalizedPrefix = $Prefix.Trim().ToLowerInvariant()
    return "$normalizedPrefix-$((Get-Date).ToUniversalTime().ToString('yyyyMMdd-HHmmss'))"
}
