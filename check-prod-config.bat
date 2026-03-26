@echo off
chcp 65001 >nul
echo ====================================================
echo KUJI Admin Production Configuration Check
echo ====================================================
echo.

echo [1/5] Checking application.yml...
findstr /C:"active: prod" src\main\resources\application.yml >nul
if %errorlevel%==0 (
    echo [OK] Default profile is set to prod
) else (
    echo [WARN] Default profile is not prod!
)
echo.

echo [2/5] Checking application-prod.yml...
findstr /C:"database-1.clsi2geo699r.ap-northeast-1.rds.amazonaws.com" src\main\resources\application-prod.yml >nul
if %errorlevel%==0 (
    echo [OK] RDS endpoint configured
) else (
    echo [ERROR] RDS endpoint not found!
)

findstr /C:"test-ourkuji" src\main\resources\application-prod.yml >nul
if %errorlevel%==0 (
    echo [OK] S3 bucket configured
) else (
    echo [ERROR] S3 bucket not found!
)
echo.

echo [3/5] Checking pom.xml...
findstr /C:"software.amazon.awssdk" pom.xml >nul
if %errorlevel%==0 (
    echo [OK] AWS SDK dependencies added
) else (
    echo [ERROR] AWS SDK dependencies not found!
)
echo.

echo [4/5] Checking S3 implementation files...
if exist "src\main\java\com\group\admin\config\S3Config.java" (
    echo [OK] S3Config.java exists
) else (
    echo [ERROR] S3Config.java not found!
)

if exist "src\main\java\com\group\admin\service\impl\S3ServiceImpl.java" (
    echo [OK] S3ServiceImpl.java exists
) else (
    echo [ERROR] S3ServiceImpl.java not found!
)
echo.

echo [5/5] Checking deployment files...
if exist "deploy.sh" (
    echo [OK] deploy.sh exists
) else (
    echo [WARN] deploy.sh not found
)

if exist "PRODUCTION_DEPLOYMENT_GUIDE.md" (
    echo [OK] Deployment guide exists
) else (
    echo [WARN] Deployment guide not found
)
echo.

echo ====================================================
echo Configuration Check Complete!
echo ====================================================
echo.
echo Next steps:
echo 1. mvn clean package -DskipTests
echo 2. Upload to EC2: scp target/admin-1.0.0.jar ec2-user@18.179.187.129:/home/ec2-user/
echo 3. SSH to EC2 and run deploy.sh
echo.
pause
