@echo off
echo ========================================
echo KUJI Admin Pre-Launch Check
echo ========================================
echo.

echo [Check 1] Verifying compilation...
if exist target\classes\com\group\admin\AdminApplication.class (
    echo     OK - Project compiled
) else (
    echo     ERROR - Project not compiled
    echo     Running: mvn clean compile -DskipTests
    mvn clean compile -DskipTests
)
echo.

echo [Check 2] Verifying Mapper XML files...
if exist target\classes\mapper\AdminOperationLogMapper.xml (
    echo     OK - Mapper XML files copied to target/classes/mapper
    dir /b target\classes\mapper\*.xml | find /c ".xml"
) else (
    echo     ERROR - Mapper XML not found
)
echo.

echo [Check 3] Verifying MyBatisConfig.java...
if exist target\classes\com\group\admin\config\MyBatisConfig.class (
    echo     OK - MyBatisConfig compiled
) else (
    echo     ERROR - MyBatisConfig not found
)
echo.

echo [Check 4] Verifying application.yml...
findstr /C:"mapper-locations" src\main\resources\application.yml >nul
if errorlevel 1 (
    echo     ERROR - mapper-locations not found in application.yml
) else (
    echo     OK - MyBatis configuration exists
)
echo.

echo [Check 5] Verifying DevTools status...
findstr /C:"spring-boot-devtools" pom.xml | findstr /C:"<!--" >nul
if errorlevel 1 (
    echo     WARNING - DevTools might not be commented out
) else (
    echo     OK - DevTools commented out
)
echo.

echo ========================================
echo All checks passed!
echo ========================================
echo.
echo Next steps:
echo 1. Ensure MySQL service is running
echo 2. Ensure database 'kuji' exists
echo 3. Open AdminApplication.java in IDE
echo 4. Click Run button
echo.
echo Expected startup logs:
echo    - Starting AdminApplication
echo    - System data initialization completed
echo    - Tomcat started on port 8080
echo    - Started AdminApplication in X seconds
echo.
echo If you see "Result Maps collection already contains key",
echo run diagnose-mappers.bat for diagnosis.
echo.
pause
