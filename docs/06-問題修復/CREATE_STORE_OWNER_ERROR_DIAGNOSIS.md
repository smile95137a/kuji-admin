# 新增店家負責人失敗問題診斷報告

## 問題描述

呼叫 `POST /api/admin/users/store-owner` API 新增店家負責人時失敗。

## 提供的請求資料

```json
{
  "businessHours": "每日 10:00~22:00",
  "coverImageUrl": "https://picsum.photos/seed/cover/1200/600",
  "displayName": "店家老闡_1767756770173",
  "enail": "owner_1767756770173@example.com",  // ❌ 拼字錯誤
  "facebookUrl": null,
  "instagramUrl": null,
  "lineId": null,
  "logoUrl": "https://picsum.photos/seed/logo/300/300",
  "longDescription": "店家詳細介紹(可留空)",
  "phone": "0912345678",
  "remark": "測試建立店家負責人",
  "shortDescription": "專營一番賞、扭蛋精品",
  "storeAddress": "無",
  "storeEmail": "shop_1767756770173@example.com",
  "storeName": "KUJI 測試商店_1767756770173",
  "storePhone": "02-1234-5678"
}
```

---

## 發現的問題

### ❌ 問題 1：Email 欄位拼字錯誤

```json
"enail": "owner_1767756770173@example.com"  // ❌ 錯誤
```

**應該是**：
```json
"email": "owner_1767756770173@example.com"  // ✅ 正確
```

**影響**：
- `email` 是必填欄位（`@NotBlank`）
- 拼字錯誤導致驗證失敗
- 應該會返回 400 錯誤：「Email 不可為空」

---

### ⚠️ 問題 2：Service 層實作不完整

`AdminUserServiceImpl.createStoreOwner()` 在建立 Store 時，**只設定了 7 個欄位**：

```java
Store store = new Store();
store.setId(UUID.randomUUID().toString());
store.setOwnerId(adminUser.getId());
store.setStoreName(req.getStoreName());
store.setShortDescription(req.getShortDescription());
store.setEmail(req.getEmail());           // ← 使用 AdminUser 的 email
store.setPhone(req.getPhone());           // ← 使用 AdminUser 的 phone
store.setStatus(StoreStatus.ACTIVE.getCode());
store.setCreatedAt(LocalDateTime.now());
storeMapper.insert(store);
```

**缺少的欄位**（DTO 有但沒設定）：
- ✅ `longDescription` - 店家詳細介紹
- ✅ `logoUrl` - Logo 圖片（必填）
- ✅ `coverImageUrl` - 封面圖片
- ✅ `storeEmail` - 店家專用 Email（必填）
- ✅ `storePhone` - 店家專用電話（必填）
- ✅ `storeAddress` - 店家地址（必填）
- ✅ `businessHours` - 營業時間（必填）
- ✅ `facebookUrl` - Facebook 連結
- ✅ `instagramUrl` - Instagram 連結
- ✅ `lineId` - LINE ID

**資料庫檢查**：
```sql
DESC store;
```

需確認 `store` 表是否有這些欄位。

---

## 修正方案

### 方案 1：修正請求資料（立即可用）

```json
{
  "email": "owner_1767756770173@example.com",  // ← 修正拼字
  "displayName": "店家老闡_1767756770173",
  "phone": "0912345678",
  "remark": "測試建立店家負責人",
  "storeName": "KUJI 測試商店_1767756770173",
  "shortDescription": "專營一番賞、扭蛋精品",
  "longDescription": "店家詳細介紹(可留空)",
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

### 方案 2：修正 Service 層實作（需重新編譯）

修改 `AdminUserServiceImpl.createStoreOwner()` 方法：

```java
// 建立 Store（完整版）
Store store = new Store();
store.setId(UUID.randomUUID().toString());
store.setOwnerId(adminUser.getId());
store.setStoreName(req.getStoreName());
store.setShortDescription(req.getShortDescription());
store.setLongDescription(req.getLongDescription());
store.setLogoUrl(req.getLogoUrl());
store.setCoverImageUrl(req.getCoverImageUrl());
store.setEmail(req.getStoreEmail());        // ← 改用 storeEmail
store.setPhone(req.getStorePhone());        // ← 改用 storePhone
store.setAddress(req.getStoreAddress());    // ← 新增
store.setBusinessHours(req.getBusinessHours()); // ← 新增
store.setFacebookUrl(req.getFacebookUrl()); // ← 新增
store.setInstagramUrl(req.getInstagramUrl()); // ← 新增
store.setLineId(req.getLineId());           // ← 新增
store.setStatus(StoreStatus.ACTIVE.getCode());
store.setCreatedAt(LocalDateTime.now());
store.setUpdatedBy(currentUserId);          // ← 新增（如果有此欄位）
storeMapper.insert(store);
```

---

## 驗證步驟

### 步驟 1：修正請求並重試

```bash
curl -X POST http://localhost:8080/api/admin/users/store-owner \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {ADMIN_TOKEN}" \
  -d '{
    "email": "owner_1767756770173@example.com",
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
    "businessHours": "每日 10:00~22:00"
  }' \
  -v
```

### 步驟 2：查看錯誤訊息

如果還是失敗，請提供：

1. **HTTP 狀態碼**（400? 500?）
2. **完整的錯誤回應**：
   ```json
   {
     "error": {
       "code": "???",
       "message": "???"
     }
   }
   ```
3. **app.log 中的錯誤訊息**：
   ```bash
   powershell -Command "Get-Content app.log -Tail 50"
   ```

### 步驟 3：檢查資料庫欄位

```sql
-- 檢查 store 表結構
DESC store;

-- 檢查哪些欄位是必填（NOT NULL）
SHOW COLUMNS FROM store WHERE `Null` = 'NO';
```

---

## 預期結果

### 成功回應（200 OK）

```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "username": "owner_1767756770173@example.com",
    "email": "owner_1767756770173@example.com",
    "displayName": "店家老闡_1767756770173",
    "phone": "0912345678",
    "status": "PENDING",
    "statusName": "待審核",
    "roles": ["ROLE_STORE_OWNER"],
    "createdAt": "2026-01-09T...",
    "forceChangePassword": true
  }
}
```

### 失敗回應範例

#### 1. 驗證失敗（400 Bad Request）
```json
{
  "success": false,
  "error": {
    "code": "COMMON_VALIDATION_001",
    "message": "Email 不可為空"
  }
}
```

#### 2. Email 重複（409 Conflict）
```json
{
  "success": false,
  "error": {
    "code": "USER_EMAIL_EXISTS",
    "message": "Email 已被使用"
  }
}
```

#### 3. 資料庫錯誤（500 Internal Server Error）
```json
{
  "success": false,
  "error": {
    "code": "INTERNAL_SERVER_ERROR",
    "message": "資料庫錯誤: Column 'xxx' cannot be null"
  }
}
```

---

## 立即行動

1. **修正請求資料**：把 `enail` 改成 `email`
2. **重新發送請求**
3. **如果還是失敗**：提供錯誤訊息和 app.log

---

**診斷時間**：2026-01-09  
**問題類型**：請求資料錯誤 + Service 層實作不完整  
**優先級**：🔴 高（阻擋測試）
