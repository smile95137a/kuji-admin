# Data Model: RBAC 權限管理

**Feature**: 009-rbac-permissions  
**Phase**: 1 — 設計與契約  
**Date**: 2026-03-22

> 所有資料表均已存在於資料庫 schema（doc/DDL.sql）中。本文件描述套用於本功能的權威 Entity 定義、關聯關係與驗證規則。核心 RBAC 資料表不需要 schema 遷移。`role_menu` 可能需要小幅新增（`updated_by`、`operated_by`）以支援稽核；詳見第 6 節。

---

## 1. Entity：`role`

**資料表**：`role`  
**已存在**：✅ 是  
**Entity 類別**：`com.group.admin.entity.Role`

| 欄位 | 型別 | 可為空 | 備註 |
|--------|------|----------|-------|
| `id` | VARCHAR / BIGINT PK | 否 | DDL 中為自動遞增；Entity 使用 String |
| `name` | VARCHAR(50) UNIQUE | 否 | 顯示名稱：Admin / StoreOwner / StoreEditor |
| `code` | VARCHAR(50) UNIQUE | 否 | 程式常數：ROLE_ADMIN / ROLE_STORE_OWNER / ROLE_STORE_EDITOR |
| `description` | VARCHAR(255) | 是 | 人可讀的描述 |
| `created_at` | DATETIME | 否 | 寫入時自動設定 |
| `updated_at` | DATETIME | 否 | 自動更新 |

**種子資料**（功能上線前必須存在）：
```sql
INSERT IGNORE INTO role (name, code, description) VALUES
  ('Admin',       'ROLE_ADMIN',        '平台最高管理員，可存取所有功能與所有店家資料'),
  ('StoreOwner',  'ROLE_STORE_OWNER',  '店家負責人，僅可存取所屬店家之資料'),
  ('StoreEditor', 'ROLE_STORE_EDITOR', '店家編輯，僅可存取所屬店家資料，權限為負責人子集');
```

**驗證規則**：
- `code` 必須符合正規表達式 `^ROLE_[A-Z_]+$`
- v1.0 只接受 3 個固定 code（不支援動態建立）

---

## 2. Entity：`menu`

**資料表**：`menu`  
**已存在**：✅ 是  
**Entity 類別**：`com.group.admin.entity.Menu`

| 欄位 | 型別 | 可為空 | 備註 |
|--------|------|----------|-------|
| `id` | VARCHAR / BIGINT PK | 否 | 自動遞增 |
| `name` | VARCHAR(50) | 否 | 顯示標籤（透過 utf8mb4 支援中文） |
| `code` | VARCHAR(50) | 是 | 程式權限代碼，例如 `PRODUCTS`、`ORDERS` |
| `path` | VARCHAR(255) | 是 | 前端路由，例如 `/products` |
| `parent_id` | BIGINT → FK `menu.id` | 是 | NULL = 頂層選單；父項刪除時 SET NULL 級聯 |
| `icon` | VARCHAR(50) | 是 | 圖示識別字串 |
| `order_num` | INT | 否 | 同層排序（數字越小越靠前） |
| `is_visible` | TINYINT(1) | 否 | 1 = 顯示；0 = 全域隱藏 |
| `created_at` | DATETIME | 否 | 自動設定 |
| `updated_at` | DATETIME | 否 | 自動更新 |

**層級規則**：
- 最大深度：2 層（父 + 子）。v1.0 不支援孫選單。
- 父選單被隱藏（`is_visible = 0`）時，所有子選單在 API 回應中也會被隱藏。
- 使用者對父選單沒有 `can_view` 權限時，所有子選單也會被排除。

**預期種子選單**（示意用 — 實際 ID 於寫入時指派）：
```
仪表板 (DASHBOARD)           order=0
商品管理 (PRODUCTS)           order=10
  └─ 商品列表 (PRODUCTS_LIST)  order=11
  └─ 商品類別 (PRODUCTS_CAT)   order=12
訂單管理 (ORDERS)              order=20
  └─ 訂單列表 (ORDERS_LIST)    order=21
店家管理 (STORES)              order=30   ← ADMIN only
用戶管理 (USERS)               order=40   ← ADMIN only
抽獎管理 (LOTTERY)             order=50
報表 (REPORTS)                 order=60
新聞/橫幅 (NEWS)               order=70
角色管理 (ROLES)               order=80   ← ADMIN only
```

---

## 3. Entity：`role_menu`

**資料表**：`role_menu`  
**已存在**：✅ 是  
**Entity 類別**：`com.group.admin.entity.RoleMenu`

| 欄位 | 型別 | 可為空 | 備註 |
|--------|------|----------|-------|
| `id` | BIGINT PK | 否 | 自動遞增 |
| `role_id` | BIGINT FK → `role.id` | 否 | CASCADE DELETE |
| `menu_id` | BIGINT FK → `menu.id` | 否 | CASCADE DELETE |
| `can_view` | TINYINT(1) | 否 | 預設 1 |
| `can_edit` | TINYINT(1) | 否 | 預設 0 |
| `can_delete` | TINYINT(1) | 否 | 預設 0 |
| `created_at` | DATETIME | 否 | 自動設定 |

**唯一鍵**：`(role_id, menu_id)` — 每個角色-選單對只有一列權限記錄。

**權限語意**：
| `can_view` | `can_edit` | `can_delete` | 意義 |
|------------|------------|--------------|---------|
| false | false | false | 無存取權限（實際上不需要此列；不存在 = 無存取權限） |
| true | false | false | 唯讀 |
| true | true | false | 可讀寫 |
| true | true | true | 完整存取 |
| false | true | * | 無效 — 編輯需要瀏覽；Service 將拒絕此情況 |

