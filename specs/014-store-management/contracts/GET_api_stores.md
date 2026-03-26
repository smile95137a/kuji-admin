# 合約：GET /api/stores

**用途**：前台玩家公開店家列表。僅返回 ENABLED 的店家，包含卡片檢視欄位（Logo、名稱、簡短描述）。  
**認證**：無（公開 — `permitAll()`）  
**路由**：`GET /api/stores`

---

## 請求

### 查詢參數（均為選填）

| 參數 | 型別 | 預設值 | 說明 |
|-------|------|---------|-------------|
| `page` | int | 1 | 頁碼（從 1 開始） |
| `size` | int | 20 | 每頁筆數（最多 100） |

### Headers
```
(無需認證)
```

### 範例
```
GET /api/stores
GET /api/stores?page=1&size=20
```

---

## 回應

### 200 OK
```json
{
  "items": [
    {
      "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "storeName": "甜甜圈抽獎屋",
      "shortDescription": "各式甜甜圈主題抽獎",
      "logoUrl": "https://cdn.example.com/stores/logo-abc.png"
    },
    {
      "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
      "storeName": "星球抽獎",
      "shortDescription": "宇宙級別的抽獎體驗",
      "logoUrl": null
    }
  ],
  "total": 42,
  "page": 1,
  "size": 20
}
```

> `logoUrl` 可能為 `null`。前端在 `null` 或 URL 返回 404 時必須顯示預設占位圖片。

### 空結果
```json
{
  "items": [],
  "total": 0,
  "page": 1,
  "size": 20
}
```

---

## 商業規則

1. **只返回 `ENABLED` 的店家** — `WHERE status = 'ENABLED'`。
2. `DISABLED` 的店家完全不可見（不返回，也不計入 `total`）。
3. `longDescription`、`coverImageUrl`、`email`、`phone`、`address`、`businessHours`、社群連結**不包含**在此端點（請使用 `GET /api/stores/{id}` 取得完整詳情）。
4. 預設排序：`created_at DESC`（最新店家優先）。

---

## Controller 實作備注

```java
// StoreController.java (api package)
@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "前台店家", description = "前台公開店家 API")
public class StoreController {

    @GetMapping
    @Operation(summary = "取得店家列表", description = "只回傳已啟用店家的卡片資訊")
    public ResponseEntity<PageResult<StoreListItemRes>> listStores(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(storeService.listEnabledStores(page, size));
    }
}
```

---

## 效能

- `(status)` 索引確保快速篩選。
- 排除 BLOB 欄位（`longDescription`、`remark`）— 使用 `Base_Column_List` SQL 片段。
- 目標：500 間店家在 < 2 秒內載入完成（SC-003）。
