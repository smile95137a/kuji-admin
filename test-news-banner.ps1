# News & Banner API 測試腳本

$baseUrl = "http://localhost:8080/api"
$newsId = ""
$bannerId = ""
$token = ""
$storeId = ""

Write-Host "🎯 開始 API 測試..." -ForegroundColor Green
Write-Host ""

# 測試 1：登入取得 Token
Write-Host "📋 測試 1：Admin 登入" -ForegroundColor Cyan
try {
    $loginBody = @{
        username = "admin@kuji.com"
        password = "admin123"
    } | ConvertTo-Json

    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post `
        -ContentType "application/json" -Body $loginBody

    $token = $loginResponse.data.accessToken
    Write-Host "✅ 登入成功" -ForegroundColor Green
    Write-Host "   Token: $($token.Substring(0, 20))..." -ForegroundColor Gray
} catch {
    Write-Host "❌ 登入失敗: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""

# 測試 2：查詢店家取得 storeId
Write-Host "📋 測試 2：查詢店家列表" -ForegroundColor Cyan
try {
    $headers = @{
        Authorization = "Bearer $token"
    }

    $storeResponse = Invoke-RestMethod -Uri "$baseUrl/admin/store/list" -Method Post `
        -Headers $headers -ContentType "application/json" -Body "{}"

    if ($storeResponse.data -and $storeResponse.data.Count -gt 0) {
        $storeId = $storeResponse.data[0].id
        Write-Host "✅ 查詢成功，取得 storeId: $storeId" -ForegroundColor Green
    } else {
        Write-Host "⚠️  查無店家資料" -ForegroundColor Yellow
        $storeId = "test-store-id"
    }
} catch {
    Write-Host "⚠️  查詢店家失敗，使用預設值: $_" -ForegroundColor Yellow
    $storeId = "test-store-id"
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Yellow
Write-Host "🧪 News API 測試" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow
Write-Host ""

# 測試 3：新增 News（草稿）
Write-Host "📋 測試 3：新增 News（草稿）" -ForegroundColor Cyan
try {
    $createNewsBody = @{
        title = "系統維護公告"
        content = "系統將於本週六凌晨進行例行維護，維護期間可能無法正常使用服務。"
        status = "DRAFT"
        imageUrl = "https://example.com/news1.jpg"
    } | ConvertTo-Json

    $headers = @{
        Authorization = "Bearer $token"
    }

    $newsResponse = Invoke-RestMethod -Uri "$baseUrl/admin/news" -Method Post `
        -Headers $headers -ContentType "application/json" -Body $createNewsBody

    $newsId = $newsResponse.data.id
    Write-Host "✅ 新增成功" -ForegroundColor Green
    Write-Host "   newsId: $newsId" -ForegroundColor Gray
    Write-Host "   title: $($newsResponse.data.title)" -ForegroundColor Gray
    Write-Host "   status: $($newsResponse.data.status)" -ForegroundColor Gray
    Write-Host "   statusName: $($newsResponse.data.statusName)" -ForegroundColor Gray
    
    # 檢查 null
    if (-not $newsResponse.data.id) { Write-Host "   ⚠️  id 為 null" -ForegroundColor Red }
    if (-not $newsResponse.data.title) { Write-Host "   ⚠️  title 為 null" -ForegroundColor Red }
    if (-not $newsResponse.data.status) { Write-Host "   ⚠️  status 為 null" -ForegroundColor Red }
    if (-not $newsResponse.data.statusName) { Write-Host "   ⚠️  statusName 為 null" -ForegroundColor Red }
    if (-not $newsResponse.data.createdBy) { Write-Host "   ⚠️  createdBy 為 null" -ForegroundColor Red }
} catch {
    Write-Host "❌ 新增失敗: $_" -ForegroundColor Red
}

Write-Host ""

# 測試 4：查詢 News 詳情
Write-Host "📋 測試 4：查詢 News 詳情" -ForegroundColor Cyan
try {
    $headers = @{
        Authorization = "Bearer $token"
    }

    $newsDetailResponse = Invoke-RestMethod -Uri "$baseUrl/admin/news/$newsId" -Method Get `
        -Headers $headers

    Write-Host "✅ 查詢成功" -ForegroundColor Green
    Write-Host "   title: $($newsDetailResponse.data.title)" -ForegroundColor Gray
    Write-Host "   content: $($newsDetailResponse.data.content.Substring(0, 30))..." -ForegroundColor Gray
    Write-Host "   status: $($newsDetailResponse.data.status)" -ForegroundColor Gray
} catch {
    Write-Host "❌ 查詢失敗: $_" -ForegroundColor Red
}

Write-Host ""

# 測試 5：更新 News
Write-Host "📋 測試 5：更新 News" -ForegroundColor Cyan
try {
    $updateNewsBody = @{
        title = "系統維護公告（更新）"
        content = "系統維護時間已調整為週日凌晨，造成不便敬請見諒。"
        status = "DRAFT"
    } | ConvertTo-Json

    $headers = @{
        Authorization = "Bearer $token"
    }

    $updateResponse = Invoke-RestMethod -Uri "$baseUrl/admin/news/$newsId" -Method Put `
        -Headers $headers -ContentType "application/json" -Body $updateNewsBody

    Write-Host "✅ 更新成功" -ForegroundColor Green
    Write-Host "   新標題: $($updateResponse.data.title)" -ForegroundColor Gray
} catch {
    Write-Host "❌ 更新失敗: $_" -ForegroundColor Red
}

Write-Host ""

# 測試 6：查詢 News 列表（後台）
Write-Host "📋 測試 6：查詢 News 列表（後台）" -ForegroundColor Cyan
try {
    $queryBody = @{
        condition = @{
            status = "DRAFT"
        }
    } | ConvertTo-Json

    $headers = @{
        Authorization = "Bearer $token"
    }

    $listResponse = Invoke-RestMethod -Uri "$baseUrl/admin/news/list" -Method Post `
        -Headers $headers -ContentType "application/json" -Body $queryBody

    Write-Host "✅ 查詢成功，共 $($listResponse.data.Count) 筆" -ForegroundColor Green
} catch {
    Write-Host "❌ 查詢失敗: $_" -ForegroundColor Red
}

Write-Host ""

# 測試 7：上架 News
Write-Host "📋 測試 7：上架 News" -ForegroundColor Cyan
try {
    $headers = @{
        Authorization = "Bearer $token"
    }

    $publishResponse = Invoke-RestMethod -Uri "$baseUrl/admin/news/$newsId/publish" -Method Post `
        -Headers $headers

    Write-Host "✅ 上架成功" -ForegroundColor Green
    Write-Host "   status: $($publishResponse.data.status)" -ForegroundColor Gray
    Write-Host "   statusName: $($publishResponse.data.statusName)" -ForegroundColor Gray
} catch {
    Write-Host "❌ 上架失敗: $_" -ForegroundColor Red
}

Write-Host ""

# 測試 8：查詢前台 News 列表
Write-Host "📋 測試 8：查詢前台 News 列表（無需登入）" -ForegroundColor Cyan
try {
    $frontendResponse = Invoke-RestMethod -Uri "$baseUrl/news?limit=10" -Method Get

    Write-Host "✅ 查詢成功，共 $($frontendResponse.data.Count) 筆" -ForegroundColor Green
    if ($frontendResponse.data.Count -gt 0) {
        Write-Host "   第一筆: $($frontendResponse.data[0].title)" -ForegroundColor Gray
    }
} catch {
    Write-Host "❌ 查詢失敗: $_" -ForegroundColor Red
}

Write-Host ""

# 測試 9：下架 News
Write-Host "📋 測試 9：下架 News" -ForegroundColor Cyan
try {
    $headers = @{
        Authorization = "Bearer $token"
    }

    $unpublishResponse = Invoke-RestMethod -Uri "$baseUrl/admin/news/$newsId/unpublish" -Method Post `
        -Headers $headers

    Write-Host "✅ 下架成功" -ForegroundColor Green
    Write-Host "   status: $($unpublishResponse.data.status)" -ForegroundColor Gray
} catch {
    Write-Host "❌ 下架失敗: $_" -ForegroundColor Red
}

Write-Host ""

# 測試 10：查詢前台 News（確認不可見）
Write-Host "📋 測試 10：查詢前台 News（確認已下架不可見）" -ForegroundColor Cyan
try {
    $frontendResponse2 = Invoke-RestMethod -Uri "$baseUrl/news?limit=10" -Method Get

    $found = $false
    foreach ($item in $frontendResponse2.data) {
        if ($item.id -eq $newsId) {
            $found = $true
            break
        }
    }

    if (-not $found) {
        Write-Host "✅ 確認成功：已下架的 News 未出現在前台列表" -ForegroundColor Green
    } else {
        Write-Host "⚠️  警告：已下架的 News 仍出現在前台列表" -ForegroundColor Yellow
    }
} catch {
    Write-Host "❌ 查詢失敗: $_" -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Yellow
Write-Host "🎠 Banner API 測試" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow
Write-Host ""

# 測試 11：新增 Banner（未上架）
Write-Host "📋 測試 11：新增 Banner（未上架）" -ForegroundColor Cyan
try {
    $createBannerBody = @{
        storeId = $storeId
        title = "新年活動宣傳"
        imageUrl = "https://example.com/banner1.jpg"
        linkUrl = "https://example.com/promo"
        status = "UNPUBLISHED"
        orderNum = 1
    } | ConvertTo-Json

    $headers = @{
        Authorization = "Bearer $token"
    }

    $bannerResponse = Invoke-RestMethod -Uri "$baseUrl/admin/banner" -Method Post `
        -Headers $headers -ContentType "application/json" -Body $createBannerBody

    $bannerId = $bannerResponse.data.id
    Write-Host "✅ 新增成功" -ForegroundColor Green
    Write-Host "   bannerId: $bannerId" -ForegroundColor Gray
    Write-Host "   title: $($bannerResponse.data.title)" -ForegroundColor Gray
    Write-Host "   status: $($bannerResponse.data.status)" -ForegroundColor Gray
    Write-Host "   statusName: $($bannerResponse.data.statusName)" -ForegroundColor Gray
    Write-Host "   storeName: $($bannerResponse.data.storeName)" -ForegroundColor Gray
    
    # 檢查 null
    if (-not $bannerResponse.data.id) { Write-Host "   ⚠️  id 為 null" -ForegroundColor Red }
    if (-not $bannerResponse.data.storeName) { Write-Host "   ⚠️  storeName 為 null" -ForegroundColor Red }
} catch {
    Write-Host "❌ 新增失敗: $_" -ForegroundColor Red
}

Write-Host ""

# 測試 12：更新 Banner 排序
Write-Host "📋 測試 12：更新 Banner 排序" -ForegroundColor Cyan
try {
    $headers = @{
        Authorization = "Bearer $token"
    }

    $orderResponse = Invoke-RestMethod -Uri "$baseUrl/admin/banner/$bannerId/order?orderNum=5" -Method Put `
        -Headers $headers

    Write-Host "✅ 更新成功" -ForegroundColor Green
    Write-Host "   新排序: $($orderResponse.data.orderNum)" -ForegroundColor Gray
} catch {
    Write-Host "❌ 更新失敗: $_" -ForegroundColor Red
}

Write-Host ""

# 測試 13：上架 Banner
Write-Host "📋 測試 13：上架 Banner" -ForegroundColor Cyan
try {
    $headers = @{
        Authorization = "Bearer $token"
    }

    $publishBannerResponse = Invoke-RestMethod -Uri "$baseUrl/admin/banner/$bannerId/publish" -Method Post `
        -Headers $headers

    Write-Host "✅ 上架成功" -ForegroundColor Green
    Write-Host "   status: $($publishBannerResponse.data.status)" -ForegroundColor Gray
} catch {
    Write-Host "❌ 上架失敗: $_" -ForegroundColor Red
}

Write-Host ""

# 測試 14：查詢前台輪播 Banner
Write-Host "📋 測試 14：查詢前台輪播 Banner（無需登入）" -ForegroundColor Cyan
try {
    $carouselResponse = Invoke-RestMethod -Uri "$baseUrl/banner/carousel" -Method Get

    Write-Host "✅ 查詢成功，共 $($carouselResponse.data.Count) 筆" -ForegroundColor Green
    if ($carouselResponse.data.Count -gt 0) {
        Write-Host "   第一筆: $($carouselResponse.data[0].title)" -ForegroundColor Gray
        Write-Host "   storeName: $($carouselResponse.data[0].storeName)" -ForegroundColor Gray
    }
} catch {
    Write-Host "❌ 查詢失敗: $_" -ForegroundColor Red
}

Write-Host ""

# 測試 15：下架 Banner
Write-Host "📋 測試 15：下架 Banner" -ForegroundColor Cyan
try {
    $headers = @{
        Authorization = "Bearer $token"
    }

    Invoke-RestMethod -Uri "$baseUrl/admin/banner/$bannerId/unpublish" -Method Post `
        -Headers $headers | Out-Null

    Write-Host "✅ 下架成功" -ForegroundColor Green
} catch {
    Write-Host "❌ 下架失敗: $_" -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Yellow
Write-Host "🔒 權限測試" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow
Write-Host ""

# 測試 16：未登入存取後台 API
Write-Host "📋 測試 16：未登入存取後台 API（預期 403）" -ForegroundColor Cyan
try {
    Invoke-RestMethod -Uri "$baseUrl/admin/news/list" -Method Post `
        -ContentType "application/json" -Body "{}" | Out-Null
    Write-Host "⚠️  警告：應該返回 403 但成功了" -ForegroundColor Yellow
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 403 -or $statusCode -eq 401) {
        Write-Host "✅ 正確：返回 $statusCode（權限不足）" -ForegroundColor Green
    } else {
        Write-Host "⚠️  返回非預期的狀態碼: $statusCode" -ForegroundColor Yellow
    }
}

Write-Host ""

# 測試 17：前台 API 無需登入
Write-Host "📋 測試 17：前台 API 無需登入（預期 200）" -ForegroundColor Cyan
try {
    $publicResponse = Invoke-RestMethod -Uri "$baseUrl/news?limit=5" -Method Get
    Write-Host "✅ 正確：前台 API 無需登入即可存取" -ForegroundColor Green
    Write-Host "   查詢到 $($publicResponse.data.Count) 筆 News" -ForegroundColor Gray
} catch {
    Write-Host "❌ 錯誤：前台 API 應該可以公開存取" -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "✅ 測試完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "📊 測試結果摘要：" -ForegroundColor Cyan
Write-Host "   - newsId: $newsId" -ForegroundColor Gray
Write-Host "   - bannerId: $bannerId" -ForegroundColor Gray
Write-Host "   - storeId: $storeId" -ForegroundColor Gray
Write-Host ""
Write-Host "💡 提示：請檢查上方日誌，確認：" -ForegroundColor Yellow
Write-Host "   1. 所有 Response 欄位都有值（無 null）" -ForegroundColor White
Write-Host "   2. 權限控管正確（403/200）" -ForegroundColor White
Write-Host "   3. 狀態切換正常（DRAFT → PUBLISHED → ARCHIVED）" -ForegroundColor White
