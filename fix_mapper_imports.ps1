Get-ChildItem 'src\main\java\com\group\admin\mapper\*Mapper.java' | ForEach-Object {
    $content = Get-Content $_.FullName -Raw
    $content = $content -replace 'import com\.group\.admin\.entity\.(\w+)Example;', 'import com.group.admin.example.$1Example;'
    Set-Content $_.FullName -Value $content -NoNewline
    Write-Host "Fixed: $($_.Name)"
}
Write-Host "All Mapper imports fixed!"
