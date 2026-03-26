@echo off
chcp 65001 >nul
echo ========================================
echo KUJI 後端快速部署腳本
echo ========================================
echo.
echo 📦 打包並上傳到 EC2...
echo.

REM 檢查 Maven 打包結果
if not exist "target\admin-1.0.0.jar" (
    echo [步驟 1] Maven 打包...
    call mvn clean package -DskipTests -Pprod
    if %ERRORLEVEL% NEQ 0 (
        echo ❌ 打包失敗
        pause
        exit /b 1
    )
) else (
    echo ✅ 使用現有的 JAR 檔案
)

REM 設定變數
set EC2_HOST=ec2-user@18.179.187.129
set KEY_FILE=C:\Users\user\OneDrive\Desktop\dream\ourkuji\ourkuji.pem
set REMOTE_DIR=/home/ec2-user/kuji-backend

echo.
echo [步驟 2] 建立遠端目錄...
ssh -i "%KEY_FILE%" %EC2_HOST% "mkdir -p %REMOTE_DIR%/logs"

echo.
echo [步驟 3] 上傳檔案到 EC2...
scp -i "%KEY_FILE%" target\admin-1.0.0.jar %EC2_HOST%:%REMOTE_DIR%/
scp -i "%KEY_FILE%" start.sh %EC2_HOST%:%REMOTE_DIR%/
scp -i "%KEY_FILE%" stop.sh %EC2_HOST%:%REMOTE_DIR%/

echo.
echo [步驟 4] 重啟應用程式...
ssh -i "%KEY_FILE%" %EC2_HOST% "cd %REMOTE_DIR% && chmod +x *.sh && ./stop.sh && sleep 2 && ./start.sh"

echo.
echo ========================================
echo ✅ 部署完成！
echo ========================================
echo.
echo 🌐 後端 API: http://18.179.187.129:8080/api
echo 🏥 健康檢查: http://18.179.187.129:8080/actuator/health
echo 📊 測試登入: http://18.179.187.129:8080/api/admin/auth/login
echo.
echo 📝 查看日誌:
echo   ssh -i "%KEY_FILE%" %EC2_HOST% "tail -f %REMOTE_DIR%/logs/application.log"
echo.
echo 🔍 檢查狀態:
echo   ssh -i "%KEY_FILE%" %EC2_HOST% "ps aux | grep java"
echo.

REM 自動測試健康檢查（等待 10 秒）
echo 等待應用程式啟動...
timeout /t 10 /nobreak >nul

echo.
echo 測試健康檢查...
curl -s http://18.179.187.129:8080/actuator/health
echo.
echo.

pause
