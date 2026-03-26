@echo off
echo ========================================
echo KUJI 專案 - 缺少表的建立與 Entity 生成
echo ========================================
echo.

echo [步驟 1] 請先執行 SQL 檔案
echo 檔案位置: missing_tables_ddl.sql
echo.
echo 請手動執行以下操作：
echo 1. 開啟 MySQL Workbench 或 DBeaver
echo 2. 連線到資料庫: database-1.clsi2geo699r.ap-northeast-1.rds.amazonaws.com
echo 3. 執行 missing_tables_ddl.sql 檔案
echo.

set /p confirm="已經執行完 SQL 了嗎？(Y/N): "
if /i "%confirm%" NEQ "Y" (
    echo 取消執行
    pause
    exit /b
)

echo.
echo [步驟 2] 執行 MyBatis Generator
echo.
call mvn mybatis-generator:generate

if %ERRORLEVEL% EQU 0 (
    echo.
    echo [步驟 3] 重新編譯專案
    echo.
    call mvn clean compile -DskipTests
    
    if %ERRORLEVEL% EQU 0 (
        echo.
        echo ========================================
        echo ✅ 完成！所有表和 Entity 已建立
        echo ========================================
    ) else (
        echo.
        echo ========================================
        echo ❌ 編譯失敗，請檢查錯誤訊息
        echo ========================================
    )
) else (
    echo.
    echo ========================================
    echo ❌ MBG 執行失敗，請檢查資料庫連線
    echo ========================================
)

pause
