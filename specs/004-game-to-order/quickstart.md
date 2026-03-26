# 快速入門：遊戲至訂單流程（Game-to-Order Flow）

**功能**：`004-game-to-order`  
**分支**：`004-game-to-order`  
**日期**：2026-03-22

---

## 概覽

本指南說明 KUJI Server 後端中「抽獎 → PrizeBox → 訂單」出貨流程的運作方式，以及如何在本機執行、測試和擴充。

---

## 前置條件

| 工具 | 版本 | 用途 |
|------|---------|---------|
| Java | 21 | 執行環境 |
| Maven | 3.9+ | 建構工具 |
| MySQL | 8.3 | 資料庫 |
| AWS credentials | — | S3 圖片存取（本機可選） |

---

## 1. 本機啟動伺服器

```bash
cd C:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin

# Start with local profile
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

或使用現有的腳本：

```bat
start.sh   # Linux / WSL
start-test.bat   # Windows
```

伺服器啟動於 **8080** 埠（或依 `application-local.yml` 中的設定）。

---

## 2. 取得認證（JWT Token）

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "player@example.com", "password": "yourpassword"}'
```

從回應中儲存 `token` — 在後續所有請求中以 `Authorization: Bearer <token>` 傳送。

---

## 3. 模擬完整流程

### 步驟 1 — 抽獎

```bash
curl -X POST http://localhost:8080/api/lottery/draw/{lotteryId} \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json"
```

系統會自動建立一筆 `status = IN_BOX` 的 `PrizeBox` 記錄。

---

### 步驟 2 — 查看獎品盒

```bash
curl -X GET http://localhost:8080/api/prize-box \
  -H "Authorization: Bearer <token>"
```

**預期回應**（縮略）：
```json
{
  "code": 200,
  "data": [
    {
      "id": "prize-box-uuid",
      "prizeName": "A賞 — 特製抱枕",
      "status": "IN_BOX",
      "storeName": "台北旗艦店"
    }
  ]
}
```

---

### 步驟 3 — 出貨獎品（建立訂單）

```bash
curl -X POST http://localhost:8080/api/prize-box/ship \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "prizeBoxIds": ["prize-box-uuid"],
    "shippingMethod": "HOME_DELIVERY",
    "recipientName": "王小明",
    "recipientPhone": "0912345678",
    "recipientAddress": "台北市信義區市府路1號"
  }'
```

**預期回應**：
```json
{
  "code": 200,
  "message": "出貨成功",
  "data": ["order-uuid-001"]
}
```

該獎品盒項目的 `status` 現在為 `SHIPPED`。`Order` 及其 `OrderItem` 記錄已建立。

---

### 步驟 4 — 查看訂單清單

```bash
curl -X POST http://localhost:8080/api/order/list \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{}'
```

---

### 步驟 5 — 查看訂單詳情

```bash
curl -X GET http://localhost:8080/api/order/{orderId} \
  -H "Authorization: Bearer <token>"
```

---

## 4. 多店家訂單分單測試

測試來自不同店家的兩件獎品是否建立兩筆獨立訂單：

1. 從**不同店家**各抽一次彩券 — 取得兩個 `prizeBoxId`
2. 呼叫 `POST /api/prize-box/ship`，在 `prizeBoxIds` 中傳入**兩個** ID
3. 回應的 `data` 陣列將包含**兩個訂單 ID**（每個店家各一筆）

---

## 5. 取消訂單 → 獎品返回獎品盒

```bash
# Cancel an order (must be in PENDING status)
curl -X POST http://localhost:8080/api/order/{orderId}/cancel \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"cancelReason": "改變心意"}'
```

取消後：
- `Order.status` = `CANCELLED`
- 所有關聯的 `PrizeBox` 項目返回 `status = IN_BOX`
- 不退還錢包積分

---

## 6. 關鍵原始碼檔案

