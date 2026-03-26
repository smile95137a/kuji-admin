@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ================================================
echo  測試註冊 API
echo ================================================

set BASE_URL=http://localhost:8080/api

REM 測試完整註冊資料
echo.
echo [1] POST /auth/register - 完整註冊資料
echo ================================================
powershell -Command "$body = @{email='test@example.com';password='123456';confirmPassword='123456';nickname='測試用戶';phoneNumber='0912345678';addressName='王小明';zipCode='103';city='臺北市';area='大同區';address='測試路123號';lineId='testline';agreeTerms=$true;referralCode='TESTCODE'} | ConvertTo-Json; try { $r = Invoke-RestMethod -Uri '%BASE_URL%/auth/register' -Method Post -Body $body -ContentType 'application/json'; Write-Host 'PASS - 註冊成功' -ForegroundColor Green; Write-Host '  UserID:' $r.data.user.id; Write-Host '  Email:' $r.data.user.email; Write-Host '  暱稱:' $r.data.user.nickname; Write-Host '  手機:' $r.data.user.phoneNumber; Write-Host '  收件人:' $r.data.user.recipientName; Write-Host '  城市:' $r.data.user.city; Write-Host '  區域:' $r.data.user.district; Write-Host '  地址:' $r.data.user.addressDetail; Write-Host '  LINE ID:' $r.data.user.lineId; Write-Host '  Token:' $r.data.accessToken.substring(0,30) '...'; } catch { Write-Host 'FAIL:' $_.Exception.Message -ForegroundColor Red }"

REM 測試密碼不一致
echo.
echo [2] POST /auth/register - 測試密碼不一致
echo ================================================
powershell -Command "$body = @{email='test2@example.com';password='123456';confirmPassword='654321';nickname='測試用戶2';phoneNumber='0912345679';addressName='王小明';zipCode='103';city='臺北市';area='大同區';address='測試路123號';agreeTerms=$true} | ConvertTo-Json; try { $r = Invoke-RestMethod -Uri '%BASE_URL%/auth/register' -Method Post -Body $body -ContentType 'application/json'; Write-Host 'FAIL - 應該要失敗但成功了' -ForegroundColor Red } catch { if ($_.Exception.Message -like '*密碼*一致*') { Write-Host 'PASS - 正確攔截密碼不一致' -ForegroundColor Green } else { Write-Host 'FAIL:' $_.Exception.Message -ForegroundColor Red } }"

REM 測試缺少必填欄位
echo.
echo [3] POST /auth/register - 測試缺少必填欄位
echo ================================================
powershell -Command "$body = @{email='test3@example.com';password='123456';confirmPassword='123456'} | ConvertTo-Json; try { $r = Invoke-RestMethod -Uri '%BASE_URL%/auth/register' -Method Post -Body $body -ContentType 'application/json'; Write-Host 'FAIL - 應該要失敗但成功了' -ForegroundColor Red } catch { Write-Host 'PASS - 正確攔截缺少必填欄位' -ForegroundColor Green; Write-Host '  錯誤:' $_.Exception.Message }"

echo.
echo ================================================
echo  測試完成！
echo ================================================
echo.
echo 檢查重點：
echo   - 完整註冊資料能成功建立會員
echo   - 密碼不一致會被攔截
echo   - 缺少必填欄位會被攔截
echo   - 返回的 User 物件包含所有新增的欄位
echo.
endlocal
