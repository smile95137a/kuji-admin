# Quickstart: RBAC 權限管理

**Feature**: 009-rbac-permissions  
**Date**: 2026-03-22

本指南涵蓋：本機設定、關鍵 API 呼叫，以及 RBAC 功能的手動驗證步驟。

---

## 前置條件

| 工具 | 版本 | 備註 |
|------|---------|-------|
| JDK | 21 | `java -version` 應顯示 21 |
| Maven | 3.9+ | `mvn -version` |
| MySQL | 8.3 | 本機實例或 RDS 通道連線 |
| Postman / curl | 任意 | 手動測試用 |

---

## 1. 資料庫設定

Schema 資料表已存在，只有種子資料可能尚未填入。

```sql
-- 1. Ensure the 3 fixed roles exist
INSERT IGNORE INTO role (name, code, description) VALUES
  ('Admin',       'ROLE_ADMIN',        '平台最高管理員'),
  ('StoreOwner',  'ROLE_STORE_OWNER',  '店家負責人'),
  ('StoreEditor', 'ROLE_STORE_EDITOR', '店家編輯');

-- 2. Verify menus exist
SELECT id, name, code, parent_id, is_visible FROM menu ORDER BY order_num;

-- 3. Assign ROLE_ADMIN to your test admin user (replace 1 with actual user id)
INSERT IGNORE INTO admin_user_role (admin_user_id, role_id)
SELECT 1, id FROM role WHERE code = 'ROLE_ADMIN';
```

---

## 2. 建構與執行

```bash
cd C:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin

# Build (skip tests for fast startup)
mvn clean package -DskipTests

# Run with local profile
java -jar target/admin-*.jar --spring.profiles.active=local
```

或使用 IDE：以 `local` profile 執行 `AdminApplication.java` 的 main 類別。

---

## 3. 取得 Admin JWT Token

```bash
curl -s -X POST http://localhost:8080/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin@example.com","password":"your_password"}' | jq .
```

複製 `data.accessToken` 的值，在後續所有呼叫中以 `Bearer <token>` 方式使用。

```bash
export TOKEN="eyJhbGci..."
```

---

## 4. 關鍵 API 呼叫

### 4.1 取得我的授權選單（動態側邊欄）

```bash
curl -s http://localhost:8080/admin/menus/my \
  -H "Authorization: Bearer $TOKEN" | jq .
```

**預期回應**（admin 看到所有內容，所有權限旗標均為 true）：
```json
{
  "code": 200,
  "data": [
    {
      "id": "1",
      "name": "商品管理",
      "code": "PRODUCTS",
      "path": "/products",
      "canView": true,
      "canEdit": true,
      "canDelete": true,
      "children": [...]
    }
  ]
}
```

### 4.2 列出角色

```bash
curl -s http://localhost:8080/admin/roles \
  -H "Authorization: Bearer $TOKEN" | jq .
```

### 4.3 取得角色權限

```bash
# Replace {roleId} with the actual role ID from step 4.2
curl -s http://localhost:8080/admin/roles/{roleId}/permissions \
  -H "Authorization: Bearer $TOKEN" | jq .
```

### 4.4 更新角色權限（僅限 Admin）

```bash
curl -s -X PUT http://localhost:8080/admin/roles/{roleId}/permissions \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "menuPermissions": [
      { "menuId": "2", "canView": true, "canEdit": true, "canDelete": false },
      { "menuId": "3", "canView": true, "canEdit": false, "canDelete": false }
    ]
  }' | jq .
```

---

## 5. 驗證清單

### 驗證店家負責人隔離

1. 建立或使用現有的 StoreOwner 使用者（僅連結至店家 A）。
2. 以 StoreOwner A 登入；複製 token。
3. `GET /admin/products?storeId=<store_A_id>` → 必須只回傳店家 A 的商品。
4. `GET /admin/products?storeId=<store_B_id>` → 必須回傳 HTTP 403。

### 驗證選單篩選

1. 以 StoreEditor（僅有 PRODUCTS 權限）登入。
2. `GET /admin/menus/my` → 回應應只包含 editor 具有 `can_view=true` 的選單。
3. 所有回傳選單的 `canDelete` 必須為 false（StoreEditor 預設值）。

### 驗證角色管理上的 @PreAuthorize

1. 以 StoreOwner（非 admin）登入。
2. `PUT /admin/roles/{id}/permissions` → 必須回傳 HTTP 403。
3. 以 Admin 登入。
4. 相同請求 → 必須回傳 HTTP 200。

### 驗證稽核日誌

1. 以 Admin 身份，使用新值呼叫 `PUT /admin/roles/{id}/permissions`。
2. 查詢 DB：`SELECT * FROM admin_operation_log WHERE operation_type='UPDATE_ROLE_PERMISSIONS' ORDER BY created_at DESC LIMIT 1;`
3. 確認 operator_id、target_id 以及 JSON before/after 均已填入。

---

## 6. 常見錯誤

| 錯誤 | 原因 | 解決方式 |
|-------|-------|-----|
| `/admin/menus/my` 回傳 `403 Forbidden` | JWT 不包含有效角色 | 確認 `admin_user_role` 資料表；指派角色後重新登入 |
| 選單陣列為空 | 角色對應的 `role_menu` 不存在 | 執行第 1 節的種子 SQL；透過 API 為角色指派選單 |
| PUT 權限時出現 `422 Unprocessable Entity` | StoreEditor 權限超出 StoreOwner | 降低 StoreEditor 權限使其 ≤ StoreOwner |
| `401 Unauthorized` | Token 已過期或缺少 Bearer 前綴 | 重新登入；確認 `Authorization: Bearer <token>` 標頭 |
| 有 `can_edit` 但無 `can_view` | 無效的權限組合 | Service 強制執行：`can_edit=true` 需要 `can_view=true` |

---

## 7. 執行測試

```bash
# Unit tests only (fast)
mvn test -Dtest="MenuServiceTest,RoleServiceTest" -pl .

# Controller integration tests
mvn test -Dtest="MenuControllerTest,RoleControllerTest" -pl .

# All tests
mvn test
```

---

## 8. 開發提示

- **權限變更立即生效**（v1.0 無快取）。不需重啟。
- **ROLE_ADMIN 繞過**：`StorePermissionUtil.assertStoreAccess()` 方法對 `ROLE_ADMIN` 提前回傳。管理員永遠能看到所有店家和所有選單。
- **isVisible 旗標**：設定 `menu.is_visible = 0` 會對**所有**使用者（包含 admin）隱藏選單。用於全域停用某功能。
- **多角色使用者**：若測試使用者同時有 ROLE_STORE_OWNER 和 ROLE_STORE_EDITOR，有效權限為聯集（OR）。請為此加入測試案例。
