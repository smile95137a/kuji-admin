# KUJI API 完整測試腳本 (PowerShell)
# 解決 UTF-8 編碼問題
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

$BASE_URL = "http://localhost:8080/api"
$Pass = 0
$Fail = 0
$Total = 0

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  KUJI 完整 API 測試腳本 (PowerShell)" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

function Test-Api {
    param(
        [string]$Method,
        [string]$Endpoint,
        [string]$Description,
        [string]$Body = $null,
        [hashtable]$Headers = @{},
        [bool]$ExpectFail = $false
    )
    
    $script:Total++
    Write-Host ""
    Write-Host "[$Total] $Method $Endpoint - $Description..." -NoNewline
    
    try {
        $params = @{
            Uri = "$BASE_URL$Endpoint"
            Method = $Method
            ContentType = "application/json"
            ErrorAction = "Stop"
        }
        
        if ($Headers.Count -gt 0) {
            $params.Headers = $Headers
        }
        
        if ($Body) {
            $params.Body = $Body
        }
        
        $response = Invoke-RestMethod @params
        
        if ($ExpectFail) {
            Write-Host " FAIL - 應該失敗但成功了" -ForegroundColor Red
            $script:Fail++
            return $null
        }
        
        if ($response.success) {
            Write-Host " PASS" -ForegroundColor Green
            $script:Pass++
            return $response.data
        } else {
            Write-Host " FAIL" -ForegroundColor Red
            $script:Fail++
            return $null
        }
    }
    catch {
        if ($ExpectFail) {
            Write-Host " PASS (預期失敗)" -ForegroundColor Green
            $script:Pass++
        } else {
            Write-Host " ERROR: $($_.Exception.Message)" -ForegroundColor Red
            $script:Fail++
        }
        return $null
    }
}

# ====== 第一部分：後台認證 API ======
Write-Host ""
Write-Host "================================================" -ForegroundColor Yellow
Write-Host "第一部分：後台認證 API" -ForegroundColor Yellow
Write-Host "================================================" -ForegroundColor Yellow

$loginBody = '{"username":"admin@kuji.com","password":"admin123"}'
$loginResult = Test-Api -Method "POST" -Endpoint "/admin/auth/login" -Description "後台登入" -Body $loginBody
$ADMIN_TOKEN = $loginResult.accessToken

if ($ADMIN_TOKEN) {
    Write-Host "    Token: $($ADMIN_TOKEN.Substring(0, [Math]::Min(50, $ADMIN_TOKEN.Length)))..." -ForegroundColor DarkGray
}

$adminHeaders = @{ Authorization = "Bearer $ADMIN_TOKEN" }

# ====== 第二部分：後台選單管理 API ======
Write-Host ""
Write-Host "================================================" -ForegroundColor Yellow
Write-Host "第二部分：後台選單管理 API" -ForegroundColor Yellow
Write-Host "================================================" -ForegroundColor Yellow

Test-Api -Method "GET" -Endpoint "/admin/menus/accessible" -Description "取得可存取選單" -Headers $adminHeaders
Test-Api -Method "GET" -Endpoint "/admin/menus" -Description "取得所有選單" -Headers $adminHeaders
Test-Api -Method "GET" -Endpoint "/admin/menus/tree" -Description "取得選單樹狀結構" -Headers $adminHeaders

# ====== 第三部分：後台店家管理 API ======
Write-Host ""
Write-Host "================================================" -ForegroundColor Yellow
Write-Host "第三部分：後台店家管理 API" -ForegroundColor Yellow
Write-Host "================================================" -ForegroundColor Yellow

Test-Api -Method "GET" -Endpoint "/admin/stores/options" -Description "取得店家選項" -Headers $adminHeaders

# ====== 第四部分：後台角色管理 API ======
Write-Host ""
Write-Host "================================================" -ForegroundColor Yellow
Write-Host "第四部分：後台角色管理 API" -ForegroundColor Yellow
Write-Host "================================================" -ForegroundColor Yellow

