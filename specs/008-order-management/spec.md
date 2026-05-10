# 功能規格書：訂單管理

功能分支：008-order-management  
建立日期：2026-03-22  
最後更新：2026-05-11  
狀態：已對齊目前後端實作

## 使用者情境與測試

### 使用者故事 1 — 店家管理自己店家的訂單（P1）

身為店家人員（STORE_OWNER / STORE_EDITOR），我希望只看到自己店家的訂單並推進出貨流程。

驗收情境：
1. 在店家人員登入後查詢訂單列表，只返回該店家的資料。
2. 在訂單為 PENDING 時，店家可標記 PREPARING。
3. 在訂單為 PREPARING 時，店家可填 trackingNo 後標記 SHIPPED。
4. 在訂單不屬於自己店家時，系統回傳 FORBIDDEN。

### 使用者故事 2 — 玩家從賞品盒建立出貨訂單（P1）

身為玩家，我希望從賞品盒建立出貨訂單，並在付款前調整資訊。

驗收情境：
1. 在玩家選取跨店賞品時，系統按店家自動拆單建立多筆訂單。
2. 建單後訂單初始狀態為 PAYMENT_PENDING，paymentStatus 為 PAYMENT_PENDING。
3. 訂單付款前（PAYMENT_PENDING）可更新 shipping-info。
4. 付款成功後進入 PENDING，不可再修改 shipping-info。

### 使用者故事 3 — 運費付款失敗與重付款（P1）

身為玩家，我希望付款失敗後可以重試付款或取消訂單。

驗收情境：
1. GoMyPay callback 失敗時，訂單狀態轉為 PAYMENT_FAILED。
2. PAYMENT_FAILED 訂單可呼叫重付款，成功後回到 PAYMENT_PENDING，待 callback 成功再進入 PENDING。
3. PAYMENT_PENDING / PAYMENT_FAILED 狀態玩家都可取消訂單。

### 使用者故事 4 — 管理員跨店查詢與完成訂單（P2）

身為 ADMIN，我希望跨店查詢訂單並進行必要的人工介入。

驗收情境：
1. ADMIN 不指定 storeId 可查全部店家。
2. SHIPPED 訂單可由 ADMIN 或 STORE_OWNER 手動完成為 COMPLETED。
3. STORE_EDITOR 不可完成訂單，不可取消訂單。

## 邊界情況

1. 重複收到成功 callback，不應造成重複扣款或異常狀態回跳。
2. 已 SHIPPED / COMPLETED 訂單不可取消。
3. 取消訂單時，PrizeBox 需回到 AVAILABLE，並解除 orderId 綁定。
4. PrizeBox 舊資料 IN_BOX 仍可建立訂單（相容期）。

## 需求規格

### 功能需求

1. FR-001：訂單由玩家從賞品盒明確建立，不由抽獎流程直接建立。
2. FR-002：建單需按店家拆單，每筆訂單只屬於單一店家。
3. FR-003：訂單主狀態含 PAYMENT_PENDING、PAYMENT_FAILED、PENDING、PREPARING、SHIPPED、COMPLETED、CANCELLED。
4. FR-004：付款成功後才可進入履約鏈（PENDING → PREPARING → SHIPPED → COMPLETED）。
5. FR-005：玩家可取消狀態為 PAYMENT_PENDING / PAYMENT_FAILED / PENDING 的訂單。
6. FR-006：後台（ADMIN / STORE_OWNER）可取消 PAYMENT_PENDING / PAYMENT_FAILED / PENDING / PREPARING。
7. FR-007：STORE_EDITOR 可查詢、備貨、出貨；不可取消、不可完成。
8. FR-008：玩家僅能在 PAYMENT_PENDING 修改 shipping-info。
9. FR-009：付款失敗需轉為 PAYMENT_FAILED，並可重付款。
10. FR-010：取消後 PrizeBox 回 AVAILABLE（IN_BOX 為舊資料相容），不退還抽獎點數。
11. FR-011：狀態變更必須寫入 OrderStatusLog（含 operatorType 與時間）。

### 核心實體

1. 訂單（Order）：包含狀態、付款狀態、收件資訊、運費、物流資訊與時間戳。
2. 訂單項目（OrderItem）：保存獎品與商品快照（名稱、圖片、等級）。
3. 訂單狀態日誌（OrderStatusLog）：追蹤狀態轉移與操作者。

## 成功標準

1. SC-001：玩家可於 2 分鐘內完成建單並取得 paymentUrl。
2. SC-002：店家資料隔離 100%，無跨店資料讀取。
3. SC-003：狀態流轉不可逆，無非法回退。
4. SC-004：付款失敗可重付款，不需重建整張訂單。

## 假設前提

1. 金流以 GoMyPay 為主，stub 僅做開發/測試用途。
2. 出貨完成（COMPLETED）目前為人工操作，不做自動完成。
3. 退款政策待金流規格確認，先保留 TODO 擴充點。
