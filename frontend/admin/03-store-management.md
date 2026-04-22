# 03 - 店家管理

> **路由前綴**：`/admin/stores`  
> **允許角色**：ADMIN（完整），STORE_OWNER / STORE_EDITOR（僅可讀自己的店家）

---

## API 列表

| 方法 | 路徑 | 說明 | 需要角色 |
|------|------|------|---------|
| GET | `/admin/stores/options` | 取得店家下拉選項 | 全角色 |
| GET | `/admin/stores/all-options` | 取得所有店家選項 | ADMIN |
| GET | `/admin/stores/search?keyword=` | 關鍵字搜尋店家 | 全角色 |
| POST | `/admin/stores/list` | 查詢店家列表（含條件） | 全角色 |
| GET | `/admin/stores/{id}` | 取得店家詳情 | 全角色 |
| POST | `/admin/stores` | 新增店家 | ADMIN |
| PUT | `/admin/stores/{id}` | 更新店家資訊 | ADMIN |
| PUT | `/admin/stores/{id}/status` | 更新店家狀態 | ADMIN |

---

## 取得店家下拉選項

```
GET /api/admin/stores/options?activeOnly=true
Authorization: Bearer {token}
```

| 參數 | 說明 | 預設 |
|------|------|------|
| `activeOnly` | 是否只取啟用的店家 | `true` |

### 回應
```typescript
interface EnumOption {
  label: string;        // 店家名稱（顯示用）
  value: string;        // 店家 ID（傳給後端用）
  description: string;  // 短描述 + 狀態（輔助）
}
```

**前端使用時機**：新增商品、Banner 時的店家下拉選單（Admin 視角）

---

## 查詢店家列表

```
POST /api/admin/stores/list
Authorization: Bearer {token}
```

### 請求
```typescript
interface QueryReq<StoreCondition> {
  condition?: {
    keyword?: string;       // 店家名稱關鍵字
    status?: 'ACTIVE' | 'INACTIVE';
    createdAtStart?: string;
    createdAtEnd?: string;
  };
  sortBy?: string;
  sortOrder?: 'ASC' | 'DESC';
}
```

### 回應
```typescript
interface StoreRes {
  id: string;
  storeName: string;
  shortDescription: string;
  longDescription: string;
  logoUrl: string;
  coverImageUrl: string;
  email: string;
  phone: string;
  address: string;
  businessHours: string;
  facebookUrl: string;
  instagramUrl: string;
  lineId: string;
  status: 'ACTIVE' | 'INACTIVE';
  remark: string;
  createdAt: string;
  updatedAt: string;
}
```

---

## 新增店家（同時建立負責人帳號）

```
POST /api/admin/stores
Authorization: Bearer {token}（需 ADMIN）
```

### 請求

```typescript
interface CreateStoreReq {
  // === 店家資訊 ===
  storeName: string;          // 必填
  shortDescription?: string;
  longDescription?: string;
  logoUrl?: string;
  coverImageUrl?: string;
  email?: string;
  phone?: string;
  address?: string;
  businessHours?: string;
  facebookUrl?: string;
  instagramUrl?: string;
  lineId?: string;
  remark?: string;

  // === 同時建立負責人帳號（選填，可後續手動建立） ===
  owner?: {
    username: string;       // 登入 Email（必填，若有 owner）
    password?: string;      // 初始密碼（留空後端自動生成）
    displayName?: string;
    email?: string;
  };
}
```

### 業務邏輯
- 店家與帳號在同一事務內建立（`@Transactional`）
- 若 `owner.password` 未傳，系統自動生成初始密碼並設定 `mustChangePassword=true`
- 若不傳 `owner`，僅建立店家，後續可透過「帳號管理」補建

---

## 更新店家資訊

```
PUT /api/admin/stores/{id}
Authorization: Bearer {token}（需 ADMIN）
```

### 請求
```typescript
interface UpdateStoreReq {
  storeName?: string;
  shortDescription?: string;
  longDescription?: string;
  logoUrl?: string;
  coverImageUrl?: string;
  email?: string;
  phone?: string;
  address?: string;
  businessHours?: string;
  facebookUrl?: string;
  instagramUrl?: string;
  lineId?: string;
  remark?: string;
}
```

**⚠️ 注意：`owner_id` 建立後不可修改**

---

## 停用/啟用店家

```
PUT /api/admin/stores/{id}/status
Authorization: Bearer {token}（需 ADMIN）
```

### 請求
```typescript
interface UpdateStoreStatusReq {
  status: 'ACTIVE' | 'INACTIVE';
  reason?: string;  // 停用原因（選填）
}
```

### ⚠️ 停用連動效果（後端自動執行，前端需提示使用者）

當店家狀態改為 `INACTIVE` 時：
1. 該店家所有商品自動下架（`OFF_SHELF`）
2. 前台搜尋不到該店家及商品
3. 進行中的抽獎可繼續，但不可新開
4. 相關 Banner 一併停用

**前端建議**：在確認彈窗中說明上述連動效果，請 Admin 確認後再送出。
