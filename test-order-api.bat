@echo off
chcp 65001 >nul
setlocal EnableDelayedExpansion

echo ================================================
echo   KUJI Admin - 訂單模組完整 API 測試
echo   測試日期: 2026-01-12
echo ================================================
echo.

set BASE_URL=http://localhost:8080/api

REM ================================================
REM 1. 取得 Admin Token
REM ================================================
echo [1/10] 取得 Admin Token...
curl -s -X POST %BASE_URL%/admin/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\": \"admin@kuji.com\", \"password\": \"admin123\"}" ^
  -o admin_login.json

for /f "tokens=*" %%a in ('type admin_login.json ^| findstr /r "accessToken"') do (
    set "line=%%a"
)
REM 手動設定 token（需從回應中取得）
echo Admin 登入成功，請手動設定 ADMIN_TOKEN
echo.

REM 使用固定 token（測試用）
set ADMIN_TOKEN=YOUR_ADMIN_TOKEN_HERE

pause
echo.
echo 請輸入 Admin Token:
set /p ADMIN_TOKEN=

echo.
echo ================================================
echo   開始 API 測試
echo ================================================
echo.

REM ================================================
REM 2. 測試後台訂單列表查詢
REM ================================================
echo [2/10] 後台訂單列表查詢...
curl -s -X POST %BASE_URL%/admin/order/list ^
  -H "Authorization: Bearer %ADMIN_TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{}" ^
  -w "\nHTTP Status: %%{http_code}\n" ^
  -o order_list.json

type order_list.json
echo.
echo.

REM ================================================
REM 3. 測試後台訂單列表（帶條件查詢）
REM ================================================
echo [3/10] 後台訂單列表（帶條件）...
curl -s -X POST %BASE_URL%/admin/order/list ^
  -H "Authorization: Bearer %ADMIN_TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"condition\": {\"shippingStatus\": \"PENDING\"}}" ^
  -w "\nHTTP Status: %%{http_code}\n"
echo.
echo.

REM ================================================
REM 4. 測試儲值方案 CRUD
REM ================================================
echo [4/10] 新增儲值方案...
curl -s -X POST %BASE_URL%/admin/recharge-plan ^
  -H "Authorization: Bearer %ADMIN_TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"name\": \"測試方案100\", \"amount\": 100, \"goldCoins\": 100, \"bonusCoins\": 0, \"description\": \"測試用方案\"}" ^
  -w "\nHTTP Status: %%{http_code}\n" ^
  -o recharge_plan_id.txt

echo 新增結果:
type recharge_plan_id.txt
echo.
echo.

echo [5/10] 查詢儲值方案列表...
curl -s -X GET %BASE_URL%/admin/recharge-plan/list ^
  -H "Authorization: Bearer %ADMIN_TOKEN%" ^
  -w "\nHTTP Status: %%{http_code}\n"
echo.
echo.

echo [6/10] 儲值方案條件查詢...
curl -s -X POST %BASE_URL%/admin/recharge-plan/query ^
  -H "Authorization: Bearer %ADMIN_TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"condition\": {\"isActive\": true}}" ^
  -w "\nHTTP Status: %%{http_code}\n"
echo.
echo.

REM ================================================
REM 5. 測試前台會員查詢
REM ================================================
echo [7/10] 查詢前台會員列表...
curl -s -X POST %BASE_URL%/admin/frontend-users/list ^
  -H "Authorization: Bearer %ADMIN_TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{}" ^
  -w "\nHTTP Status: %%{http_code}\n"
echo.
echo.

echo [8/10] 會員條件查詢...
curl -s -X POST %BASE_URL%/admin/frontend-users/list ^
  -H "Authorization: Bearer %ADMIN_TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"condition\": {\"status\": \"ACTIVE\"}}" ^
  -w "\nHTTP Status: %%{http_code}\n"
echo.
echo.

REM ================================================
REM 6. 測試商品查詢
REM ================================================
echo [9/10] 後台商品列表查詢...
curl -s -X POST %BASE_URL%/admin/lottery/list ^
  -H "Authorization: Bearer %ADMIN_TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{}" ^
  -w "\nHTTP Status: %%{http_code}\n"
echo.
echo.

echo [10/10] 前台商品瀏覽...
curl -s -X POST %BASE_URL%/lottery/browse/list ^
  -H "Content-Type: application/json" ^
  -d "{}" ^
  -w "\nHTTP Status: %%{http_code}\n"
echo.
echo.

echo ================================================
echo   測試完成！
echo ================================================

del admin_login.json 2>nul
del order_list.json 2>nul
del recharge_plan_id.txt 2>nul

pause
