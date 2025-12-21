@echo off
REM KUJI Admin System - 快速測試腳本
REM 此腳本會測試所有主要 API 端點

echo ========================================
echo KUJI Admin System - API 快速測試
echo ========================================
echo.

set BASE_URL=http://localhost:8080/api

echo [1/11] 測試健康檢查...
curl -s %BASE_URL%/test/health
echo.
echo.

echo [2/11] 測試資料庫連接...
curl -s %BASE_URL%/test/db-check
echo.
echo.

echo [3/11] 測試系統資訊...
curl -s %BASE_URL%/test/system-info
echo.
echo.

echo [4/11] 查詢所有角色...
curl -s %BASE_URL%/test/roles
echo.
echo.

echo [5/11] 查詢所有選單...
curl -s %BASE_URL%/test/menus
echo.
echo.

echo [6/11] 查詢所有管理員...
curl -s %BASE_URL%/test/admin-users
echo.
echo.

echo [7/11] 查詢所有店家...
curl -s %BASE_URL%/test/stores
echo.
echo.

echo [8/11] 查詢所有會員...
curl -s %BASE_URL%/test/users
echo.
echo.

echo [9/11] 查詢所有商品...
curl -s %BASE_URL%/test/lotteries
echo.
echo.

echo [10/11] 查詢預設帳號...
curl -s %BASE_URL%/test/default-accounts
echo.
echo.

echo [11/11] 測試管理員登入...
curl -s -X POST %BASE_URL%/test/admin-login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"admin@kuji.com\",\"password\":\"admin123\"}"
echo.
echo.

echo ========================================
echo 測試完成！
echo ========================================
echo.
echo 如果所有測試都返回正確的 JSON 資料，表示系統運行正常！
echo.
echo 您也可以訪問 Swagger UI 進行互動式測試：
echo URL: http://localhost:8080/api/swagger-ui/index.html
echo.

pause
