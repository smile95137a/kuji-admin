@echo off
chcp 65001 > nul
setlocal enabledelayedexpansion

echo ================================================
echo   KUJI 完整 API 測試腳本
echo   Generated: 2025-01-21
echo ================================================
echo.

set BASE_URL=http://localhost:8080/api
set PASS=0
set FAIL=0
set TOTAL=0

:: ================================================
:: 第一部分：後台 API 測試 (需要 Admin Token)
:: ================================================

echo ================================================
echo 第一部分：後台認證 API
echo ================================================

:: 1. 後台登入
set /a TOTAL+=1
echo.
echo [%TOTAL%] POST /admin/auth/login - 後台登入...
powershell -Command "$body = '{\"username\":\"admin@kuji.com\",\"password\":\"admin123\"}'; try { $r = Invoke-RestMethod -Uri '%BASE_URL%/admin/auth/login' -Method Post -Body $body -ContentType 'application/json'; $r | ConvertTo-Json -Compress | Out-File -Encoding utf8 'last_response.json'; if ($r.success) { Write-Host 'PASS' -ForegroundColor Green; exit 0 } else { Write-Host 'FAIL' -ForegroundColor Red; exit 1 } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red; exit 1 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

:: 提取 Admin Token
for /f "tokens=*" %%a in ('powershell -Command "$j = Get-Content 'last_response.json' | ConvertFrom-Json; $j.data.accessToken"') do set ADMIN_TOKEN=%%a
echo     Token: !ADMIN_TOKEN:~0,50!...

:: ================================================
echo.
echo ================================================
echo 第二部分：後台選單管理 API
echo ================================================

:: 2. 取得可存取的選單 (用於後台 Sidebar)
set /a TOTAL+=1
echo.
echo [%TOTAL%] GET /admin/menus/accessible - 取得可存取選單...
powershell -Command "try { $r = Invoke-RestMethod -Uri '%BASE_URL%/admin/menus/accessible' -Method Get -Headers @{Authorization='Bearer !ADMIN_TOKEN!'} -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS' -ForegroundColor Green; exit 0 } else { Write-Host 'FAIL' -ForegroundColor Red; exit 1 } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red; exit 1 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

:: 3. 取得所有選單
set /a TOTAL+=1
echo.
echo [%TOTAL%] GET /admin/menus - 取得所有選單...
powershell -Command "try { $r = Invoke-RestMethod -Uri '%BASE_URL%/admin/menus' -Method Get -Headers @{Authorization='Bearer !ADMIN_TOKEN!'} -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS' -ForegroundColor Green; exit 0 } else { Write-Host 'FAIL' -ForegroundColor Red; exit 1 } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red; exit 1 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

:: 4. 取得選單樹狀結構
set /a TOTAL+=1
echo.
echo [%TOTAL%] GET /admin/menus/tree - 取得選單樹狀結構...
powershell -Command "try { $r = Invoke-RestMethod -Uri '%BASE_URL%/admin/menus/tree' -Method Get -Headers @{Authorization='Bearer !ADMIN_TOKEN!'} -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS' -ForegroundColor Green; exit 0 } else { Write-Host 'FAIL' -ForegroundColor Red; exit 1 } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red; exit 1 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

:: ================================================
echo.
echo ================================================
echo 第三部分：後台店家管理 API
echo ================================================

:: 5. 取得店家選項列表
set /a TOTAL+=1
echo.
echo [%TOTAL%] GET /admin/stores/options - 取得店家選項...
powershell -Command "try { $r = Invoke-RestMethod -Uri '%BASE_URL%/admin/stores/options' -Method Get -Headers @{Authorization='Bearer !ADMIN_TOKEN!'} -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS' -ForegroundColor Green; exit 0 } else { Write-Host 'FAIL' -ForegroundColor Red; exit 1 } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red; exit 1 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

:: ================================================
echo.
echo ================================================
echo 第四部分：後台角色管理 API
echo ================================================

:: 6. 取得所有角色
set /a TOTAL+=1
echo.
echo [%TOTAL%] GET /admin/roles - 取得所有角色...
powershell -Command "try { $r = Invoke-RestMethod -Uri '%BASE_URL%/admin/roles' -Method Get -Headers @{Authorization='Bearer !ADMIN_TOKEN!'} -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS' -ForegroundColor Green; exit 0 } else { Write-Host 'FAIL' -ForegroundColor Red; exit 1 } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red; exit 1 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

