# 功能規格書：訂單物流與運費付款

功能分支：021-order-logistics  
建立日期：2026-04-13  
最後更新：2026-05-11  
狀態：已對齊目前後端實作

## 目標

1. 運送方式改由 shipping_method 表管理，不使用 hardcode。
2. 玩家建單後需先處理運費付款（GoMyPay），再進入履約流程。
3. 付款失敗要有明確狀態（PAYMENT_FAILED）並可重付款。

## 使用者情境

### 使用者故事 1 — 管理員管理運送方式（P1）

1. 管理員可新增/停用運送方式。
2. 前台只顯示 ACTIVE 的運送方式。

### 使用者故事 2 — 玩家建立訂單與付款（P1）

1. 玩家從賞品盒申請出貨後，系統建立訂單並返回 paymentUrl。
2. 訂單初始為 PAYMENT_PENDING。
3. GoMyPay callback 成功後，訂單進入 PENDING。

### 使用者故事 3 — 付款失敗重試（P1）

1. callback 失敗時，訂單進入 PAYMENT_FAILED。
2. 玩家可在 PAYMENT_FAILED 狀態重付款（/order/{id}/repay）。
3. 玩家也可在 PAYMENT_FAILED 直接取消訂單。

## 邊界條件

1. 付款 callback 重複通知時，不可破壞既有狀態。
2. 不可把已 SHIPPED 或 COMPLETED 的訂單改回未出貨狀態。
3. shipping-info 只允許 PAYMENT_PENDING 編輯。

## 功能需求

1. FR-001：shipping_method 由 DB 管理，前台讀取 ACTIVE 列表。
2. FR-002：建單時按店家拆單，每筆訂單獨立建立支付單。
3. FR-003：建單成功回傳 paymentUrl、gatewayTradeNo。
4. FR-004：callback success 將訂單轉為 PENDING，paymentStatus 轉為 PAID。
5. FR-005：callback failed 將訂單轉為 PAYMENT_FAILED，paymentStatus 轉為 FAILED。
6. FR-006：新增重付款 API，僅 PAYMENT_PENDING / PAYMENT_FAILED 可呼叫。
7. FR-007：重付款成功回傳新的 paymentUrl，供前端再次導轉。
8. FR-008：PAYMENT_FAILED 不解除 PrizeBox 綁定；只有取消時才回收 PrizeBox。
9. FR-009：取消後 PrizeBox 回 AVAILABLE（IN_BOX 僅舊資料相容）。

## 成功標準

1. SC-001：前台可正確顯示運送方式與對應運費。
2. SC-002：付款失敗案例可由玩家重付款完成，不需重建訂單。
3. SC-003：付款失敗與成功都可在狀態日誌追蹤。
4. SC-004：狀態語意與 008-order-management 規格一致。

## 實作假設

1. 金流 provider 可切換 stub 或 gomypay。
2. 真實退款流程待支付規格文件完成後再實作。
