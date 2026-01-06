# News & Banner API 完整測試指南

## 📋 測試前準備

### 1. 啟動應用
```bash
mvn spring-boot:run
```

### 2. 取得 Admin Token
```bash
POST http://localhost:8080/api/test/admin-login
Content-Type: application/json

{
  "username": "admin@kuji.com",
  "password": "admin123"
}
```

**保存回應中的 `accessToken`，所有後台 API 都需要這個 Token！**

### 3. 取得 Store ID（用於建立 Banner）
```bash
GET http://localhost:8080/api/test/stores
```

**保存第一個店家的 `id`，建立 Banner 時需要！**

---

## 🧪 News API 測試流程

### 測試 1：新增最新消息（草稿）
```bash
POST http://localhost:8080/api/admin/news
Authorization: Bearer {your_access_token}
Content-Type: application/json

{
  "title": "系統維護公告",
  "content": "系統將於本週六凌晨進行例行維護，維護期間無法使用服務。",
  "imageUrl": "https://via.placeholder.com/800x400",
  "status": "DRAFT"
}
```

**預期結果：**
- ✅ HTTP 200
- ✅ `data.id` 不為 null
- ✅ `data.status` = "DRAFT"
- ✅ `data.statusName` = "草稿"
- ✅ `data.createdBy` = 當前使用者 ID
- ✅ `data.createdAt` 不為 null

**保存 `data.id` 作為 newsId！**

---

### 測試 2：查詢最新消息詳情
```bash
GET http://localhost:8080/api/admin/news/{newsId}
Authorization: Bearer {your_access_token}
```

**預期結果：**
- ✅ HTTP 200
- ✅ 所有欄位都不為 null（除了可選欄位 imageUrl, endTime）
- ✅ `data.title` = "系統維護公告"
- ✅ `data.content` 完整顯示

---

### 測試 3：更新最新消息
```bash
PUT http://localhost:8080/api/admin/news/{newsId}
Authorization: Bearer {your_access_token}
Content-Type: application/json

{
  "title": "系統維護公告（更新）",
  "content": "維護時間調整為週日凌晨。"
}
```

**預期結果：**
- ✅ HTTP 200
- ✅ `data.title` = "系統維護公告（更新）"
- ✅ `data.updatedAt` 時間更新

---

### 測試 4：查詢最新消息列表（後台）
```bash
POST http://localhost:8080/api/admin/news/list
Authorization: Bearer {your_access_token}
Content-Type: application/json

{
  "condition": {
    "status": "DRAFT"
  },
  "sortBy": "created_at",
  "sortOrder": "DESC"
}
```

**預期結果：**
- ✅ HTTP 200
- ✅ `data` 是陣列
- ✅ 至少包含剛才建立的消息
- ✅ 所有消息的 `status` = "DRAFT"

---

### 測試 5：上架最新消息
```bash
POST http://localhost:8080/api/admin/news/{newsId}/publish
Authorization: Bearer {your_access_token}
```

**預期結果：**
- ✅ HTTP 200
- ✅ `data.status` = "PUBLISHED"
- ✅ `data.statusName` = "已上架"
- ✅ `data.scheduledAt` = 當前時間

---

### 測試 6：前台查詢最新消息列表
```bash
GET http://localhost:8080/api/news?limit=5
```

**預期結果：**
- ✅ HTTP 200
- ✅ `data` 是陣列
- ✅ 包含剛才上架的消息
- ✅ 所有消息的 `status` = "PUBLISHED"
- ✅ 按 `scheduledAt` 降序排列（最新的在前面）
- ✅ **無需 Token！**

---

### 測試 7：前台查詢最新消息詳情
```bash
GET http://localhost:8080/api/news/{newsId}
```

**預期結果：**
- ✅ HTTP 200
- ✅ 顯示完整內容
- ✅ **無需 Token！**

---

### 測試 8：下架最新消息
```bash
POST http://localhost:8080/api/admin/news/{newsId}/unpublish
Authorization: Bearer {your_access_token}
```

**預期結果：**
- ✅ HTTP 200
- ✅ `data.status` = "ARCHIVED"
- ✅ `data.statusName` = "已下架"
- ✅ `data.endTime` = 當前時間

---

### 測試 9：驗證前台不顯示已下架消息
```bash
GET http://localhost:8080/api/news
```

**預期結果：**
- ✅ HTTP 200
- ✅ `data` 陣列中不包含已下架的消息

---

### 測試 10：刪除最新消息
```bash
DELETE http://localhost:8080/api/admin/news/{newsId}
Authorization: Bearer {your_access_token}
```

**預期結果：**
- ✅ HTTP 200
- ✅ `data` = null（刪除成功）

---

## 🎠 Banner API 測試流程

### 測試 1：新增 Banner（未上架）
```bash
POST http://localhost:8080/api/admin/banner
Authorization: Bearer {your_access_token}
Content-Type: application/json

{
  "storeId": "{your_store_id}",
  "title": "春節限時優惠",
  "imageUrl": "https://via.placeholder.com/1200x400",
  "orderNum": 1,
  "status": "UNPUBLISHED"
}
```

