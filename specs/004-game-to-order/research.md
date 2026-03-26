# 研究：遊戲至訂單流程（Game-to-Order Flow）

**功能**：`004-game-to-order`  
**日期**：2026-03-22  
**狀態**：已完成 — 所有未知問題均已解決

---

## 1. 術語對齊：`AVAILABLE` vs `IN_BOX`

**決策**：全面使用 `IN_BOX` — 包含程式碼、合約、任務清單以及所有新文件。

**理由**：功能規格在檢視程式碼庫之前撰寫，使用了抽象術語 `AVAILABLE`。現有程式碼將 `PrizeBoxStatusEnum.IN_BOX` 定義為獎品存放於玩家獎品盒時的初始狀態。將 enum 重命名為 `AVAILABLE` 需要資料庫遷移、重新生成 mapper，以及對 `PrizeBoxServiceImpl`、`OrderServiceImpl` 和 MyBatis XML 檔案進行大範圍修改，且毫無功能上的增益。

**已考慮的替代方案**：新增 `AVAILABLE` 別名 enum 成員（已拒絕 — 會造成程式碼模型混亂）；僅更新 spec 的描述語言（已採用 — 零風險）。

**供 spec 讀者參照的對映表**：

| Spec 術語 | 程式碼 enum | DB 值 | 含義 |
|-----------|-----------|----------|---------|
| `AVAILABLE` | `PrizeBoxStatusEnum.IN_BOX` | `"IN_BOX"` | 獎品在盒中，已準備好出貨 |
| `SHIPPED` | `PrizeBoxStatusEnum.SHIPPED` | `"SHIPPED"` | 獎品已透過訂單出貨 |
| `RECYCLED` | `PrizeBoxStatusEnum.RECYCLED` | `"RECYCLED"` | 獎品已兌換為積分 |

---

## 2. 抽獎 → PrizeBox 整合

**決策**：抽獎服務已在每次成功抽獎後自動插入一筆 `PrizeBox` 記錄（狀態 = `IN_BOX`）。FR-001 無需新增任何程式碼。

**依據**：`LotteryTicketServiceImpl` 在成功將抽獎結果記錄至 `LotteryDrawRecord` 後，立即呼叫 `PrizeBoxService.addToPrizeBox(userId, lotteryId, prizeId, storeId, recycleBonus)`。`PrizeBox` 實體透過 `drawResultId` 作為外鍵連結回 `LotteryDrawRecord`。

**理由**：此路徑已可在生產環境使用。本功能的實作任務是撰寫整合測試，確認端對端的連接（抽獎 → 插入正確 `userId`、`storeId`、status = `IN_BOX` 的 prize-box 記錄）。

---

## 3. 多店家訂單分單

**決策**：`PrizeBoxServiceImpl.shipPrizes()` 已依 `storeId` 分組所選的 prize-box ID，並對每個店家呼叫一次 `OrderService.createOrdersFromPrizeBox()`。FR-004 無需新增任何程式碼。

**理由**：分組是在進入訂單建立迴圈之前，使用 Java 的 `Collectors.groupingBy(PrizeBox::getStoreId)` 完成的。每個店家的子集被包裝在各自的 `@Transactional` 訂單建立呼叫中，因此某一店家的訂單建立失敗不會導致其他店家的訂單處於中間狀態（外層的 `@Transactional` on `shipPrizes` 會回滾所有操作）。

**最佳實踐確認**：這是多租戶訂單分單的業界標準做法 — 在 service 層分組、依商家/店家建立獨立的 Order 記錄、回傳所有已建立的訂單 ID 清單。

---

## 4. 訂單取消 → 獎品返回 IN_BOX

**決策**：當訂單被取消時，所有關聯的 `PrizeBox` 項目必須重置為 `IN_BOX` 狀態。此邏輯**必須在 `OrderServiceImpl.cancelOrder()` 中確認**。

**理由（來自 2026-03-22 釐清）**：產品決策是取消後的獎品返回玩家的獎品盒（status = `IN_BOX`），以便可以重新出貨。不退還積分；運費視為已消耗。這符合 FR-007（不刪除獎品盒）並與 `IN_BOX → SHIPPED → IN_BOX` 的可逆狀態機一致。

**實作模式**：
```java
// In OrderServiceImpl.cancelOrder():
// 1. Find all OrderItems linked to this order
// 2. For each OrderItem, fetch the PrizeBox by prizeBoxId
// 3. Reset prizeBox.status = IN_BOX, prizeBox.orderId = null, prizeBox.shippedAt = null
// 4. Update prizeBox in DB
// 5. Update order.status = CANCELLED, order.cancelledAt, order.cancelledBy, order.cancelReason
// All within @Transactional
```

**風險**：若目前的實作缺少取消返回邏輯，則為缺口，必須作為實作任務新增。

---

## 5. 獎品盒清單：狀態篩選

**決策**：目前的 `GET /prize-box` 端點永遠只回傳 `IN_BOX` 的項目。在 v1.0 中，這是可接受的。若有需要，可新增可選的 `status` 查詢參數，但延後實作。

**理由**：主要使用案例（玩家在出貨前查看獎品盒）只需要 `IN_BOX` 的項目。一旦出貨，項目改在訂單頁面顯示。新增狀態篩選屬於低優先度的增強功能。spec 的驗收條件確認了這點：「已出貨的獎品顯示為 SHIPPED，不再顯示為 AVAILABLE [IN_BOX]」— 意即清單直接停止顯示它們；不需要篩選「顯示 SHIPPED」的視圖。