Test-Api -Method "GET" -Endpoint "/admin/roles" -Description "取得所有角色" -Headers $adminHeaders

# ====== 第五部分：後台推薦碼管理 API ======
Write-Host ""
Write-Host "================================================" -ForegroundColor Yellow
Write-Host "第五部分：後台推薦碼管理 API" -ForegroundColor Yellow
Write-Host "================================================" -ForegroundColor Yellow

Test-Api -Method "GET" -Endpoint "/admin/referral-codes/my-store" -Description "取得我的店家推薦碼" -Headers $adminHeaders

# ====== 第六部分：後台一番賞管理 API ======
Write-Host ""
Write-Host "================================================" -ForegroundColor Yellow
Write-Host "第六部分：後台一番賞管理 API" -ForegroundColor Yellow
Write-Host "================================================" -ForegroundColor Yellow

Test-Api -Method "POST" -Endpoint "/admin/lottery/list" -Description "查詢一番賞列表" -Body "{}" -Headers $adminHeaders

# ====== 第七部分：後台 Banner 管理 API ======
Write-Host ""
Write-Host "================================================" -ForegroundColor Yellow
Write-Host "第七部分：後台 Banner 管理 API" -ForegroundColor Yellow
Write-Host "================================================" -ForegroundColor Yellow

Test-Api -Method "GET" -Endpoint "/admin/banner" -Description "取得所有 Banner" -Headers $adminHeaders

# ====== 第八部分：後台最新消息管理 API ======
Write-Host ""
Write-Host "================================================" -ForegroundColor Yellow
Write-Host "第八部分：後台最新消息管理 API" -ForegroundColor Yellow
Write-Host "================================================" -ForegroundColor Yellow

Test-Api -Method "GET" -Endpoint "/admin/news" -Description "取得所有最新消息" -Headers $adminHeaders

# ====== 第九部分：後台跑馬燈管理 API ======
Write-Host ""
Write-Host "================================================" -ForegroundColor Yellow
Write-Host "第九部分：後台跑馬燈管理 API" -ForegroundColor Yellow
Write-Host "================================================" -ForegroundColor Yellow

Test-Api -Method "GET" -Endpoint "/admin/marquee" -Description "取得所有跑馬燈" -Headers $adminHeaders

# ====== 第十部分：後台儲值方案管理 API ======
Write-Host ""
Write-Host "================================================" -ForegroundColor Yellow
Write-Host "第十部分：後台儲值方案管理 API" -ForegroundColor Yellow
Write-Host "================================================" -ForegroundColor Yellow

Test-Api -Method "GET" -Endpoint "/admin/recharge-plan" -Description "取得所有儲值方案" -Headers $adminHeaders

# ====== 第十一部分：前台行政區 API (公開) ======
Write-Host ""
Write-Host "================================================" -ForegroundColor Yellow
Write-Host "第十一部分：前台行政區 API (公開)" -ForegroundColor Yellow
Write-Host "================================================" -ForegroundColor Yellow

Test-Api -Method "GET" -Endpoint "/district/cities" -Description "取得所有縣市"

$encodedCity = [System.Web.HttpUtility]::UrlEncode("臺北市")
Test-Api -Method "GET" -Endpoint "/district/districts/$encodedCity" -Description "取得臺北市行政區"

Test-Api -Method "GET" -Endpoint "/district/tree" -Description "取得行政區樹狀結構"
Test-Api -Method "GET" -Endpoint "/district/all" -Description "取得所有行政區"

# ====== 第十二部分：前台枚舉 API (公開) ======
Write-Host ""
Write-Host "================================================" -ForegroundColor Yellow
Write-Host "第十二部分：前台枚舉 API (公開)" -ForegroundColor Yellow
Write-Host "================================================" -ForegroundColor Yellow

