# API Contract: PUT /admin/system-config/{id}

**Endpoint**: `PUT /admin/system-config/{id}`
**認證**: 需要 JWT（ROLE_ADMIN）
**說明**: 修改系統參數值（含樂觀鎖版本控制）

## Request

### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | String | 參數 UUID |

### Body

```json
{
  "configValue": "6",
  "description": "保護初始時間改為 6 分鐘",
  "version": 0
}
```

| Field | Type | Required | Validation |
|-------|------|----------|------------|
| `configValue` | String | Yes | `@NotBlank @Size(max=500)` |
| `description` | String | No | `@Size(max=500)` |
| `version` | Integer | Yes | `@NotNull`，樂觀鎖比對 |

## Response

### 200 OK

```json
{
  "success": true,
  "data": {
    "id": "uuid-1",
    "configKey": "protection_initial_minutes",
    "configValue": "6",
    "configType": "INTEGER",
    "configGroup": "DRAW",
    "description": "保護初始時間改為 6 分鐘",
    "version": 1,
    "updatedAt": "2026-04-13T10:05:00"
  }
}
```

### 404 Not Found

參數 ID 不存在。

### 409 Conflict

版本號不匹配（其他管理員已先修改）。

### 400 Bad Request

configValue 不符合 configType 型別。
