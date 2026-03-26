# Contract: PUT /admin/roles/{id}/permissions

**Feature**: 009-rbac-permissions  
**Category**: Role Management — Permission Update  
**Date**: 2026-03-22

---

## Overview

Replaces the entire menu permission matrix for a role. The caller submits the full desired
permission set; the service performs a delete-then-insert (upsert) to synchronize
`role_menu` rows. An audit log entry is written before and after the update.

**Access**: ROLE_ADMIN only. This is the most privileged write operation in the RBAC system.

---

## HTTP Request

```
PUT /admin/roles/{id}/permissions
Authorization: Bearer <access_token>
Content-Type: application/json
```

### Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | String | Yes | Role ID to update permissions for |

### Request Body

```json
{
  "menuPermissions": [
    {
      "menuId": "1",
      "canView": true,
      "canEdit": true,
      "canDelete": false
    },
    {
      "menuId": "11",
      "canView": true,
      "canEdit": true,
      "canDelete": false
    },
    {
      "menuId": "2",
      "canView": true,
      "canEdit": false,
      "canDelete": false
    },
    {
      "menuId": "4",
      "canView": false,
      "canEdit": false,
      "canDelete": false
    }
  ]
}
```

**Semantics**: The `menuPermissions` array is the **complete desired state**. Any menu not
included in the array will have its permissions cleared (all flags → false). Callers should
include every menu (even those with all-false) to make the intent explicit; however, entries
with all-false are treated as "remove permission" and the corresponding `role_menu` row is deleted.

### Request Body Fields

| Field | Type | Required | Default | Description |
|-------|------|----------|---------|-------------|
| `menuPermissions` | Array | Yes | — | Permission entries; may be empty to revoke all |
| `menuPermissions[].menuId` | String | Yes | — | Target menu ID (must exist in `menu` table) |
| `menuPermissions[].canView` | Boolean | No | `false` | Grant view access |
| `menuPermissions[].canEdit` | Boolean | No | `false` | Grant edit access |
| `menuPermissions[].canDelete` | Boolean | No | `false` | Grant delete access |

---

## Response — 200 OK

Returns the updated permission matrix (same schema as `GET /admin/roles/{id}/permissions`).

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
      }
    ]
  }
}
```

---

## Response — 422 Unprocessable Entity

Returned when business rules are violated. All validation errors are returned together.

```json
{
  "code": 422,
  "message": "Validation failed",
  "data": {
    "errors": [
      {
        "menuId": "1",
        "field": "canEdit",
        "message": "StoreEditor cannot have canEdit=true when StoreOwner has canEdit=false for this menu"
      },
      {
        "menuId": "3",
        "field": "canView",
        "message": "canEdit=true requires canView=true"
      }
    ]
  }
}
```

**Validation rules enforced**:
1. `canEdit = true` requires `canView = true` for the same entry.
2. `canDelete = true` requires `canView = true` for the same entry.
3. If role = `ROLE_STORE_EDITOR`: for every menu M where `canEdit=true`, `ROLE_STORE_OWNER` must also have `canEdit=true` for M (subset rule, FR-008).
4. All `menuId` values must exist in the `menu` table.

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

## Idempotency

This endpoint is **idempotent**: calling it twice with the same payload produces the same
result. The delete-then-insert pattern ensures no duplicates.

---

## Audit Log

Every successful `PUT` writes one record to `admin_operation_log`:

| Field | Value |
|-------|-------|
| `operator_id` | `userId` from JWT |
| `operation_type` | `UPDATE_ROLE_PERMISSIONS` |
| `target_type` | `ROLE` |
| `target_id` | `{id}` (roleId) |
| `content` | `{"before": [...old permissions...], "after": [...new permissions...]}` |
| `created_at` | Server time at execution |

---

## Implementation Notes

- **Security annotation**: `@PreAuthorize("hasRole('ADMIN')")`.
- **Controller**: `RoleController.updateRolePermissions(@PathVariable String id, @Valid @RequestBody UpdateRolePermissionsReq req)`.
- **Transaction**: The entire operation (snapshot → delete → insert → audit log) runs in a
  single `@Transactional` block. If any step fails, all changes roll back.
- **Delete strategy**: `DELETE FROM role_menu WHERE role_id = #{roleId}` then bulk insert.
  Avoids complex diff logic while remaining correct.
- **Subset validation timing**: Fetch `ROLE_STORE_OWNER` permissions first, validate, then
  proceed with the delete-insert. If `ROLE_STORE_OWNER` permissions don't exist yet for a
  menu, treat owner's permission as `false` (no access) — so editor also cannot have any access.
- **Empty array**: Sending `"menuPermissions": []` is valid and clears all permissions for
  the role (effectively locking out all users with that role).

---

## Related Endpoints

| Endpoint | Description |
|----------|-------------|
| `GET /admin/roles/{id}/permissions` | Read current permissions before editing |
| `GET /admin/roles` | List all roles to get the `{id}` |
| `GET /admin/menus/my` | Verify effect from a role-holder's perspective |
