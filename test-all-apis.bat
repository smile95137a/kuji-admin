@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ================================
echo   KUJI 後台前台 API 完整測試
echo ================================
echo.

set BASE_URL=http://localhost:8080/api
set ADMIN_TOKEN=
set USER_TOKEN=
set LOTTERY_ID=
set STORE_ID=
set TEST_USER_ID=
set RESET_TOKEN=

set PASS_COUNT=0
set FAIL_COUNT=0

:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: 輔助函數
:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

:test_api_env
    echo.
    echo [測試] !API_TEST_NAME!
    echo 請求: !API_METHOD! !API_URL!

    if "!API_TOKEN!"=="" (
        set "has_token=0"
    ) else (
        set "has_token=1"
    )

    if "!API_METHOD!"=="GET" (
        if "!has_token!"=="1" (
            curl -s -X GET "!API_URL!" -H "Authorization: Bearer !API_TOKEN!" > temp_response.json
        ) else (
            curl -s -X GET "!API_URL!" > temp_response.json
        )
    ) else (
        if "!API_DATA!"=="" (
            if "!has_token!"=="1" (
                curl -s -X !API_METHOD! "!API_URL!" -H "Content-Type: application/json" -H "Authorization: Bearer !API_TOKEN!" > temp_response.json
            ) else (
                curl -s -X !API_METHOD! "!API_URL!" -H "Content-Type: application/json" > temp_response.json
            )
        ) else (
            echo !API_DATA! > temp_payload.json
            if "!has_token!"=="1" (
                curl -s -X !API_METHOD! "!API_URL!" -H "Content-Type: application/json" -H "Authorization: Bearer !API_TOKEN!" -d @temp_payload.json > temp_response.json
            ) else (
                curl -s -X !API_METHOD! "!API_URL!" -H "Content-Type: application/json" -d @temp_payload.json > temp_response.json
            )
            del temp_payload.json >nul 2>&1
        )
    )

    copy /Y temp_response.json last_response.json >nul 2>&1

    :: check response
    findstr /C:"\"success\":true" temp_response.json >nul
    if !errorlevel! equ 0 (
        if "!API_EXPECT!"=="true" (
            echo ✓ PASS
            set /a PASS_COUNT+=1
        ) else (
            echo ✗ FAIL - 預期失敗但成功了
            set /a FAIL_COUNT+=1
        )
    ) else (
        if "!API_EXPECT!"=="false" (
            echo ✓ PASS (預期失敗)
            set /a PASS_COUNT+=1
        ) else (
            echo ✗ FAIL
            type temp_response.json
            set /a FAIL_COUNT+=1
        )
    )

    del temp_response.json >nul 2>&1
    goto :eof

:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: 1. 後台測試
:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

echo.
echo ========================================
echo   第一部分：後台 API 測試
echo ========================================

:: 1.1 後台登入 (env-driven)
set "API_TEST_NAME=後台登入"
set "API_METHOD=POST"
set "API_URL=%BASE_URL%/admin/auth/login"
set "API_DATA={\"username\":\"admin@kuji.com\",\"password\":\"admin123\"}"
set "API_TOKEN="
set "API_EXPECT=true"
call :test_api_env

:: 從 last_response.json 提取 token（test_api 已將回應寫入 last_response.json）
set "token_line="
for /f "delims=" %%i in ('findstr /C:"accessToken" last_response.json 2^>nul') do set "token_line=%%i"
if defined token_line (
    for /f "tokens=2 delims=:,\" " %%a in ("!token_line!") do set "ADMIN_TOKEN=%%a"
    set "ADMIN_TOKEN=!ADMIN_TOKEN: =!"
)

echo.
echo 後台 Token: !ADMIN_TOKEN:~0,50!...

:: 1.2 取得使用者選單
set "API_TEST_NAME=取得使用者選單"
set "API_METHOD=GET"
set "API_URL=%BASE_URL%/admin/users/menu"
set "API_DATA="
set "API_TOKEN=!ADMIN_TOKEN!"
set "API_EXPECT=true"
call :test_api_env

:: 1.3 取得店家列表
set "API_TEST_NAME=取得店家列表"
set "API_METHOD=POST"
set "API_URL=%BASE_URL%/admin/store/list"
set "API_DATA={}" 
set "API_TOKEN=!ADMIN_TOKEN!"
set "API_EXPECT=true"
call :test_api_env

