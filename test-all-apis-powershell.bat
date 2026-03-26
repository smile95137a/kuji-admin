@echo off
setlocal enabledelayedexpansion

echo ================================
echo   KUJI API 測試腳本
echo ================================
echo.

set BASE_URL=http://localhost:8080/api
set PASS=0
set FAIL=0

echo 第一部分：後台 API 測試
echo ========================================

:: 1. 後台登入
echo.
echo [1] 後台登入...
powershell -Command "$body = '{\"username\":\"admin@kuji.com\",\"password\":\"admin123\"}'; try { $r = Invoke-RestMethod -Uri '%BASE_URL%/admin/auth/login' -Method Post -Body $body -ContentType 'application/json'; $r | ConvertTo-Json -Compress | Out-File -Encoding utf8 'last_response.json'; if ($r.success) { Write-Host 'PASS' -ForegroundColor Green; exit 0 } else { Write-Host 'FAIL' -ForegroundColor Red; exit 1 } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red; exit 1 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

:: 提取 token
for /f "tokens=*" %%a in ('powershell -Command "$j = Get-Content 'last_response.json' | ConvertFrom-Json; $j.data.accessToken"') do set ADMIN_TOKEN=%%a
echo Token: !ADMIN_TOKEN:~0,50!...

:: 2. 取得使用者選單（修正路徑）
echo.
echo [2] 取得使用者選單...
powershell -Command "try { $r = Invoke-RestMethod -Uri '%BASE_URL%/admin/menus/accessible' -Method Get -Headers @{Authorization='Bearer !ADMIN_TOKEN!'} -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS' -ForegroundColor Green; exit 0 } else { Write-Host 'FAIL' -ForegroundColor Red; exit 1 } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red; exit 1 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

:: 3. 取得店家列表（修正路徑）
echo.
echo [3] 取得店家列表...
powershell -Command "try { $r = Invoke-RestMethod -Uri '%BASE_URL%/admin/stores/options' -Method Get -Headers @{Authorization='Bearer !ADMIN_TOKEN!'} -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS' -ForegroundColor Green; exit 0 } else { Write-Host 'FAIL' -ForegroundColor Red; exit 1 } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red; exit 1 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

:: 4. 取得所有縣市（改用前台 API，後台沒有獨立的 district 路由）
echo.
echo [4] 取得所有縣市...
powershell -Command "try { $r = Invoke-RestMethod -Uri '%BASE_URL%/district/cities' -Method Get -Headers @{Authorization='Bearer !ADMIN_TOKEN!'} -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS' -ForegroundColor Green; exit 0 } else { Write-Host 'FAIL' -ForegroundColor Red; exit 1 } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red; exit 1 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

:: 5. 取得推薦碼
echo.
echo [5] 取得推薦碼...
powershell -Command "try { $r = Invoke-RestMethod -Uri '%BASE_URL%/admin/referral-codes/my-store' -Method Get -Headers @{Authorization='Bearer !ADMIN_TOKEN!'} -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS' -ForegroundColor Green; exit 0 } else { Write-Host 'FAIL' -ForegroundColor Red; exit 1 } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red; exit 1 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

echo.
echo ========================================
echo 第二部分：前台 API 測試
echo ========================================

:: 6. 前台註冊
set "TEST_EMAIL=test%random%@example.com"
echo.
echo [6] 前台註冊 (!TEST_EMAIL!)...
powershell -Command "$body = '{\"email\":\"!TEST_EMAIL!\",\"password\":\"Test123\",\"nickname\":\"測試用戶\"}'; try { $r = Invoke-RestMethod -Uri '%BASE_URL%/auth/register' -Method Post -Body $body -ContentType 'application/json'; $r | ConvertTo-Json -Compress | Out-File -Encoding utf8 'last_response.json'; if ($r.success) { Write-Host 'PASS' -ForegroundColor Green; exit 0 } else { Write-Host 'FAIL' -ForegroundColor Red; exit 1 } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red; exit 1 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

:: 提取 user token
for /f "tokens=*" %%a in ('powershell -Command "$j = Get-Content 'last_response.json' | ConvertFrom-Json; $j.data.accessToken"') do set USER_TOKEN=%%a
echo User Token: !USER_TOKEN:~0,50!...

:: 7. 前台登入
echo.
echo [7] 前台登入...
powershell -Command "$body = '{\"email\":\"!TEST_EMAIL!\",\"password\":\"Test123\"}'; try { $r = Invoke-RestMethod -Uri '%BASE_URL%/auth/login' -Method Post -Body $body -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS' -ForegroundColor Green; exit 0 } else { Write-Host 'FAIL' -ForegroundColor Red; exit 1 } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red; exit 1 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

