@echo off
chcp 65001 >nul
echo ========================================
echo KUJI CORS 測試腳本
echo ========================================
echo.

set API_URL=http://18.179.187.129:8080
set ORIGIN=http://18.179.187.129

echo 測試目標: %API_URL%
echo 來源: %ORIGIN%
echo.

echo [1] 測試健康檢查...
curl -s %API_URL%/actuator/health
echo.
echo.

echo [2] 測試 CORS Preflight (OPTIONS)...
curl -H "Origin: %ORIGIN%" ^
     -H "Access-Control-Request-Method: POST" ^
     -H "Access-Control-Request-Headers: Content-Type,Authorization" ^
     -X OPTIONS ^
     %API_URL%/api/admin/auth/login ^
     -i -s
echo.
echo.

echo [3] 測試登入 API (POST)...
curl -X POST %API_URL%/api/admin/auth/login ^
  -H "Content-Type: application/json" ^
  -H "Origin: %ORIGIN%" ^
  -d "{\"email\":\"admin@kuji.com\",\"password\":\"admin123\"}" ^
  -i -s
echo.
echo.

echo [4] 測試公開 API - 行政區...
curl -H "Origin: %ORIGIN%" ^
     %API_URL%/api/district/cities ^
     -i -s
echo.
echo.

echo ========================================
echo 測試完成
echo ========================================
echo.
echo 如果看到 "Access-Control-Allow-Origin: %ORIGIN%"
echo 表示 CORS 設定正確！
echo.

pause
