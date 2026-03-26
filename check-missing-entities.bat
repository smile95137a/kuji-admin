@echo off
chcp 65001 >nul
echo ========================================
echo 🔍 檢查缺少的 Entity
echo ========================================
echo.

set MISSING=0

echo 檢查 Entity 檔案...
echo.

if exist "src\main\java\com\group\admin\entity\SystemLog.java" (
    echo ✅ SystemLog.java
) else (
    echo ❌ SystemLog.java
    set /a MISSING+=1
)

if exist "src\main\java\com\group\admin\entity\UserAddress.java" (
    echo ✅ UserAddress.java
) else (
    echo ❌ UserAddress.java
    set /a MISSING+=1
)

if exist "src\main\java\com\group\admin\entity\Marquee.java" (
    echo ✅ Marquee.java
) else (
    echo ❌ Marquee.java
    set /a MISSING+=1
)

if exist "src\main\java\com\group\admin\entity\ReferralCode.java" (
    echo ✅ ReferralCode.java
) else (
    echo ❌ ReferralCode.java
    set /a MISSING+=1
)

if exist "src\main\java\com\group\admin\entity\ReferralRecord.java" (
    echo ✅ ReferralRecord.java
) else (
    echo ❌ ReferralRecord.java
    set /a MISSING+=1
)

if exist "src\main\java\com\group\admin\entity\EmailLog.java" (
    echo ✅ EmailLog.java
) else (
    echo ❌ EmailLog.java
    set /a MISSING+=1
)

if exist "src\main\java\com\group\admin\entity\District.java" (
    echo ✅ District.java
) else (
    echo ❌ District.java
    set /a MISSING+=1
)

if exist "src\main\java\com\group\admin\entity\ReportSnapshot.java" (
    echo ✅ ReportSnapshot.java
) else (
    echo ❌ ReportSnapshot.java
    set /a MISSING+=1
)

echo.
echo ========================================
if %MISSING%==0 (
    echo ✅ 所有 Entity 都已存在！
    echo.
    echo 可以執行編譯：
    echo   mvn clean compile -DskipTests
) else (
    echo ⚠️  缺少 %MISSING% 個 Entity
    echo.
    echo 請執行以下步驟：
    echo   1. 開啟 MySQL Workbench
    echo   2. 執行 docs\03-資料庫相關\missing_tables_ddl.sql
    echo   3. 執行 mvn mybatis-generator:generate
    echo.
    echo 或使用自動化腳本：
    echo   create-missing-tables.bat
)
echo ========================================
echo.
pause
