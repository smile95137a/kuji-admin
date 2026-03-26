# 新增店家負責人 API 修正報告

## 修正內容

### ✅ 修正檔案

**檔案**：`service/impl/AdminUserServiceImpl.java`  
**方法**：`createStoreOwner(CreateStoreOwnerReq req)`  
**修正時間**：2026-01-09

---

## 修正前後對比

### ❌ 修正前（不完整）

```java
// 建立 Store
Store store = new Store();
store.setId(UUID.randomUUID().toString());
store.setOwnerId(adminUser.getId());
store.setStoreName(req.getStoreName());              // ✅
store.setShortDescription(req.getShortDescription()); // ✅
store.setEmail(req.getEmail());                       // ❌ 使用 AdminUser 的 email
store.setPhone(req.getPhone());                       // ❌ 使用 AdminUser 的 phone
store.setStatus(StoreStatus.ACTIVE.getCode());        // ✅
store.setCreatedAt(LocalDateTime.now());              // ✅
storeMapper.insert(store);
```

**問題**：
1. ❌ 只設定 7 個欄位，缺少 11 個欄位
2. ❌ `email` 和 `phone` 使用錯誤的來源（應該用 storeEmail/storePhone）
3. ❌ 缺少必填欄位：`logoUrl`, `storeAddress`, `businessHours`

---

### ✅ 修正後（完整）

```java
// 建立 Store（完整資料）
Store store = new Store();
store.setId(UUID.randomUUID().toString());
store.setOwnerId(adminUser.getId());
store.setStoreName(req.getStoreName());
store.setShortDescription(req.getShortDescription());
store.setLongDescription(req.getLongDescription());       // ← 新增
store.setLogoUrl(req.getLogoUrl());                       // ← 新增（必填）
store.setCoverImageUrl(req.getCoverImageUrl());           // ← 新增
store.setEmail(req.getStoreEmail());                      // ← 修正（改用 storeEmail）
store.setPhone(req.getStorePhone());                      // ← 修正（改用 storePhone）
store.setAddress(req.getStoreAddress());                  // ← 新增（必填）
store.setBusinessHours(req.getBusinessHours());           // ← 新增（必填）
store.setFacebookUrl(req.getFacebookUrl());               // ← 新增
store.setInstagramUrl(req.getInstagramUrl());             // ← 新增
store.setLineId(req.getLineId());                         // ← 新增
store.setStatus(StoreStatus.ACTIVE.getCode());
store.setCreatedAt(LocalDateTime.now());
store.setUpdatedAt(LocalDateTime.now());                  // ← 新增
storeMapper.insert(store);
```

**修正內容**：
1. ✅ 設定 **18 個欄位**（完整）
2. ✅ `email` 改用 `req.getStoreEmail()`
3. ✅ `phone` 改用 `req.getStorePhone()`
4. ✅ 新增 11 個欄位
5. ✅ 所有必填欄位都已設定

---

## 欄位對應表

| CreateStoreOwnerReq | Store Entity | 說明 | 狀態 |
|---------------------|--------------|------|------|
| `storeName` | `storeName` | 店家名稱（必填） | ✅ |
| `shortDescription` | `shortDescription` | 店家短描述（必填） | ✅ |
| `longDescription` | `longDescription` | 店家詳細介紹 | ✅ |
| `logoUrl` | `logoUrl` | Logo 圖片（必填） | ✅ |
| `coverImageUrl` | `coverImageUrl` | 封面圖片 | ✅ |
| `storeEmail` | `email` | 店家專用 Email（必填） | ✅ |
| `storePhone` | `phone` | 店家專用電話（必填） | ✅ |
| `storeAddress` | `address` | 店家地址（必填） | ✅ |
| `businessHours` | `businessHours` | 營業時間（必填） | ✅ |
| `facebookUrl` | `facebookUrl` | Facebook 連結 | ✅ |
| `instagramUrl` | `instagramUrl` | Instagram 連結 | ✅ |
| `lineId` | `lineId` | LINE ID | ✅ |
| - | `id` | UUID（自動生成） | ✅ |
| - | `ownerId` | AdminUser ID | ✅ |
| - | `status` | ACTIVE | ✅ |
| - | `createdAt` | 建立時間 | ✅ |
| - | `updatedAt` | 更新時間 | ✅ |

