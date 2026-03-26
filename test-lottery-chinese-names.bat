@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ================================================
echo  測試前後台商品列表 API（中文翻譯）
echo ================================================

set BASE_URL=http://localhost:8080/api

REM 1. 測試前台商品列表（不需要 token）
echo.
echo [1] POST /lottery/browse/list - 前台商品列表（無條件）
echo ================================================
powershell -Command "try { $r = Invoke-RestMethod -Uri '%BASE_URL%/lottery/browse/list' -Method Post -Body '{}' -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS - 查詢到' $r.data.Count '個商品' -ForegroundColor Green; Write-Host ''; foreach ($item in $r.data[0..2]) { Write-Host '  商品:' $item.title; Write-Host '    - 分類:' $item.category '→' $item.categoryName; Write-Host '    - 子類型:' $item.subCategory '→' $item.subCategoryName; Write-Host '    - 狀態:' $item.status '→' $item.statusName; Write-Host '    - 價格:' $item.pricePerDraw; Write-Host '    - 店家:' $item.storeName; Write-Host '' } } else { Write-Host 'FAIL' -ForegroundColor Red } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red }"

REM 2. 測試前台商品列表（分類過濾）
echo.
echo [2] POST /lottery/browse/list - 查詢自製賞
echo ================================================
powershell -Command "$body = '{\"condition\":{\"category\":\"CUSTOM_GACHA\"}}'; try { $r = Invoke-RestMethod -Uri '%BASE_URL%/lottery/browse/list' -Method Post -Body $body -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS - 查詢到' $r.data.Count '個自製賞' -ForegroundColor Green; foreach ($item in $r.data) { Write-Host '  -' $item.title '('$item.categoryName')' } } else { Write-Host 'FAIL' -ForegroundColor Red } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red }"

REM 3. 測試後台商品列表（需要 token）
echo.
echo [3] 後台登入取得 Token...
powershell -Command "$body = '{\"username\":\"admin@kuji.com\",\"password\":\"admin123\"}'; try { $r = Invoke-RestMethod -Uri '%BASE_URL%/admin/auth/login' -Method Post -Body $body -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS - Token:' $r.data.accessToken.substring(0,20) '...' -ForegroundColor Green; $r.data.accessToken } else { Write-Host 'FAIL' -ForegroundColor Red; exit 1 } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red; exit 1 }" > temp_token.txt
if !errorlevel! neq 0 (
    echo 登入失敗！
    del temp_token.txt 2>nul
    goto :end
)

set /p ADMIN_TOKEN=<temp_token.txt
del temp_token.txt

echo.
echo [4] POST /admin/lottery/list - 後台商品列表
echo ================================================
powershell -Command "$body = '{}'; try { $r = Invoke-RestMethod -Uri '%BASE_URL%/admin/lottery/list' -Method Post -Body $body -Headers @{Authorization='Bearer %ADMIN_TOKEN%'} -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS - 查詢到' $r.data.Count '個商品（所有狀態）' -ForegroundColor Green; Write-Host ''; foreach ($item in $r.data[0..2]) { Write-Host '  商品:' $item.title; Write-Host '    - ID:' $item.id; Write-Host '    - 分類:' $item.category '→' $item.categoryName; Write-Host '    - 子類型:' $item.subCategory '→' $item.subCategoryName; Write-Host '    - 狀態:' $item.status '→' $item.statusName; Write-Host '    - 建立者:' $item.createdBy; Write-Host '    - 備註:' $item.remark; Write-Host '' } } else { Write-Host 'FAIL' -ForegroundColor Red } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red }"

:end
echo.
echo ================================================
echo  測試完成！
echo ================================================
echo.
echo 檢查重點：
echo   - categoryName 應該是中文
echo   - subCategoryName 應該是中文
echo   - statusName 應該是中文
echo   - 前台只能查詢 ON_SHELF 狀態
echo   - 後台可查詢所有狀態
echo.
endlocal