Test-Api -Method "GET" -Endpoint "/enums/all" -Description "取得所有枚舉"
Test-Api -Method "GET" -Endpoint "/enums/prize-level" -Description "取得獎項等級枚舉"

# ====== 第十三部分：前台 Banner API (公開) ======
Write-Host ""
Write-Host "================================================" -ForegroundColor Yellow
Write-Host "第十三部分：前台 Banner API (公開)" -ForegroundColor Yellow
Write-Host "================================================" -ForegroundColor Yellow

Test-Api -Method "GET" -Endpoint "/banner/carousel" -Description "取得輪播 Banner"

# ====== 第十四部分：前台最新消息 API (公開) ======
Write-Host ""
Write-Host "================================================" -ForegroundColor Yellow
Write-Host "第十四部分：前台最新消息 API (公開)" -ForegroundColor Yellow
Write-Host "================================================" -ForegroundColor Yellow

Test-Api -Method "GET" -Endpoint "/news" -Description "取得最新消息列表"
Test-Api -Method "GET" -Endpoint "/news?limit=5" -Description "取得最新 5 則消息"

# ====== 第十五部分：前台跑馬燈 API (公開) ======
Write-Host ""
Write-Host "================================================" -ForegroundColor Yellow
Write-Host "第十五部分：前台跑馬燈 API (公開)" -ForegroundColor Yellow
Write-Host "================================================" -ForegroundColor Yellow

Test-Api -Method "GET" -Endpoint "/marquee" -Description "取得跑馬燈"

# ====== 第十六部分：前台一番賞瀏覽 API (公開) ======
Write-Host ""
Write-Host "================================================" -ForegroundColor Yellow
Write-Host "第十六部分：前台一番賞瀏覽 API (公開)" -ForegroundColor Yellow
Write-Host "================================================" -ForegroundColor Yellow

Test-Api -Method "POST" -Endpoint "/lottery/browse/list" -Description "瀏覽一番賞列表" -Body "{}"

# ====== 第十七部分：前台店家 API (公開) ======
Write-Host ""
Write-Host "================================================" -ForegroundColor Yellow
Write-Host "第十七部分：前台店家 API (公開)" -ForegroundColor Yellow
Write-Host "================================================" -ForegroundColor Yellow

Test-Api -Method "GET" -Endpoint "/stores/options" -Description "取得店家選項"

# ====== 第十八部分：前台儲值方案 API (公開) ======
Write-Host ""
Write-Host "================================================" -ForegroundColor Yellow
Write-Host "第十八部分：前台儲值方案 API (公開)" -ForegroundColor Yellow
Write-Host "================================================" -ForegroundColor Yellow

Test-Api -Method "GET" -Endpoint "/api/recharge-plan" -Description "取得儲值方案"

# ====== 第十九部分：前台認證 API ======
Write-Host ""
Write-Host "================================================" -ForegroundColor Yellow
Write-Host "第十九部分：前台認證 API" -ForegroundColor Yellow
Write-Host "================================================" -ForegroundColor Yellow

$testEmail = "test$(Get-Random)@example.com"
$registerBody = "{`"email`":`"$testEmail`",`"password`":`"Test123456`",`"nickname`":`"測試用戶`"}"
$registerResult = Test-Api -Method "POST" -Endpoint "/auth/register" -Description "前台註冊 ($testEmail)" -Body $registerBody
$USER_TOKEN = $registerResult.accessToken

if ($USER_TOKEN) {
    Write-Host "    User Token: $($USER_TOKEN.Substring(0, [Math]::Min(50, $USER_TOKEN.Length)))..." -ForegroundColor DarkGray
}

$loginBody = "{`"email`":`"$testEmail`",`"password`":`"Test123456`"}"
Test-Api -Method "POST" -Endpoint "/auth/login" -Description "前台登入" -Body $loginBody

$forgotBody = "{`"email`":`"$testEmail`"}"
Test-Api -Method "POST" -Endpoint "/auth/forgot-password" -Description "忘記密碼請求" -Body $forgotBody

