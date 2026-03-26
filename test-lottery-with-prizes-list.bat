@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ================================================
echo  測試商品與獎品列表 API
echo ================================================

set BASE_URL=http://localhost:8080/api

REM 1. 先登入取得 Token
echo.
echo [1] POST /admin/auth/login - 後台登入...
powershell -Command "$body = '{\"email\":\"admin@kuji.com\",\"password\":\"admin123\"}'; try { $r = Invoke-RestMethod -Uri '%BASE_URL%/admin/auth/login' -Method Post -Body $body -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS - Token: ' $r.data.token.substring(0,20) '...' -ForegroundColor Green; $r.data.token } else { Write-Host 'FAIL' -ForegroundColor Red; exit 1 } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red; exit 1 }" > temp_token.txt
if !errorlevel! neq 0 (
    echo 登入失敗！
    del temp_token.txt 2>nul
    exit /b 1
)

set /p ADMIN_TOKEN=<temp_token.txt
del temp_token.txt

echo.
echo Token 已取得: %ADMIN_TOKEN:~0,30%...

REM 2. 測試查詢全部商品與獎品（無條件）
echo.
echo ================================================
echo [2] POST /admin/lottery-with-prizes/list - 查詢全部商品與獎品（無條件）
echo ================================================
powershell -Command "$body = '{}'; try { $r = Invoke-RestMethod -Uri '%BASE_URL%/admin/lottery-with-prizes/list' -Method Post -Body $body -Headers @{Authorization='Bearer %ADMIN_TOKEN%'} -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS - 查詢到' $r.data.Count '個商品' -ForegroundColor Green; Write-Host ''; foreach ($item in $r.data) { Write-Host '  商品:' $item.title; Write-Host '    - 商品 ID:' $item.id; Write-Host '    - 店家:' $item.storeName; Write-Host '    - 分類:' $item.category; Write-Host '    - 狀態:' $item.status; Write-Host '    - 價格:' $item.pricePerDraw; Write-Host '    - 獎品數量:' $item.totalPrizeCount; Write-Host '    - 剩餘獎品:' $item.remainingPrizeCount; Write-Host '    - 進度:' $item.progressPercentage'%%'; Write-Host '    - 獎品列表:'; foreach ($prize in $item.prizes) { Write-Host '      *' $prize.level '-' $prize.name '(數量:' $prize.quantity ', 剩餘:' $prize.remaining ')' }; Write-Host '' } } else { Write-Host 'FAIL' -ForegroundColor Red } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red }"

REM 3. 測試查詢全部商品與獎品（帶條件 - ON_SHELF）
echo.
echo ================================================
echo [3] POST /admin/lottery-with-prizes/list - 查詢上架商品
echo ================================================
powershell -Command "$body = '{\"condition\":{\"status\":\"ON_SHELF\"}}'; try { $r = Invoke-RestMethod -Uri '%BASE_URL%/admin/lottery-with-prizes/list' -Method Post -Body $body -Headers @{Authorization='Bearer %ADMIN_TOKEN%'} -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS - 查詢到' $r.data.Count '個上架商品' -ForegroundColor Green; foreach ($item in $r.data) { Write-Host '  -' $item.title '('$item.status')' } } else { Write-Host 'FAIL' -ForegroundColor Red } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red }"

REM 4. 測試查詢全部商品與獎品（帶條件 - 分類過濾）
echo.
echo ================================================
echo [4] POST /admin/lottery-with-prizes/list - 按分類查詢
echo ================================================
powershell -Command "$body = '{\"condition\":{\"category\":\"OFFICIAL_ICHIBAN\"}}'; try { $r = Invoke-RestMethod -Uri '%BASE_URL%/admin/lottery-with-prizes/list' -Method Post -Body $body -Headers @{Authorization='Bearer %ADMIN_TOKEN%'} -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS - 查詢到' $r.data.Count '個官方一番賞' -ForegroundColor Green; foreach ($item in $r.data) { Write-Host '  -' $item.title '(分類:' $item.category ')' } } else { Write-Host 'FAIL' -ForegroundColor Red } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red }"

REM 5. 測試查詢全部商品與獎品（帶條件 - 標題模糊查詢）
echo.
echo ================================================
echo [5] POST /admin/lottery-with-prizes/list - 標題模糊查詢
echo ================================================
powershell -Command "$body = '{\"condition\":{\"title\":\"test\"}}'; try { $r = Invoke-RestMethod -Uri '%BASE_URL%/admin/lottery-with-prizes/list' -Method Post -Body $body -Headers @{Authorization='Bearer %ADMIN_TOKEN%'} -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS - 查詢到' $r.data.Count '個包含 test 的商品' -ForegroundColor Green; foreach ($item in $r.data) { Write-Host '  -' $item.title } } else { Write-Host 'FAIL' -ForegroundColor Red } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red }"

REM 6. 測試查詢全部商品與獎品（排序）
echo.
echo ================================================
echo [6] POST /admin/lottery-with-prizes/list - 按價格排序（降冪）
echo ================================================
powershell -Command "$body = '{\"sortBy\":\"price_per_draw\",\"sortOrder\":\"DESC\"}'; try { $r = Invoke-RestMethod -Uri '%BASE_URL%/admin/lottery-with-prizes/list' -Method Post -Body $body -Headers @{Authorization='Bearer %ADMIN_TOKEN%'} -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS - 查詢到' $r.data.Count '個商品（按價格降冪）' -ForegroundColor Green; foreach ($item in $r.data) { Write-Host '  -' $item.title '- $'$item.pricePerDraw } } else { Write-Host 'FAIL' -ForegroundColor Red } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red }"

echo.
echo ================================================
echo  測試完成！
echo ================================================
endlocal
