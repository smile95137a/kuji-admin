# 研究：物流與出貨管理 (Express Shipping)

**功能分支**：`002-express-shipping`  
**產生日期**：2026-03-22  
**階段**：0 — 未知項目解析

---

## 1. 現有程式碼審查 — 已建置項目

### 決策
以現有的 `Order` 實體、列舉、服務方法及控制器作為穩定基礎。**不**重新設計資料模型或狀態機。

### 理由
對 `src/main/java/com/group/admin/` 的完整審查確認以下項目已可投入生產：

| 元件 | 狀態 | 備註 |
|-----------|--------|-------|
| `Order` 實體 | ✅ 完整 | 22 個欄位，包含所有收件人 / 超商 / 物流追蹤欄位 |
| `OrderStatusEnum` | ✅ 完整 | PENDING → PREPARING → SHIPPED → COMPLETED / CANCELLED，含 `isCancellable()` 與 `isFinished()` 守衛 |
| `ShippingMethodEnum` | ✅ 完整 | HOME_DELIVERY、SEVEN_ELEVEN、FAMILY_MART，含 `fromCode()` 驗證 |
| `OrderStatusLog` 實體 + mapper | ✅ 完整 | 每次狀態轉換的稽核追蹤 |
| `OrderService` + `OrderServiceImpl` | ✅ 完整 | `prepareShipping()`、`ship()`、`complete()`、`cancel()` |
| `AdminOrderController` | ✅ 完整 | 清單、詳情、備貨、出貨、完成、取消端點 |
| `OrderController`（玩家端） | ✅ 部分完成 | 清單（POST /order/list）與詳情（GET /order/{id}）已存在；**玩家提交出貨資訊功能缺失** |
| `OrderShipReq` | ✅ 完整 | 管理員物流單號 DTO |
| `OrderCancelReq` | ✅ 完整 | 取消原因 DTO |
| `OrderRes` / `OrderDetailRes` | ✅ 完整 | 所有狀態與出貨欄位均已揭露 |

### 已考慮的替代方案
- **重新設計狀態機** — 已拒絕。現有 `OrderStatusEnum` 與規格完全吻合。
- **新增 `ShippingInfo` 表格** — 已拒絕。所有出貨欄位已存在於 `Order` 中；在目前規模下，獨立表格需要 JOIN 但不帶來額外效益。

---

## 2. 缺口：玩家提交出貨資訊端點

### 決策
在 `OrderController` 新增 `POST /order/{orderId}/shipping-info`，並在 `OrderService` 新增 `submitShippingInfo()` 方法。

### 理由
規格（US1）要求玩家選擇出貨方式並填寫收件人資訊。目前，出貨資訊是在建立訂單時透過 `PrizeBoxService.createOrdersFromPrizeBox()` 擷取。然而：

1. 獎品箱建立流程接受參數，但玩家若留空或希望在訂單確認 / 備貨**前**修正資訊，目前沒有辦法**更新**出貨資訊。
2. FR-005 說明：「玩家只能在訂單送出前設定出貨資訊；送出後不可變更。」— 這暗示存在明確的「鎖定」時機。新端點透過僅接受訂單處於 `PENDING` 狀態時的更新來強制執行此規則。
3. `OrderController` 目前只有玩家的讀取端點；新增提交端點填補了 US1 的缺口。

### 新 DTO：`ShipInfoReq`
```java
@Data
public class ShipInfoReq {
    @NotBlank  private String shippingMethod;    // HOME_DELIVERY | SEVEN_ELEVEN | FAMILY_MART
    // Home delivery (required when shippingMethod = HOME_DELIVERY)
    private String recipientName;
    private String recipientPhone;
    private String recipientAddress;
    // Convenience store (required when shippingMethod = SEVEN_ELEVEN | FAMILY_MART)
    private String storeCode;
    private String storeName;
    private String storeAddress;
    // Optional
    private String remark;
}
```

跨欄位驗證：若為 `HOME_DELIVERY` 則姓名 / 電話 / 地址必須非空白；若為超商取貨則 storeCode / storeName 必須非空白。以 `@Valid` + 自訂驗證器或明確的服務層檢查實作。

### 已考慮的替代方案
- **僅在建立訂單時要求出貨資訊** — 已拒絕。部分玩家可能希望中獎後再確認出貨（加入購物車模式）。新端點在狀態為 `PENDING` 時提供寬限期。
- **允許更新至 `PREPARING` 前** — 已拒絕。規格 FR-005 說明「送出前」；PENDING 為正確的截止點。

---

## 3. 管理員統一狀態端點 vs. 細粒度端點

### 決策
保留現有細粒度端點（`/prepare`、`/ship`、`/complete`、`/cancel`）。**不**新增接受任意目標狀態的單一 `PUT /admin/orders/{id}/status` 端點。

### 理由
現有的細粒度設計較為安全：
- 每個端點只攜帶所需的酬載（例如 `/ship` 需要 `trackingNo`；`/prepare` 與 `/complete` 不需要請求本體）。
- 單一通用 `PUT /admin/orders/{id}/status` 需要服務層驗證目標狀態是否符合允許的轉換 — 邏輯相同但 API 介面可發現性較低。
- 在 contracts 中記錄 `PUT /admin/orders/{id}/status` 的使用者指引，是指**概念上**的管理員狀態管理合約；實作對應至現有的細粒度端點。

