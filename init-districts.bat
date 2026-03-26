@echo off
echo ========================================
echo   初始化台灣行政區資料
echo ========================================
echo.

echo [1] 檢查 MySQL 連接...
echo.

REM 尋找 MySQL 路徑
set MYSQL_PATH=
if exist "C:\Program Files\MySQL\MySQL Server 8.3\bin\mysql.exe" (
    set "MYSQL_PATH=C:\Program Files\MySQL\MySQL Server 8.3\bin\mysql.exe"
) else if exist "C:\xampp\mysql\bin\mysql.exe" (
    set "MYSQL_PATH=C:\xampp\mysql\bin\mysql.exe"
) else if exist "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" (
    set "MYSQL_PATH=C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
)

if "%MYSQL_PATH%"=="" (
    echo ❌ 找不到 MySQL，請確認 MySQL 已安裝
    pause
    exit /b 1
)

echo ✅ 找到 MySQL: %MYSQL_PATH%
echo.

echo [2] 執行 SQL 初始化...
"%MYSQL_PATH%" -u root -pWUfan0667. kuji_db < init-taiwan-districts.sql

if %errorlevel% neq 0 (
    echo.
    echo ❌ SQL 執行失敗
    pause
    exit /b 1
)

echo.
echo ✅ SQL 執行成功
echo.

echo [3] 檢查資料筆數...
"%MYSQL_PATH%" -u root -pWUfan0667. -e "SELECT COUNT(*) as total_districts FROM district;" kuji_db

echo.
echo [4] 列出所有縣市...
"%MYSQL_PATH%" -u root -pWUfan0667. -e "SELECT DISTINCT city FROM district ORDER BY city;" kuji_db

echo.
echo ========================================
echo   初始化完成！
echo ========================================
pause