**業務規則 — FR-008**（StoreEditor 為 StoreOwner 子集）：  
儲存 ROLE_STORE_EDITOR 權限時，對每個選單 M：
```
editor.can_edit(M)   ≤ owner.can_edit(M)
editor.can_delete(M) ≤ owner.can_delete(M)
```
違規 → HTTP 422 並附帶欄位層級的錯誤詳細資訊。

**有效權限聚合**（多角色使用者）：  
查詢模式（參見 research.md §5）：
```sql
SELECT m.*, MAX(rm.can_view) can_view, MAX(rm.can_edit) can_edit, MAX(rm.can_delete) can_delete
FROM menu m
JOIN role_menu rm ON rm.menu_id = m.id
JOIN admin_user_role aur ON aur.role_id = rm.role_id
WHERE aur.admin_user_id = #{userId} AND m.is_visible = 1
GROUP BY m.id ORDER BY m.order_num;
```

---

## 4. Entity：`admin_user_role`

**資料表**：`admin_user_role`  
**已存在**：✅ 是  
**Entity 類別**：`com.group.admin.entity.AdminUserRole`

| 欄位 | 型別 | 可為空 | 備註 |
|--------|------|----------|-------|
| `id` | BIGINT PK | 否 | 自動遞增 |
| `admin_user_id` | BIGINT FK → `admin_user.id` | 否 | CASCADE DELETE |
| `role_id` | BIGINT FK → `role.id` | 否 | CASCADE DELETE |
| `created_at` | DATETIME | 否 | 自動設定 |

**唯一鍵**：`(admin_user_id, role_id)` — 不允許重複指派角色。

**規則**：
- 使用者可擁有 0..N 個角色。0 個角色 = 可登入但無選單存取權限，且所有資料端點將拒絕。
- 角色指派僅由 ROLE_ADMIN 管理。

---

## 5. Entity：`admin_user`（相關，無變更）

**已存在**：✅ 是  
不需要 schema 變更。`AdminUser` entity 已儲存 id、username、status 等欄位。店家所有權透過 `store_user` 關聯資料表追蹤（本功能不修改）。

---

## 6. 稽核補充 — `admin_operation_log`（複用）

**已存在**：✅ 是（已確認 mapper）  
呼叫 `PUT /admin/roles/{id}/permissions` 時，寫入一筆新的日誌記錄：

| 欄位 | 值 |
|-------|-------|
| `operator_id` | JWT 的 `userId` claim |
| `operation_type` | `UPDATE_ROLE_PERMISSIONS` |
| `target_type` | `ROLE` |
| `target_id` | `{roleId}` |
| `content` | JSON：`{"before": [...], "after": [...]}` |
| `created_at` | now() |

此設計滿足 FR-011 與使用者故事 4。

---

## 7. 狀態轉換

### 權限更新生命週期

```
ROLE_ADMIN issues PUT /admin/roles/{id}/permissions
           │
           ▼
     Service validates:
     • roleId exists
     • all menuIds exist
     • if role=STORE_EDITOR → subset check against STORE_OWNER
     • can_edit=true requires can_view=true
           │
     ┌──────┴──────┐
   VALID         INVALID
     │               │
     ▼               ▼
   Delete old     HTTP 422
   role_menu rows  + error detail
   Insert new rows
   Write audit log
     │
     ▼
   HTTP 200 + updated permissions
```

### 使用者登入 → 選單解析

```
User authenticates → JWT issued (contains userId, roles, storeIds)
         │
         ▼
GET /admin/menus/my
         │
         ▼
MenuServiceImpl.getAuthorizedMenusForUser(userId, roles)
         │
    ROLE_ADMIN? ─── Yes ──▶ Return ALL visible menus (can_view/edit/delete all true)
         │
        No
         ▼
  Execute aggregation SQL (GROUP BY menu, MAX per permission bit)
         │
         ▼
  Filter: keep only rows where can_view = 1
         │
         ▼
  Build tree (parent → children)
         │
         ▼
  Return MenuTreeRes[]
```

---

## 8. DTO / 回應物件

### `MenuPermissionRes`（樹狀結構中每個選單節點）
```java
public class MenuPermissionRes {
    private String id;
    private String name;
    private String code;
    private String path;
    private String parentId;
    private String icon;
    private Integer orderNum;
    private Boolean canView;
    private Boolean canEdit;
    private Boolean canDelete;
    private List<MenuPermissionRes> children;
}
```

### `RoleWithPermissionsRes`（用於 GET /admin/roles/{id}/permissions）
```java
public class RoleWithPermissionsRes {
    private String id;
    private String name;
    private String code;
    private String description;
    private List<MenuPermissionItem> menuPermissions;

    public static class MenuPermissionItem {
        private String menuId;
        private String menuName;
        private String menuCode;
        private Boolean canView;
        private Boolean canEdit;
        private Boolean canDelete;
    }
}
```

### `UpdateRolePermissionsReq`（用於 PUT /admin/roles/{id}/permissions）
```java
public class UpdateRolePermissionsReq {
    @NotNull
    private List<MenuPermissionItem> menuPermissions;

    public static class MenuPermissionItem {
        @NotBlank
        private String menuId;
        private Boolean canView   = false;
        private Boolean canEdit   = false;
        private Boolean canDelete = false;
    }
}
```
