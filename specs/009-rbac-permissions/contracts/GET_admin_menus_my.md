# Contract: GET /admin/menus/my

**Feature**: 009-rbac-permissions  
**Category**: 選單 — 動態授權  
**Date**: 2026-03-22

---

## 概述

回傳當前已認證後台使用者的階層式選單樹，只包含使用者在任意角色中具有至少 `can_view = true` 的選單。每個選單節點附帶有效權限旗標，讓前端可條件式渲染編輯/刪除控制項，無需額外呼叫。

ROLE_ADMIN 會收到所有可見選單，且所有權限均設為 `true`。

---

## HTTP 請求

```
GET /admin/menus/my
Authorization: Bearer <access_token>
```

**無請求本體。無查詢參數。**

---

## 回應 — 200 OK

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": "1",
      "name": "商品管理",
      "code": "PRODUCTS",
      "path": "/products",
      "parentId": null,
      "icon": "ShoppingCart",
      "orderNum": 10,
      "canView": true,
      "canEdit": true,
      "canDelete": false,
      "children": [
        {
          "id": "11",
          "name": "商品列表",
          "code": "PRODUCTS_LIST",
          "path": "/products/list",
          "parentId": "1",
          "icon": null,
          "orderNum": 11,
          "canView": true,
          "canEdit": true,
          "canDelete": false,
          "children": []
        }
      ]
    },
    {
      "id": "2",
      "name": "訂單管理",
      "code": "ORDERS",
      "path": "/orders",
      "parentId": null,
      "icon": "Receipt",
      "orderNum": 20,
      "canView": true,
      "canEdit": false,
      "canDelete": false,
      "children": []
    }
  ]
}
```

### 回應欄位

| 欄位 | 型別 | 說明 |
|-------|------|-------------|
| `id` | String | 選單 ID |
| `name` | String | 顯示名稱（中文） |
| `code` | String | 程式常數（例如 `PRODUCTS`） |
| `path` | String | 前端路由路徑 |
| `parentId` | String\|null | 父選單 ID；頂層為 null |
| `icon` | String\|null | UI 渲染用圖示識別碼 |
| `orderNum` | Integer | 排序順序（升冪 = 靠前） |
| `canView` | Boolean | 有效的瀏覽權限 |
| `canEdit` | Boolean | 有效的編輯權限 |
| `canDelete` | Boolean | 有效的刪除權限 |
| `children` | Array | 巢狀子選單（相同 schema）；葉節點為空陣列 |

**排序**：頂層選單依 `orderNum` 升冪排列。每個父選單的子選單也依 `orderNum` 升冪排列。

**伺服器端套用的篩選規則**：
1. 只包含 `menu.is_visible = 1` 的選單。
2. 只包含使用者有效 `can_view = true` 的選單。
3. 使用者對父選單沒有 `can_view` 時，無論子選單為何，父選單均排除。
4. 被排除的父選單的子選單也一併排除。

---

## 回應 — 401 Unauthorized

```json
{
  "code": 401,
  "message": "Unauthorized: missing or invalid token",
  "data": null
}
```

---

## 回應 — 選單為空（使用者無角色權限）

```json
{
  "code": 200,
  "message": "success",
  "data": []
}
```

當使用者沒有 `admin_user_role` 記錄，或其所有角色的 `role_menu` 列均無 `can_view = true` 時，會出現此情況。

---

## 實作備註

- **查詢策略**：單一 SQL JOIN（參見 data-model.md §3 聚合查詢），使用 `GROUP BY menu.id` 並取 `MAX(can_view)` / `MAX(can_edit)` / `MAX(can_delete)`。
- **ROLE_ADMIN 捷徑**：若使用者角色包含 `ROLE_ADMIN`，跳過 JOIN 查詢，直接回傳所有旗標為 `true` 的所有選單。
- **樹狀結構建立**：在 Java（服務層）完成，非 SQL — 平面清單 → 排序 → 依 `parentId` 巢狀化。
- **Controller 方法**：`MenuController.getMyMenus()` 呼叫 `MenuService.getAuthorizedMenusForUser(userId, roles)`。
- **安全性**：方法受 `@PreAuthorize("isAuthenticated()")` 保護（任何已登入的後台使用者）。
- **快取**：v1.0 無快取。權限變更在下次呼叫時立即生效。

---

## 相關端點

| 端點 | 說明 |
|----------|-------------|
| `GET /admin/menus` | 僅限 Admin：所有選單的平面清單（CRUD 管理） |
| `GET /admin/menus/tree` | 僅限 Admin：完整選單樹（無權限篩選） |
| `PUT /admin/roles/{id}/permissions` | 更新角色可存取的選單 |
