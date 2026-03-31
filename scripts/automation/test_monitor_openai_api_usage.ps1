# Test lightweight OpenAI API usage monitor with a fixture payload.
# Run from repo root: .\scripts\automation\test_monitor_openai_api_usage.ps1

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

$monitorScript = Join-Path $RepoRoot "scripts\automation\monitor_openai_api_usage.ps1"
$tmpDir = Join-Path $RepoRoot "docs\automation\_continuity_test_tmp"
$fixtureFile = Join-Path $tmpDir "openai_api_usage.fixture.json"
$reportFile = Join-Path $tmpDir "openai_api_usage.report.json"

if (-not (Test-Path $tmpDir)) {
    New-Item -ItemType Directory -Path $tmpDir -Force | Out-Null
}

Write-Host "Testing lightweight OpenAI API usage monitor..."
Write-Host ""

if (-not (Test-Path $monitorScript)) {
    Fail "monitor_openai_api_usage.ps1 not found"
}

@"
{
  "data": [
    {
      "start_time": 1710547200,
      "end_time": 1710633600,
      "results": [
        {
          "num_model_requests": 3,
          "input_tokens": 1000,
          "output_tokens": 250,
          "input_cached_tokens": 400,
          "reasoning_tokens": 20
        },
        {
          "num_model_requests": 2,
          "input_tokens": 500,
          "output_tokens": 100,
          "input_cached_tokens": 100,
          "reasoning_tokens": 10
        }
      ]
    }
  ]
}
"@ | Set-Content -Path $fixtureFile -Encoding UTF8

$result = $null
if ($failCount -eq 0) {
    $result = & $monitorScript -FixturePath $fixtureFile -OutPath $reportFile -PassThru
}

if (-not $result) {
    Fail "Monitor script did not return a result"
} else {
    if ($result.provider -ne "openai" -or $result.source -ne "fixture") {
        Fail "Provider/source mismatch"
    } else {
        Pass "Provider/source set correctly"
    }
    if ($result.num_model_requests -ne 5) {
        Fail "Expected 5 total model requests, got $($result.num_model_requests)"
    } else {
        Pass "Model requests aggregated"
    }
    if ($result.input_tokens -ne 1500 -or $result.output_tokens -ne 350) {
        Fail "Input/output token aggregation mismatch"
    } else {
        Pass "Input/output tokens aggregated"
    }
    if ($result.input_cached_tokens -ne 500 -or $result.reasoning_tokens -ne 30) {
        Fail "Cached/reasoning token aggregation mismatch"
    } else {
        Pass "Cached/reasoning tokens aggregated"
    }
}

if (-not (Test-Path $reportFile)) {
    Fail "Report file not written"
} else {
    try {
        $reportJson = Get-Content -Path $reportFile -Raw | ConvertFrom-Json
        if ($reportJson.num_model_requests -ne 5) {
            Fail "Report file contents mismatch"
        } else {
            Pass "Report file written as valid JSON"
        }
    } catch {
        Fail "Report file is not valid JSON"
    }
}

Remove-Item $fixtureFile, $reportFile -Force -ErrorAction SilentlyContinue

Write-Host ""
if ($failCount -eq 0) {
    Write-Host "All OpenAI API usage monitor tests passed."
    exit 0
} else {
    Write-Host "FAIL: $failCount OpenAI API usage monitor test(s) failed."
    exit 1
}