:: ================================================
echo.
echo ================================================
echo 第五部分：後台推薦碼管理 API
echo ================================================

:: 7. 取得我的店家推薦碼
set /a TOTAL+=1
echo.
echo [%TOTAL%] GET /admin/referral-codes/my-store - 取得我的店家推薦碼...
powershell -Command "try { $r = Invoke-RestMethod -Uri '%BASE_URL%/admin/referral-codes/my-store' -Method Get -Headers @{Authorization='Bearer !ADMIN_TOKEN!'} -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS' -ForegroundColor Green; exit 0 } else { Write-Host 'FAIL' -ForegroundColor Red; exit 1 } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red; exit 1 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

:: ================================================
echo.
echo ================================================
echo 第六部分：後台一番賞管理 API
echo ================================================

:: 8. 查詢一番賞列表
set /a TOTAL+=1
echo.
echo [%TOTAL%] POST /admin/lottery/list - 查詢一番賞列表...
powershell -Command "$body = '{}'; try { $r = Invoke-RestMethod -Uri '%BASE_URL%/admin/lottery/list' -Method Post -Body $body -Headers @{Authorization='Bearer !ADMIN_TOKEN!'} -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS' -ForegroundColor Green; exit 0 } else { Write-Host 'FAIL' -ForegroundColor Red; exit 1 } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red; exit 1 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

:: ================================================
echo.
echo ================================================
echo 第七部分：後台 Banner 管理 API
echo ================================================

:: 9. 取得所有 Banner（使用 POST /list）
set /a TOTAL+=1
echo.
echo [%TOTAL%] POST /admin/banner/list - 取得所有 Banner...
powershell -Command "$body = '{}'; try { $r = Invoke-RestMethod -Uri '%BASE_URL%/admin/banner/list' -Method Post -Body $body -Headers @{Authorization='Bearer !ADMIN_TOKEN!'} -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS' -ForegroundColor Green; exit 0 } else { Write-Host 'FAIL' -ForegroundColor Red; exit 1 } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red; exit 1 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

:: ================================================
echo.
echo ================================================
echo 第八部分：後台最新消息管理 API
echo ================================================

:: 10. 取得所有消息（使用 POST /list）
set /a TOTAL+=1
echo.
echo [%TOTAL%] POST /admin/news/list - 取得所有最新消息...
powershell -Command "$body = '{}'; try { $r = Invoke-RestMethod -Uri '%BASE_URL%/admin/news/list' -Method Post -Body $body -Headers @{Authorization='Bearer !ADMIN_TOKEN!'} -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS' -ForegroundColor Green; exit 0 } else { Write-Host 'FAIL' -ForegroundColor Red; exit 1 } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red; exit 1 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

:: ================================================
echo.
echo ================================================
echo 第九部分：後台跑馬燈管理 API
echo ================================================

:: 11. 取得所有跑馬燈
set /a TOTAL+=1
echo.
echo [%TOTAL%] GET /admin/marquee - 取得所有跑馬燈...
powershell -Command "try { $r = Invoke-RestMethod -Uri '%BASE_URL%/admin/marquee' -Method Get -Headers @{Authorization='Bearer !ADMIN_TOKEN!'} -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS' -ForegroundColor Green; exit 0 } else { Write-Host 'FAIL' -ForegroundColor Red; exit 1 } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red; exit 1 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

:: ================================================
echo.
echo ================================================
echo 第十部分：後台儲值方案管理 API
echo ================================================

:: 12. 取得所有儲值方案（使用 /list）
set /a TOTAL+=1
echo.
echo [%TOTAL%] GET /admin/recharge-plan/list - 取得所有儲值方案...
powershell -Command "try { $r = Invoke-RestMethod -Uri '%BASE_URL%/admin/recharge-plan/list' -Method Get -Headers @{Authorization='Bearer !ADMIN_TOKEN!'} -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS' -ForegroundColor Green; exit 0 } else { Write-Host 'FAIL' -ForegroundColor Red; exit 1 } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red; exit 1 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

