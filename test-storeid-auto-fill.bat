@echo off
chcp 65001 >nul
echo ========================================
echo  測試 StoreID 自動帶入功能
echo ========================================
echo.

REM 設定變數
set BASE_URL=http://localhost:8080/api

echo [1/3] 登入 StoreOwner 帳號...
curl -X POST %BASE_URL%/admin/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"owner@teststore.com\",\"password\":\"Test1234\"}" ^
  -o login-response.json
echo.

REM 從回應中提取 token（手動複製）
echo.
echo ⚠️ 請從 login-response.json 複製 token，然後按任意鍵繼續...
pause
echo.

REM 請手動設定 TOKEN
set /p TOKEN="請貼上 Token: "
echo.

echo [2/3] 測試新增商品（storeId 為空字串）...
curl -X POST %BASE_URL%/admin/lottery ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %TOKEN%" ^
  -d "{\"storeId\":\"\",\"title\":\"測試商品 - 自動帶入\",\"description\":\"測試 storeId 自動帶入功能\",\"category\":\"OFFICIAL_ICHIBAN\",\"pricePerDraw\":100,\"startTime\":\"2026-01-10T10:00:00\",\"endTime\":\"2026-12-31T23:59:59\"}" ^
  -o create-response.json
echo.

echo [3/3] 查看結果...
type create-response.json
echo.
echo.

echo ========================================
echo  測試完成！
echo ========================================
echo.
echo 預期結果：
echo - success: true
echo - data.storeId: （自動帶入的店家 ID）
echo.
echo 如果還是返回「店家ID不可為空」，表示：
echo 1. 舊的 JAR 還在運行（需要重啟）
echo 2. LotteryCreateReq 還有 @NotBlank 驗證
echo.

pause
