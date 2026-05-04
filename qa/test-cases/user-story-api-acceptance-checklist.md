# User Story API 驗收清單（KUJI-Server）

這份文件要呈現什麼內容：
以「使用者要完成的操作情境」為核心，整理 KUJI-Server 後端 API 的驗收案例。每個案例都包含正確/錯誤 request、成功/錯誤 response、資料庫檢查點，以及是否屬於 P0 上線阻擋項目。

## 範圍與判定原則

- 範圍優先順序：商品、商品明細、刮刮樂、抽獎、訂單、付款、權限、audit log。
- Base Path：`/api`（Spring `context-path`）。
- 回應格式：多數 Controller 會被 `GlobalResponseAspect` 包裝為 `ApiResponse`；例外由 `GlobalExceptionHandler` 回傳錯誤格式。
- 錯誤碼重點：
  - `COMMON_VALIDATION_001`：`@Valid` 驗證失敗（通常 HTTP 400）
  - `BIZ_ERROR`：業務錯誤（通常 HTTP 400）
  - `FORBIDDEN` / `ORDER_ACCESS_DENIED`：權限不足（HTTP 403）
- P0 定義：金流、扣點、庫存、訂單、權限、資料一致性錯誤，一律不可上線。

## User Story 驗收表

| id | user_story | role | scenario | precondition | api | method | request | expected_status | expected_response | success_criteria | failure_criteria | db_check | priority |
|----|------------|------|----------|--------------|-----|--------|---------|-----------------|-------------------|------------------|------------------|----------|----------|
| AQA-US-001 | 作為店家管理者，我要一次建立商品與獎項，讓商品可進入上架準備 | Admin/StoreOwner/StoreEditor | 建立 `CUSTOM_GACHA + SCRATCH_MODE` 商品與多個獎項 | 已登入後台；店家帳號已綁定 store | `/api/admin/lottery/with-prizes` | POST | 正確：`lottery.title/category/playMode/gameMode/pricePerDraw/maxDraws` + `prizes[]`（含 `name/quantity/weight`）。錯誤：缺 `title`、`category`、`prizes.quantity<1` | 成功 200；驗證失敗 400 | 成功：`success=true`，`data.id` 有值，`data.prizes` 有資料。失敗：`success=false`，`error.code=COMMON_VALIDATION_001` 或 `BIZ_ERROR` | 商品與獎項同時建立成功，前端可直接拿同一筆回應做編輯頁 | 任何必填漏傳卻仍建立成功、或只建到 lottery 未建 prizes，判定失敗 | `lottery` 新增 1 筆；`lottery_prize` 新增 N 筆；`lottery_prize.remaining=quantity`；`lottery.store_id` 為登入者可管理店家 | P0 |
| AQA-US-002 | 作為店家管理者，我希望刮刮樂可不啟用免費抽門檻，避免建立被擋住 | Admin/StoreOwner/StoreEditor | `freeDrawThreshold = null` 建立刮刮樂商品 | 商品類型為 `CUSTOM_GACHA + SCRATCH_MODE` | `/api/admin/lottery/with-prizes` | POST | 正確：`freeDrawThreshold` 不傳或傳 `null`。錯誤：`freeDrawThreshold=0` 或負數 | 成功 200；錯誤 400 | 成功：`success=true` 且商品建立成功。錯誤：`success=false`，訊息指向門檻需 >=1 | `null` 必須可建立；`<=0` 必須失敗 | `null` 被拒絕或 `0` 被接受都算失敗 | `lottery.free_draw_threshold`：正確情境為 `NULL`；錯誤情境不得落地新商品 | P0 |
| AQA-US-003 | 作為店家管理者，我要變更商品狀態，確保草稿/上架流程可控 | Admin/StoreOwner | 商品狀態 FSM 轉換（例如 DRAFT -> ON_SHELF） | 該商品屬於登入者可管理範圍 | `/api/admin/lottery/{id}/status` | PUT | 正確：`{"targetStatus":"ON_SHELF","reason":"..."}`。錯誤：`targetStatus` 空值或非法值 | 成功 200；錯誤 400 | 成功：`success=true`，`data.status` 變更。錯誤：`success=false` + 驗證/業務錯誤碼 | 合法轉換可成功，非法轉換被阻擋 | 非法狀態仍寫入、或跨店可修改他店商品 | `lottery.status`、`lottery.updated_at`、狀態歷程（若有） | P1 |
| AQA-US-004 | 作為前台使用者，我要看到商品完整明細但不能提前知道未抽獎品資訊 | User/未登入 | 查詢商品明細（含獎項、籤位、安全隱藏） | 商品已上架，且已有 ticket | `/api/lottery/browse/{id}/detail` | GET | 正確：上架商品 ID。錯誤：不存在 ID、未上架 ID | 成功 200；不存在/未上架 404 | 成功：`success=true`，`data.lottery/prizes/tickets` 存在；`tickets.status=AVAILABLE` 時不應有 `prizeId/prizeName/prizeLevel/prizeImageUrl/revealedNumber` | 未抽 ticket 洩漏獎品或 revealed number 即失敗 | ticket 安全遮罩規則必須生效 | `lottery.status` 需為 `ON_SHELF`；明細查詢不應改動 `lottery_ticket`/`lottery_session`（若有 create session 行為需確認是否為預期） | P0 |
| AQA-US-005 | 作為刮刮樂開套玩家，我要先指定大獎位置再抽，確保玩法正確 | User（開套者） | `SCRATCH_PLAYER` 模式指定大獎位置 | 已有 ACTIVE session，且目前玩家為 opener | `/api/lottery/{lotteryId}/designate` | POST | 正確：`{"designations":[{"revealedNumber":7,"prizeId":"..."}]}`。錯誤：空陣列、revealedNumber 不存在、prizeId 非大獎 | 成功 200；錯誤 400 | 成功：`success=true`，回 `designatedWinningNumbers`。錯誤：`success=false` + `BIZ_ERROR` | 指定完成後可進入抽獎；指定資訊可被查詢 | 非 opener 也可指定、或指定後資料未落地 | `lottery_ticket` 對應 `revealed_number` 的 `prize_id/prize_level/is_designated_prize/designated_by`；`lottery_session.player_designated_numbers` 更新 | P0 |
| AQA-US-006 | 作為非開套玩家，我不能搶先指定大獎，避免破壞公平性 | User（非 opener） | 非開套者呼叫 designate | 同一商品有 ACTIVE session，且指定未完成 | `/api/lottery/{lotteryId}/designate` | POST | 錯誤：非 opener 發送合法 designate payload | 403 或 400（依實作拋錯類型） | 回應需明確拒絕，不可回 success | 非 opener 無法寫入指定資料 | 若非 opener 可成功寫入，屬重大缺陷 | `lottery_ticket`、`lottery_session.player_designated_numbers` 不得被該使用者改動 | P0 |
| AQA-US-007 | 作為玩家，我指定抽某張刮刮樂票時，回應必須對應同一張票 | User | `tickets(UUID)` 指定抽獎一致性 | 目標 ticket 狀態為 AVAILABLE | `/api/lottery/draw/{lotteryId}/draw` | POST | 正確：`{"count":1,"tickets":["<ticketId>"]}`。錯誤：`count!=tickets.size`、重複 UUID、非法 UUID | 成功 200；錯誤 200/400（目前有些錯誤訊息由資料層包為 success data） | 成功：`results[0].ticketId` 必須等於 request ticketId；`ticketNumber/revealedNumber` 與 DB 該 ticket 一致 | 回應票券對不上（例如指定第1格回第42格）即失敗 | 指定抽票結果與請求 1:1 對應 | `lottery_ticket.id` 對應那筆改為 `DRAWN`、`drawn_by/drawn_at` 正確；不得誤改其他 ticket | P0 |
| AQA-US-008 | 作為玩家，我在抽獎時要被正確限制請求數量，避免異常扣款 | User | 抽獎 count 邊界檢查 | 已登入，商品上架 | `/api/lottery/{lotteryId}/draw` | POST | 正確：`count=1..max`。錯誤：`count=0`、`count>max`、未帶 count | 預期 400（但需留意目前 Controller + AOP 可能包裝成 200 + data 錯誤訊息） | 錯誤時不得產生任何抽獎副作用 | 任何錯誤 count 造成扣款、扣庫存、寫抽獎紀錄都失敗 | 請求邊界要可預期且不可落地副作用 | `wallet_transaction`、`consumption_record`、`lottery_draw_record`、`lottery_ticket` 均不得異動 | P0 |
| AQA-US-009 | 作為玩家，我成功抽獎後要看到正確獎項、扣款與保護期資訊 | User | 統一抽獎成功流程（含 costType） | 商品 ON_SHELF；餘額足夠；ticket 可抽 | `/api/lottery/{lotteryId}/draw` | POST | 正確：`{"count":1}` 或指定 `ticketNumber/tickets`。錯誤：餘額不足、商品未上架 | 成功 200；錯誤 400/403 | 成功：`draws[].success=true`，`prize...` 合理，`costType` 與商品 paymentType 一致，必要時有 `protectionEndTime` | 任何成功回應但未扣款、未改 ticket、未寫紀錄都失敗 | 抽獎結果與帳務一致 | `lottery_ticket.status=DRAWN`；`lottery_draw_record` 新增；`wallet_transaction`/`consumption_record` 新增；`lottery_session` 抽數/保護期更新 | P0 |
| AQA-US-010 | 作為玩家，我要用賞品盒建立出貨訂單，且跨店要自動拆單 | User | 從 prize box 建立訂單（含支付初始化） | prize_box 均屬本人、狀態 IN_BOX、可出貨 | `/api/order/ship` | POST | 正確：`prizeBoxIds + shippingMethod + recipient...`。錯誤：他人 prizeBox、非 IN_BOX、shippingMethod 無效 | 成功 200；錯誤 400/403 | 成功：`data` 為 `OrderPaymentInitRes[]`，每店至少 1 單，回 `orderId/orderNumber/shippingFee/paymentStatus/paymentUrl` | 任何權限繞過或錯誤資料仍建單都失敗 | 建單、拆單、付款初始化必須一致 | `order` 新增（按店拆單）；`order_item` 新增；`prize_box.status` 變 `SHIPPING` 且關聯 `order_id`；`order.shipping_fee/payment_status/gomypay_trade_no` 合理 | P0 |
| AQA-US-011 | 作為玩家，我要避免前端運費被竄改，後端應以資料庫運費為準 | User | 建單時送錯 shippingFee | shipping_method 為 ACTIVE 且 fee 已知 | `/api/order/ship` | POST | 錯誤：`shippingFee` 與 DB fee 不一致 | 400 | `success=false`，訊息含「運費資訊已更新...」 | 運費不一致仍可建單即失敗 | 防止金額被竄改 | 不得新增 `order/order_item`；`prize_box` 狀態不變；帳務不應新增 | P0 |
| AQA-US-012 | 作為玩家，我要能提交出貨資訊，且只能改自己的待出貨訂單 | User | 提交 `shipping-info` | 訂單屬於本人且狀態允許更新 | `/api/order/{orderId}/shipping-info` | POST | 正確：`HOME_DELIVERY` 帶姓名/電話/地址，或超商帶 `storeCode/storeName`。錯誤：跨帳號訂單、狀態不允許 | 成功 200；錯誤 403/400 | 成功：`success=true`，資料更新 | 跨帳號可改、或狀態不允許仍可改即失敗 | 訂單收件資訊僅由本人在合法狀態更新 | `order.shipping_method/recipient.../store.../updated_at` 更新；他人訂單不得異動 | P1 |
| AQA-US-013 | 作為管理者，我要按狀態機處理訂單（備貨/出貨/完成），避免跳步 | Admin/StoreOwner/StoreEditor | `/prepare -> /ship -> /complete` 流程 | 訂單屬店家可管理範圍；目前狀態符合前置 | `/api/admin/orders/{orderId}/prepare`、`/ship`、`/complete` | PUT | 正確：依序呼叫；`/ship` 帶 `trackingNo`。錯誤：跳步、重複出貨、空 trackingNo | 成功 200；錯誤 400/403 | 成功：每步 `success=true`，狀態依序變更 | 非法跳步成功即失敗 | 訂單狀態機不可被繞過 | `order.status/preparing_at/shipped_at/completed_at/tracking_no`；`order_status_log` 需有完整軌跡 | P0 |
| AQA-US-014 | 作為玩家，我付款成功 callback 後，訂單應進入已付款可處理狀態 | 系統 callback | GoMyPay callback 成功通知 | 先有 `PAYMENT_PENDING` 訂單且 `order_number` 存在 | `/api/payment/callback` | POST (form-urlencoded) | 正確：至少含 `Order_No` + 成功狀態（如 `Status=SUCCESS/PAID/1/00`）+ `TradeNo`。錯誤：缺 orderNo、狀態失敗 | 成功 200 `"OK"`；失敗 400（BusinessException） | 成功：訂單付款狀態更新，狀態由 `PAYMENT_PENDING` 前進，`gomypay_trade_no` 落地 | callback 成功但訂單不更新、或重複回呼導致狀態異常為失敗 | 金流回呼需冪等且資料一致 | `order.payment_status`、`order.status`、`order.gomypay_trade_no`；`order_status_log` 新增對應紀錄 | P0 |
| AQA-US-015 | 作為系統，我要防止未知或錯誤 callback 破壞既有訂單資料 | 系統 callback | callback 帶不存在 `Order_No` 或失敗狀態 | 有其他正常訂單存在 | `/api/payment/callback` | POST | 錯誤：不存在 orderNo、`Status=FAIL`、空參數 | 400 或錯誤回應 | 回應應為失敗，不得回成功並改資料 | 任一既有訂單被誤改即失敗 | 錯誤 callback 不得造成資料汙染 | 所有 `order` 狀態不變；不新增錯誤狀態遷移 | P0 |
| AQA-US-016 | 作為一般玩家，我不該能呼叫後台管理 API | User/未登入 | 測試後台權限隔離 | 已有有效 USER token（非 admin） | `/api/admin/lottery/list`（或任一 `/api/admin/**`） | POST | 錯誤：使用 USER token 呼叫後台 API | 403 | `success=false`（或安全框架拒絕） | 若可讀取/修改後台資料即失敗 | 角色邊界必須嚴格 | DB 不得被異動 | P0 |
| AQA-US-017 | 作為未登入訪客，我可以看公開資料但不能做需登入操作 | 未登入 | 公開/受保護 API 邊界 | 無 token | `/api/lottery/browse/list`、`/api/shipping-methods`、`/api/order/list` | POST/GET | 正確：公開 API 不帶 token。錯誤：不帶 token 呼叫受保護 API | 公開 200；受保護 401/403 | 公開 API 可正常查；受保護 API 被拒絕 | 未登入可查個人訂單或可抽獎即失敗 | 匿名與會員權限分界清楚 | 公開查詢不改 DB；受保護拒絕不改 DB | P1 |
| AQA-US-018 | 作為系統管理員，我要查到系統稽核日誌，支援類型/使用者/時間範圍 | Admin | 查詢 audit log | 使用 ADMIN token；已有 system_log 測試資料 | `/api/admin/system-log/type/{logType}`、`/user/{userId}`、`/date-range` | GET | 正確：傳 `limit`、時間區間 ISO。錯誤：非 ADMIN 角色查詢、時間格式錯誤 | 成功 200；錯誤 403/400 | 成功：回傳 `SystemLog[]` 且欄位完整（action/userId/requestMethod/responseStatus/createdAt） | 非 ADMIN 可查到日誌即失敗 | 稽核資料存取權限正確 | `system_log` 查詢結果與 DB 一致；僅讀取不可改動 | P0 |
| AQA-US-019 | 作為系統管理員，我要定期清理過期 audit log，避免資料膨脹 | Admin | cleanup 舊日誌 | DB 已有超過 N 天舊資料 | `/api/admin/system-log/cleanup?days=90` | DELETE | 正確：`days` 正整數。錯誤：非 ADMIN、非法 days | 成功 200 | 成功：回傳刪除筆數（整數）且實際刪除 | 回傳筆數與 DB 不一致或誤刪近期待查資料即失敗 | 清理行為可追蹤且可驗證 | `system_log.created_at < now-days` 被刪除；近期待查資料保留 | P1 |
| AQA-US-020 | 作為平台，我要確保抽獎/訂單/付款關鍵流程有可追蹤的稽核證據 | Admin/QA | 串接檢查：先執行抽獎、建單、callback，再查 system log | 具備可重現測試資料與 token | `/api/admin/system-log/date-range` | GET | 正確：查詢測試時間窗 | 成功 200 | 成功：可查到對應 action/錯誤訊息或結果，支持問題追查 | 無法從日誌追到異常流程根因，視為失敗 | 重大流程需具備可追蹤性 | `system_log` 至少含相關操作痕跡；若缺漏需列為上線風險 | P0 |

## P0 上線阻擋清單

以下案例任一不通過，建議直接阻擋上線：

- AQA-US-001
- AQA-US-002
- AQA-US-004
- AQA-US-005
- AQA-US-006
- AQA-US-007
- AQA-US-008
- AQA-US-009
- AQA-US-010
- AQA-US-011
- AQA-US-013
- AQA-US-014
- AQA-US-015
- AQA-US-016
- AQA-US-018
- AQA-US-020