:: ================================================
:: 第二大部分：前台公開 API 測試 (無需登入)
:: ================================================

echo.
echo ================================================
echo 第十一部分：前台行政區 API (公開)
echo ================================================

:: 13. 取得所有縣市
set /a TOTAL+=1
echo.
echo [%TOTAL%] GET /district/cities - 取得所有縣市...
powershell -Command "try { $r = Invoke-RestMethod -Uri '%BASE_URL%/district/cities' -Method Get -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS' -ForegroundColor Green; exit 0 } else { Write-Host 'FAIL' -ForegroundColor Red; exit 1 } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red; exit 1 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

:: 14. 取得臺北市行政區 (使用 query 參數)
set /a TOTAL+=1
echo.
echo [%TOTAL%] GET /district?city=臺北市 - 取得臺北市行政區...
powershell -Command "try { Add-Type -AssemblyName System.Web; $city = [System.Web.HttpUtility]::UrlEncode('臺北市'); $url = '%BASE_URL%/district?city=' + $city + '&districtName=' + [System.Web.HttpUtility]::UrlEncode('中正區'); $r = Invoke-RestMethod -Uri $url -Method Get -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS' -ForegroundColor Green; exit 0 } else { Write-Host 'FAIL' -ForegroundColor Red; exit 1 } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red; exit 1 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

:: 15. 取得行政區樹狀結構
set /a TOTAL+=1
echo.
echo [%TOTAL%] GET /district/tree - 取得行政區樹狀結構...
powershell -Command "try { $r = Invoke-RestMethod -Uri '%BASE_URL%/district/tree' -Method Get -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS' -ForegroundColor Green; exit 0 } else { Write-Host 'FAIL' -ForegroundColor Red; exit 1 } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red; exit 1 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

:: 16. 取得所有行政區
set /a TOTAL+=1
echo.
echo [%TOTAL%] GET /district/all - 取得所有行政區...
powershell -Command "try { $r = Invoke-RestMethod -Uri '%BASE_URL%/district/all' -Method Get -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS' -ForegroundColor Green; exit 0 } else { Write-Host 'FAIL' -ForegroundColor Red; exit 1 } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red; exit 1 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

:: ================================================
echo.
echo ================================================
echo 第十二部分：前台枚舉 API (公開)
echo ================================================

:: 17. 取得所有枚舉
set /a TOTAL+=1
echo.
echo [%TOTAL%] GET /enums/all - 取得所有枚舉...
powershell -Command "try { $r = Invoke-RestMethod -Uri '%BASE_URL%/enums/all' -Method Get -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS' -ForegroundColor Green; exit 0 } else { Write-Host 'FAIL' -ForegroundColor Red; exit 1 } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red; exit 1 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

:: 18. 取得獎項等級枚舉
set /a TOTAL+=1
echo.
echo [%TOTAL%] GET /enums/prize-level - 取得獎項等級枚舉...
powershell -Command "try { $r = Invoke-RestMethod -Uri '%BASE_URL%/enums/prize-level' -Method Get -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS' -ForegroundColor Green; exit 0 } else { Write-Host 'FAIL' -ForegroundColor Red; exit 1 } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red; exit 1 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

:: ================================================
echo.
echo ================================================
echo 第十三部分：前台 Banner API (公開)
echo ================================================

:: 19. 取得輪播 Banner
set /a TOTAL+=1
echo.
echo [%TOTAL%] GET /banner/carousel - 取得輪播 Banner...
powershell -Command "try { $r = Invoke-RestMethod -Uri '%BASE_URL%/banner/carousel' -Method Get -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS' -ForegroundColor Green; exit 0 } else { Write-Host 'FAIL' -ForegroundColor Red; exit 1 } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red; exit 1 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

:: ================================================
echo.
echo ================================================
echo 第十四部分：前台最新消息 API (公開)
echo ================================================

:: 20. 取得最新消息列表
set /a TOTAL+=1
echo.
echo [%TOTAL%] GET /news - 取得最新消息列表...
powershell -Command "try { $r = Invoke-RestMethod -Uri '%BASE_URL%/news' -Method Get -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS' -ForegroundColor Green; exit 0 } else { Write-Host 'FAIL' -ForegroundColor Red; exit 1 } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red; exit 1 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