:: 1.4 取得所有縣市
set "API_TEST_NAME=取得所有縣市"
set "API_METHOD=GET"
set "API_URL=%BASE_URL%/admin/district/cities"
set "API_DATA="
set "API_TOKEN=!ADMIN_TOKEN!"
set "API_EXPECT=true"
call :test_api_env

:: 1.5 取得推薦碼列表
set "API_TEST_NAME=取得推薦碼"
set "API_METHOD=GET"
set "API_URL=%BASE_URL%/admin/referral-codes/my-store"
set "API_DATA="
set "API_TOKEN=!ADMIN_TOKEN!"
set "API_EXPECT=true"
call :test_api_env

:: 1.6 創建商品+獎品 (env-driven)
set "lottery_data={\"lottery\":{\"title\":\"測試一番賞\",\"pricePerDraw\":100,\"category\":\"OFFICIAL_ICHIBAN\",\"status\":\"OFF_SHELF\"},\"prizes\":[{\"name\":\"A賞\",\"quantity\":1,\"grade\":\"A\"}]}"
set "API_TEST_NAME=創建商品與獎品"
set "API_METHOD=POST"
set "API_URL=%BASE_URL%/admin/lottery-with-prizes"
set "API_DATA=!lottery_data!"
set "API_TOKEN=!ADMIN_TOKEN!"
set "API_EXPECT=true"
call :test_api_env

:: 從 last_response.json 提取 lottery ID
set "id_line="
for /f "delims=" %%i in ('findstr /C:"\"id\"" last_response.json 2^>nul') do set "id_line=%%i"
if defined id_line (
    for /f "tokens=2 delims=:,\" " %%a in ("!id_line!") do set "LOTTERY_ID=%%a"
    set "LOTTERY_ID=!LOTTERY_ID: =!"
)

if not "!LOTTERY_ID!"=="" (
    echo.
    echo 創建的商品 ID: !LOTTERY_ID!
    
    :: 1.7 查詢商品詳情
    set "API_TEST_NAME=查詢商品詳情"
    set "API_METHOD=GET"
    set "API_URL=%BASE_URL%/admin/lottery-with-prizes/!LOTTERY_ID!"
    set "API_DATA="
    set "API_TOKEN=!ADMIN_TOKEN!"
    set "API_EXPECT=true"
    call :test_api_env
    
    :: 1.8 更新商品
    set "update_data={\"lottery\":{\"title\":\"測試一番賞（更新）\",\"pricePerDraw\":120}}"
    set "API_TEST_NAME=更新商品"
    set "API_METHOD=PUT"
    set "API_URL=%BASE_URL%/admin/lottery-with-prizes/!LOTTERY_ID!"
    set "API_DATA=!update_data!"
    set "API_TOKEN=!ADMIN_TOKEN!"
    set "API_EXPECT=true"
    call :test_api_env
)

:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: 2. 前台測試
:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

echo.
echo ========================================
echo   第二部分：前台 API 測試
echo ========================================

:: 2.1 前台註冊 (env-driven)
set "timestamp=%random%"
set "test_email=test!timestamp!@example.com"
set "register_data={\"email\":\"!test_email!\",\"password\":\"Test123\",\"nickname\":\"測試用戶!timestamp!\"}"
set "API_TEST_NAME=前台註冊"
set "API_METHOD=POST"
set "API_URL=%BASE_URL%/auth/register"
set "API_DATA=!register_data!"
set "API_TOKEN="
set "API_EXPECT=true"
call :test_api_env

:: 從 last_response.json 提取 user token
set "token_line="
for /f "delims=" %%i in ('findstr /C:"accessToken" last_response.json 2^>nul') do set "token_line=%%i"
if defined token_line (
    for /f "tokens=2 delims=:,\" " %%a in ("!token_line!") do set "USER_TOKEN=%%a"
    set "USER_TOKEN=!USER_TOKEN: =!"
)

echo.
echo 前台 Token: !USER_TOKEN:~0,50!...

:: 2.2 前台登入
set "login_data={\"email\":\"!test_email!\",\"password\":\"Test123\"}"
    set "API_TEST_NAME=前台登入"
    set "API_METHOD=POST"
    set "API_URL=%BASE_URL%/auth/login"
    set "API_DATA=!login_data!"
    set "API_TOKEN="
    set "API_EXPECT=true"
    call :test_api_env

:: 2.3 忘記密碼
    set "API_TEST_NAME=忘記密碼請求"
    set "API_METHOD=POST"
    set "API_URL=%BASE_URL%/auth/forgot-password"
    set "API_DATA={\"email\":\"!test_email!\"}"
    set "API_TOKEN="
    set "API_EXPECT=true"
    call :test_api_env

