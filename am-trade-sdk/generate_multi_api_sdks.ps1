# Multi-Language SDK Generator for AM Trade Management
# Usage: ./generate_multi_api_sdks.ps1 [-SkipPython] [-SkipFlutter]

param(
    [switch]$SkipFlutter,
    [switch]$SkipPython
)

$ScriptRoot = $PSScriptRoot
$OpenApiSpec = Join-Path $ScriptRoot "am-trade-openapi.json"

if (-not (Test-Path $OpenApiSpec)) {
    Write-Host "[ERROR] am-trade-openapi.json not found!" -ForegroundColor Red
    exit 1
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " AM Trade Management SDK Generator      " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# --- STEP 1: Flutter SDK ---
if (-not $SkipFlutter) {
    Write-Host "[STEP 1] Generating Flutter SDK..." -ForegroundColor Cyan
    $OutDir = Join-Path $ScriptRoot "flutter-trade-sdk"
    
    npx @openapitools/openapi-generator-cli generate `
        -i $OpenApiSpec `
        -g dart-dio `
        -o $OutDir `
        --additional-properties="pubName=am_trade_client,pubDescription=AM Trade Management Flutter Client"
}

# --- STEP 2: Python SDK ---
if (-not $SkipPython) {
    Write-Host "[STEP 2] Generating Python SDK..." -ForegroundColor Cyan
    $OutDir = Join-Path $ScriptRoot "python-trade-sdk"
    
    npx @openapitools/openapi-generator-cli generate `
        -i $OpenApiSpec `
        -g python `
        -o $OutDir `
        --additional-properties="packageName=am_trade_client,projectName=am-trade-client"
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " SDK Generation Complete!               " -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