:: 21. 取得最新消息 (限制 5 則)
set /a TOTAL+=1
echo.
echo [%TOTAL%] GET /news?limit=5 - 取得最新 5 則消息...
powershell -Command "try { $r = Invoke-RestMethod -Uri '%BASE_URL%/news?limit=5' -Method Get -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS' -ForegroundColor Green; exit 0 } else { Write-Host 'FAIL' -ForegroundColor Red; exit 1 } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red; exit 1 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

:: ================================================
echo.
echo ================================================
echo 第十五部分：前台跑馬燈 API (公開)
echo ================================================

:: 22. 取得啟用中的跑馬燈
set /a TOTAL+=1
echo.
echo [%TOTAL%] GET /marquee - 取得跑馬燈...
powershell -Command "try { $r = Invoke-RestMethod -Uri '%BASE_URL%/marquee' -Method Get -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS' -ForegroundColor Green; exit 0 } else { Write-Host 'FAIL' -ForegroundColor Red; exit 1 } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red; exit 1 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

:: ================================================
echo.
echo ================================================
echo 第十六部分：前台一番賞瀏覽 API (公開)
echo ================================================

:: 23. 瀏覽一番賞列表
set /a TOTAL+=1
echo.
echo [%TOTAL%] POST /lottery/browse/list - 瀏覽一番賞列表...
powershell -Command "$body = '{}'; try { $r = Invoke-RestMethod -Uri '%BASE_URL%/lottery/browse/list' -Method Post -Body $body -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS' -ForegroundColor Green; exit 0 } else { Write-Host 'FAIL' -ForegroundColor Red; exit 1 } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red; exit 1 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

:: ================================================
echo.
echo ================================================
echo 第十七部分：前台店家 API (公開)
echo ================================================

:: 24. 取得店家選項列表
set /a TOTAL+=1
echo.
echo [%TOTAL%] GET /stores/options - 取得店家選項...
powershell -Command "try { $r = Invoke-RestMethod -Uri '%BASE_URL%/stores/options' -Method Get -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS' -ForegroundColor Green; exit 0 } else { Write-Host 'FAIL' -ForegroundColor Red; exit 1 } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red; exit 1 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

:: ================================================
echo.
echo ================================================
echo 第十八部分：前台儲值方案 API (公開)
echo ================================================

:: 25. 取得儲值方案列表（前台路由）
set /a TOTAL+=1
echo.
echo [%TOTAL%] GET /api/recharge-plan/list - 取得儲值方案...
powershell -Command "try { $r = Invoke-RestMethod -Uri '%BASE_URL%/api/recharge-plan/list' -Method Get -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS' -ForegroundColor Green; exit 0 } else { Write-Host 'FAIL' -ForegroundColor Red; exit 1 } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red; exit 1 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

:: ================================================
:: 第三大部分：前台認證 API 測試
:: ================================================

echo.
echo ================================================
echo 第十九部分：前台認證 API
echo ================================================

:: 26. 前台註冊
set "TEST_EMAIL=test%random%@example.com"
set /a TOTAL+=1
echo.
echo [%TOTAL%] POST /auth/register - 前台註冊 (!TEST_EMAIL!)...
powershell -Command "$body = '{\"email\":\"!TEST_EMAIL!\",\"password\":\"Test123456\",\"nickname\":\"測試用戶\"}'; try { $r = Invoke-RestMethod -Uri '%BASE_URL%/auth/register' -Method Post -Body $body -ContentType 'application/json'; $r | ConvertTo-Json -Compress | Out-File -Encoding utf8 'user_response.json'; if ($r.success) { Write-Host 'PASS' -ForegroundColor Green; exit 0 } else { Write-Host 'FAIL' -ForegroundColor Red; exit 1 } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red; exit 1 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

:: 提取 User Token
for /f "tokens=*" %%a in ('powershell -Command "$j = Get-Content 'user_response.json' | ConvertFrom-Json; $j.data.accessToken"') do set USER_TOKEN=%%a
echo     User Token: !USER_TOKEN:~0,50!...

