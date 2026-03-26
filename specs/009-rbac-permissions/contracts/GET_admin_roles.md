# Contract: GET /admin/roles

**Feature**: 009-rbac-permissions  
**Category**: Role Management  
**Date**: 2026-03-22

---

## Overview

Returns the list of all system roles. In v1.0 there are exactly 3 fixed roles
(ROLE_ADMIN, ROLE_STORE_OWNER, ROLE_STORE_EDITOR). This endpoint is used by the
frontend role-management page to populate dropdowns and role tables.

**Access**: Requires ROLE_ADMIN. Store-owner and store-editor roles cannot view the
full role list.

---

## HTTP Request

```
GET /admin/roles
Authorization: Bearer <access_token>
```

**No request body. No query parameters.**

---

## Response — 200 OK

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": "1",
      "name": "Admin",
      "code": "ROLE_ADMIN",
      "description": "平台最高管理員，可存取所有功能與所有店家資料",
      "createdAt": "2026-01-01T00:00:00",
      "updatedAt": "2026-01-01T00:00:00"
    },
    {
      "id": "2",
      "name": "StoreOwner",
      "code": "ROLE_STORE_OWNER",
      "description": "店家負責人，僅可存取所屬店家之資料",
      "createdAt": "2026-01-01T00:00:00",
      "updatedAt": "2026-01-01T00:00:00"
    },
    {
      "id": "3",
      "name": "StoreEditor",
      "code": "ROLE_STORE_EDITOR",
      "description": "店家編輯，僅可存取所屬店家資料，權限為負責人子集",
      "createdAt": "2026-01-01T00:00:00",
      "updatedAt": "2026-01-01T00:00:00"
    }
  ]
}
```

### Response Fields

| Field | Type | Description |
|-------|------|-------------|
| `id` | String | Role ID (DB auto-increment, returned as String for consistency) |
| `name` | String | Human-readable role name |
| `code` | String | Programme constant — always prefixed `ROLE_` |
| `description` | String\|null | Role description |
| `createdAt` | ISO-8601 datetime | Role creation timestamp |
| `updatedAt` | ISO-8601 datetime | Last modification timestamp |

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

Returned when the caller does not have `ROLE_ADMIN`.

---

## Implementation Notes

- **Security annotation**: `@PreAuthorize("hasRole('ADMIN')")` on the controller method.
- **Controller**: `RoleController.listRoles()` → `RoleService.getAllRoles()`.
- **Query**: `SELECT * FROM role ORDER BY id` — simple full-table scan (3 rows).
- **v1.0 constraint**: No pagination required (always ≤ 3 roles).
- **No create/delete endpoints**: Roles are fixed in v1.0. Role CRUD is intentionally
  excluded from this feature scope.

---

## Related Endpoints

| Endpoint | Description |
|----------|-------------|
| `GET /admin/roles/{id}/permissions` | Get a role's menu permission matrix |
| `PUT /admin/roles/{id}/permissions` | Update a role's menu permissions |
