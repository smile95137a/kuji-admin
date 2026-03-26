# 📋 完整測試計劃 - News & Banner 模組

## 🎯 測試目標

驗證所有 API 符合以下原則：
1. **前端友好**：操作符合使用者期待，不需要額外資訊就能完成流程
2. **符合需求文件**：banner.prompt.md 和 news.prompt.md 的所有需求
3. **CRUD 完整性**：新增、查詢、更新、刪除、狀態切換都正常
4. **資料一致性**：關聯資料（店家）必須正確顯示

---

## 🔧 前置準備

### 1. 啟動應用程式
```bash
mvn clean package -DskipTests
mvn spring-boot:run
```

### 2. 取得 Admin Token
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin@kuji.com",
    "password": "admin123"
  }'
```

**設定為環境變數：**
```bash
# Windows CMD
set TOKEN=Bearer eyJhbGc...

# PowerShell
$TOKEN="Bearer eyJhbGc..."

# Linux/Mac
export TOKEN="Bearer eyJhbGc..."
```

---

## 📝 測試案例

### 測試 1：Enum API（無需登入）

#### ✅ 1.1 取得所有 Enum
```bash
curl http://localhost:8080/api/enums/all
```

**驗證點：**
- [ ] 回應包含 11 種 Enum
- [ ] 格式為 `{lotteryStatus: [...], newsStatus: [...], bannerStatus: [...]}`
- [ ] 每個選項有 `label`（中文）和 `value`（英文）
- [ ] newsStatus 和 bannerStatus 有 `description` 欄位

#### ✅ 1.2 取得 Banner 狀態選項
```bash
curl http://localhost:8080/api/enums/banner-status
```

**預期回應：**
```json
{
  "success": true,
  "data": [
    {
      "label": "已上架",
      "value": "PUBLISHED",
      "description": "前台輪播顯示"
    },
    {
      "label": "未上架",
      "value": "UNPUBLISHED",
      "description": "前台不顯示"
    }
  ]
}
```

---

### 測試 2：店家選項 API（無需登入）

#### ✅ 2.1 取得所有店家
```bash
curl http://localhost:8080/api/stores/options
```

**驗證點：**
- [ ] 只返回 ACTIVE 店家
- [ ] 格式為 `[{label, value, description}, ...]`
- [ ] label 為店家名稱，value 為店家 ID

#### ✅ 2.2 取得所有店家（包含停用）
```bash
curl "http://localhost:8080/api/stores/options?activeOnly=false"
```

**驗證點：**
- [ ] 返回所有店家（包含非 ACTIVE）

#### ✅ 2.3 搜尋店家
```bash
curl "http://localhost:8080/api/stores/search?keyword=玩具"
```

**驗證點：**
- [ ] 返回店家名稱或描述包含「玩具」的結果

---

### 測試 3：圖片上傳 API（需 Admin 權限）

#### ✅ 3.1 上傳 News 圖片
```bash
# Windows CMD
curl -X POST http://localhost:8080/api/admin/upload/news ^
  -H "Authorization: %TOKEN%" ^
  -F "file=@test-image.jpg"

# PowerShell
curl -X POST http://localhost:8080/api/admin/upload/news `
  -H "Authorization: $TOKEN" `
  -F "file=@test-image.jpg"

# Linux/Mac
curl -X POST http://localhost:8080/api/admin/upload/news \
  -H "Authorization: $TOKEN" \
  -F "file=@test-image.jpg"
```

**預期回應：**
```json
{
  "success": true,
  "data": {
    "imageUrl": "/img/news/uuid-123.jpg"
  }
}
```

**驗證點：**
- [ ] imageUrl 格式為 `/img/news/{uuid}.jpg`
- [ ] 檔案儲存在 `src/main/resources/static/img/news/`
- [ ] 可以透過瀏覽器存取：`http://localhost:8080/img/news/{uuid}.jpg`

#### ✅ 3.2 上傳 Banner 圖片
```bash
curl -X POST http://localhost:8080/api/admin/upload/banner \
  -H "Authorization: $TOKEN" \
  -F "file=@banner-image.jpg"
```