:: 27. 前台登入
set /a TOTAL+=1
echo.
echo [%TOTAL%] POST /auth/login - 前台登入...
powershell -Command "$body = '{\"email\":\"!TEST_EMAIL!\",\"password\":\"Test123456\"}'; try { $r = Invoke-RestMethod -Uri '%BASE_URL%/auth/login' -Method Post -Body $body -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS' -ForegroundColor Green; exit 0 } else { Write-Host 'FAIL' -ForegroundColor Red; exit 1 } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red; exit 1 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

:: 28. 忘記密碼請求
set /a TOTAL+=1
echo.
echo [%TOTAL%] POST /auth/forgot-password - 忘記密碼請求...
powershell -Command "$body = '{\"email\":\"!TEST_EMAIL!\"}'; try { $r = Invoke-RestMethod -Uri '%BASE_URL%/auth/forgot-password' -Method Post -Body $body -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS' -ForegroundColor Green; exit 0 } else { Write-Host 'FAIL' -ForegroundColor Red; exit 1 } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red; exit 1 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

:: 29. 重設密碼(無效 token - 預期失敗)
set /a TOTAL+=1
echo.
echo [%TOTAL%] POST /auth/reset-password - 重設密碼(無效 token - 預期失敗)...
powershell -Command "$body = '{\"token\":\"invalid-token\",\"newPassword\":\"NewPass123\",\"confirmPassword\":\"NewPass123\"}'; try { $r = Invoke-RestMethod -Uri '%BASE_URL%/auth/reset-password' -Method Post -Body $body -ContentType 'application/json'; if (-not $r.success) { Write-Host 'PASS (預期失敗)' -ForegroundColor Green; exit 0 } else { Write-Host 'FAIL - 應該返回錯誤' -ForegroundColor Red; exit 1 } } catch { Write-Host 'PASS (預期 400 錯誤)' -ForegroundColor Green; exit 0 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

:: ================================================
:: 第四大部分：前台需認證 API 測試
:: ================================================

echo.
echo ================================================
echo 第二十部分：前台用戶 API (需要認證)
echo ================================================

:: 30. 取得當前用戶資訊
set /a TOTAL+=1
echo.
echo [%TOTAL%] GET /user/me - 取得當前用戶資訊...
powershell -Command "try { $r = Invoke-RestMethod -Uri '%BASE_URL%/user/me' -Method Get -Headers @{Authorization='Bearer !USER_TOKEN!'} -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS' -ForegroundColor Green; exit 0 } else { Write-Host 'FAIL' -ForegroundColor Red; exit 1 } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red; exit 1 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

:: 31. 測試 Hello API
set /a TOTAL+=1
echo.
echo [%TOTAL%] GET /user/hello - 測試 Hello API...
powershell -Command "try { $r = Invoke-RestMethod -Uri '%BASE_URL%/user/hello' -Method Get -Headers @{Authorization='Bearer !USER_TOKEN!'} -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS' -ForegroundColor Green; exit 0 } else { Write-Host 'FAIL' -ForegroundColor Red; exit 1 } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red; exit 1 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

:: ================================================
echo.
echo ================================================
echo 第二十一部分：前台錢包 API (需要認證)
echo ================================================

:: 32. 取得我的錢包
set /a TOTAL+=1
echo.
echo [%TOTAL%] GET /api/wallet - 取得我的錢包...
powershell -Command "try { $r = Invoke-RestMethod -Uri '%BASE_URL%/api/wallet' -Method Get -Headers @{Authorization='Bearer !USER_TOKEN!'} -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS' -ForegroundColor Green; exit 0 } else { Write-Host 'FAIL' -ForegroundColor Red; exit 1 } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red; exit 1 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

:: 33. 查詢我的交易記錄
set /a TOTAL+=1
echo.
echo [%TOTAL%] POST /api/wallet/transactions - 查詢交易記錄...
powershell -Command "$body = '{}'; try { $r = Invoke-RestMethod -Uri '%BASE_URL%/api/wallet/transactions' -Method Post -Body $body -Headers @{Authorization='Bearer !USER_TOKEN!'} -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS' -ForegroundColor Green; exit 0 } else { Write-Host 'FAIL' -ForegroundColor Red; exit 1 } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red; exit 1 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

:: ================================================
echo.
echo ================================================
echo 第二十二部分：前台訂單 API (需要認證)
echo ================================================