**預期結果：**
- ✅ HTTP 200
- ✅ `data.id` 不為 null
- ✅ `data.storeId` = 輸入的 storeId
- ✅ `data.storeName` 不為 null（自動查詢店家名稱）
- ✅ `data.status` = "UNPUBLISHED"
- ✅ `data.statusName` = "未上架"
- ✅ `data.orderNum` = 1

**保存 `data.id` 作為 bannerId！**

---

### 測試 2：查詢 Banner 詳情
```bash
GET http://localhost:8080/api/admin/banner/{bannerId}
Authorization: Bearer {your_access_token}
```

**預期結果：**
- ✅ HTTP 200
- ✅ 所有欄位都不為 null（除了可選欄位 endTime）
- ✅ `data.storeName` 正確顯示店家名稱

---

### 測試 3：更新 Banner
```bash
PUT http://localhost:8080/api/admin/banner/{bannerId}
Authorization: Bearer {your_access_token}
Content-Type: application/json

{
  "title": "春節限時優惠（延長）",
  "orderNum": 2
}
```

**預期結果：**
- ✅ HTTP 200
- ✅ `data.title` = "春節限時優惠（延長）"
- ✅ `data.orderNum` = 2

---

### 測試 4：查詢 Banner 列表（後台）
```bash
POST http://localhost:8080/api/admin/banner/list
Authorization: Bearer {your_access_token}
Content-Type: application/json

{
  "condition": {
    "status": "UNPUBLISHED"
  },
  "sortBy": "order_num",
  "sortOrder": "ASC"
}
```

**預期結果：**
- ✅ HTTP 200
- ✅ `data` 是陣列
- ✅ 包含剛才建立的 Banner
- ✅ 按 `orderNum` 升序排列

---

### 測試 5：更新 Banner 排序
```bash
PUT http://localhost:8080/api/admin/banner/{bannerId}/order?orderNum=5
Authorization: Bearer {your_access_token}
```

**預期結果：**
- ✅ HTTP 200
- ✅ `data.orderNum` = 5

---

### 測試 6：上架 Banner
```bash
POST http://localhost:8080/api/admin/banner/{bannerId}/publish
Authorization: Bearer {your_access_token}
```

**預期結果：**
- ✅ HTTP 200
- ✅ `data.status` = "PUBLISHED"
- ✅ `data.statusName` = "已上架"
- ✅ `data.startTime` = 當前時間

---

### 測試 7：前台查詢輪播 Banner
```bash
GET http://localhost:8080/api/banner/carousel
```

**預期結果：**
- ✅ HTTP 200
- ✅ `data` 是陣列
- ✅ 包含剛才上架的 Banner
- ✅ 按 `orderNum` 升序排列
- ✅ 所有 Banner 的 `status` = "PUBLISHED"
- ✅ 所有店家狀態為 "ACTIVE"（已自動過濾停用店家）
- ✅ **無需 Token！**

---

### 測試 8：下架 Banner
```bash
POST http://localhost:8080/api/admin/banner/{bannerId}/unpublish
Authorization: Bearer {your_access_token}
```

**預期結果：**
- ✅ HTTP 200
- ✅ `data.status` = "UNPUBLISHED"
- ✅ `data.statusName` = "未上架"
- ✅ `data.endTime` = 當前時間

---

### 測試 9：驗證前台不顯示已下架 Banner
```bash
GET http://localhost:8080/api/banner/carousel
```

**預期結果：**
- ✅ HTTP 200
- ✅ `data` 陣列中不包含已下架的 Banner

---

### 測試 10：刪除 Banner
```bash
DELETE http://localhost:8080/api/admin/banner/{bannerId}
Authorization: Bearer {your_access_token}
```

**預期結果：**
- ✅ HTTP 200
- ✅ `data` = null（刪除成功）

---

## 🔒 權限測試

### 測試 1：未登入存取後台 API
```bash
POST http://localhost:8080/api/admin/news/list
# 不加 Authorization Header
```

**預期結果：**
- ✅ HTTP 403 Forbidden
- ✅ `success` = false
- ✅ `message` = "無權限執行此操作"

---

### 測試 2：StoreOwner 嘗試存取 News API
```bash
# 先用 StoreOwner 帳號登入
POST http://localhost:8080/api/test/admin-login
Content-Type: application/json

{
  "username": "owner@teststore.com",
  "password": "Test1234"
}

# 使用取得的 Token 嘗試存取
POST http://localhost:8080/api/admin/news/list
Authorization: Bearer {store_owner_token}
```

**預期結果：**
- ✅ HTTP 403 Forbidden
- ✅ 因為 News 和 Banner 只有 ROLE_ADMIN 可存取

---

### 測試 3：前台 API 無需 Token
```bash
GET http://localhost:8080/api/news
GET http://localhost:8080/api/banner/carousel
```

**預期結果：**
- ✅ HTTP 200
- ✅ 正常返回資料
- ✅ **不需要 Authorization Header！**

---

## ⚠️ 常見問題檢查

### 問題 1：Response 中有 null 欄位
**檢查項目：**
- ✅ Service 的 `convertToRes()` 方法是否映射所有欄位？
- ✅ NewsRes / BannerRes 是否有 `@Builder` 註解？
- ✅ 資料庫欄位是否正確設定？

