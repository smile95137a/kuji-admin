@echo off
chcp 65001 >nul
echo ========================================
echo 修正資料表結構並重新生成 Entity
echo ========================================
echo.

echo [1/2] 執行 SQL 修正腳本...
echo.
echo 請手動執行以下 SQL 檔案：
echo doc\sql\fix-prize-box-wallet-order-columns.sql
echo.
echo 或使用以下命令：
echo mysql -u root -p kuji_db ^< doc\sql\fix-prize-box-wallet-order-columns.sql
echo.
pause

echo.
echo [2/2] 重新生成 Entity/Mapper/Example...
echo.
call mvn mybatis-generator:generate

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ 修正完成！
    echo.
    echo 接下來可以執行：
    echo   - mvn clean compile：檢查編譯錯誤
    echo   - 繼續實作 Controller 層
) else (
    echo.
    echo ❌ 生成失敗，請檢查錯誤訊息
)

echo.
pause
