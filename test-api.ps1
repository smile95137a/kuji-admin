# 測試 API 腳本

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "開始測試完整的抽獎流程" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$baseUrl = "http://localhost:8080/api"

# Step 1: 登入取得 Token
Write-Host "`n[Step 1] 登入後台取得 Token..." -ForegroundColor Yellow
$loginBody = @{
    email = "admin@kuji.com"
    password = "admin123"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/admin/auth/login" -Method POST -ContentType "application/json" -Body $loginBody
    $token = $loginResponse.data.token
    Write-Host "✅ 登入成功！Token: $($token.Substring(0, 50))..." -ForegroundColor Green
} catch {
    Write-Host "❌ 登入失敗: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

# Step 2: 查詢店家
Write-Host "`n[Step 2] 查詢店家列表..." -ForegroundColor Yellow
try {
    $storesResponse = Invoke-RestMethod -Uri "$baseUrl/admin/store/list" -Method POST -Headers $headers -Body "{}"
    $stores = $storesResponse.data
    Write-Host "✅ 查詢到 $($stores.Count) 個店家" -ForegroundColor Green
    
    if ($stores.Count -eq 0) {
        Write-Host "⚠️ 沒有店家，先建立一個..." -ForegroundColor Yellow
        $newStore = @{
            name = "測試店家"
            description = "用於測試的店家"
            status = "ACTIVE"
        } | ConvertTo-Json
        $storeResponse = Invoke-RestMethod -Uri "$baseUrl/admin/store" -Method POST -Headers $headers -Body $newStore
        $storeId = $storeResponse.data.id
        Write-Host "✅ 店家建立成功: $storeId" -ForegroundColor Green
    } else {
        $storeId = $stores[0].id
        Write-Host "使用現有店家: $storeId ($($stores[0].name))" -ForegroundColor Cyan
    }
} catch {
    Write-Host "❌ 查詢店家失敗: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host $_.Exception.Response
    exit 1
}

# Step 3: 建立商品與獎品
Write-Host "`n[Step 3] 建立商品與獎品..." -ForegroundColor Yellow
$lotteryData = @{
    lottery = @{
        storeId = $storeId
        title = "測試一番賞 - $(Get-Date -Format 'HHmmss')"
        description = "測試用的一番賞商品"
        category = "CUSTOM_LOTTERY"
        subCategory = "LOTTERY_MODE"
        pricePerDraw = 100
        maxDraws = 10
        status = "ON_SHELF"
    }
    prizes = @(
        @{
            name = "A賞 - 限定公仔"
            level = "A"
            quantity = 1
            isGrandPrize = $true
        },
        @{
            name = "B賞 - 精美掛畫"
            level = "B"
            quantity = 2
            isGrandPrize = $false
        },
        @{
            name = "C賞 - 鑰匙圈"
            level = "C"
            quantity = 3
            isGrandPrize = $false
        },
        @{
            name = "D賞 - 小公仔"
            level = "D"
            quantity = 4
            isGrandPrize = $false
        }
    )
} | ConvertTo-Json -Depth 5

try {
    $lotteryResponse = Invoke-RestMethod -Uri "$baseUrl/admin/lottery/with-prizes" -Method POST -Headers $headers -Body $lotteryData
    $lottery = $lotteryResponse.data.lottery
    $lotteryId = $lottery.id
    Write-Host "✅ 商品建立成功: $lotteryId ($($lottery.title))" -ForegroundColor Green
    Write-Host "   獎品數量: $($lotteryResponse.data.prizes.Count)" -ForegroundColor Cyan
} catch {
    Write-Host "❌ 建立商品失敗: $($_.Exception.Message)" -ForegroundColor Red
    $reader = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream())
    Write-Host $reader.ReadToEnd()
    exit 1
}