**驗證點：**
- [ ] imageUrl 格式為 `/img/banner/{uuid}.jpg`

#### ✅ 3.3 檔案大小限制測試
```bash
# 上傳超過 5MB 的檔案
curl -X POST http://localhost:8080/api/admin/upload/news \
  -H "Authorization: $TOKEN" \
  -F "file=@large-file.jpg"
```

**預期回應：**
```json
{
  "success": false,
  "error": {
    "message": "檔案大小不能超過 5MB"
  }
}
```

#### ✅ 3.4 檔案類型限制測試
```bash
# 上傳非圖片檔案
curl -X POST http://localhost:8080/api/admin/upload/news \
  -H "Authorization: $TOKEN" \
  -F "file=@document.pdf"
```

**預期回應：**
```json
{
  "success": false,
  "error": {
    "message": "只支援圖片檔案"
  }
}
```

---

### 測試 4：News 完整 CRUD 流程

#### ✅ 4.1 新增 News（草稿）
```bash
curl -X POST http://localhost:8080/api/admin/news \
  -H "Authorization: $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "春節活動開跑！",
    "content": "春節期間推出限定活動，參加抽獎就有機會獲得豐富獎品！詳細規則請見活動頁面。",
    "imageUrl": "/img/news/test-uuid-123.jpg",
    "status": "DRAFT"
  }'
```

**驗證點：**
- [ ] 建立成功
- [ ] 回應包含 newsId
- [ ] status 為 DRAFT
- [ ] createdAt 和 updatedAt 自動填入

#### ✅ 4.2 查詢 News 列表（後台）
```bash
curl -X POST http://localhost:8080/api/admin/news/list \
  -H "Authorization: $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{}'
```

**驗證點：**
- [ ] 返回所有 News（包含草稿）
- [ ] 包含剛才建立的 News

#### ✅ 4.3 查詢 News 詳情
```bash
curl -X GET http://localhost:8080/api/admin/news/{newsId} \
  -H "Authorization: $TOKEN"
```

**驗證點：**
- [ ] 返回完整 News 資訊
- [ ] 所有欄位都有值（無 null）

#### ✅ 4.4 更新 News
```bash
curl -X PUT http://localhost:8080/api/admin/news/{newsId} \
  -H "Authorization: $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "春節活動開跑！（更新）",
    "content": "更新後的內容..."
  }'
```

**驗證點：**
- [ ] 更新成功
- [ ] updatedAt 時間更新

#### ✅ 4.5 上架 News
```bash
curl -X POST http://localhost:8080/api/admin/news/{newsId}/publish \
  -H "Authorization: $TOKEN"
```

**驗證點：**
- [ ] status 變更為 PUBLISHED
- [ ] updatedAt 時間更新

#### ✅ 4.6 前台查詢 News
```bash
curl http://localhost:8080/api/news
```

**驗證點：**
- [ ] 只返回 PUBLISHED 的 News
- [ ] 包含剛才上架的 News
- [ ] 按 createdAt 降序排列

#### ✅ 4.7 下架 News
```bash
curl -X POST http://localhost:8080/api/admin/news/{newsId}/archive \
  -H "Authorization: $TOKEN"
```

**驗證點：**
- [ ] status 變更為 ARCHIVED
- [ ] 前台不再顯示此 News

#### ✅ 4.8 刪除 News
```bash
curl -X DELETE http://localhost:8080/api/admin/news/{newsId} \
  -H "Authorization: $TOKEN"
```

**驗證點：**
- [ ] 刪除成功
- [ ] 查詢列表時不再出現

---

### 測試 5：Banner 完整 CRUD 流程（關鍵！）

#### ✅ 5.1 取得店家選項（給前端下拉選單）
```bash
curl http://localhost:8080/api/stores/options
```

**記下店家 ID（例如：uuid-store-123）**

#### ✅ 5.2 上傳 Banner 圖片
```bash
curl -X POST http://localhost:8080/api/admin/upload/banner \
  -H "Authorization: $TOKEN" \
  -F "file=@banner.jpg"
```

**記下 imageUrl（例如：/img/banner/uuid-456.jpg）**

