# 抽獎流程完整測試腳本
# 使用方式: powershell -ExecutionPolicy Bypass -File test-lottery-flow.ps1

$baseUrl = "http://localhost:8080/api"
$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " 一番賞抽獎流程完整測試" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# ===== Step 1: 後台管理員登入 =====
Write-Host "`n[Step 1] 後台管理員登入..." -ForegroundColor Yellow
$loginBody = @{
    email = "admin@kuji.com"
    password = "admin123"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method POST -ContentType "application/json" -Body $loginBody
    $adminToken = $loginResponse.data.token
    Write-Host "✅ 登入成功，取得 Token" -ForegroundColor Green
    Write-Host "   Token 前 50 字元: $($adminToken.Substring(0, [Math]::Min(50, $adminToken.Length)))..." -ForegroundColor Gray
} catch {
    Write-Host "❌ 登入失敗: $_" -ForegroundColor Red
    exit 1
}

# 設定 Headers
$adminHeaders = @{
    Authorization = "Bearer $adminToken"
    "Content-Type" = "application/json"
}

# ===== Step 2: 查詢店家 (需要 storeId) =====
Write-Host "`n[Step 2] 查詢店家列表..." -ForegroundColor Yellow
try {
    $storeResponse = Invoke-RestMethod -Uri "$baseUrl/admin/stores" -Method GET -Headers $adminHeaders
    if ($storeResponse.data -and $storeResponse.data.Count -gt 0) {
        $storeId = $storeResponse.data[0].id
        Write-Host "✅ 取得店家 ID: $storeId" -ForegroundColor Green
    } else {
        Write-Host "⚠️ 沒有店家資料，需要先建立店家" -ForegroundColor Yellow
        # 建立測試店家
        $storeBody = @{
            name = "測試一番賞店"
            description = "這是一間測試用店家"
        } | ConvertTo-Json
        $createStoreResponse = Invoke-RestMethod -Uri "$baseUrl/admin/stores" -Method POST -Headers $adminHeaders -Body $storeBody
        $storeId = $createStoreResponse.data.id
        Write-Host "✅ 已建立測試店家 ID: $storeId" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ 查詢/建立店家失敗: $_" -ForegroundColor Red
    # 使用預設值繼續
    $storeId = $null
}

# ===== Step 3: 建立一番賞商品（含獎品） =====
Write-Host "`n[Step 3] 建立一番賞商品（LOTTERY_MODE）..." -ForegroundColor Yellow
$lotteryBody = @{
    lottery = @{
        title = "測試鬼滅一番賞 $(Get-Date -Format 'yyyyMMdd-HHmmss')"
        category = "OFFICIAL"
        subCategory = "LOTTERY_MODE"
        pricePerDraw = 80
        totalDraws = 10
        maxDraws = 10
        status = "ON_SHELF"
        description = "這是一個測試用的一番賞商品"
        imageUrl = "https://example.com/test.jpg"
    }
    prizes = @(
        @{
            name = "A賞 炭治郎公仔"
            level = "A"
            quantity = 1
            imageUrl = "https://example.com/prize-a.jpg"
            pointValue = 500
        },
        @{
            name = "B賞 禰豆子公仔"
            level = "B"
            quantity = 2
            imageUrl = "https://example.com/prize-b.jpg"
            pointValue = 300
        },
        @{
            name = "C賞 掛軸"
            level = "C"
            quantity = 3
            imageUrl = "https://example.com/prize-c.jpg"
            pointValue = 150
        },
        @{
            name = "D賞 色紙"
            level = "D"
            quantity = 4
            imageUrl = "https://example.com/prize-d.jpg"
            pointValue = 80
        }
    )
} | ConvertTo-Json -Depth 5

try {
    $createLotteryResponse = Invoke-RestMethod -Uri "$baseUrl/admin/lottery/with-prizes" -Method POST -Headers $adminHeaders -Body $lotteryBody
    $lotteryId = $createLotteryResponse.data.id
    Write-Host "✅ 一番賞建立成功，ID: $lotteryId" -ForegroundColor Green
    Write-Host "   商品標題: $($createLotteryResponse.data.title)" -ForegroundColor Gray
} catch {
    Write-Host "❌ 建立一番賞失敗: $_" -ForegroundColor Red
    Write-Host "   Response: $($_.Exception.Response)" -ForegroundColor Red
    exit 1
}

# ===== Step 4: 檢查籤位是否生成 =====
Write-Host "`n[Step 4] 檢查籤位是否自動生成..." -ForegroundColor Yellow
try {
    $ticketsResponse = Invoke-RestMethod -Uri "$baseUrl/admin/lottery/$lotteryId/tickets" -Method GET -Headers $adminHeaders
    $ticketCount = $ticketsResponse.data.Count
    Write-Host "✅ 籤位數量: $ticketCount" -ForegroundColor Green
    if ($ticketCount -gt 0) {
        Write-Host "   第一個籤位: 序號=$($ticketsResponse.data[0].ticketNumber), 狀態=$($ticketsResponse.data[0].status)" -ForegroundColor Gray
    }
} catch {
    Write-Host "⚠️ 檢查籤位失敗（可能 API 尚未實作）: $_" -ForegroundColor Yellow
}

# ===== Step 5: 前台用戶登入/註冊 =====
Write-Host "`n[Step 5] 前台用戶登入..." -ForegroundColor Yellow
$userLoginBody = @{
    email = "testuser@example.com"
    password = "test12345"
} | ConvertTo-Json

try {
    $userLoginResponse = Invoke-RestMethod -Uri "$baseUrl/user/login" -Method POST -ContentType "application/json" -Body $userLoginBody
    $userToken = $userLoginResponse.data.token
    Write-Host "✅ 用戶登入成功" -ForegroundColor Green
} catch {
    Write-Host "⚠️ 用戶登入失敗，嘗試註冊..." -ForegroundColor Yellow
    $registerBody = @{
        email = "testuser@example.com"
        password = "test12345"
        nickname = "測試用戶"
    } | ConvertTo-Json
    try {
        $registerResponse = Invoke-RestMethod -Uri "$baseUrl/user/register" -Method POST -ContentType "application/json" -Body $registerBody
        $userToken = $registerResponse.data.token
        Write-Host "✅ 用戶註冊成功" -ForegroundColor Green
    } catch {
        Write-Host "⚠️ 註冊也失敗: $_" -ForegroundColor Yellow
        # 繼續使用管理員 Token
        $userToken = $adminToken
        Write-Host "   使用管理員 Token 繼續測試" -ForegroundColor Gray
    }
}

$userHeaders = @{
    Authorization = "Bearer $userToken"
    "Content-Type" = "application/json"
}

# ===== Step 6: 前台瀏覽商品列表 =====
Write-Host "`n[Step 6] 前台查詢商品列表..." -ForegroundColor Yellow
try {
    $listBody = @{} | ConvertTo-Json
    $browseResponse = Invoke-RestMethod -Uri "$baseUrl/lottery/browse/list" -Method POST -Headers $userHeaders -Body $listBody
    Write-Host "✅ 商品列表查詢成功，共 $($browseResponse.data.Count) 個商品" -ForegroundColor Green
    if ($browseResponse.data.Count -gt 0) {
        Write-Host "   最新商品: $($browseResponse.data[0].title)" -ForegroundColor Gray
    }
} catch {
    Write-Host "⚠️ 商品列表查詢失敗: $_" -ForegroundColor Yellow
}

# ===== Step 7: 查詢商品詳情（含籤位和獎品） =====
Write-Host "`n[Step 7] 查詢商品詳情（含籤位）..." -ForegroundColor Yellow
try {
    $detailResponse = Invoke-RestMethod -Uri "$baseUrl/lottery/browse/$lotteryId/detail" -Method GET -Headers $userHeaders
    Write-Host "✅ 商品詳情查詢成功" -ForegroundColor Green
    Write-Host "   標題: $($detailResponse.data.title)" -ForegroundColor Gray
    Write-Host "   籤位數: $($detailResponse.data.tickets.Count)" -ForegroundColor Gray
    Write-Host "   獎品數: $($detailResponse.data.prizes.Count)" -ForegroundColor Gray
} catch {
    Write-Host "⚠️ 商品詳情查詢失敗: $_" -ForegroundColor Yellow
}

# ===== Step 8: 抽獎 =====
Write-Host "`n[Step 8] 進行抽獎..." -ForegroundColor Yellow
$drawBody = @{
    lotteryId = $lotteryId
    drawCount = 1
} | ConvertTo-Json

try {
    $drawResponse = Invoke-RestMethod -Uri "$baseUrl/lottery/draw" -Method POST -Headers $userHeaders -Body $drawBody
    Write-Host "✅ 抽獎成功!" -ForegroundColor Green
    if ($drawResponse.data) {
        Write-Host "   抽獎結果:" -ForegroundColor Gray
        foreach ($result in $drawResponse.data) {
            Write-Host "     籤號: $($result.ticketNumber), 獎項: $($result.prizeLevel) - $($result.prizeName)" -ForegroundColor Cyan
        }
    }
} catch {
    Write-Host "❌ 抽獎失敗: $_" -ForegroundColor Red
    Write-Host "   請確認：" -ForegroundColor Yellow
    Write-Host "   1. 用戶餘額是否足夠" -ForegroundColor Yellow
    Write-Host "   2. 商品是否上架中" -ForegroundColor Yellow
    Write-Host "   3. 籤位是否已生成" -ForegroundColor Yellow
}

# ===== Step 9: 查詢賞品盒 =====
Write-Host "`n[Step 9] 查詢賞品盒..." -ForegroundColor Yellow
try {
    $prizeBoxResponse = Invoke-RestMethod -Uri "$baseUrl/prize-box" -Method GET -Headers $userHeaders
    Write-Host "✅ 賞品盒查詢成功" -ForegroundColor Green
    if ($prizeBoxResponse.data -and $prizeBoxResponse.data.Count -gt 0) {
        Write-Host "   賞品盒內容:" -ForegroundColor Gray
        foreach ($item in $prizeBoxResponse.data) {
            Write-Host "     $($item.prizeName) (狀態: $($item.status))" -ForegroundColor Cyan
        }
    } else {
        Write-Host "   賞品盒為空" -ForegroundColor Gray
    }
} catch {
    Write-Host "⚠️ 賞品盒查詢失敗: $_" -ForegroundColor Yellow
}

# ===== Step 10: 出貨（建立訂單） =====
Write-Host "`n[Step 10] 從賞品盒出貨..." -ForegroundColor Yellow
try {
    # 假設取第一個賞品盒物品
    if ($prizeBoxResponse.data -and $prizeBoxResponse.data.Count -gt 0) {
        $prizeBoxId = $prizeBoxResponse.data[0].id
        $shipBody = @{
            prizeBoxIds = @($prizeBoxId)
            shippingAddressId = "test-address-id"
        } | ConvertTo-Json -Depth 3
        
        $shipResponse = Invoke-RestMethod -Uri "$baseUrl/prize-box/ship" -Method POST -Headers $userHeaders -Body $shipBody
        Write-Host "✅ 出貨成功，訂單已建立" -ForegroundColor Green
        if ($shipResponse.data) {
            Write-Host "   訂單號: $($shipResponse.data.orderNumber)" -ForegroundColor Gray
        }
    } else {
        Write-Host "⚠️ 賞品盒為空，無法出貨" -ForegroundColor Yellow
    }
} catch {
    Write-Host "⚠️ 出貨失敗: $_" -ForegroundColor Yellow
}

# ===== Step 11: 後台查詢訂單 =====
Write-Host "`n[Step 11] 後台查詢訂單列表..." -ForegroundColor Yellow
try {
    $ordersResponse = Invoke-RestMethod -Uri "$baseUrl/admin/orders" -Method GET -Headers $adminHeaders
    Write-Host "✅ 訂單列表查詢成功" -ForegroundColor Green
    if ($ordersResponse.data) {
        Write-Host "   訂單數量: $($ordersResponse.data.Count)" -ForegroundColor Gray
    }
} catch {
    Write-Host "⚠️ 訂單查詢失敗: $_" -ForegroundColor Yellow
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host " 測試完成!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "測試項目回顧:" -ForegroundColor White
Write-Host "  1. 後台管理員登入" -ForegroundColor Gray
Write-Host "  2. 查詢/建立店家" -ForegroundColor Gray
Write-Host "  3. 建立一番賞商品（含獎品）" -ForegroundColor Gray
Write-Host "  4. 檢查籤位自動生成" -ForegroundColor Gray
Write-Host "  5. 前台用戶登入" -ForegroundColor Gray
Write-Host "  6. 前台瀏覽商品列表" -ForegroundColor Gray
Write-Host "  7. 查詢商品詳情" -ForegroundColor Gray
Write-Host "  8. 執行抽獎" -ForegroundColor Gray
Write-Host "  9. 查詢賞品盒" -ForegroundColor Gray
Write-Host " 10. 從賞品盒出貨" -ForegroundColor Gray
Write-Host " 11. 後台查詢訂單" -ForegroundColor Gray
