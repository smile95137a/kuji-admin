# fix-all-mappers.ps1 - Fix ALL duplicate content in Mapper XML files

$mapperDir = "c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin\src\main\resources\mapper"
$files = Get-ChildItem "$mapperDir\*.xml"
$fixedCount = 0
$skippedCount = 0

foreach ($file in $files) {
    $lines = Get-Content $file.FullName -Encoding UTF8
    $baseResultMapCount = ($lines | Select-String -Pattern 'id="BaseResultMap"').Count
    
    if ($baseResultMapCount -le 1) {
        Write-Host "OK: $($file.Name)" -ForegroundColor Green
        $skippedCount++
        continue
    }
    
    # Find the first </mapper> closing tag (0-based index)
    $firstClosingIndex = -1
    for ($i = 0; $i -lt $lines.Count; $i++) {
        if ($lines[$i] -match '^\s*</mapper>\s*$') {
            $firstClosingIndex = $i
            break
        }
    }
    
    if ($firstClosingIndex -eq -1) {
        Write-Host "WARNING: $($file.Name) - no closing mapper tag found, skipping" -ForegroundColor Yellow
        continue
    }
    
    # Keep lines from 0 to the first </mapper> (inclusive)
    $keepLines = $lines[0..$firstClosingIndex]
    
    # Verify: only 1 BaseResultMap after truncation
    $verifyCount = ($keepLines | Select-String -Pattern 'id="BaseResultMap"').Count
    if ($verifyCount -ne 1) {
        Write-Host "WARNING: $($file.Name) - still has $verifyCount BaseResultMap after fix, skipping" -ForegroundColor Yellow
        continue
    }
    
    # Write back
    $keepLines | Set-Content $file.FullName -Encoding UTF8
    $fixedCount++
    $totalLines = $lines.Count
    $keptLines = $keepLines.Count
    Write-Host "FIXED: $($file.Name) ($totalLines -> $keptLines lines)" -ForegroundColor Cyan
}

Write-Host ""
Write-Host "===== DONE =====" -ForegroundColor Green
Write-Host "Fixed: $fixedCount files"
Write-Host "Already OK: $skippedCount files"
Write-Host "Total: $($files.Count) files"
