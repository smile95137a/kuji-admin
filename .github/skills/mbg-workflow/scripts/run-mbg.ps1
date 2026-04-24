#!/usr/bin/env pwsh
# Pre-cleans MBG-managed Mapper XML files before regenerating to prevent duplicate ResultMap errors.
# REQUIRES: DB connection to AWS RDS (only run when connected to VPN or from EC2)
# Usage: .\run-mbg.ps1  (from project root) or call this script directly

$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

# Navigate to project root from .github\skills\mbg-workflow\scripts
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = (Resolve-Path (Join-Path $scriptDir "..\..\..\..")).Path
Set-Location $projectRoot

$mapperXmlDir = "src\main\resources\mapper"
$generatorConfig = "generatorConfig.xml"
$backupDir = "$env:TEMP\mbg-backup-$(Get-Date -Format 'yyyyMMdd-HHmmss')"

if (-not (Test-Path $generatorConfig)) {
    Write-Host "ERROR: generatorConfig.xml not found. Run this script from the project root." -ForegroundColor Red
    exit 1
}

Write-Host "=== MBG Pre-Clean + Generate ===" -ForegroundColor Cyan
Write-Host "NOTE: Requires live DB connection to AWS RDS" -ForegroundColor Yellow
Write-Host ""

# Read all tableName from generatorConfig.xml
$tableNames = @()
Select-String -Path $generatorConfig -Pattern 'tableName="([^"]+)"' | ForEach-Object {
    if ($_.Line -match 'tableName="([^"]+)"') {
        $tableNames += $matches[1].Trim('`')
    }
}

Write-Host "Tables found: $($tableNames.Count)" -ForegroundColor Gray

function ToPascalCase($str) {
    ($str -split '_' | ForEach-Object { $_.Substring(0, 1).ToUpper() + $_.Substring(1).ToLower() }) -join ''
}

# Backup then delete existing MBG-managed Mapper XMLs
New-Item -ItemType Directory -Path $backupDir -Force | Out-Null
$deleted = 0
$tableNames | ForEach-Object {
    $xmlPath = Join-Path $mapperXmlDir "$(ToPascalCase $_)Mapper.xml"
    if (Test-Path $xmlPath) {
        Copy-Item $xmlPath $backupDir
        Remove-Item $xmlPath -Force
        $deleted++
    }
}

Write-Host "Backed up $deleted files to: $backupDir" -ForegroundColor Gray
Write-Host "Running MBG..." -ForegroundColor Cyan

mvn mybatis-generator:generate

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "SUCCESS: MBG generation complete." -ForegroundColor Green
    Write-Host "Backup can be deleted: $backupDir" -ForegroundColor DarkGray
    $check = Select-String -Path "$mapperXmlDir\UserMapper.xml" -Pattern 'id="BaseResultMap"' | Measure-Object
    Write-Host "Verification - UserMapper BaseResultMap count: $($check.Count) (expected: 1)" -ForegroundColor $(if ($check.Count -eq 1) { "Green" } else { "Red" })
} else {
    Write-Host ""
    Write-Host "FAILED: MBG generation failed. Restoring backup..." -ForegroundColor Red
    Get-ChildItem $backupDir | ForEach-Object {
        Copy-Item $_.FullName $mapperXmlDir -Force
    }
    Write-Host "Backup restored from: $backupDir" -ForegroundColor Yellow
    Write-Host "Check DB connection (AWS RDS must be reachable)." -ForegroundColor Yellow
    exit 1
}
