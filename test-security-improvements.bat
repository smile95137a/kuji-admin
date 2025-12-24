@echo off
chcp 65001 >nul
echo ========================================
echo   API 安全性改進測試腳本
echo ========================================
echo.

set BASE_URL=http://localhost:8080

echo [1/5] 測試登入 API...
echo.
curl -X POST %BASE_URL%/admin/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"admin@kuji.com\",\"password\":\"Admin123\"}" ^
  -o login_response.json
echo.
echo 登入回應已儲存到 login_response.json
echo.

echo [2/5] 從回應中提取 Token...
for /f "tokens=*" %%i in ('powershell -Command "(Get-Content login_response.json | ConvertFrom-Json).data.accessToken"') do set TOKEN=%%i
echo Token: %TOKEN:~0,50%...
echo.

echo [3/5] 測試選單 API (新版 - 不需要 userId)...
echo.
curl -X GET %BASE_URL%/admin/menus/accessible ^
  -H "Authorization: Bearer %TOKEN%" ^
  -o menu_response.json
echo 選單回應已儲存到 menu_response.json
echo.

echo [4/5] 測試權限檢查 API...
echo.
curl -X GET %BASE_URL%/admin/permissions/check/PRODUCT_MANAGEMENT ^
  -H "Authorization: Bearer %TOKEN%" ^
  -o permission_response.json
echo 權限檢查回應已儲存到 permission_response.json
echo.

echo [5/5] 測試是否為 Admin...
echo.
curl -X GET %BASE_URL%/admin/permissions/is-admin ^
  -H "Authorization: Bearer %TOKEN%" ^
  -o admin_check_response.json
echo Admin 檢查回應已儲存到 admin_check_response.json
echo.

echo ========================================
echo   測試完成！
echo ========================================
echo.
echo 回應檔案：
echo   - login_response.json
echo   - menu_response.json
echo   - permission_response.json
echo   - admin_check_response.json
echo.
echo 使用以下命令查看結果：
echo   type menu_response.json
echo.

pause