:: 從資料庫模擬取得 reset token（實際測試需要從資料庫或郵件取得）
:: 這裡測試錯誤的 token
    set "API_TEST_NAME=重設密碼(無效token)"
    set "API_METHOD=POST"
    set "API_URL=%BASE_URL%/auth/reset-password"
    set "API_DATA={\"token\":\"invalid-token\",\"newPassword\":\"NewPass123\",\"confirmPassword\":\"NewPass123\"}"
    set "API_TOKEN="
    set "API_EXPECT=false"
    call :test_api_env

:: 2.4 縣市資料 API
set "API_TEST_NAME=取得所有縣市(前台)"
set "API_METHOD=GET"
set "API_URL=%BASE_URL%/district/cities"
set "API_DATA="
set "API_TOKEN="
set "API_EXPECT=true"
call :test_api_env

set "API_TEST_NAME=取得台北市行政區"
set "API_METHOD=GET"
set "API_URL=%BASE_URL%/district/districts/台北市"
set "API_DATA="
set "API_TOKEN="
set "API_EXPECT=true"
call :test_api_env

set "API_TEST_NAME=取得行政區樹狀結構"
set "API_METHOD=GET"
set "API_URL=%BASE_URL%/district/tree"
set "API_DATA="
set "API_TOKEN="
set "API_EXPECT=true"
call :test_api_env

set "API_TEST_NAME=查詢指定行政區"
set "API_METHOD=GET"
set "API_URL=%BASE_URL%/district?city=台北市&district=中正區"
set "API_DATA="
set "API_TOKEN="
set "API_EXPECT=true"
call :test_api_env

:: 2.5 Token 刷新
if not "!USER_TOKEN!"=="" (
    set "refresh_data={\"refreshToken\":\"!USER_TOKEN!\"}"
    set "API_TEST_NAME=Token 刷新"
    set "API_METHOD=POST"
    set "API_URL=%BASE_URL%/auth/refresh"
    set "API_DATA=!refresh_data!"
    set "API_TOKEN="
    set "API_EXPECT=true"
    call :test_api_env
)

:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: 3. 權限測試
:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

echo.
echo ========================================
echo   第三部分：權限與錯誤處理測試
echo ========================================

:: 3.1 未登入訪問需要認證的 API（應該失敗）
set "API_TEST_NAME=未登入訪問後台"
set "API_METHOD=GET"
set "API_URL=%BASE_URL%/admin/users/menu"
set "API_DATA="
set "API_TOKEN="
set "API_EXPECT=false"
call :test_api_env

:: 3.2 前台 token 訪問後台（應該失敗）
set "API_TEST_NAME=前台token訪問後台"
set "API_METHOD=GET"
set "API_URL=%BASE_URL%/admin/users/menu"
set "API_DATA="
set "API_TOKEN=!USER_TOKEN!"
set "API_EXPECT=false"
call :test_api_env

:: 3.3 後台 token 訪問前台公開 API（應該成功）
set "API_TEST_NAME=後台token訪問前台公開API"
set "API_METHOD=GET"
set "API_URL=%BASE_URL%/district/cities"
set "API_DATA="
set "API_TOKEN=!ADMIN_TOKEN!"
set "API_EXPECT=true"
call :test_api_env

:: 3.4 錯誤的登入資訊
set "API_TEST_NAME=錯誤密碼登入"
set "API_METHOD=POST"
set "API_URL=%BASE_URL%/admin/auth/login"
set "API_DATA={\"username\":\"admin@kuji.com\",\"password\":\"wrongpass\"}"
set "API_TOKEN="
set "API_EXPECT=false"
call :test_api_env

:: 3.5 註冊重複 Email
set "API_TEST_NAME=註冊重複Email"
set "API_METHOD=POST"
set "API_URL=%BASE_URL%/auth/register"
set "API_DATA=!register_data!"
set "API_TOKEN="
set "API_EXPECT=false"
call :test_api_env

:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: 4. 顯示測試結果
:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

echo.
echo ========================================
echo   測試結果摘要
echo ========================================
echo.
echo 通過: %PASS_COUNT%
echo 失敗: %FAIL_COUNT%
echo.

if %FAIL_COUNT% gtr 0 (
    echo ========================================
    echo   測試失敗
    echo ========================================
    echo.
    exit /b 1
) else (
    echo ========================================
    echo   所有測試通過！
    echo ========================================
    echo.
    exit /b 0
)

endlocal
