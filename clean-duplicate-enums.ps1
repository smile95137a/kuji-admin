Write-Host ""
Write-Host "========================================"
Write-Host "🧹 清理重複的 Enum 檔案"
Write-Host "========================================"
Write-Host ""

$enumDir = "src\main\java\com\group\admin\enums"
$filesToDelete = @(
    "LotteryStatus.java",
    "LotteryCategory.java",
    "LotterySubCategory.java"
)

foreach ($file in $filesToDelete) {
    $fullPath = Join-Path $enumDir $file
    if (Test-Path $fullPath) {
        Remove-Item $fullPath -Force
        Write-Host "✅ 已刪除 $file" -ForegroundColor Green
    } else {
        Write-Host "⚠️  $file 不存在" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "========================================"
Write-Host "保留的 Enum 檔案（正在使用中）："
Write-Host "========================================"
Write-Host "✅ LotteryStatusEnum.java" -ForegroundColor Green
Write-Host "✅ LotteryCategoryEnum.java" -ForegroundColor Green
Write-Host "✅ LotterySubCategoryEnum.java" -ForegroundColor Green
Write-Host ""
Write-Host "清理完成！" -ForegroundColor Cyan
Write-Host ""
