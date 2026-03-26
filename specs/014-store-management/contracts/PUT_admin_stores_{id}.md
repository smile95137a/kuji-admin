# 合約：PUT /admin/stores/{id}

**用途**：編輯店家的顯示資訊與聯絡資料。  
**認證**：`ROLE_ADMIN`（完整編輯）或 `ROLE_STORE_OWNER`（限制編輯 — 僅限自己的店家）  
**路由**：`PUT /admin/stores/{id}`

---

## 請求

### 路徑參數
| 參數 | 型別 | 說明 |
|-------|------|-------------|
| `id` | UUID string | 店家 ID |

### Headers
```
Authorization: Bearer <jwt>
Content-Type: application/json
```

### Body
```json
{
  "storeName": "甜甜圈抽獎屋 2.0",
  "shortDescription": "升級版甜甜圈抽獎",
  "longDescription": "更新後的完整描述...",
  "logoUrl": "https://cdn.example.com/stores/logo-new.png",
  "coverImageUrl": "https://cdn.example.com/stores/cover-new.png",
  "email": "new@donut-lottery.tw",
  "phone": "02-9999-8888",
  "address": "台北市信義區松高路100號",
  "businessHours": "每日 09:00–22:00",
  "facebookUrl": "https://facebook.com/donut-lottery-v2",
  "instagramUrl": "https://instagram.com/donut_v2",
  "lineId": "@donut_v2",
  "remark": "更新備注"
}
```

> **備注**：`ownerId` 在此端點中刻意省略。負責人綁定不能透過此合約變更。管理員若需轉移所有權，須使用未來的專用端點（v2+）。

### 所有欄位均為選填
所有欄位均為選填。僅套用非 null 的欄位（`updateByPrimaryKeySelective`）。

---

## 依角色欄位限制

| 欄位 | ADMIN | STORE_OWNER |
|-------|-------|------------|
| `storeName` | ✅ 可編輯 | ✅ 可編輯 |
| `shortDescription` | ✅ | ✅ |
| `longDescription` | ✅ | ✅ |
| `logoUrl` | ✅ | ✅ |
| `coverImageUrl` | ✅ | ✅ |
| `email` | ✅ | ✅ |
| `phone` | ✅ | ✅ |
| `address` | ✅ | ✅ |
| `businessHours` | ✅ | ✅ |
| `facebookUrl` | ✅ | ✅ |
| `instagramUrl` | ✅ | ✅ |
| `lineId` | ✅ | ✅ |
| `remark` | ✅ | ❌ 忽略（內部管理員欄位） |
| `ownerId` | ❌ 此端點不支援 | ❌ |
| `status` | ❌ 使用 `/status` 端點 | ❌ |

---

## 回應

### 200 OK
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "storeName": "甜甜圈抽獎屋 2.0",
  "shortDescription": "升級版甜甜圈抽獎",
  "logoUrl": "https://cdn.example.com/stores/logo-new.png",
  "coverImageUrl": "https://cdn.example.com/stores/cover-new.png",
  "email": "new@donut-lottery.tw",
  "phone": "02-9999-8888",
  "address": "台北市信義區松高路100號",
  "businessHours": "每日 09:00–22:00",
  "facebookUrl": "https://facebook.com/donut-lottery-v2",
  "instagramUrl": "https://instagram.com/donut_v2",
  "lineId": "@donut_v2",
  "status": "ENABLED",
  "ownerId": "f1e2d3c4-b5a6-9870-fedc-ba9876543210",
  "ownerDisplayName": "甜甜圈店長",
  "updatedAt": "2026-03-22T11:00:00",
  "updatedBy": "caller-admin-uuid"
}
```

### 400 Bad Request
```json
{
  "code": "VALIDATION_ERROR",
  "message": "欄位驗證失敗",
  "errors": [
    { "field": "storeName", "message": "超過最大長度 100 字元" }
  ]
}
```

### 403 Forbidden — 跨店家存取
```json
{
  "code": "ACCESS_DENIED",
  "message": "無權限編輯此店家"
}
```

### 404 Not Found
```json
{
  "code": "STORE_NOT_FOUND",
  "message": "店家不存在"
}
```

---

## 強制執行邏輯（服務層）

```java
public StoreRes updateStore(String storeId, UpdateStoreReq req) {
    Store store = storeMapper.selectByPrimaryKey(storeId);
    if (store == null) throw new BusinessException("店家不存在");

    String callerId = SecurityUtils.getCurrentUserId();
    boolean isAdmin = SecurityUtils.isAdmin();

    if (!isAdmin) {
        // 1. Verify caller owns this store
        if (!storeUserService.ownsStore(callerId, storeId)) {
            throw new AccessDeniedException("無權限編輯此店家");
        }
        // 2. Strip admin-only fields
        req.setRemark(null);
    }

    // Apply non-null fields
    copyNonNullProperties(req, store);
    store.setUpdatedAt(LocalDateTime.now());
    store.setUpdatedBy(callerId);
    storeMapper.updateByPrimaryKeySelective(store);

    return toStoreRes(store);
}
```
