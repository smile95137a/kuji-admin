# 合約：GET /admin/stores

**用途**：列出所有店家（ENABLED 與 DISABLED 均包含），供管理員管理使用。支援篩選與排序。  
**認證**：`ROLE_ADMIN`、`ROLE_STORE_OWNER`、`ROLE_STORE_EDITOR`  
**路由**：`POST /admin/stores/list`（遵循現有程式庫慣例：列表 + 篩選使用 POST 帶 Body）

> **慣例備注**：本專案對篩選列表端點使用 `POST /admin/{resource}/list`（而非帶查詢參數的 `GET`），與現有的 `AdminLotteryController`、`AdminOrderController` 等一致。

---

## 請求

### Headers
```
Authorization: Bearer <jwt>
Content-Type: application/json
```

### Body
```json
{
  "condition": {
    "storeName": "甜甜圈",
    "status": "ENABLED"
  },
  "sortBy": "createdAt",
  "sortOrder": "DESC",
  "page": 1,
  "size": 20
}
```

**所有欄位均為選填。** 空 Body `{}` 返回呼叫者有權限存取的所有店家。

### 條件欄位
| 欄位 | 型別 | 說明 |
|-------|------|-------------|
| `storeName` | String | 模糊比對（`LIKE %value%`） |
| `status` | String | `ENABLED` \| `DISABLED` \| null（全部） |

### 排序
| 欄位 | 預設值 |
|-------|---------|
| `sortBy` | `createdAt` |
| `sortOrder` | `DESC` |

### 依角色自動篩選（服務層強制執行）
- `ADMIN`：可看到**所有**店家。
- `STORE_OWNER` / `STORE_EDITOR`：只能看到**自己的店家**（透過 `store_user` 對應關係）。

---

## 回應

### 200 OK
```json
[
  {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "storeName": "甜甜圈抽獎屋",
    "shortDescription": "各式甜甜圈主題抽獎",
    "logoUrl": "https://cdn.example.com/stores/logo-abc.png",
    "status": "ENABLED",
    "ownerId": "f1e2d3c4-b5a6-9870-fedc-ba9876543210",
    "ownerDisplayName": "甜甜圈店長",
    "createdAt": "2026-03-22T10:30:00",
    "updatedAt": "2026-03-22T11:00:00"
  },
  {
    "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "storeName": "星球抽獎",
    "shortDescription": null,
    "logoUrl": null,
    "status": "DISABLED",
    "ownerId": "c3d4e5f6-a7b8-9012-cdef-123456789012",
    "ownerDisplayName": "星球店長",
    "createdAt": "2026-01-10T08:00:00",
    "updatedAt": "2026-02-15T14:00:00"
  }
]
```

> 若無符合條件的店家，或呼叫者無任何店家存取權，則返回 `[]`。

### 401 / 403 — 認證失敗
標準 JWT 錯誤回應。

---

## 備注

- `longDescription` 與 `remark`（BLOB 欄位）**不包含**在列表回應中，以提升效能。
- 完整店家詳情（含 BLOB）僅由 `GET /admin/stores/{id}` 返回（未來端點）。
- v1 不使用分頁包裝 — 返回完整列表。請求 Body 中的分頁參數保留供未來使用，v1 中忽略。
