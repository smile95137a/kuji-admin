# Contract: DELETE /admin/banners/{id}

**模組**: Banner 管理  
**Auth**: JWT Bearer — 需要 `ROLE_ADMIN`  
**Context Path**: `/api`（完整 URL：`DELETE /api/admin/banners/{id}`）

---

## 描述

永久刪除一則廣告記錄。這是硬刪除——廣告列會從資料庫中移除。儲存於 S3 的圖片檔案**不會**被此端點刪除（圖片生命週期由上傳/資產管理系統另行管理）。

---

## 請求

### 路徑參數

| 參數 | 類型 | 必填 | 描述 |
|-----------|------|----------|-------------|
| `id` | `string (UUID)` | ✅ | 要刪除的廣告 ID |

### 標頭

| 標頭 | 值 | 必填 |
|--------|-------|----------|
| `Authorization` | `Bearer <jwt_token>` | ✅ |

### 請求範例

```http
DELETE /api/admin/banners/f7e8d9c0-b1a2-3456-789a-bcdef0123456
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## 回應

### 204 No Content — 成功

空回應主體。HTTP 204。

### 404 Not Found

```json
{ "code": 404, "message": "廣告不存在" }
```

### 401 Unauthorized

```json
{ "code": 401, "message": "未授權，請先登入" }
```

### 403 Forbidden

```json
{ "code": 403, "message": "無操作權限" }
```

---

## 行為說明

- 透過 `deleteByPrimaryKey` 硬刪除——無軟刪除/狀態轉換。
- 目前為 `PUBLISHED` 的廣告可直接刪除——無需先執行下架步驟。
- 公開輪播在刪除後的下一次請求即停止提供該廣告。
- S3 圖片保留；管理員需自行負責清理未使用的圖片。
- 刪除廣告**不會**對連結的店家產生任何影響。

---

## Controller 對應

```java
@DeleteMapping("/{id}")
@ResponseStatus(HttpStatus.NO_CONTENT)
public ResponseEntity<Void> deleteBanner(@PathVariable String id) { ... }
```