**未來增強**：新增 `?status=SHIPPED` 查詢參數以支援「透過獎品盒查看訂單歷史」的視圖。

---

## 6. 使用者訂單清單端點：GET vs POST

**決策**：保留現有的 `POST /order/list` 端點，**不予替換**。可選地在後續任務中新增 `GET /api/orders` 便利別名。

**理由**：現有的 `POST /order/list` 接受 `OrderCondition` 請求體，支援豐富的篩選功能（依狀態、日期區間、店家）。純 `GET /api/orders` 加查詢參數的方式較不靈活，且需要複製篩選邏輯。spec 提議的 `GET /api/orders` 路徑在合約中記錄為**建議新增的別名**，但本功能僅靠現有的 POST 端點即可在功能上完整。

**業界模式**：REST 純粹主義者偏好用 `GET` 來查詢；然而，在請求體中傳送複雜篩選物件（`POST /list`）是中文市場 B2C 後端中常見的務實做法，鑑於現有程式碼庫慣例，這在此處是可接受的。

---

## 7. PrizeBox 實體欄位對應規格

| Spec 欄位 | 實體欄位 | 備註 |
|-----------|-------------|-------|
| 獎品名稱 | `prizeName`（透過 join 查詢 → `PrizeBoxItemRes.prizeName`） | 存放於 res DTO |
| 獎品等級 | `prizeLevel` | `PrizeBoxItemRes.prizeLevel` |
| 獎品圖片 | `prizeImageUrl` | 完整 S3 URL |
| 來源店家 | `storeId`、`storeName` | `storeId` 存放於實體；`storeName` 在 service 中解析 |
| 抽獎記錄連結 | `drawResultId` | FK to `LotteryDrawRecord.id` |
| 訂單連結 | `orderId` | 狀態轉換為 `SHIPPED` 時設定 |

---

## 8. MyBatis 模式：自訂方法 vs MBG 生成方法

**決策**：所有狀態轉換使用 `updateByPrimaryKeySelective()`（MBG 生成）。僅在需要批次更新以提升效能時，才新增自訂 mapper 方法。

**理由**：`shipPrizes()` 目前在迴圈中逐一更新每筆 `PrizeBox` 記錄。在預期規模（每次出貨請求少於 100 筆項目）下，這是可接受的。若效能分析顯示 N+1 開銷，可新增批次更新 XML 方法（`updateStatusByIds`）。

**最佳實踐參考**：MyBatis Generator 生成的 `*Selective` 方法只設定非 null 欄位，防止意外覆寫不相關的欄位 — 這對狀態轉換而言是正確的做法。

---

## 9. JWT 與安全性：無需修改

**決策**：所有新的 prize-box 和 order 端點位於 `/api/**` 下，已受 `ApiJwtAuthenticationFilter` 保護。使用者 ID 透過 `SecurityUtils.getCurrentUserId()` 取得。

**已確認的認證流程**：
1. 客戶端傳送 `Authorization: Bearer <jwt>`
2. `ApiJwtAuthenticationFilter` 驗證 token，在 `SecurityContext` 中設定 `UserPrincipal`
3. Controller 呼叫 `SecurityUtils.getCurrentUserId()` — 回傳 UUID 字串
4. Service 拒絕 `prizeBoxId.userId ≠ currentUserId` 的請求

---

## 10. 測試策略

**決策**：整合測試使用 JUnit 5 + `@SpringBootTest`；service 層單元測試使用 Mockito。

**從驗收條件衍生的關鍵測試場景**：

| 測試 | 類型 | 驗證項目 |
|------|------|-----------|
| 抽獎後建立狀態為 IN_BOX 的 PrizeBox | 整合測試 | FR-001, SC-001 |
| 出貨後每個店家各建立一筆 Order | 整合測試 | FR-004, SC-004 |
| 出貨後 PrizeBox 從 IN_BOX 轉換為 SHIPPED | 整合測試 | FR-005, SC-003 |
| 訂單取消後 PrizeBox 返回 IN_BOX | 整合測試 | 釐清 2026-03-22 |
| 使用錯誤的 userId 出貨 → 403 | 單元測試 | 安全邊界 |
| 出貨已 SHIPPED 的獎品 → 400 | 單元測試 | FR-011 |
| 多店家出貨 → 回傳多筆訂單 | 整合測試 | FR-004 |

---

## 未知問題：全數已解決

| 未知問題 | 解決方案 |
|---------|-----------|
| `AVAILABLE` vs `IN_BOX` 術語 | 使用 `IN_BOX`；合約已更新 |
| 取消 → 獎品返回邏輯 | 必須在 `OrderServiceImpl` 中確認；模式已定義於上方 |
| 訂單清單端點模式 | 保留 `POST /order/list`；GET 別名為可選增強 |
| 獎品盒狀態篩選 | 延後至未來增強；v1.0 只回傳 IN_BOX |
| 抽獎 → 獎品盒連接 | 已在 `LotteryTicketServiceImpl` 中實作 |
| 多店家訂單分單 | 已在 `PrizeBoxServiceImpl` 中實作 |