:: 34. 查詢我的訂單列表
set /a TOTAL+=1
echo.
echo [%TOTAL%] POST /api/order/list - 查詢我的訂單列表...
powershell -Command "$body = '{}'; try { $r = Invoke-RestMethod -Uri '%BASE_URL%/api/order/list' -Method Post -Body $body -Headers @{Authorization='Bearer !USER_TOKEN!'} -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS' -ForegroundColor Green; exit 0 } else { Write-Host 'FAIL' -ForegroundColor Red; exit 1 } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red; exit 1 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

:: ================================================
echo.
echo ================================================
echo 第二十三部分：前台獎品盒 API (需要認證)
echo ================================================

:: 35. 查詢我的獎品盒
set /a TOTAL+=1
echo.
echo [%TOTAL%] GET /api/prize-box - 查詢我的獎品盒...
powershell -Command "try { $r = Invoke-RestMethod -Uri '%BASE_URL%/api/prize-box' -Method Get -Headers @{Authorization='Bearer !USER_TOKEN!'} -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS' -ForegroundColor Green; exit 0 } else { Write-Host 'FAIL' -ForegroundColor Red; exit 1 } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red; exit 1 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

:: ================================================
:: 第五大部分：權限測試
:: ================================================

echo.
echo ================================================
echo 第二十四部分：權限測試
echo ================================================

:: 36. 未登入訪問後台 API (預期失敗)
set /a TOTAL+=1
echo.
echo [%TOTAL%] 未登入訪問後台 - 預期失敗...
powershell -Command "try { $r = Invoke-RestMethod -Uri '%BASE_URL%/admin/menus/accessible' -Method Get -ContentType 'application/json'; if ($r.success) { Write-Host 'FAIL - 應該失敗但成功了' -ForegroundColor Red; exit 1 } else { Write-Host 'PASS (預期失敗)' -ForegroundColor Green; exit 0 } } catch { Write-Host 'PASS (預期失敗)' -ForegroundColor Green; exit 0 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

:: 37. 前台 Token 訪問後台 API (預期失敗)
set /a TOTAL+=1
echo.
echo [%TOTAL%] 前台 Token 訪問後台 - 預期失敗...
powershell -Command "try { $r = Invoke-RestMethod -Uri '%BASE_URL%/admin/menus/accessible' -Method Get -Headers @{Authorization='Bearer !USER_TOKEN!'} -ContentType 'application/json'; if ($r.success) { Write-Host 'FAIL - 應該失敗但成功了' -ForegroundColor Red; exit 1 } else { Write-Host 'PASS (預期失敗)' -ForegroundColor Green; exit 0 } } catch { Write-Host 'PASS (預期失敗)' -ForegroundColor Green; exit 0 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

:: 38. 後台 Token 訪問前台公開 API
set /a TOTAL+=1
echo.
echo [%TOTAL%] 後台 Token 訪問前台公開 API...
powershell -Command "try { $r = Invoke-RestMethod -Uri '%BASE_URL%/district/cities' -Method Get -Headers @{Authorization='Bearer !ADMIN_TOKEN!'} -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS' -ForegroundColor Green; exit 0 } else { Write-Host 'FAIL' -ForegroundColor Red; exit 1 } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red; exit 1 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

:: 39. 未登入訪問需認證 API (預期失敗)
set /a TOTAL+=1
echo.
echo [%TOTAL%] 未登入訪問需認證 API - 預期失敗...
powershell -Command "try { $r = Invoke-RestMethod -Uri '%BASE_URL%/api/wallet' -Method Get -ContentType 'application/json'; if ($r.success) { Write-Host 'FAIL - 應該失敗但成功了' -ForegroundColor Red; exit 1 } else { Write-Host 'PASS (預期失敗)' -ForegroundColor Green; exit 0 } } catch { Write-Host 'PASS (預期失敗)' -ForegroundColor Green; exit 0 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)

:: ================================================
echo.
echo ================================================
echo              測試結果摘要
echo ================================================
echo.
echo 總共測試：%TOTAL% 個 API
echo 通過：%PASS%
echo 失敗：%FAIL%
echo.

if %FAIL% gtr 0 (
    echo 狀態：有 %FAIL% 個測試失敗
    exit /b 1
) else (
    echo 狀態：所有測試通過！
    exit /b 0
)

endlocal
