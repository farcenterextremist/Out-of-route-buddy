# LLM Loop — permanent local-first entrypoint for LLM workflow work in Cursor.
# The token-audit lane remains the first stable lane under this contract.
# Run from repo root: .\scripts\automation\run_llm_loop.ps1
# Optional: .\scripts\automation\run_llm_loop.ps1 -Test

param(
    [switch]$Test,
    [string]$RunId = ""
)

$ErrorActionPreference = "Continue"

$RepoRoot = $PSScriptRoot
for ($i = 0; $i -lt 2; $i++) { $RepoRoot = Split-Path -Parent $RepoRoot }
Set-Location $RepoRoot

$tokenRunner = Join-Path $RepoRoot "scripts\automation\run_token_loop.ps1"
if (-not (Test-Path $tokenRunner)) {
    Write-Host "FAIL: run_token_loop.ps1 not found."
    exit 1
}

$ollama = Get-Command "ollama" -ErrorAction SilentlyContinue
if ($ollama) {
    Write-Host "LLM loop: Ollama detected on PATH."
    Write-Host "Local-first provider guidance: use 'ollama ls' to inspect installed models and 'ollama ps' to inspect loaded models."
} else {
    Write-Host "WARN: Ollama was not found on PATH."
    Write-Host "The LLM loop can still run its token-audit lane, but local-model workflow tasks will need Ollama installed."
}

Write-Host "LLM loop entrypoint: permanent local-first loop."
Write-Host "Active lane: token_audit"
Write-Host "Contract doc: docs\automation\LLM_LOOP.md"
Write-Host ""

$params = @{}
if ($Test) { $params["Test"] = $true }
if ($RunId) { $params["RunId"] = $RunId }

& $tokenRunner @params
exit $LASTEXITCODE
