# API Contract: POST /admin/system-config

**Endpoint**: `POST /admin/system-config`
**認證**: 需要 JWT（ROLE_ADMIN）
**說明**: 新增系統參數

## Request

### Body

```json
{
  "configKey": "new_param_key",
  "configValue": "100",
  "configType": "INTEGER",
  "configGroup": "DRAW",
  "description": "新參數說明"
}
```

| Field | Type | Required | Validation |
|-------|------|----------|------------|
| `configKey` | String | Yes | `@NotBlank @Size(max=100)`，唯一 |
| `configValue` | String | Yes | `@NotBlank @Size(max=500)` |
| `configType` | String | Yes | `@NotBlank`，限 INTEGER/STRING/BOOLEAN |
| `configGroup` | String | Yes | `@NotBlank @Size(max=50)` |
| `description` | String | No | `@Size(max=500)` |

## Response

### 200 OK

```json
{
  "success": true,
  "data": {
    "id": "uuid-new",
    "configKey": "new_param_key",
    "configValue": "100",
    "configType": "INTEGER",
    "configGroup": "DRAW",
    "description": "新參數說明",
    "version": 0,
    "updatedAt": "2026-04-13T10:00:00"
  }
}
```

### 400 Bad Request

- configKey 已存在
- configValue 不符合 configType 宣告的型別

### 403 Forbidden

非 ADMIN 角色。
