# Research: 訂單管理 (Order Management)

**功能分支**：`008-order-management`  
**日期**：2026-03-22  
**階段**：0 — 前期設計研究與澄清解決

---

## 1. 狀態機設計

### 決策
四個操作狀態依嚴格線性順序排列，加上一個終止取消狀態：

```
PENDING → PREPARING → SHIPPED → COMPLETED
                    ↓
               CANCELLED  (only from PENDING or PREPARING)
```

### 理由
- `PENDING` → 已建立，等待店家操作  
- `PREPARING` → 店家正在實體揀貨／包裝商品  
- `SHIPPED` → 商品已交付物流業者；**不可逆點**  
- `COMPLETED` → 玩家確認收貨（或 N 天後自動完成）  
- `CANCELLED` → 終止狀態；僅允許在 `SHIPPED` 之前

`OrderStatusEnum` 已定義全部五個值。轉換規則為：`nextOrdinal == currentOrdinal + 1`（僅向前），取消為側分支。

### 已考慮的替代方案
- 允許管理員逆轉狀態以處理爭議 → **已拒絕**，依據 FR-003 及 FR-005；規格明確規定不可逆。
- 新增 `RETURN_REQUESTED` 狀態 → **已拒絕**，依據假設「平台不處理退款、退貨」。

---

## 2. 獎品盒扣除的原子性

### 決策
`OrderServiceImpl.createOrdersFromPrizeBox()` 必須在單一 `@Transactional` 方法內執行。步驟：

1. 載入所有請求的 `PrizeBox` 記錄並驗證：status = `IN_BOX`，storeId 未停用。
2. 依 `storeId` 分組 → 每家店建立一筆 `Order`。
3. 對每個分組：
   a. `INSERT` 至 `order`  
   b. `INSERT` 至 `order_item`（每個獎品盒一行）  
   c. 對每個項目執行 `UPDATE prize_box SET status='SHIPPED', order_id=?, shipped_at=NOW()`
4. 任何步驟失敗 → 完整回滾（Spring 對 unchecked exception 的預設行為）。

### 理由
FR-011 要求原子性扣除。MySQL InnoDB + Spring `@Transactional` 可在 `RuntimeException` 時保證回滾。現有的 `PrizeBoxMapper.updateByPrimaryKeySelective()` 用於狀態更新。

### 已考慮的替代方案
- Saga／最終一致性 → 對於單一 DB 服務而言過度設計；已拒絕。

---

## 3. 店家隔離策略

### 決策
使用 `AdminLotteryController` 及 `AdminStoreController` 中已建立的 `SecurityUtils` + `store_user` 中介表模式。流程：

1. Controller 呼叫 `SecurityUtils.getCurrentUserId()` → `adminUserId`
2. 若角色為 `STORE_OWNER` 或 `STORE_EDITOR`：service join `store_user` 以解析 `storeId`，然後在所有查詢中附加 `AND store_id = ?`。
3. 若角色為 `ADMIN`：不套用店家篩選（可查看全部）。

`OrderCondition.storeId` 欄位將已解析的店家 ID 傳遞至查詢層。

### 理由
JWT 中不含 storeId；現有程式碼庫透過 DB 解析。與 `AdminLotteryController` 模式一致，避免客製化 token claim。

### 已考慮的替代方案
- 將 storeId 嵌入 JWT → 重新指派店家時需重新簽發 token；已拒絕。
- MySQL 的列層級安全性 → 應用層配置不支援；已拒絕。

---

## 4. 訂單編號產生

### 決策
`ORD-{yyyyMMddHHmmss}-{6位隨機英數字}` 格式，在 `OrderServiceImpl.generateOrderNumber()` 中產生。範例：`ORD-20260322143000-AB1C2D`。

### 理由
- 對客服人員具可讀性
- 顯示用途的唯一性足夠（UUID `id` 才是真正的 PK）
- 與現有慣例一致（抽獎結果使用類似的帶時間戳代碼）

