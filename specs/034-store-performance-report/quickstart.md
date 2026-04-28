# Quickstart: 034 - 店家績效比較報表

**Branch**: `034-store-performance-report`

---

## 功能概覽

提供 Admin 跨店家績效 KPI 比較，以及 StoreOwner 查看自己店家詳細趨勢的 API。

| 角色 | 功能 |
|------|------|
| Admin | 查全部店家，依任意 KPI 欄位排序 |
| Admin + storeId | 查單一店家 + dailyStats 趨勢 |
| StoreOwner | 只能查自己的店 + dailyStats 趨勢 |

---

## 快速上手

### 1. 查詢全部店家績效（Admin）

```http
POST /admin/report/store-performance
Authorization: Bearer {adminToken}
Content-Type: application/json

{
  "condition": {
    "startDate": "2026-04-01",
    "endDate": "2026-04-30"
  },
  "sortBy": "totalRevenue",
  "sortOrder": "DESC"
}
```

**回傳**: `stores` 陣列（所有店），`dailyStats: null`。

---

### 2. 查詢單一店家詳細績效（Admin 帶 storeId）

```http
POST /admin/report/store-performance
Authorization: Bearer {adminToken}
Content-Type: application/json

{
  "condition": {
    "storeId": "store-uuid-a",
    "startDate": "2026-04-01",
    "endDate": "2026-04-30"
  }
}
```

**回傳**: 單一店家的 `stores[]`，且 `dailyStats` 非 null（包含每日 drawCount / revenue / newUsers）。

---

### 3. StoreOwner 查看自己店家

```http
POST /admin/report/store-performance
Authorization: Bearer {storeOwnerToken}
Content-Type: application/json

{
  "condition": {
    "startDate": "2026-04-01",
    "endDate": "2026-04-30"
  }
}
```

**行為**: 後端自動將 `storeId` 設為 StoreOwner 綁定的店家，回傳含 `dailyStats` 的單店報表。  
**403 情境**: 若 `condition.storeId` 帶入他店的 ID，後端回傳 403。

---

## 欄位說明（KPI）

| 欄位 | 說明 | 029 依賴 |
|------|------|---------|
| `totalRevenue` | 期間抽獎消費總點數（wallet_transaction DRAW，取絕對值） | 否 |
| `drawCount` | 期間抽籤總數（lottery_ticket DRAWN） | 否 |
| `activeUsers` | 期間不重複活躍用戶（抽獎 + 訂單 UNION） | 否 |
| `shipRate` | (SHIPPED+COMPLETED) / 非CANCELLED × 100，單位 %；無訂單時 null | 否 |
| `overdueRate` | PENDING 超 7 天 / 全部訂單 × 100，單位 %；無訂單時 null | 否 |
| `avgShipDays` | 平均備貨到出貨天數；**029 合併前恆為 null** | ✅ 是 |

---

## 實作檔案清單

| 動作 | 檔案 |
|------|------|
| NEW | `condition/report/StorePerformanceCondition.java` |
| NEW | `dto/res/report/StorePerformanceReportRes.java` |
| MODIFY | `service/ReportService.java` — 新增 `getStorePerformanceReport()` |
| MODIFY | `service/impl/ReportServiceImpl.java` — JdbcTemplate SQL 實作 |
| MODIFY | `controller/admin/AdminReportController.java` — 新增 `POST /store-performance` |
| MODIFY | `controller/AdminReportControllerTest.java` — 新增測試案例 |

---

## 注意事項

- **029 依賴**: `avgShipDays` 等 029 合併後，移除 `null` hardcode，改為查詢 `DATEDIFF(shipped_at, preparing_at)`。
- **sortBy 安全**: 非白名單值 silently fallback 為 `totalRevenue`，不回傳錯誤。
- **效能**: 跨所有店聚合目標 < 5 秒；若店家數或訂單數增長，可考慮加索引 `(store_id, created_at)` 及 `(lottery_id, status, drawn_at)`。
- **資料一致性**: `stores[].totalRevenue` 加總應與 033 平台報表 `totalSpend` 一致（SC-003）。