# Step 4: 檢查籤位是否生成
Write-Host "`n[Step 4] 檢查籤位是否生成..." -ForegroundColor Yellow
try {
    $ticketsResponse = Invoke-RestMethod -Uri "$baseUrl/lottery/draw/$lotteryId/tickets" -Method GET -Headers $headers
    $tickets = $ticketsResponse.data.tickets
    Write-Host "✅ 籤位數量: $($tickets.Count)" -ForegroundColor Green
    
    if ($tickets.Count -eq 0) {
        Write-Host "❌ 籤位未生成！這是一個 BUG" -ForegroundColor Red
        exit 1
    }
    
    # 顯示前 5 個籤位
    Write-Host "   前 5 個籤位:" -ForegroundColor Cyan
    $tickets | Select-Object -First 5 | ForEach-Object {
        Write-Host "   - 籤位 $($_.ticketNumber): $($_.status)" -ForegroundColor Gray
    }
} catch {
    Write-Host "❌ 查詢籤位失敗: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Step 5: 前台查詢商品列表
Write-Host "`n[Step 5] 前台查詢商品列表..." -ForegroundColor Yellow
try {
    $frontendListResponse = Invoke-RestMethod -Uri "$baseUrl/lottery/browse/list" -Method POST -ContentType "application/json" -Body "{}"
    $frontendLotteries = $frontendListResponse.data
    Write-Host "✅ 前台商品數量: $($frontendLotteries.Count)" -ForegroundColor Green
} catch {
    Write-Host "❌ 前台查詢商品失敗: $($_.Exception.Message)" -ForegroundColor Red
}

# Step 6: 前台查詢店家商品
Write-Host "`n[Step 6] 前台查詢店家 $storeId 的商品..." -ForegroundColor Yellow
try {
    $storeProductsResponse = Invoke-RestMethod -Uri "$baseUrl/lottery/browse/store/$storeId" -Method GET
    $storeProducts = $storeProductsResponse.data
    Write-Host "✅ 該店家商品數量: $($storeProducts.Count)" -ForegroundColor Green
} catch {
    Write-Host "❌ 查詢店家商品失敗: $($_.Exception.Message)" -ForegroundColor Red
}

# Step 7: 註冊/登入前台使用者
Write-Host "`n[Step 7] 建立前台測試使用者..." -ForegroundColor Yellow
$testEmail = "test_user_$(Get-Date -Format 'HHmmss')@test.com"
$registerBody = @{
    username = "testuser_$(Get-Date -Format 'HHmmss')"
    email = $testEmail
    password = "Test123456"
    nickname = "測試玩家"
} | ConvertTo-Json

try {
    $registerResponse = Invoke-RestMethod -Uri "$baseUrl/auth/register" -Method POST -ContentType "application/json" -Body $registerBody
    Write-Host "✅ 使用者註冊成功" -ForegroundColor Green
    
    # 登入取得 user token
    $userLoginBody = @{
        email = $testEmail
        password = "Test123456"
    } | ConvertTo-Json
    
    $userLoginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method POST -ContentType "application/json" -Body $userLoginBody
    $userToken = $userLoginResponse.data.token
    Write-Host "✅ 使用者登入成功！Token: $($userToken.Substring(0, 50))..." -ForegroundColor Green
} catch {
    Write-Host "⚠️ 註冊/登入失敗，嘗試使用現有使用者: $($_.Exception.Message)" -ForegroundColor Yellow
    
    # 嘗試使用 admin token 作為測試（因為 API 允許 admin 訪問前台 API）
    $userToken = $token
    Write-Host "使用 admin token 進行測試" -ForegroundColor Cyan
}

$userHeaders = @{
    "Authorization" = "Bearer $userToken"
    "Content-Type" = "application/json"
}

# Step 8: 執行抽獎
Write-Host "`n[Step 8] 執行抽獎..." -ForegroundColor Yellow
try {
    $drawBody = @{
        ticketNumber = $null
        drawCount = 1
    } | ConvertTo-Json
    
    $drawResponse = Invoke-RestMethod -Uri "$baseUrl/lottery/draw/$lotteryId/draw" -Method POST -Headers $userHeaders -Body $drawBody
    $drawResult = $drawResponse.data
    Write-Host "✅ 抽獎成功！" -ForegroundColor Green
    Write-Host "   籤位: $($drawResult.ticketNumber)" -ForegroundColor Cyan
    Write-Host "   獎品等級: $($drawResult.prizeLevel)" -ForegroundColor Cyan
    Write-Host "   獎品名稱: $($drawResult.prizeName)" -ForegroundColor Cyan
} catch {
    Write-Host "❌ 抽獎失敗: $($_.Exception.Message)" -ForegroundColor Red
    $reader = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream())
    Write-Host $reader.ReadToEnd()
}

# Step 9: 檢查保護時間（嘗試用另一個使用者抽獎）
Write-Host "`n[Step 9] 測試保護時間機制..." -ForegroundColor Yellow
# 這裡需要另一個使用者的 token 來測試保護時間
Write-Host "   (保護時間測試需要兩個不同的使用者)" -ForegroundColor Gray

# Step 10: 查詢賞品盒
Write-Host "`n[Step 10] 查詢賞品盒..." -ForegroundColor Yellow
try {
    $prizeBoxResponse = Invoke-RestMethod -Uri "$baseUrl/prize-box" -Method GET -Headers $userHeaders
    $prizeBox = $prizeBoxResponse.data
    Write-Host "✅ 賞品盒項目數量: $($prizeBox.Count)" -ForegroundColor Green
} catch {
    Write-Host "❌ 查詢賞品盒失敗: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "測試完成！" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
