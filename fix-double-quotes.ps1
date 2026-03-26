# fix-double-quotes.ps1 - Replace double-quoted table names with backtick-quoted table names in all Mapper XMLs
# MBG with delimitIdentifiers=true uses double quotes (SQL standard), but MySQL needs backticks

$mapperDir = "c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin\src\main\resources\mapper"
$files = Get-ChildItem "$mapperDir\*.xml"
$fixedCount = 0

foreach ($file in $files) {
    $content = [System.IO.File]::ReadAllText($file.FullName, [System.Text.Encoding]::UTF8)
    $original = $content
    
    # Replace patterns like: from "table_name"  ->  from `table_name`
    # Replace patterns like: into "table_name"  ->  into `table_name`
    # Replace patterns like: update "table_name" -> update `table_name`
    # Replace patterns like: join "table_name"   -> join `table_name`
    # Also handle: delete from "table_name"
    
    # Generic regex: replace "word" when preceded by SQL keywords
    # Pattern: (from|into|update|join)\s+"([^"]+)"
    $content = [regex]::Replace($content, '(from|into|update|join)\s+"([^"]+)"', '$1 `$2`')
    
    if ($content -ne $original) {
        [System.IO.File]::WriteAllText($file.FullName, $content, (New-Object System.Text.UTF8Encoding $false))
        $fixedCount++
        Write-Host "FIXED: $($file.Name)" -ForegroundColor Cyan
    } else {
        Write-Host "OK: $($file.Name) (no double-quoted table names)" -ForegroundColor Green
    }
}

Write-Host ""
Write-Host "===== DONE =====" -ForegroundColor Green
Write-Host "Fixed: $fixedCount files"
Write-Host "Total: $($files.Count) files"