### 問題 2：前台查詢不到資料
**檢查項目：**
- ✅ 是否已經上架（status = PUBLISHED）？
- ✅ `scheduledAt` 是否已經到達？
- ✅ `endTime` 是否已過期？
- ✅ Banner：綁定的店家狀態是否為 ACTIVE？

### 問題 3：後台 API 返回 403
**檢查項目：**
- ✅ Token 是否有效？
- ✅ 是否用 Admin 帳號登入？
- ✅ Controller 是否有 `@PreAuthorize("hasRole('ADMIN')")`？

### 問題 4：Banner 建立失敗
**檢查項目：**
- ✅ `storeId` 是否存在？
- ✅ 店家狀態是否為 ACTIVE？
- ✅ 所有必填欄位是否都有提供？

---

## 📊 完整測試檢查表

### News API（後台 - 需 Admin 權限）
- [ ] POST `/admin/news/list` - 查詢列表
- [ ] GET `/admin/news/{id}` - 查詢詳情
- [ ] POST `/admin/news` - 新增（草稿）
- [ ] PUT `/admin/news/{id}` - 更新
- [ ] POST `/admin/news/{id}/publish` - 上架
- [ ] POST `/admin/news/{id}/unpublish` - 下架
- [ ] DELETE `/admin/news/{id}` - 刪除

### News API（前台 - 無需權限）
- [ ] GET `/news?limit=5` - 查詢列表
- [ ] GET `/news/{id}` - 查詢詳情

### Banner API（後台 - 需 Admin 權限）
- [ ] POST `/admin/banner/list` - 查詢列表
- [ ] GET `/admin/banner/{id}` - 查詢詳情
- [ ] POST `/admin/banner` - 新增（未上架）
- [ ] PUT `/admin/banner/{id}` - 更新
- [ ] PUT `/admin/banner/{id}/order?orderNum=N` - 更新排序
- [ ] POST `/admin/banner/{id}/publish` - 上架
- [ ] POST `/admin/banner/{id}/unpublish` - 下架
- [ ] DELETE `/admin/banner/{id}` - 刪除

### Banner API（前台 - 無需權限）
- [ ] GET `/banner/carousel` - 查詢輪播

### 權限測試
- [ ] 未登入存取後台 API → 403
- [ ] StoreOwner 存取 News/Banner → 403
- [ ] 前台 API 無需 Token → 200

---

## 🎉 測試完成標準

所有測試通過後，應該確認：

1. ✅ **沒有 null 欄位問題**
   - 所有 Response 的必填欄位都有值
   - `storeName` 正確顯示（Banner）
   - `statusName` 正確顯示中文

2. ✅ **權限控管正確**
   - Admin 可存取所有後台 API
   - StoreOwner/Editor 無法存取 News/Banner
   - 前台 API 無需登入

3. ✅ **狀態切換正常**
   - 上架後前台可見
   - 下架後前台不可見
   - 狀態名稱正確顯示

4. ✅ **資料完整性**
   - 建立時自動帶入當前使用者
   - 建立時自動產生 UUID
   - 時間戳記自動設定

5. ✅ **前台過濾正確**
   - News：只顯示 PUBLISHED
   - Banner：只顯示 PUBLISHED + 店家 ACTIVE
   - 排序正確（News 降序，Banner 升序）

---

## 📝 測試報告範例

```
測試日期：2026-01-06
測試人員：[您的名字]
測試環境：本機開發環境

News API 測試結果：
- 後台查詢列表：✅ 通過
- 後台查詢詳情：✅ 通過
- 新增消息：✅ 通過
- 更新消息：✅ 通過
- 上架/下架：✅ 通過
- 刪除消息：✅ 通過
- 前台查詢：✅ 通過

Banner API 測試結果：
- 後台查詢列表：✅ 通過
- 後台查詢詳情：✅ 通過
- 新增 Banner：✅ 通過
- 更新 Banner：✅ 通過
- 更新排序：✅ 通過
- 上架/下架：✅ 通過
- 刪除 Banner：✅ 通過
- 前台輪播：✅ 通過

權限測試：
- 未登入存取後台：✅ 403
- StoreOwner 存取：✅ 403
- 前台無需登入：✅ 200

結論：所有測試通過 ✅
```

---

## 🚀 使用 Postman Collection

1. 匯入 `KUJI_Admin_API_Tests.postman_collection.json`
2. 執行「管理員登入 - Admin」取得 Token
3. 執行「資料查詢」→「查詢所有店家」取得 StoreID
4. 按照順序執行 News 和 Banner 測試
5. 所有變數會自動保存（newsId, bannerId, storeId）

**測試順序建議：**
1. 登入取得 Token
2. 查詢店家取得 StoreID
3. 執行 News 完整流程（新增→查詢→更新→上架→前台確認→下架→刪除）
4. 執行 Banner 完整流程（新增→查詢→更新→排序→上架→前台確認→下架→刪除）
5. 執行權限測試

---

**所有 API 都應該正常運作，沒有 null 問題，權限控管正確！** 🎉
