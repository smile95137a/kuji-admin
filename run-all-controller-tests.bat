@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ================================================
echo  批量執行所有 Controller 測試
echo ================================================

set BASE_DIR=c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin

REM 切換到專案目錄
cd /d "%BASE_DIR%"

echo.
echo [1] 清理舊的測試結果...
echo ================================================
call mvn clean test-compile -q

echo.
echo [2] 執行所有測試...
echo ================================================
call mvn test -Dtest=**/*ControllerTest

echo.
echo [3] 生成測試報告...
echo ================================================
if exist target\surefire-reports (
    echo 測試報告位置: target\surefire-reports
    dir target\surefire-reports /b | find "TEST-" > nul
    if !errorlevel! equ 0 (
        echo ✅ 測試完成！
        for %%f in (target\surefire-reports\TEST-*.xml) do (
            echo   - %%~nxf
        )
    ) else (
        echo ⚠️ 未找到測試報告
    )
) else (
    echo ❌ 測試報告目錄不存在
)

echo.
echo ================================================
echo  測試執行完成！
echo ================================================
echo.
echo 查看詳細報告:
echo   target\surefire-reports\*.txt
echo   target\surefire-reports\*.xml
echo.

pause
