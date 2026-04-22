# 04 - 後台帳號管理

> **路由前綴**：`/admin/users`  
> **允許角色**：ADMIN（全部操作）

---

## API 列表

| 方法 | 路徑 | 說明 |
|------|------|------|
| POST | `/admin/users/store-owner` | 建立店家負責人帳號 |
| POST | `/admin/users/store-editor` | 建立店家編輯帳號 |
| GET | `/admin/users` | 查詢後台帳號列表 |
| GET | `/admin/users/{id}` | 取得帳號詳情 |
| PUT | `/admin/users/{id}` | 更新帳號資訊 |
| PUT | `/admin/users/{id}/status` | 停用/啟用帳號 |
| DELETE | `/admin/users/{id}` | 刪除帳號 |

---

## 帳號建立流程圖

```
Admin                           系統
  │                              │
  │── POST /users/store-owner ──▶│
  │                              │ 1. 建立 admin_user 記錄
  │                              │ 2. 指派 ROLE_STORE_OWNER
  │                              │ 3. 建立 store_user 關聯
  │                              │ 4. 設定 mustChangePassword=true
  │◀── { AdminUserRes } ─────────│
  │                              │
  │   [系統發出通知/告知初始密碼] │
  │                              │
StoreOwner 首次登入:              │
  │── POST /auth/login ─────────▶│
  │◀── { mustChangePassword:true }│
  │── POST /auth/first-login... ─▶│
  │◀── { 新 Token } ──────────────│
```

---

## 建立店家負責人（StoreOwner）

```
POST /api/admin/users/store-owner
Authorization: Bearer {token}（需 ADMIN）
```

### 請求
```typescript
interface CreateStoreOwnerReq {
  email: string;           // 登入 Email（唯一）
  password?: string;       // 初始密碼（不填則後端自動生成）
  displayName?: string;
  // 注: 店家綁定在建立店家時完成，此處僅建立帳號
}
```

### 回應
```typescript
interface AdminUserRes {
  id: string;
  username: string;        // Email
  displayName: string;
  roles: RoleInfo[];
  status: 'ACTIVE' | 'INACTIVE' | 'FIRST_LOGIN';
  mustChangePassword: boolean;
  storeIds: string[];      // 已關聯的店家 ID 列表
  createdAt: string;
  lastLoginAt: string | null;
}

interface RoleInfo {
  id: string;
  code: string;   // ROLE_STORE_OWNER
  name: string;   // 店家負責人
}
```

---

## 建立店家編輯（StoreEditor）

```
POST /api/admin/users/store-editor
Authorization: Bearer {token}（需 ADMIN）
```

### 請求
```typescript
interface CreateStoreEditorReq {
  email: string;          // 登入 Email（唯一）
  password?: string;      // 初始密碼（不填則後端自動生成）
  displayName?: string;
  storeId: string;        // 必填：指定歸屬的店家 ID
}
```

⚠️ **StoreEditor 建立時必須指定 `storeId`**，帳號建立後自動與該店家關聯。

---

## 查詢帳號列表

```
GET /api/admin/users?storeId=&role=&status=
Authorization: Bearer {token}（需 ADMIN）
```

| 參數 | 說明 |
|------|------|
| `storeId` | 篩選特定店家的帳號 |
| `role` | 篩選角色（如 `ROLE_STORE_OWNER`） |
| `status` | 篩選狀態（`ACTIVE`/`INACTIVE`/`FIRST_LOGIN`） |

---

## 停用/啟用帳號

```
PUT /api/admin/users/{id}/status
Authorization: Bearer {token}（需 ADMIN）
```

### 請求
```typescript
interface UpdateStatusReq {
  status: 'ACTIVE' | 'INACTIVE';
  reason?: string;
}
```

⚠️ **帳號停用後**：
- 相關的 JWT Token 立即失效（現有 Session 被踢出）
- 無法再登入
- 但已建立的商品、訂單記錄保留

---

## 取得目前登入帳號資訊

```
GET /api/admin/users/me
Authorization: Bearer {token}
```

回傳當前登入帳號的完整資訊，包含 `storeIds` 和 `roles`，前端可用於初始化 store。

---

## 業務規則

| 規則 | 說明 |
|------|------|
| StoreOwner 無法新增帳號 | 只有 Admin 可建立所有帳號 |
| StoreEditor 無法查看帳號管理 | 此模組頁面 StoreEditor 無權訪問 |
| 帳號 Email 唯一 | 後端唯一約束，重複會回傳 `409` |
| 刪除帳號 | 需確認沒有進行中訂單才可刪除（後端驗證） |
