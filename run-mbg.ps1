#!/usr/bin/env pwsh

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$skillScript = Join-Path $scriptDir ".github\skills\mbg-workflow\scripts\run-mbg.ps1"

if (-not (Test-Path $skillScript)) {
    Write-Host "ERROR: Skill helper script not found: $skillScript" -ForegroundColor Red
    exit 1
}

& $skillScript @args
exit $LASTEXITCODE