$resetBody = '{"token":"invalid-token","newPassword":"NewPass123","confirmPassword":"NewPass123"}'
Test-Api -Method "POST" -Endpoint "/auth/reset-password" -Description "重設密碼 (無效 token - 預期失敗)" -Body $resetBody -ExpectFail $true

$userHeaders = @{ Authorization = "Bearer $USER_TOKEN" }

# ====== 第二十部分：前台用戶 API (需要認證) ======
Write-Host ""
Write-Host "================================================" -ForegroundColor Yellow
Write-Host "第二十部分：前台用戶 API (需要認證)" -ForegroundColor Yellow
Write-Host "================================================" -ForegroundColor Yellow

Test-Api -Method "GET" -Endpoint "/user/me" -Description "取得當前用戶資訊" -Headers $userHeaders
Test-Api -Method "GET" -Endpoint "/user/hello" -Description "測試 Hello API" -Headers $userHeaders

# ====== 第二十一部分：前台錢包 API (需要認證) ======
Write-Host ""
Write-Host "================================================" -ForegroundColor Yellow
Write-Host "第二十一部分：前台錢包 API (需要認證)" -ForegroundColor Yellow
Write-Host "================================================" -ForegroundColor Yellow

Test-Api -Method "GET" -Endpoint "/api/wallet" -Description "取得我的錢包" -Headers $userHeaders
Test-Api -Method "POST" -Endpoint "/api/wallet/transactions" -Description "查詢交易記錄" -Body "{}" -Headers $userHeaders

# ====== 第二十二部分：前台訂單 API (需要認證) ======
Write-Host ""
Write-Host "================================================" -ForegroundColor Yellow
Write-Host "第二十二部分：前台訂單 API (需要認證)" -ForegroundColor Yellow
Write-Host "================================================" -ForegroundColor Yellow

Test-Api -Method "POST" -Endpoint "/api/order/list" -Description "查詢我的訂單列表" -Body "{}" -Headers $userHeaders

# ====== 第二十三部分：前台獎品盒 API (需要認證) ======
Write-Host ""
Write-Host "================================================" -ForegroundColor Yellow
Write-Host "第二十三部分：前台獎品盒 API (需要認證)" -ForegroundColor Yellow
Write-Host "================================================" -ForegroundColor Yellow

Test-Api -Method "POST" -Endpoint "/api/prize-box/list" -Description "查詢我的獎品盒" -Body "{}" -Headers $userHeaders

# ====== 第二十四部分：權限測試 ======
Write-Host ""
Write-Host "================================================" -ForegroundColor Yellow
Write-Host "第二十四部分：權限測試" -ForegroundColor Yellow
Write-Host "================================================" -ForegroundColor Yellow

Test-Api -Method "GET" -Endpoint "/admin/menus/accessible" -Description "未登入訪問後台 (預期失敗)" -ExpectFail $true
Test-Api -Method "GET" -Endpoint "/admin/menus/accessible" -Description "前台 Token 訪問後台 (預期失敗)" -Headers $userHeaders -ExpectFail $true
Test-Api -Method "GET" -Endpoint "/district/cities" -Description "後台 Token 訪問前台公開 API" -Headers $adminHeaders
Test-Api -Method "GET" -Endpoint "/api/wallet" -Description "未登入訪問需認證 API (預期失敗)" -ExpectFail $true

# ====== 結果摘要 ======
Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "              測試結果摘要" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "總共測試：$Total 個 API"
Write-Host "通過：$Pass" -ForegroundColor Green
Write-Host "失敗：$Fail" -ForegroundColor Red
Write-Host ""

if ($Fail -gt 0) {
    Write-Host "狀態：有 $Fail 個測試失敗" -ForegroundColor Red
    exit 1
} else {
    Write-Host "狀態：所有測試通過！" -ForegroundColor Green
    exit 0
}