| 檔案 | 說明 |
|------|-------------|
| `src/main/java/com/group/admin/controller/api/PrizeBoxController.java` | 獎品盒 REST controller |
| `src/main/java/com/group/admin/controller/api/OrderController.java` | 訂單 REST controller |
| `src/main/java/com/group/admin/service/impl/PrizeBoxServiceImpl.java` | 出貨邏輯、店家分單、狀態轉換 |
| `src/main/java/com/group/admin/service/impl/OrderServiceImpl.java` | 訂單建立/取消、取消後獎品返回 |
| `src/main/java/com/group/admin/entity/PrizeBox.java` | PrizeBox 實體 |
| `src/main/java/com/group/admin/entity/Order.java` | Order 實體 |
| `src/main/java/com/group/admin/entity/OrderItem.java` | OrderItem 實體（連結 PrizeBox ↔ Order） |
| `src/main/java/com/group/admin/enums/PrizeBoxStatusEnum.java` | `IN_BOX` / `SHIPPED` / `RECYCLED` |
| `src/main/java/com/group/admin/enums/OrderStatusEnum.java` | `PENDING` / `PREPARING` / `SHIPPED` / `COMPLETED` / `CANCELLED` |
| `src/main/resources/mapper/PrizeBoxMapper.xml` | PrizeBox 查詢的 MyBatis XML |
| `src/main/resources/mapper/OrderMapper.xml` | Order 查詢的 MyBatis XML |

---

## 7. 執行測試

```bash
# Run all tests
mvn test

# Run a specific test class
mvn test -Dtest=PrizeBoxServiceTest

# Run with local profile
mvn test -Dspring.profiles.active=local
```

需要查找或建立的關鍵測試類別：

| 測試類別 | 覆蓋範圍 |
|-----------|---------|
| `PrizeBoxServiceTest` | `shipPrizes()` 的單元測試、擁有者驗證、狀態檢查 |
| `OrderServiceTest` | `cancelOrder()` 獎品返回邏輯的單元測試 |
| `PrizeBoxFlowIntegrationTest` | 端對端：抽獎 → IN_BOX → SHIPPED → 訂單建立 |
| `OrderCancelIntegrationTest` | 取消 → 獎品返回 IN_BOX |

---

## 8. 資料庫結構驗證

```sql
-- Check prize box items for a user
SELECT id, user_id, prize_id, store_id, status, order_id, created_at
FROM prize_box
WHERE user_id = 'your-user-uuid'
ORDER BY created_at DESC;

-- Check orders for a user
SELECT id, order_number, store_id, status, total_items, created_at
FROM `order`
WHERE user_id = 'your-user-uuid'
ORDER BY created_at DESC;

-- Check order items for an order
SELECT oi.id, oi.prize_name, oi.prize_level, pb.status AS prize_box_status
FROM order_item oi
JOIN prize_box pb ON oi.prize_box_id = pb.id
WHERE oi.order_id = 'your-order-uuid';
```

---

## 9. 術語快速對照表

| Spec 術語 | 程式碼術語 | DB 值 | 含義 |
|-----------|-----------|----------|---------|
| `AVAILABLE` | `IN_BOX` | `"IN_BOX"` | 獎品在盒中，已準備出貨 |
| `SHIPPED` | `SHIPPED` | `"SHIPPED"` | 已連結至訂單 |
| `RECYCLED` | `RECYCLED` | `"RECYCLED"` | 已兌換為積分 |

---

## 10. 後續步驟

- 執行 `/speckit.tasks` 以產生實作任務清單（`tasks.md`）
- `tasks.md` 中需處理的關鍵缺口：
  1. **確認** `OrderServiceImpl.cancelOrder()` 是否重置 `PrizeBox.status → IN_BOX`
  2. **撰寫**整合測試：抽獎 → 獎品盒、出貨 → 訂單、取消 → 返回 IN_BOX
  3. **考慮**新增 `GET /api/orders` 別名端點（可選增強）
  4. **考慮**為 `GET /api/prize-box` 新增 `?status=` 篩選器（可選增強）