### 已考慮的替代方案
- 純 UUID → 對支援人員不易閱讀
- 資料庫序列 → MySQL 無原生支援，需額外資料表

---

## 5. 取消流程與獎品退還

### 決策
取消時（status 為 `PENDING` 或 `PREPARING`）：

1. 驗證呼叫者：僅限 `ADMIN`、`STORE_OWNER` 或 `STORE_EDITOR`；玩家不能取消（FR-004）。
2. 驗證 status 不為 `SHIPPED` 或 `COMPLETED`（FR-005）。
3. `UPDATE order SET status='CANCELLED', cancelled_at=NOW(), cancelled_by=?, cancel_reason=?`
4. `UPDATE prize_box SET status='IN_BOX', order_id=NULL, shipped_at=NULL WHERE order_id=?`  
   — 將獎品恢復為 `AVAILABLE`（在 `PrizeBoxStatusEnum` 中以 `IN_BOX` 表示）。
5. `INSERT order_status_log` 含操作者資訊。
6. 全部在 `@Transactional` 內執行。

**不退還點數** — 點數在抽獎時已消耗（FR-004a）。

### 理由
規格澄清（2026-03-22）：「獎品歸還獎品盒（AVAILABLE），點數不退.」獎品盒 status `IN_BOX` = 「可出貨」。

---

## 6. 狀態更新的冪等性

### 決策
推進狀態前，檢查 `current_status == expected_from_status`。若請求的轉換為無操作（相同狀態），回傳 HTTP 200 及當前狀態（冪等成功）。若轉換無效（例如嘗試後退），回傳 HTTP 409 Conflict。

### 理由
規格邊界案例：「相同訂單狀態更新被提交兩次時會如何？系統應具冪等性。」這可防止前端重試導致的重複推進錯誤。

---

## 7. 已停用店家的邊界案例

### 決策
建立訂單時，在插入前對每個目標店家驗證 `store.status == 'ACTIVE'`。若任何目標店家為非啟用狀態，以 HTTP 422 及明確錯誤訊息拒絕整個請求。

### 理由
規格邊界：「玩家嘗試為已停用店家的獎品出貨時，系統應阻止。」已存在的獎品仍然有效；只有新訂單被阻擋。

---

## 8. 管理員跨店查詢

### 決策
`GET /admin/orders` 接受可選的 `storeId` query 參數。當偵測到 `ADMIN` 角色時（透過 `SecurityUtils.isAdmin()`），除非提供了 `storeId` 參數，否則不套用店家篩選。當角色為 `STORE_OWNER/EDITOR` 時，該參數**被忽略**，始終使用呼叫者自己的店家。

### 理由
FR-010：管理員可稽核所有店家。FR-006：店家負責人隔離為強制要求。

---

## 9. 分頁

### 決策
所有列表 endpoints 接受 `QueryReq<OrderCondition>`（與現有模式一致）。預設分頁大小：20。最大：100。預設以 `created_at DESC` 排序。

`QueryReq` 在程式碼庫中已存在，包含 `page`、`pageSize`、`sortBy`、`sortDirection` 欄位。

---

## 10. 已解決的澄清項目

| # | 問題 | 答案 |
|---|------|------|
| 1 | 取消：獎品如何處理？ | 退回獎品盒（`IN_BOX` = AVAILABLE） |
| 2 | 取消：點數如何處理？ | 不退還——點數在抽獎時已消耗 |
| 3 | 玩家可以取消嗎？ | 不行——僅限 ADMIN、STORE_OWNER、STORE_EDITOR |
| 4 | 已停用店家的出貨？ | 阻擋新訂單；現有獎品仍然有效 |
| 5 | 重複狀態更新？ | 冪等：相同狀態回傳 200；無效轉換回傳 409 |
| 6 | 一筆訂單跨多家店的獎品？ | 系統自動依店家拆單 |
