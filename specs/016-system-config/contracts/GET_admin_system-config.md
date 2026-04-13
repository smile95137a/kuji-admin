# API Contract: GET /admin/system-config

**Endpoint**: `GET /admin/system-config`
**認證**: 需要 JWT（ROLE_ADMIN）
**說明**: 查詢所有系統參數，可選依 group 篩選

## Request

### Query Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `group` | String | No | 參數分組篩選（如 DRAW、GENERAL） |

### Headers

| Header | Value |
|--------|-------|
| `Authorization` | `Bearer {jwt_token}` |

## Response

### 200 OK

```json
{
  "success": true,
  "data": [
    {
      "id": "uuid-1",
      "configKey": "protection_initial_minutes",
      "configValue": "5",
      "configType": "INTEGER",
      "configGroup": "DRAW",
      "description": "保護初始時間（分鐘）",
      "version": 0,
      "updatedAt": "2026-04-13T10:00:00"
    }
  ],
  "error": null,
  "meta": { "timestamp": "...", "requestId": "..." }
}
```

### 403 Forbidden

非 ADMIN 角色存取。

## Business Rules

- 回傳全部參數，不分頁（預計 < 20 筆）
- 若指定 group，僅回傳該分組之參數