#### ✅ 5.3 新增 Banner（未上架）
```bash
curl -X POST http://localhost:8080/api/admin/banner \
  -H "Authorization: $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "storeId": "uuid-store-123",
    "title": "春節限時優惠",
    "imageUrl": "/img/banner/uuid-456.jpg",
    "orderNum": 1,
    "status": "UNPUBLISHED",
    "startTime": "2026-01-10T00:00:00",
    "endTime": "2026-02-28T23:59:59"
  }'
```

**驗證點：**
- [ ] 建立成功
- [ ] 回應包含 bannerId
- [ ] storeName 有正確顯示（不是 null）
- [ ] status 為 UNPUBLISHED

#### ✅ 5.4 查詢 Banner 列表（後台）
```bash
curl -X POST http://localhost:8080/api/admin/banner/list \
  -H "Authorization: $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{}'
```

**驗證點：**
- [ ] 返回所有 Banner
- [ ] 包含剛才建立的 Banner
- [ ] storeName 有正確顯示

#### ✅ 5.5 查詢 Banner 詳情
```bash
curl -X GET http://localhost:8080/api/admin/banner/{bannerId} \
  -H "Authorization: $TOKEN"
```

**驗證點：**
- [ ] 返回完整 Banner 資訊
- [ ] storeName 有值
- [ ] 所有時間欄位格式正確

#### ✅ 5.6 更新 Banner
```bash
curl -X PUT http://localhost:8080/api/admin/banner/{bannerId} \
  -H "Authorization: $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "春節限時優惠（更新）",
    "orderNum": 2
  }'
```

**驗證點：**
- [ ] 更新成功
- [ ] title 和 orderNum 已變更
- [ ] 其他欄位保持不變

#### ✅ 5.7 上架 Banner
```bash
curl -X POST http://localhost:8080/api/admin/banner/{bannerId}/publish \
  -H "Authorization: $TOKEN"
```

**驗證點：**
- [ ] status 變更為 PUBLISHED
- [ ] updatedAt 時間更新

#### ✅ 5.8 前台查詢輪播 Banner
```bash
curl http://localhost:8080/api/banner/carousel
```

**驗證點：**
- [ ] 只返回 PUBLISHED 且店家 ACTIVE 的 Banner
- [ ] 包含剛才上架的 Banner
- [ ] storeName 有正確顯示
- [ ] 按 orderNum 升序排列

#### ✅ 5.9 下架 Banner
```bash
curl -X POST http://localhost:8080/api/admin/banner/{bannerId}/unpublish \
  -H "Authorization: $TOKEN"
```

**驗證點：**
- [ ] status 變更為 UNPUBLISHED
- [ ] 前台不再顯示此 Banner

#### ✅ 5.10 刪除 Banner
```bash
curl -X DELETE http://localhost:8080/api/admin/banner/{bannerId} \
  -H "Authorization: $TOKEN"
```

**驗證點：**
- [ ] 刪除成功
- [ ] 查詢列表時不再出現

---

### 測試 6：Banner 時間排程（自動上下架）

#### ✅ 6.1 建立有時間限制的 Banner
```bash
curl -X POST http://localhost:8080/api/admin/banner \
  -H "Authorization: $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "storeId": "uuid-store-123",
    "title": "限時 Banner",
    "imageUrl": "/img/banner/test.jpg",
    "orderNum": 1,
    "status": "PUBLISHED",
    "startTime": "2026-01-01T00:00:00",
    "endTime": "2026-01-05T23:59:59"
  }'
```

**驗證點：**
- [ ] 建立成功
- [ ] 如果當前時間在範圍內，前台可看到
- [ ] 如果當前時間不在範圍內，前台看不到

---

### 測試 7：Banner 與店家關聯（關鍵！）

#### ✅ 7.1 查詢指定店家的 Banner
```bash
curl -X POST http://localhost:8080/api/admin/banner/list \
  -H "Authorization: $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "condition": {
      "storeId": "uuid-store-123"
    }
  }'
```

**驗證點：**
- [ ] 只返回該店家的 Banner

#### ✅ 7.2 店家停用時 Banner 不顯示
**步驟：**
1. 建立並上架一個 Banner
2. 將該 Banner 的店家狀態改為 INACTIVE
3. 查詢前台輪播 Banner

