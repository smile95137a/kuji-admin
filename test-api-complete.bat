@echo off
chcp 65001 >nul
echo ========================================
echo 🧪 KUJI API 完整測試腳本
echo ========================================
echo.

REM 設定 Base URL
set BASE_URL=http://localhost:8080/api

echo [提示] 請確認後端已啟動在 %BASE_URL%
echo.
pause
echo.

REM 測試變數
set ADMIN_TOKEN=
set USER_TOKEN=
set LOTTERY_ID=

echo ========================================
echo 測試 1: 後台登入
echo ========================================
echo.

curl -X POST "%BASE_URL%/admin/auth/login" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"admin@kuji.com\",\"password\":\"admin123\"}" ^
  -o temp_admin_login.json

echo.
echo 回應已儲存到 temp_admin_login.json
echo 請從檔案中複製 token 並設定到環境變數
echo.
pause
echo.

echo ========================================
echo 測試 2: 建立商品+獎品（整合 API）
echo ========================================
echo.
echo 請輸入 Admin Token:
set /p ADMIN_TOKEN=

curl -X POST "%BASE_URL%/admin/lottery-with-prizes" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %ADMIN_TOKEN%" ^
  -d "{\"lottery\":{\"title\":\"測試商品\",\"description\":\"API測試用\",\"category\":\"OFFICIAL_ICHIBAN\",\"pricePerDraw\":80,\"totalDraws\":100,\"status\":\"ON_SHELF\"},\"prizes\":[{\"name\":\"A賞\",\"level\":\"A\",\"quantity\":1,\"weight\":5},{\"name\":\"B賞\",\"level\":\"B\",\"quantity\":5,\"weight\":10},{\"name\":\"C賞\",\"level\":\"C\",\"quantity\":20,\"weight\":30}]}" ^
  -o temp_create_lottery.json

echo.
echo 回應已儲存到 temp_create_lottery.json
echo 請從檔案中複製 lottery.id
echo.
pause
echo.

echo ========================================
echo 測試 3: 查詢商品列表
echo ========================================
echo.

curl -X POST "%BASE_URL%/api/lottery/list" ^
  -H "Content-Type: application/json" ^
  -d "{\"condition\":{\"status\":\"ON_SHELF\"}}" ^
  -o temp_lottery_list.json

echo.
echo 回應已儲存到 temp_lottery_list.json
echo.
pause
echo.

echo ========================================
echo 測試 4: 前台用戶註冊
echo ========================================
echo.

curl -X POST "%BASE_URL%/api/auth/register" ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"testuser\",\"email\":\"test@example.com\",\"password\":\"password123\",\"phone\":\"0912345678\"}" ^
  -o temp_user_register.json

echo.
echo 回應已儲存到 temp_user_register.json
echo.
pause
echo.

echo ========================================
echo 測試 5: 前台用戶登入
echo ========================================
echo.

curl -X POST "%BASE_URL%/api/auth/login" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"test@example.com\",\"password\":\"password123\"}" ^
  -o temp_user_login.json

echo.
echo 回應已儲存到 temp_user_login.json
echo 請從檔案中複製 token
echo.
pause
echo.

echo ========================================
echo 測試 6: 查詢用戶錢包
echo ========================================
echo.
echo 請輸入 User Token:
set /p USER_TOKEN=

curl -X GET "%BASE_URL%/api/wallet/my" ^
  -H "Authorization: Bearer %USER_TOKEN%" ^
  -o temp_wallet.json

echo.
echo 回應已儲存到 temp_wallet.json
echo.
pause
echo.

echo ========================================
echo 測試 7: 抽獎
echo ========================================
echo.
echo 請輸入 Lottery ID:
set /p LOTTERY_ID=

curl -X POST "%BASE_URL%/api/lottery/random/%LOTTERY_ID%/draw" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %USER_TOKEN%" ^
  -d "{\"drawCount\":1}" ^
  -o temp_draw.json

echo.
echo 回應已儲存到 temp_draw.json
echo.
pause
echo.

echo ========================================
echo 測試 8: 查詢獎品池
echo ========================================
echo.

curl -X GET "%BASE_URL%/api/prize-box/my" ^
  -H "Authorization: Bearer %USER_TOKEN%" ^
  -o temp_prize_box.json

echo.
echo 回應已儲存到 temp_prize_box.json
echo.
pause
echo.

echo ========================================
echo ✅ 測試完成！
echo ========================================
echo.
echo 所有回應已儲存為 temp_*.json 檔案
echo 請檢查檔案內容確認 API 運作正常
echo.
echo 清理臨時檔案？
pause
del temp_*.json 2>nul
echo.
echo 完成！
pause
