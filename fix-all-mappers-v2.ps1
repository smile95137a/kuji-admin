# fix-all-mappers-v2.ps1 - Fix ALL duplicate content in Mapper XML files
# Strategy: Find the 2nd occurrence of BaseResultMap, keep everything before it, add </mapper>

$mapperDir = "c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin\src\main\resources\mapper"
$files = Get-ChildItem "$mapperDir\*.xml"
$fixedCount = 0
$skippedCount = 0

foreach ($file in $files) {
    $lines = Get-Content $file.FullName -Encoding UTF8
    
    # Find all lines with BaseResultMap
    $resultMapLines = @()
    for ($i = 0; $i -lt $lines.Count; $i++) {
        if ($lines[$i] -match 'id="BaseResultMap"') {
            $resultMapLines += $i
        }
    }
    
    if ($resultMapLines.Count -le 1) {
        Write-Host "OK: $($file.Name)" -ForegroundColor Green
        $skippedCount++
        continue
    }
    
    # The second BaseResultMap starts at this index
    $secondResultMapIndex = $resultMapLines[1]
    
    # Keep everything BEFORE the second BaseResultMap line
    # But we need to also remove the blank/comment lines right before it
    $cutIndex = $secondResultMapIndex - 1
    
    # Trim trailing empty lines before the cut point
    while ($cutIndex -gt 0 -and $lines[$cutIndex].Trim() -eq '') {
        $cutIndex--
    }
    
    # Keep lines 0 to cutIndex, then add </mapper>
    $keepLines = $lines[0..$cutIndex]
    $keepLines += '</mapper>'
    
    # Verify: exactly 1 BaseResultMap in kept content
    $verifyCount = ($keepLines | Select-String -Pattern 'id="BaseResultMap"').Count
    if ($verifyCount -ne 1) {
        Write-Host "WARNING: $($file.Name) - verification failed ($verifyCount BaseResultMap after fix)" -ForegroundColor Yellow
        continue
    }
    
    # Write back
    $keepLines | Set-Content $file.FullName -Encoding UTF8
    $fixedCount++
    Write-Host "FIXED: $($file.Name) ($($lines.Count) -> $($keepLines.Count) lines)" -ForegroundColor Cyan
}

Write-Host ""
Write-Host "===== DONE =====" -ForegroundColor Green
Write-Host "Fixed: $fixedCount files"
Write-Host "Already OK: $skippedCount files"
Write-Host "Total: $($files.Count) files"