---

## 使用者請求資料問題

### ❌ 原始請求（有拼字錯誤）

```json
{
  "enail": "owner_1767756770173@example.com",  // ❌ 錯誤：應該是 "email"
  "displayName": "店家老闡_1767756770173",
  "phone": "0912345678",
  "storeName": "KUJI 測試商店_1767756770173",
  ...
}
```

**錯誤**：`"enail"` 拼字錯誤，應該是 `"email"`

---

### ✅ 正確請求

```json
{
  "email": "owner_1767756770173@example.com",  // ← 修正拼字
  "displayName": "店家老闡_1767756770173",
  "phone": "0912345678",
  "remark": "測試建立店家負責人",
  "storeName": "KUJI 測試商店_1767756770173",
  "shortDescription": "專營一番賞、扭蛋精品",
  "longDescription": "店家詳細介紹",
  "logoUrl": "https://picsum.photos/seed/logo/300/300",
  "coverImageUrl": "https://picsum.photos/seed/cover/1200/600",
  "storeEmail": "shop_1767756770173@example.com",
  "storePhone": "02-1234-5678",
  "storeAddress": "無",
  "businessHours": "每日 10:00~22:00",
  "facebookUrl": null,
  "instagramUrl": null,
  "lineId": null
}
```

---

## 測試步驟

### 1. 編譯專案

```bash
mvn clean package -DskipTests
```

### 2. 啟動服務

```bash
java -jar target/admin-1.0.0.jar
```

### 3. 呼叫 API（使用正確的請求資料）

```bash
curl -X POST http://localhost:8080/api/admin/users/store-owner \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {ADMIN_TOKEN}" \
  -d '{
    "email": "owner_test_001@example.com",
    "displayName": "測試店家老闆",
    "phone": "0912345678",
    "remark": "測試建立店家負責人",
    "storeName": "KUJI 測試商店",
    "shortDescription": "專營一番賞、扭蛋精品",
    "longDescription": "我們是專業的一番賞銷售店家，提供最新最熱門的商品。",
    "logoUrl": "https://picsum.photos/seed/logo/300/300",
    "coverImageUrl": "https://picsum.photos/seed/cover/1200/600",
    "storeEmail": "shop_test_001@example.com",
    "storePhone": "02-1234-5678",
    "storeAddress": "台北市信義區信義路五段7號",
    "businessHours": "週一至週日 10:00~22:00",
    "facebookUrl": "https://facebook.com/kuji-test",
    "instagramUrl": "https://instagram.com/kuji-test",
    "lineId": "@kujitest"
  }'
```

### 4. 驗證資料

```sql
-- 查詢 AdminUser
SELECT id, username, email, display_name, status, force_change_password
FROM admin_user
WHERE email = 'owner_test_001@example.com';

-- 查詢 Store（完整欄位）
SELECT 
    id,
    owner_id,
    store_name,
    short_description,
    long_description,
    logo_url,
    cover_image_url,
    email,
    phone,
    address,
    business_hours,
    facebook_url,
    instagram_url,
    line_id,
    status,
    created_at
FROM store
WHERE owner_id = (
    SELECT id FROM admin_user WHERE email = 'owner_test_001@example.com'
);

-- 查詢 StoreUser 關聯
SELECT su.*, au.email, s.store_name
FROM store_user su
JOIN admin_user au ON su.admin_user_id = au.id
JOIN store s ON su.store_id = s.id
WHERE au.email = 'owner_test_001@example.com';

-- 查詢角色綁定
SELECT r.name, r.code
FROM role r
JOIN admin_user_role aur ON r.id = aur.role_id
JOIN admin_user au ON aur.admin_user_id = au.id
WHERE au.email = 'owner_test_001@example.com';
```

---

## 預期結果

### 成功回應（200 OK）