### 已考慮的替代方案
- **單一通用狀態端點** — 已評估。稍微簡化前端整合，但擴大攻擊面（任意狀態注入）。拒絕採用，保留現有細粒度設計。

---

## 4. 玩家地址預填（FR-004）

### 決策
預填為**客戶端責任**，使用來自 `GET /user/me` 的資料。後端不自動填入出貨欄位。

### 理由
`User` 實體已儲存 `defaultAddress`、`addressDistrict`、`addressCity` 欄位（來自推薦碼 / 地址功能實作）。玩家 API 已透過 `/user/me` 揭露這些欄位。從現有端點在客戶端預填，比在 `ShipInfoReq` 處理中新增伺服器端預設值解析邏輯更為簡單。

FR-004 說明系統必須「預先填入作為預設值」— 這是 UI 層關注點（在表單欄位中顯示已儲存的資料）。後端驗證只關心提交的值是否有效且非空白。

### 已考慮的替代方案
- **後端在建立時自動填入 Order.recipientAddress** — 已拒絕。在 UserService 與 OrderService 之間造成隱性耦合；預填應為 UI 層行為。

---

## 5. 超商代碼驗證（規格邊緣案例）

### 決策
在 v1 中，超商代碼**不對外部 API 進行驗證**。`ShippingMethodEnum.fromCode()` 驗證出貨方式類型；storeCode / storeName / storeAddress 照原樣儲存（如規格所假設）。

### 理由
規格明確說明：「實際超商 API 串接（例如 7-11、全家）可延後處理；v1.0 只記錄資料。」— 真實超商 API 整合已延後。驗證限於：
- `shippingMethod` 必須為有效的 `ShippingMethodEnum` 代碼。
- 若為超商取貨方式，`storeCode` 與 `storeName` 必須非空白。

### 已考慮的替代方案
- **靜態超商代碼查詢表** — 已拒絕。無可用來源資料；依規格延後處理。

---

## 6. 取消端點：DELETE vs. PUT（FR-007、FR-008）

### 決策
保留 `PUT /admin/orders/{orderId}/cancel`（現有）。使用者指引將 `DELETE /admin/orders/{id}` 作為概念模式；實作使用 PUT 以攜帶 `cancelReason` 請求本體。

### 理由
HTTP `DELETE` 習慣上不攜帶請求本體。`OrderCancelReq` DTO 需要非空白的 `reason` 欄位。因此 `PUT` 是「將訂單狀態更新為 CANCELLED 並附上原因」的正確 HTTP 方法。

規格只要求取消操作限制為 ADMIN（FR-008）且在 SHIPPED 前（FR-007）— 兩者均已透過 `@PreAuthorize("hasRole('ADMIN')")` 與服務層中的 `isCancellable()` 強制執行。

---

## 7. 測試策略

### 決策
對 `OrderController` 與 `AdminOrderController` 使用 **MockMvc 控制器切片測試**，並對 `OrderServiceImpl` 的狀態機轉換使用 **Mockito 單元測試**。

### 理由
- 兩個測試類別均已存在但為空（`OrderControllerTest`、`AdminOrderControllerTest`）。
- 控制器切片測試（`@WebMvcTest`）在不需要完整應用程式上下文的情況下，能快速回饋序列化、驗證與認證結果。
- 使用 `@ExtendWith(MockitoExtension.class)` 的服務單元測試，覆蓋所有狀態機路徑（有效轉換、非法逆向轉換、出貨後取消拒絕）。

### 關鍵測試情境
| 測試 | 類型 | 驗證項目 |
|------|------|-----------|
| PENDING 狀態下提交出貨資訊 → 200 | 控制器 | US1 正常路徑 |
| PREPARING 狀態下提交出貨資訊 → 400 | 控制器 | FR-005 鎖定 |
| HOME_DELIVERY 缺少 recipientName → 400 | 控制器 | 驗證 |
| PENDING → PREPARING → SHIPPED → COMPLETED | 服務單元 | 狀態機正向路徑 |
| SHIPPED → PENDING（逆向）→ 400 | 服務單元 | FR-006 單向限制 |
| 取消 PENDING 訂單 | 服務單元 | FR-007 正常路徑 |
| 取消 SHIPPED 訂單 → 400 | 服務單元 | FR-007 守衛 |
| 玩家無法取消 | 控制器 | FR-008 角色守衛 |

---

## 已解析未知項目摘要

| 未知項目 | 解析結果 |
|---------|-------------|
| 是否需要新的 `ShippingInfo` 表格？ | 否 — 所有欄位已存在於 `Order` 中 |
| 玩家如何提交出貨資訊？ | 新增 `POST /order/{orderId}/shipping-info` 端點 + `ShipInfoReq` DTO |
| 管理員應使用單一或細粒度狀態端點？ | 保留現有細粒度設計；概念上記錄為「狀態管理」 |
| 地址預填機制？ | 客戶端從 `GET /user/me` 資料填入 |
| 超商代碼驗證？ | 延後至 v2；v1 僅做非空白檢查 |
| 取消使用 HTTP DELETE 還是 PUT？ | PUT（需要請求本體傳遞原因） |
| 測試方式？ | MockMvc 切片測試 + Mockito 單元測試 |
