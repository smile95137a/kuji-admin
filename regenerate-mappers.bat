@echo off
REM ============================================
REM MyBatis Generator 快速修復腳本
REM 用途：重新生成所有 Mapper 並自動修正 order 保留字問題
REM ============================================

echo 🔄 開始重新生成 Mapper...
echo.

REM 刪除舊的 Mapper XML
echo ❌ 刪除舊的 Mapper XML 檔案...
del /F /Q src\main\resources\mapper\*.xml 2>nul

REM 執行 MyBatis Generator
echo 🔨 執行 MyBatis Generator...
call mvn mybatis-generator:generate -q

if %ERRORLEVEL% NEQ 0 (
    echo ❌ MyBatis Generator 執行失敗！
    pause
    exit /b 1
)

echo ✅ MyBatis Generator 執行成功！
echo.

REM 修正 OrderMapper.xml 的 order 保留字問題
echo 🔧 修正 OrderMapper.xml 的 order 保留字...
powershell -Command "$file='src\main\resources\mapper\OrderMapper.xml'; $content=Get-Content $file -Raw; $content=$content -replace ' from order\b',' from `order`'; $content=$content -replace ' into order\b',' into `order`'; $content=$content -replace ' update order\b',' update `order`'; $content=$content -replace 'delete from order\b','delete from `order`'; $content | Set-Content $file -NoNewline"

if %ERRORLEVEL% NEQ 0 (
    echo ❌ OrderMapper.xml 修正失敗！
    pause
    exit /b 1
)

echo ✅ OrderMapper.xml 修正完成！
echo.

REM 驗證結果
echo 🔍 驗證修正結果...
findstr /C:"from `order`" src\main\resources\mapper\OrderMapper.xml >nul
if %ERRORLEVEL% EQU 0 (
    echo ✅ 驗證通過：OrderMapper.xml 已正確加上反引號
) else (
    echo ⚠️  警告：未找到 'from `order`'，請手動檢查
)

echo.
echo ============================================
echo ✅ 所有 Mapper 已重新生成並修正完成！
echo ============================================
echo.
echo 📝 提醒：每次執行 mvn mybatis-generator:generate 後都需要執行此腳本
echo.
pause
