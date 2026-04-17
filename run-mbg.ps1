#!/usr/bin/env pwsh
# run-mbg.ps1
# 執行 MyBatis Generator 前，先清除 MBG 管理的 Mapper XML，避免重複定義導致啟動失敗。

$mapperXmlDir = "src/main/resources/mapper"
$generatorConfig = "generatorConfig.xml"

# 從 generatorConfig.xml 讀取所有 tableName
$tableNames = @()
Select-String -Path $generatorConfig -Pattern 'tableName="([^"]+)"' | ForEach-Object {
    if ($_.Line -match 'tableName="([^"]+)"') {
        $tableNames += $matches[1].Trim('`')
    }
}

# snake_case → PascalCase 轉換函數
function ToPascalCase($str) {
    ($str -split '_' | ForEach-Object { $_.Substring(0,1).ToUpper() + $_.Substring(1).ToLower() }) -join ''
}

# 計算要清除的 XML 清單
$toDelete = $tableNames | ForEach-Object {
    Join-Path $mapperXmlDir "$(ToPascalCase $_)Mapper.xml"
} | Where-Object { Test-Path $_ }

if ($toDelete.Count -eq 0) {
    Write-Host "✅ 沒有需要清除的 Mapper XML" -ForegroundColor Green
} else {
    Write-Host "🗑️  清除以下 MBG 管理的 Mapper XML ($($toDelete.Count) 個):" -ForegroundColor Yellow
    $toDelete | ForEach-Object {
        Write-Host "   $_" -ForegroundColor Gray
        Remove-Item $_
    }
}

Write-Host ""
Write-Host "🚀 執行 MBG 生成..." -ForegroundColor Cyan
mvn mybatis-generator:generate

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✅ MBG 生成完成！" -ForegroundColor Green
    Write-Host "⚠️  提醒：如有自訂 SQL（如 UserTokenBlacklistMapper.xml），請確認是否需要手動補回" -ForegroundColor Yellow
} else {
    Write-Host ""
    Write-Host "❌ MBG 生成失敗，請檢查上方錯誤訊息" -ForegroundColor Red
    exit 1
}
