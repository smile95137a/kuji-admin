# API Contract: DELETE /admin/system-config/{id}

**Endpoint**: `DELETE /admin/system-config/{id}`
**認證**: 需要 JWT（ROLE_ADMIN）
**說明**: 刪除系統參數

## Request

### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | String | 參數 UUID |

## Response

### 200 OK

```json
{
  "success": true,
  "data": null
}
```

### 404 Not Found

參數 ID 不存在。

### 403 Forbidden

非 ADMIN 角色。

## Business Rules

- 刪除後相關模組讀取時將使用 fallback 預設值
- 初始系統參數（protection_* 等）刪除後系統仍可正常運作（回退到 hardcode 預設值）