:: 8. 忘記密碼
echo.
echo [8] 忘記密碼請求...
powershell -Command "$body = '{\"email\":\"!TEST_EMAIL!\"}'; try { $r = Invoke-RestMethod -Uri '%BASE_URL%/auth/forgot-password' -Method Post -Body $body -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS' -ForegroundColor Green; exit 0 } else { Write-Host 'FAIL' -ForegroundColor Red; exit 1 } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red; exit 1 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

:: 9. 重設密碼(無效token) - 修正：檢查 HTTP 錯誤而非 success 欄位
echo.
echo [9] 重設密碼(無效token - 應該失敗)...
powershell -Command "$body = '{\"token\":\"invalid-token\",\"newPassword\":\"NewPass123\",\"confirmPassword\":\"NewPass123\"}'; try { $r = Invoke-RestMethod -Uri '%BASE_URL%/auth/reset-password' -Method Post -Body $body -ContentType 'application/json'; Write-Host 'FAIL - 應該返回錯誤但成功了' -ForegroundColor Red; exit 1 } catch { if ($_.Exception.Response.StatusCode -eq 400) { Write-Host 'PASS (預期失敗: 400 Bad Request)' -ForegroundColor Green; exit 0 } else { Write-Host 'ERROR: 非預期的錯誤碼' $_.Exception.Response.StatusCode -ForegroundColor Red; exit 1 } }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

:: 10. 取得所有縣市(前台)
echo.
echo [10] 取得所有縣市(前台)...
powershell -Command "try { $r = Invoke-RestMethod -Uri '%BASE_URL%/district/cities' -Method Get -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS' -ForegroundColor Green; exit 0 } else { Write-Host 'FAIL' -ForegroundColor Red; exit 1 } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red; exit 1 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

:: 11. 取得台北市行政區 - 修正：使用 URL 編碼
echo.
echo [11] 取得台北市行政區...
powershell -Command "try { $r = Invoke-RestMethod -Uri '%BASE_URL%/district/districts/%E5%8F%B0%E5%8C%97%E5%B8%82' -Method Get -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS' -ForegroundColor Green; exit 0 } else { Write-Host 'FAIL' -ForegroundColor Red; exit 1 } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red; exit 1 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

:: 12. 取得行政區樹狀結構
echo.
echo [12] 取得行政區樹狀結構...
powershell -Command "try { $r = Invoke-RestMethod -Uri '%BASE_URL%/district/tree' -Method Get -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS' -ForegroundColor Green; exit 0 } else { Write-Host 'FAIL' -ForegroundColor Red; exit 1 } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red; exit 1 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

echo.
echo ========================================
echo 第三部分：權限測試
echo ========================================

:: 13. 未登入訪問後台
echo.
echo [13] 未登入訪問後台(應該失敗)...
powershell -Command "try { $r = Invoke-RestMethod -Uri '%BASE_URL%/admin/users/menu' -Method Get -ContentType 'application/json'; if ($r.success) { Write-Host 'FAIL - 應該失敗但成功了' -ForegroundColor Red; exit 1 } else { Write-Host 'PASS (預期失敗)' -ForegroundColor Green; exit 0 } } catch { Write-Host 'PASS (預期失敗)' -ForegroundColor Green; exit 0 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

:: 14. 前台token訪問後台
echo.
echo [14] 前台token訪問後台(應該失敗)...
powershell -Command "try { $r = Invoke-RestMethod -Uri '%BASE_URL%/admin/users/menu' -Method Get -Headers @{Authorization='Bearer !USER_TOKEN!'} -ContentType 'application/json'; if ($r.success) { Write-Host 'FAIL - 應該失敗但成功了' -ForegroundColor Red; exit 1 } else { Write-Host 'PASS (預期失敗)' -ForegroundColor Green; exit 0 } } catch { Write-Host 'PASS (預期失敗)' -ForegroundColor Green; exit 0 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

:: 15. 後台token訪問前台公開API
echo.
echo [15] 後台token訪問前台公開API...
powershell -Command "try { $r = Invoke-RestMethod -Uri '%BASE_URL%/district/cities' -Method Get -Headers @{Authorization='Bearer !ADMIN_TOKEN!'} -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS' -ForegroundColor Green; exit 0 } else { Write-Host 'FAIL' -ForegroundColor Red; exit 1 } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red; exit 1 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

echo.
echo ========================================
echo   測試結果摘要
echo ========================================
echo 通過: %PASS%
echo 失敗: %FAIL%
echo.

if %FAIL% gtr 0 (
    echo 測試失敗
    exit /b 1
) else (
    echo 所有測試通過！
    exit /b 0
)

endlocal
