ㄌˋ@echo off
chcp 65001 >nul
echo ========================================
echo KUJI 後端部署到 EC2 腳本
echo ========================================
echo.

REM 設定變數
set EC2_HOST=ec2-user@18.179.187.129
set KEY_FILE=C:\Users\user\OneDrive\Desktop\dream\ourkuji\ourkuji.pem
set APP_NAME=admin
set JAR_NAME=admin-1.0.0.jar
set REMOTE_DIR=/home/ec2-user/kuji-backend

echo [1/6] 清理舊的建置檔案...
call mvn clean
if %ERRORLEVEL% NEQ 0 (
    echo ❌ 清理失敗
    pause
    exit /b 1
)

echo.
echo [2/6] 打包專案（跳過測試）...
call mvn clean package -DskipTests -Pprod
if %ERRORLEVEL% NEQ 0 (
    echo ❌ 打包失敗
    pause
    exit /b 1
)

echo.
echo [3/6] 檢查 JAR 檔案...
if not exist "target\%JAR_NAME%" (
    echo ❌ JAR 檔案不存在: target\%JAR_NAME%
    pause
    exit /b 1
)
echo ✅ JAR 檔案存在

echo.
echo [4/6] 上傳 JAR 到 EC2...
scp -i "%KEY_FILE%" target\%JAR_NAME% %EC2_HOST%:%REMOTE_DIR%/
if %ERRORLEVEL% NEQ 0 (
    echo ❌ 上傳失敗
    pause
    exit /b 1
)
echo ✅ 上傳成功

echo.
echo [5/6] 停止舊的應用程式...
ssh -i "%KEY_FILE%" %EC2_HOST% "cd %REMOTE_DIR% && chmod +x stop.sh && ./stop.sh"

echo.
echo [6/6] 啟動新的應用程式...
ssh -i "%KEY_FILE%" %EC2_HOST% "cd %REMOTE_DIR% && chmod +x start.sh && ./start.sh"

echo.
echo ========================================
echo ✅ 部署完成！
echo ========================================
echo.
echo 應用資訊：
echo - URL: http://18.179.187.129:8080/api
echo - 健康檢查: http://18.179.187.129:8080/actuator/health
echo.
echo 查看日誌：
echo   ssh -i "%KEY_FILE%" %EC2_HOST% "tail -f %REMOTE_DIR%/logs/application.log"
echo.
pause
