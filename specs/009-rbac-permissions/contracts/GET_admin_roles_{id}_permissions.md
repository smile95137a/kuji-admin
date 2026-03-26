# Contract: GET /admin/roles/{id}/permissions

**Feature**: 009-rbac-permissions  
**Category**: Role Management — Permission Query  
**Date**: 2026-03-22

---

## Overview

Returns the full menu permission matrix for a specific role. The response lists every
menu in the system along with the role's `can_view`, `can_edit`, and `can_delete` flags
for each. Menus without an explicit `role_menu` row are returned with all flags as `false`.

**Access**: Requires ROLE_ADMIN.

---

## HTTP Request

```
GET /admin/roles/{id}/permissions
Authorization: Bearer <access_token>
```

### Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | String | Yes | Role ID (from `GET /admin/roles`) |

---

## Response — 200 OK

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "2",
    "name": "StoreOwner",
    "code": "ROLE_STORE_OWNER",
    "description": "店家負責人，僅可存取所屬店家之資料",
    "menuPermissions": [
      {
        "menuId": "1",
        "menuName": "商品管理",
        "menuCode": "PRODUCTS",
        "canView": true,
        "canEdit": true,
        "canDelete": false
      },
      {
        "menuId": "11",
        "menuName": "商品列表",
        "menuCode": "PRODUCTS_LIST",
        "canView": true,
        "canEdit": true,
        "canDelete": false
      },
      {
        "menuId": "2",
        "menuName": "訂單管理",
        "menuCode": "ORDERS",
        "canView": true,
        "canEdit": false,
        "canDelete": false
      },
      {
        "menuId": "4",
        "menuName": "店家管理",
        "menuCode": "STORES",
        "canView": false,
        "canEdit": false,
        "canDelete": false
      }
    ]
  }
}
```

### Response Fields

| Field | Type | Description |
|-------|------|-------------|
| `id` | String | Role ID |
| `name` | String | Role display name |
| `code` | String | Role code constant |
| `description` | String\|null | Role description |
| `menuPermissions` | Array | One entry per menu in the system |
| `menuPermissions[].menuId` | String | Menu ID |
| `menuPermissions[].menuName` | String | Menu display name |
| `menuPermissions[].menuCode` | String | Menu programme code |
| `menuPermissions[].canView` | Boolean | Role can view this menu |
| `menuPermissions[].canEdit` | Boolean | Role can edit items in this menu |
| `menuPermissions[].canDelete` | Boolean | Role can delete items in this menu |

**Important**: All menus are returned, including those with all flags `false`. This allows
the frontend to render a complete permission matrix with checkboxes.

**Ordering**: Menus are ordered by `menu.order_num` ascending.

---

## Response — 404 Not Found

```json
{
  "code": 404,
  "message": "Role not found: {id}",
  "data": null
}
```

---

## Response — 401 Unauthorized

```json
{
  "code": 401,
  "message": "Unauthorized: missing or invalid token",
  "data": null
}
```

---

## Response — 403 Forbidden

```json
{
  "code": 403,
  "message": "Access denied: ROLE_ADMIN required",
  "data": null
}
```

---

## Implementation Notes

- **Security annotation**: `@PreAuthorize("hasRole('ADMIN')")`.
- **Controller**: `RoleController.getRolePermissions(@PathVariable String id)`.
- **Service logic**:
  1. Fetch role by id — throw `ResourceNotFoundException` if not found.
  2. Fetch all visible menus: `SELECT * FROM menu WHERE is_visible = 1 ORDER BY order_num`.
  3. Fetch role's `role_menu` rows: `SELECT * FROM role_menu WHERE role_id = #{roleId}`.
  4. For each menu, look up its `role_menu` entry; default all flags to `false` if no entry.
  5. Return assembled `RoleWithPermissionsRes`.
- **No LEFT JOIN needed**: Fetch both sets separately and merge in Java. Simpler and avoids
  NULL handling in MyBatis.

---

## Related Endpoints

| Endpoint | Description |
|----------|-------------|
| `GET /admin/roles` | List all roles |
| `PUT /admin/roles/{id}/permissions` | Update a role's permissions |
| `GET /admin/menus/my` | Get the calling user's authorized menus |
