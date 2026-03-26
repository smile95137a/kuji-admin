@echo off
echo ====================================================
echo Database Foreign Key Fix Tool
echo ====================================================
echo.

echo Check current database structure...
echo.
echo Please select an option:
echo [1] Check database table structure
echo [2] Rebuild database (DELETE all data, use UUID)
echo [3] Execute schema without foreign keys
echo [4] Execute complete schema (requires UUID)
echo [5] View fix guide
echo [Q] Quit
echo.

set /p choice=請輸入選項 [1-5/Q]: 

if "%choice%"=="1" goto check
if "%choice%"=="2" goto rebuild
if "%choice%"=="3" goto no_fk
if "%choice%"=="4" goto with_fk
if "%choice%"=="5" goto guide
if /i "%choice%"=="Q" goto end
goto invalid

:check
echo.
echo Executing diagnostic query...
mysql -h onekuji-lotery.cdi42o44miez.ap-northeast-1.rds.amazonaws.com -u admin -pAdmin@123456 kuji -e "SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE, COLUMN_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'kuji' AND TABLE_NAME IN ('store', 'user') AND COLUMN_NAME = 'id';"
echo.
echo Check complete!
echo.
echo Result explanation:
echo - If DATA_TYPE is bigint: Need option [2] or [3]
echo - If DATA_TYPE is varchar: Can use option [4]
echo.
pause
goto end

:rebuild
echo.
echo WARNING: This will DELETE all data in the database!
echo.
set /p confirm=Continue? (yes/no): 
if /i not "%confirm%"=="yes" (
    echo Operation cancelled.
    pause
    goto end
)

echo.
echo Step 1/4: Backup database...
mysqldump -h onekuji-lotery.cdi42o44miez.ap-northeast-1.rds.amazonaws.com -u admin -pAdmin@123456 kuji > backup_%date:~0,4%%date:~5,2%%date:~8,2%_%time:~0,2%%time:~3,2%%time:~6,2%.sql
echo Backup complete!
echo.

echo Step 2/4: Drop old database...
mysql -h onekuji-lotery.cdi42o44miez.ap-northeast-1.rds.amazonaws.com -u admin -pAdmin@123456 -e "DROP DATABASE IF EXISTS kuji;"
echo Drop complete!
echo.

echo Step 3/4: Create new database and execute DDL_UUID.sql...
mysql -h onekuji-lotery.cdi42o44miez.ap-northeast-1.rds.amazonaws.com -u admin -pAdmin@123456 -e "CREATE DATABASE kuji CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -h onekuji-lotery.cdi42o44miez.ap-northeast-1.rds.amazonaws.com -u admin -pAdmin@123456 kuji < doc\DDL_UUID.sql
echo Base tables created!
echo.

echo Step 4/4: Execute referral and address schema (with FK)...
mysql -h onekuji-lotery.cdi42o44miez.ap-northeast-1.rds.amazonaws.com -u admin -pAdmin@123456 kuji < src\main\resources\db\referral_address_schema_with_fk.sql
echo Referral and address tables created!
echo.

echo Database rebuild complete!
echo.
echo Next steps:
echo 1. Start application: mvn spring-boot:run
echo 2. DataInitializer will initialize base data
echo 3. Test referral code and address APIs
echo.
pause
goto end

:no_fk
echo.
echo Executing schema without foreign keys...
mysql -h onekuji-lotery.cdi42o44miez.ap-northeast-1.rds.amazonaws.com -u admin -pAdmin@123456 kuji < src\main\resources\db\referral_address_schema.sql
echo.
if errorlevel 1 (
    echo Execution failed! Check error messages.
) else (
    echo Tables created!
    echo.
    echo WARNING: Foreign key constraints are disabled
    echo Data integrity must be ensured by application layer
)
echo.
pause
goto end

:with_fk
echo.
echo Executing complete schema (requires UUID)...
mysql -h onekuji-lotery.cdi42o44miez.ap-northeast-1.rds.amazonaws.com -u admin -pAdmin@123456 kuji < src\main\resources\db\referral_address_schema_with_fk.sql
echo.
if errorlevel 1 (
    echo Execution failed!
    echo.
    echo Possible reasons:
    echo - store or user table id is not VARCHAR(36)
    echo - Run option [1] to check table structure
    echo - Or run option [2] to rebuild database
) else (
    echo Tables created (with foreign keys)!
)
echo.
pause
goto end

:guide
echo.
echo Opening fix guide...
start FOREIGN_KEY_FIX_GUIDE.md
pause
goto end

:invalid
echo.
echo Invalid option. Please try again.
echo.
pause
goto end

:end
echo.
echo Tool finished.
pause