**驗證點：**
- [ ] 前台不顯示該店家的 Banner

---

### 測試 8：權限控管

#### ✅ 8.1 無 Token 時無法操作後台 API
```bash
curl -X POST http://localhost:8080/api/admin/news/list \
  -H "Content-Type: application/json" \
  -d '{}'
```

**預期回應：**
- [ ] 401 Unauthorized 或 403 Forbidden

#### ✅ 8.2 StoreOwner 無法操作 News 和 Banner
**使用 StoreOwner Token 測試：**
```bash
curl -X POST http://localhost:8080/api/admin/news/list \
  -H "Authorization: Bearer {storeowner_token}" \
  -H "Content-Type: application/json" \
  -d '{}'
```

**預期回應：**
- [ ] 403 Forbidden

---

## 🎯 前端整合驗證

### 操作流程 1：新增 Banner（前端視角）

1. **進入 Banner 新增頁面**
2. **選擇店家**：
   - 呼叫 `GET /api/stores/options`
   - 渲染下拉選單（顯示店家名稱，值為店家 ID）
3. **上傳圖片**：
   - 呼叫 `POST /api/admin/upload/banner`
   - 取得 imageUrl
4. **填寫資訊**：
   - 標題、排序、開始/結束時間
5. **送出新增**：
   - 呼叫 `POST /api/admin/banner`
   - 傳入 storeId 和 imageUrl
6. **成功後跳轉到列表**

**驗證點：**
- [ ] 所有步驟都不需要手動輸入 ID
- [ ] 店家選項自動載入
- [ ] 圖片上傳後自動帶入 imageUrl

### 操作流程 2：編輯 News（前端視角）

1. **進入 News 編輯頁面**
2. **載入現有資料**：
   - 呼叫 `GET /api/admin/news/{newsId}`
3. **選擇狀態**：
   - 呼叫 `GET /api/enums/news-status`
   - 渲染狀態下拉選單
4. **更新資訊**
5. **送出更新**：
   - 呼叫 `PUT /api/admin/news/{newsId}`

**驗證點：**
- [ ] 狀態選項從 API 取得（不是硬編碼）
- [ ] 更新成功後資料即時更新

---

## 📊 測試結果統計

| 測試類別 | 總數 | 通過 | 失敗 | 備註 |
|---------|------|------|------|------|
| Enum API | 2 | 0 | 0 | |
| 店家選項 API | 3 | 0 | 0 | |
| 圖片上傳 API | 4 | 0 | 0 | |
| News CRUD | 8 | 0 | 0 | |
| Banner CRUD | 10 | 0 | 0 | |
| 時間排程 | 1 | 0 | 0 | |
| 店家關聯 | 2 | 0 | 0 | |
| 權限控管 | 2 | 0 | 0 | |
| **總計** | **32** | **0** | **0** | |

---

## 🐛 已知問題清單

| 編號 | 問題描述 | 嚴重度 | 狀態 | 解決方案 |
|------|---------|--------|------|---------|
| 1 | | | | |

---

## ✅ 核心驗證清單

### 符合 banner.prompt.md 需求：
- [ ] Banner 必須綁定店家（不可為空）
- [ ] Banner 點擊導向店家頁面（前端處理）
- [ ] 只有 Admin 可管理 Banner
- [ ] 支援手動排序（orderNum）
- [ ] 支援上下架狀態切換
- [ ] 支援時間排程（startTime/endTime）
- [ ] 店家停用時 Banner 不顯示

### 符合 news.prompt.md 需求：
- [ ] News 支援草稿/上架/下架狀態
- [ ] 只有 Admin 可管理 News
- [ ] 支援長文內容
- [ ] 支援圖片上傳
- [ ] 前台按建立時間降序顯示
- [ ] 支援時間排程（startTime/endTime）

### 前端友好性：
- [ ] 不需要手動輸入 ID
- [ ] Enum 從 API 取得（不硬編碼）
- [ ] 店家選項自動載入
- [ ] 圖片上傳後自動帶入 URL
- [ ] 錯誤訊息清楚易懂

---

**準備開始測試！** 🚀