```json
{
  "success": true,
  "data": {
    "id": "uuid-string",
    "username": "owner_test_001@example.com",
    "email": "owner_test_001@example.com",
    "displayName": "測試店家老闆",
    "phone": "0912345678",
    "status": "PENDING",
    "statusName": "待審核",
    "roles": ["ROLE_STORE_OWNER"],
    "createdAt": "2026-01-09T17:00:00",
    "forceChangePassword": true
  },
  "meta": {
    "timestamp": "2026-01-09T17:00:00.123+08:00",
    "requestId": "uuid"
  }
}
```

### 資料庫資料

```sql
-- admin_user 表
id                                   | username                       | email                          | status  | force_change_password
------------------------------------ | ------------------------------ | ------------------------------ | ------- | --------------------
uuid-xxx                             | owner_test_001@example.com     | owner_test_001@example.com     | PENDING | 1

-- store 表（完整欄位）
id          | owner_id | store_name     | logo_url                              | email                    | phone        | address                | business_hours      | status
----------- | -------- | -------------- | ------------------------------------- | ------------------------ | ------------ | ---------------------- | ------------------- | ------
uuid-yyy    | uuid-xxx | KUJI 測試商店  | https://picsum.photos/seed/logo/...   | shop_test_001@example... | 02-1234-5678 | 台北市信義區信義路...   | 週一至週日 10:00... | ACTIVE

-- store_user 表
id          | store_id | admin_user_id | role_type
----------- | -------- | ------------- | ---------
uuid-zzz    | uuid-yyy | uuid-xxx      | OWNER

-- admin_user_role 表
admin_user_id | role_id                             
------------- | ------------------------------------
uuid-xxx      | (ROLE_STORE_OWNER 的 role_id)
```

---

## 常見錯誤處理

### 1. Email 格式錯誤

**錯誤**：
```json
{
  "success": false,
  "error": {
    "code": "COMMON_VALIDATION_001",
    "message": "Email 格式不正確"
  }
}
```

**解決**：確保 `email` 和 `storeEmail` 都是有效的 Email 格式。

---

### 2. Email 重複

**錯誤**：
```json
{
  "success": false,
  "error": {
    "code": "USER_EMAIL_EXISTS",
    "message": "Email 已被使用"
  }
}
```

**解決**：更換不同的 Email，或先刪除舊資料。

---

### 3. 必填欄位遺漏

**錯誤**：
```json
{
  "success": false,
  "error": {
    "code": "COMMON_VALIDATION_001",
    "message": "Email 不可為空"
  }
}
```

**解決**：確保所有 `@NotBlank` 欄位都有值（不是 `null` 或空字串）。

必填欄位清單：
- `email`
- `displayName`
- `storeName`
- `shortDescription`
- `logoUrl`
- `storeEmail`
- `storePhone`
- `storeAddress`
- `businessHours`

---

## 修正總結

### ✅ 已完成

1. **Service 層修正**
   - 新增 11 個欄位設定
   - 修正 `email` 和 `phone` 來源
   - 所有必填欄位都已設定

2. **編譯驗證**
   - 無編譯錯誤 ✅

3. **文件更新**
   - 建立診斷報告：`CREATE_STORE_OWNER_ERROR_DIAGNOSIS.md`
   - 建立修正報告：本文件

### ⏳ 待執行

1. **使用者操作**
   - 修正請求資料拼字：`"enail"` → `"email"`
   - 重新發送請求

2. **驗證**
   - 檢查 API 回應
   - 查詢資料庫確認資料完整性
   - 測試完整流程

---

## 下一步

1. **修正請求資料**並重試
2. **如果成功**：
   - 查詢資料庫確認資料完整
   - 測試登入（使用初始密碼）
   - 測試權限（是否能存取店家管理 API）

3. **如果失敗**：
   - 提供完整的錯誤回應
   - 提供 `app.log` 最後 50 行日誌
   - 檢查資料庫欄位是否存在

---

**修正時間**：2026-01-09  
**修正檔案**：`AdminUserServiceImpl.java`  
**修正狀態**：✅ 完成（無編譯錯誤）  
**待驗證**：⏳ 等待使用者測試
