# Lightweight OpenAI API usage monitor for loop runs.
# Run from repo root:
# .\scripts\automation\monitor_openai_api_usage.ps1
# .\scripts\automation\monitor_openai_api_usage.ps1 -Category completions -WindowHours 24 -OutPath docs/automation/api_usage_snapshots/openai-usage.json

param(
    [ValidateSet("completions", "embeddings", "images", "moderations", "vector_stores", "audio_speeches", "audio_transcriptions", "code_interpreter_sessions")]
    [string]$Category = "completions",
    [string]$StartTime = "",
    [string]$EndTime = "",
    [int]$WindowHours = 24,
    [string]$ApiKey = "",
    [string]$Organization = "",
    [string]$Project = "",
    [string]$FixturePath = "",
    [string]$OutPath = "",
    [switch]$PassThru
)

$ErrorActionPreference = "Stop"

$RepoRoot = $PSScriptRoot
for ($i = 0; $i -lt 2; $i++) { $RepoRoot = Split-Path -Parent $RepoRoot }
Set-Location $RepoRoot

function Convert-ToUnixTimeSeconds([string]$Value, [datetime]$DefaultValue) {
    if ([string]::IsNullOrWhiteSpace($Value)) {
        return [int][DateTimeOffset]::new($DefaultValue.ToUniversalTime()).ToUnixTimeSeconds()
    }
    if ($Value -match '^\d+$') {
        return [int64]$Value
    }
    $parsed = [datetime]::Parse($Value)
    return [int][DateTimeOffset]::new($parsed.ToUniversalTime()).ToUnixTimeSeconds()
}

function Get-AggregatedUsageMetrics($Payload) {
    $totals = [ordered]@{
        num_model_requests = 0
        input_tokens = 0
        output_tokens = 0
        input_cached_tokens = 0
        input_audio_tokens = 0
        output_audio_tokens = 0
        reasoning_tokens = 0
    }

    $resultCount = 0
    $bucketCount = 0
    $dataItems = @($Payload.data)
    foreach ($bucket in $dataItems) {
        $bucketCount++
        $results = @($bucket.results)
        if ($results.Count -eq 0) {
            $results = @($bucket)
        }
        foreach ($result in $results) {
            $resultCount++
            foreach ($key in @($totals.Keys)) {
                if ($result.PSObject.Properties.Name -contains $key -and $null -ne $result.$key) {
                    $totals[$key] += [long]$result.$key
                }
            }
        }
    }

    return @{
        totals = $totals
        bucket_count = $bucketCount
        result_count = $resultCount
    }
}

$now = (Get-Date).ToUniversalTime()
$defaultStart = $now.AddHours(-1 * [Math]::Abs($WindowHours))
$startUnix = Convert-ToUnixTimeSeconds -Value $StartTime -DefaultValue $defaultStart
$endUnix = Convert-ToUnixTimeSeconds -Value $EndTime -DefaultValue $now

if ($endUnix -lt $startUnix) {
    throw "EndTime must be greater than or equal to StartTime."
}

$payload = $null
$source = ""
if ($FixturePath) {
    $fixtureOnDisk = if ([System.IO.Path]::IsPathRooted($FixturePath)) { $FixturePath } else { Join-Path $RepoRoot $FixturePath }
    if (-not (Test-Path $fixtureOnDisk)) {
        throw "Fixture file does not exist: $fixtureOnDisk"
    }
    $payload = Get-Content -Path $fixtureOnDisk -Raw | ConvertFrom-Json
    $source = "fixture"
} else {
    if (-not $ApiKey) {
        $ApiKey = [Environment]::GetEnvironmentVariable("OPENAI_API_KEY")
    }
    if (-not $Organization) {
        $Organization = [Environment]::GetEnvironmentVariable("OPENAI_ORG_ID")
    }
    if (-not $Project) {
        $Project = [Environment]::GetEnvironmentVariable("OPENAI_PROJECT_ID")
    }
    if (-not $ApiKey) {
        throw "Set OPENAI_API_KEY or use -FixturePath to monitor API usage."
    }

    $uri = "https://api.openai.com/v1/organization/usage/$Category?start_time=$startUnix&end_time=$endUnix&bucket_width=1d"
    $headers = @{
        "Authorization" = "Bearer $ApiKey"
    }
    if ($Organization) {
        $headers["OpenAI-Organization"] = $Organization
    }
    if ($Project) {
        $headers["OpenAI-Project"] = $Project
    }

    $payload = Invoke-RestMethod -Method Get -Uri $uri -Headers $headers
    $source = "live_api"
}

$aggregated = Get-AggregatedUsageMetrics -Payload $payload
$summary = [PSCustomObject]@{
    provider = "openai"
    source = $source
    category = $Category
    start_time = $startUnix
    end_time = $endUnix
    bucket_count = $aggregated.bucket_count
    result_count = $aggregated.result_count
    num_model_requests = $aggregated.totals["num_model_requests"]
    input_tokens = $aggregated.totals["input_tokens"]
    output_tokens = $aggregated.totals["output_tokens"]
    input_cached_tokens = $aggregated.totals["input_cached_tokens"]
    input_audio_tokens = $aggregated.totals["input_audio_tokens"]
    output_audio_tokens = $aggregated.totals["output_audio_tokens"]
    reasoning_tokens = $aggregated.totals["reasoning_tokens"]
}

if (-not $OutPath) {
    $stamp = (Get-Date).ToUniversalTime().ToString("yyyyMMdd-HHmmss")
    $OutPath = "docs/automation/api_usage_snapshots/openai-usage-$Category-$stamp.json"
}

$outOnDisk = if ([System.IO.Path]::IsPathRooted($OutPath)) { $OutPath } else { Join-Path $RepoRoot $OutPath }
$outDir = Split-Path -Parent $outOnDisk
if ($outDir -and -not (Test-Path $outDir)) {
    New-Item -ItemType Directory -Path $outDir -Force | Out-Null
}

$summary | ConvertTo-Json -Depth 10 | Set-Content -Path $outOnDisk -Encoding UTF8

Write-Host "OpenAI API usage snapshot"
Write-Host "  Category: $Category"
Write-Host "  Source: $source"
Write-Host "  Window: $startUnix -> $endUnix"
Write-Host "  Requests: $($summary.num_model_requests)"
Write-Host "  Input tokens: $($summary.input_tokens)"
Write-Host "  Output tokens: $($summary.output_tokens)"
Write-Host "  Cached input tokens: $($summary.input_cached_tokens)"
Write-Host "  Output file: $outOnDisk"

if ($PassThru) {
    $summary
}
