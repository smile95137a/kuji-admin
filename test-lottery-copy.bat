@echo off
chcp 65001 >nul
echo ========================================
echo 複製商品功能測試腳本
echo ========================================
echo.

REM 設定 API Base URL
set BASE_URL=http://localhost:8080/api

REM 提示使用者輸入 Token
echo [步驟 1] 請先登入取得 Token
echo.
set /p ADMIN_TOKEN="請輸入 Admin Token: "
echo.

REM 提示使用者輸入來源商品 ID
echo [步驟 2] 請輸入要複製的商品 ID
echo.
set /p SOURCE_LOTTERY_ID="請輸入來源商品 ID (UUID): "
echo.

REM 提示使用者是否指定新標題
echo [步驟 3] 是否指定新標題？(Y/N)
set /p USE_NEW_TITLE="請選擇 (預設 N): "
echo.

if /I "%USE_NEW_TITLE%"=="Y" (
    set /p NEW_TITLE="請輸入新標題: "
    set "BODY={\"sourceLotteryId\":\"%SOURCE_LOTTERY_ID%\",\"newTitle\":\"%NEW_TITLE%\"}"
) else (
    set "BODY={\"sourceLotteryId\":\"%SOURCE_LOTTERY_ID%\"}"
)

echo.
echo ========================================
echo 正在發送複製請求...
echo ========================================
echo.
echo 請求內容: %BODY%
echo.

REM 發送請求
curl -X POST %BASE_URL%/admin/lottery/copy ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %ADMIN_TOKEN%" ^
  -d "%BODY%" ^
  -v

echo.
echo.
echo ========================================
echo 測試完成
echo ========================================
echo.
echo 如果成功，應該會看到 HTTP/1.1 200 OK
echo 以及新商品的完整資料
echo.
pause
