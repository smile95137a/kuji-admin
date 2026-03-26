@echo off
chcp 65001 >nul
echo ========================================
echo 🚀 快速修正推薦碼 403 + 部署獎品系統
echo ========================================
echo.

cd /d c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin

echo ⏳ Step 1/5: 重新編譯...
call mvn clean package -DskipTests
if errorlevel 1 (
    echo ❌ 編譯失敗！
    pause
    exit /b 1
)

echo.
echo ✅ Step 1/5: 編譯完成
echo.
echo ⏳ Step 2/5: 上傳到 EC2...
scp -i C:\Users\user\OneDrive\Desktop\dream\ourkuji\ourkuji.pem target\admin-1.0.0.jar ec2-user@18.179.187.129:/home/ec2-user/

echo.
echo ✅ Step 2/5: 上傳完成
echo.
echo ⏳ Step 3/5: 部署並重啟...
ssh -i C:\Users\user\OneDrive\Desktop\dream\ourkuji\ourkuji.pem ec2-user@18.179.187.129 "cd /home/ec2-user && pkill -f admin-1.0.0.jar ; sleep 2 ; nohup java -jar admin-1.0.0.jar --spring.profiles.active=prod > app.log 2>&1 &"

echo.
echo ✅ Step 3/5: 重啟完成
echo.
echo ⏳ Step 4/5: 等待啟動（10秒）...
timeout /t 10 /nobreak

echo.
echo ✅ Step 4/5: 等待完成
echo.
echo ⏳ Step 5/5: 測試推薦碼 API...

echo.
echo 🧪 測試 1: GET /admin/referral-codes/validate/TEST001
curl -X GET "http://18.179.187.129:8080/api/admin/referral-codes/validate/TEST001" ^
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" ^
  -H "Content-Type: application/json"

echo.
echo.
echo 🧪 測試 2: GET /admin/referral-codes （需 ADMIN 權限）
curl -X GET "http://18.179.187.129:8080/api/admin/referral-codes" ^
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" ^
  -H "Content-Type: application/json"

echo.
echo.
echo ========================================
echo ✅ 部署完成！請手動測試以下 API：
echo.
echo 1. 登入取得 Token:
echo    POST http://18.179.187.129:8080/api/admin/auth/login
echo    Body: {"email":"admin@kuji.com","password":"admin123"}
echo.
echo 2. 測試推薦碼 API（帶 Token）:
echo    GET http://18.179.187.129:8080/api/admin/referral-codes/validate/TEST001
echo    Header: Authorization: Bearer YOUR_TOKEN
echo.
echo 3. 如果還是 403，請查看日誌:
echo    ssh -i C:\Users\user\OneDrive\Desktop\dream\ourkuji\ourkuji.pem ec2-user@18.179.187.129
echo    tail -f app.log
echo ========================================
pause
