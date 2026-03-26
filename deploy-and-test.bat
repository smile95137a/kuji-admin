@echo off
chcp 65001 >nul
echo ========================================
echo KUJI 完整部署與測試流程
echo ========================================
echo.

REM 設定變數
set EC2_HOST=ec2-user@18.179.187.129
set KEY_FILE=C:\Users\user\OneDrive\Desktop\dream\ourkuji\ourkuji.pem
set REMOTE_DIR=/home/ec2-user

echo [步驟 1/5] 檢查 JAR 檔案...
if not exist "target\admin-1.0.0.jar" (
    echo ⚠️  JAR 檔案不存在，開始打包...
    call mvn clean package -DskipTests -Pprod
    if %ERRORLEVEL% NEQ 0 (
        echo ❌ 打包失敗
        pause
        exit /b 1
    )
) else (
    echo ✅ JAR 檔案已存在
)

echo.
echo [步驟 2/5] 上傳到 EC2...
scp -i "%KEY_FILE%" target\admin-1.0.0.jar %EC2_HOST%:%REMOTE_DIR%/
scp -i "%KEY_FILE%" start.sh %EC2_HOST%:%REMOTE_DIR%/
scp -i "%KEY_FILE%" stop.sh %EC2_HOST%:%REMOTE_DIR%/

echo.
echo [步驟 3/5] 重啟應用程式...
ssh -i "%KEY_FILE%" %EC2_HOST% "cd %REMOTE_DIR% && chmod +x *.sh && ./stop.sh && sleep 3 && ./start.sh"

echo.
echo [步驟 4/5] 等待應用程式啟動...
timeout /t 10 /nobreak >nul

echo.
echo [步驟 5/5] 測試應用程式...

echo.
echo 📍 測試 1: 健康檢查
curl -s http://18.179.187.129:8080/actuator/health
echo.

echo.
echo 📍 測試 2: CORS Preflight
curl -H "Origin: http://18.179.187.129" ^
     -H "Access-Control-Request-Method: POST" ^
     -H "Access-Control-Request-Headers: Content-Type" ^
     -X OPTIONS ^
     http://18.179.187.129:8080/api/admin/auth/login ^
     -I
echo.

echo.
echo 📍 測試 3: 登入 API
curl -X POST http://18.179.187.129:8080/api/admin/auth/login ^
  -H "Content-Type: application/json" ^
  -H "Origin: http://18.179.187.129" ^
  -d "{\"email\":\"admin@kuji.com\",\"password\":\"admin123\"}"
echo.

echo.
echo ========================================
echo ✅ 部署與測試完成！
echo ========================================
echo.
echo 🌐 系統資訊:
echo   - 後端 API: http://18.179.187.129:8080/api
echo   - 健康檢查: http://18.179.187.129:8080/actuator/health
echo   - 前端 URL: http://18.179.187.129/kuji/login
echo.
echo 📋 下一步:
echo   1. 在瀏覽器開啟前端: http://18.179.187.129/kuji/login
echo   2. 使用測試帳號登入: admin@kuji.com / admin123
echo   3. 如果還有 CORS 問題，查看日誌:
echo      ssh -i "%KEY_FILE%" %EC2_HOST% "tail -f %REMOTE_DIR%/logs/application.log"
echo.

pause
