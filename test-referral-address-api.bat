@echo off
REM ====================================================
REM 推薦碼 & 使用者地址 API 測試腳本
REM ====================================================

setlocal EnableDelayedExpansion

echo ====================================================
echo        KUJI 推薦碼 / 地址系統 API 測試
echo ====================================================
echo.

REM 設定 Base URL
set BASE_URL=http://localhost:8080/api

REM ====================================================
REM 1. 登入取得 Admin Token
REM ====================================================
echo [1] 登入取得 Admin Token...
curl -s -X POST "%BASE_URL%/admin/auth/login" ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"admin@kuji.com\",\"password\":\"admin123\"}" ^
  > admin_login.json

echo 登入回應已儲存至 admin_login.json
echo.

REM 手動設定 Token（從登入回應中取得後填入）
set ADMIN_TOKEN=YOUR_ADMIN_TOKEN_HERE

echo ====================================================
echo        ⚠️  請先執行資料庫 SQL 腳本
echo        src/main/resources/db/referral_address_schema.sql
echo ====================================================
echo.
pause

REM ====================================================
REM 2. 推薦碼 API 測試
REM ====================================================
echo.
echo [2] 測試推薦碼 API
echo.

REM 2.1 建立推薦碼
echo [2.1] 建立推薦碼...
curl -s -X POST "%BASE_URL%/admin/referral-codes" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %ADMIN_TOKEN%" ^
  -d "{\"code\":\"TEST2024\",\"description\":\"測試推薦碼\"}" ^
  > create_referral_code.json
echo 回應已儲存至 create_referral_code.json
type create_referral_code.json
echo.
echo.

REM 2.2 查詢所有推薦碼
echo [2.2] 查詢所有推薦碼...
curl -s -X GET "%BASE_URL%/admin/referral-codes" ^
  -H "Authorization: Bearer %ADMIN_TOKEN%" ^
  > list_referral_codes.json
echo 回應已儲存至 list_referral_codes.json
type list_referral_codes.json
echo.
echo.

REM 2.3 驗證推薦碼（公開 API）
echo [2.3] 驗證推薦碼（公開）...
curl -s -X GET "%BASE_URL%/api/auth/referral-code/validate/TEST2024" ^
  > validate_code.json
echo 回應已儲存至 validate_code.json
type validate_code.json
echo.
echo.

REM ====================================================
REM 3. 使用者地址 API 測試
REM ====================================================
echo.
echo [3] 測試使用者地址 API
echo.

REM 需要 User Token（可以用 Admin Token 測試，因為 API Filter 支援）
set USER_TOKEN=%ADMIN_TOKEN%

REM 3.1 新增地址
echo [3.1] 新增使用者地址...
curl -s -X POST "%BASE_URL%/user/addresses" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %USER_TOKEN%" ^
  -d "{\"label\":\"家\",\"recipientName\":\"王小明\",\"recipientPhone\":\"0912345678\",\"city\":\"台北市\",\"district\":\"信義區\",\"zipCode\":\"110\",\"address\":\"信義路五段7號\"}" ^
  > create_address.json
echo 回應已儲存至 create_address.json
type create_address.json
echo.
echo.

REM 3.2 查詢所有地址
echo [3.2] 查詢所有地址...
curl -s -X GET "%BASE_URL%/user/addresses" ^
  -H "Authorization: Bearer %USER_TOKEN%" ^
  > list_addresses.json
echo 回應已儲存至 list_addresses.json
type list_addresses.json
echo.
echo.

REM 3.3 查詢預設地址
echo [3.3] 查詢預設地址...
curl -s -X GET "%BASE_URL%/user/addresses/default" ^
  -H "Authorization: Bearer %USER_TOKEN%" ^
  > default_address.json
echo 回應已儲存至 default_address.json
type default_address.json
echo.
echo.

REM ====================================================
echo.
echo ====================================================
echo        測試完成！
echo ====================================================
echo.
echo 測試結果檔案：
echo   - admin_login.json
echo   - create_referral_code.json
echo   - list_referral_codes.json
echo   - validate_code.json
echo   - create_address.json
echo   - list_addresses.json
echo   - default_address.json
echo.

pause
